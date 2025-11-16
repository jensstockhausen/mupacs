package de.famst.service;

import de.famst.data.InstanceEty;
import de.famst.data.InstanceRepository;
import de.famst.data.PatientEty;
import de.famst.data.PatientRepository;
import de.famst.data.SeriesEty;
import de.famst.data.SeriesRepository;
import de.famst.data.StudyEty;
import de.famst.data.StudyRepository;
import de.famst.dcm.DicomReader;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Path;

/**
 * Service responsible for importing DICOM data into the database.
 * Handles the hierarchical DICOM structure: Patient → Study → Series → Instance.
 *
 * <p>This service ensures referential integrity by checking for existing entities
 * before creating new ones, preventing duplicate entries in the database.
 *
 * @author jens
 * @since 2016-10-08
 */
@Service
public class DicomImportService
{
    private static final Logger LOG = LoggerFactory.getLogger(DicomImportService.class);

    private final DicomReader dicomReader;
    private final InstanceRepository instanceRepository;
    private final SeriesRepository seriesRepository;
    private final StudyRepository studyRepository;
    private final PatientRepository patientRepository;

    /**
     * Constructs a new DicomImportService.
     *
     * @param instanceRepository repository for DICOM instances
     * @param seriesRepository   repository for DICOM series
     * @param studyRepository    repository for DICOM studies
     * @param patientRepository  repository for patients
     * @param dicomReader        reader for extracting data from DICOM attributes
     */
    public DicomImportService(
        InstanceRepository instanceRepository,
        SeriesRepository seriesRepository,
        StudyRepository studyRepository,
        PatientRepository patientRepository,
        DicomReader dicomReader)
    {
        this.instanceRepository = instanceRepository;
        this.seriesRepository = seriesRepository;
        this.studyRepository = studyRepository;
        this.patientRepository = patientRepository;
        this.dicomReader = dicomReader;
    }

    /**
     * Imports DICOM data into the database.
     *
     * <p>This method processes the DICOM hierarchy from top to bottom:
     * <ol>
     *   <li>Patient - identified by patient name</li>
     *   <li>Study - identified by Study Instance UID</li>
     *   <li>Series - identified by Series Instance UID</li>
     *   <li>Instance - identified by SOP Instance UID</li>
     * </ol>
     *
     * <p>Existing entities are reused to maintain referential integrity.
     * Only new entities are created and persisted to the database.
     *
     * @param dcm  the DICOM attributes containing the data to import
     * @param path the file path where the DICOM instance is stored
     * @throws IllegalArgumentException if dcm or path is null
     * @throws IllegalArgumentException if required DICOM tags are missing
     */
    @Transactional
    public void dicomToDatabase(Attributes dcm, Path path)
    {
        if (dcm == null)
        {
            throw new IllegalArgumentException("DICOM attributes cannot be null");
        }

        if (path == null)
        {
            throw new IllegalArgumentException("File path cannot be null");
        }

        // Extract required DICOM identifiers
        String patientName = dcm.getString(Tag.PatientName);
        String studyInstanceUID = dcm.getString(Tag.StudyInstanceUID);
        String seriesInstanceUID = dcm.getString(Tag.SeriesInstanceUID);
        String sopInstanceUID = dcm.getString(Tag.SOPInstanceUID);

        // Validate required tags
        validateRequiredTags(patientName, studyInstanceUID, seriesInstanceUID, sopInstanceUID);

        // Process Patient level
        PatientEty patient = findOrCreatePatient(dcm, patientName);

        // Process Study level
        StudyEty study = findOrCreateStudy(dcm, studyInstanceUID, patient);

        // Process Series level
        SeriesEty series = findOrCreateSeries(dcm, seriesInstanceUID, study);

        // Process Instance level
        processInstance(dcm, path, sopInstanceUID, series);
    }

    /**
     * Validates that all required DICOM tags are present and non-empty.
     */
    private void validateRequiredTags(String patientName, String studyInstanceUID,
                                      String seriesInstanceUID, String sopInstanceUID)
    {
        if (patientName == null || patientName.trim().isEmpty())
        {
            throw new IllegalArgumentException("Patient Name is required but missing or empty");
        }

        if (studyInstanceUID == null || studyInstanceUID.trim().isEmpty())
        {
            throw new IllegalArgumentException("Study Instance UID is required but missing or empty");
        }

        if (seriesInstanceUID == null || seriesInstanceUID.trim().isEmpty())
        {
            throw new IllegalArgumentException("Series Instance UID is required but missing or empty");
        }

        if (sopInstanceUID == null || sopInstanceUID.trim().isEmpty())
        {
            throw new IllegalArgumentException("SOP Instance UID is required but missing or empty");
        }
    }

    /**
     * Finds an existing patient or creates a new one.
     */
    private PatientEty findOrCreatePatient(Attributes dcm, String patientName)
    {
        PatientEty patient = patientRepository.findByPatientName(patientName);

        if (patient == null)
        {
            LOG.debug("Creating new patient: [{}]", patientName);
            patient = dicomReader.readPatient(dcm);
            patient = patientRepository.save(patient);
            LOG.info("Created new patient: [{}]", patientName);
        }
        else
        {
            LOG.debug("Found existing patient: [{}]", patientName);
        }

        return patient;
    }

    /**
     * Finds an existing study or creates a new one and links it to the patient.
     */
    private StudyEty findOrCreateStudy(Attributes dcm, String studyInstanceUID, PatientEty patient)
    {
        StudyEty study = studyRepository.findByStudyInstanceUID(studyInstanceUID);

        if (study == null)
        {
            LOG.debug("Creating new study: [{}]", studyInstanceUID);
            study = dicomReader.readStudy(dcm);
            study.setPatient(patient);
            study = studyRepository.save(study);

            patient.addStudy(study);
            // Patient save is handled by cascade or can be explicit if needed

            LOG.info("Created new study: [{}] for patient: [{}]",
                studyInstanceUID, patient.getPatientName());
        }
        else
        {
            LOG.debug("Found existing study: [{}]", studyInstanceUID);
        }

        return study;
    }

    /**
     * Finds an existing series or creates a new one and links it to the study.
     */
    private SeriesEty findOrCreateSeries(Attributes dcm, String seriesInstanceUID, StudyEty study)
    {
        SeriesEty series = seriesRepository.findBySeriesInstanceUID(seriesInstanceUID);

        if (series == null)
        {
            LOG.debug("Creating new series: [{}]", seriesInstanceUID);
            series = dicomReader.readSeries(dcm);
            series.setStudy(study);
            series = seriesRepository.save(series);

            study.addSeries(series);
            // Study save is handled by cascade or can be explicit if needed

            LOG.info("Created new series: [{}] for study: [{}]",
                seriesInstanceUID, study.getStudyInstanceUID());
        }
        else
        {
            LOG.debug("Found existing series: [{}]", seriesInstanceUID);
        }

        return series;
    }

    /**
     * Processes a DICOM instance, creating it if it doesn't exist or skipping if it does.
     */
    private void processInstance(Attributes dcm, Path path, String sopInstanceUID, SeriesEty series)
    {
        InstanceEty instance = instanceRepository.findByInstanceUID(sopInstanceUID);

        if (instance != null)
        {
            LOG.debug("Instance already exists, skipping: [{}]", sopInstanceUID);
            return;
        }

        LOG.debug("Creating new instance: [{}]", sopInstanceUID);
        instance = dicomReader.readInstance(dcm);
        instance.setPath(path.toAbsolutePath().toString());
        instance.setSeries(series);
        instance = instanceRepository.save(instance);

        series.addInstance(instance);
        // Series save is handled by cascade or can be explicit if needed

        LOG.info("Created new instance: [{}] for series: [{}]",
            sopInstanceUID, series.getSeriesInstanceUID());
    }


}
