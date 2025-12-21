package de.famst.service;

import de.famst.data.InstanceEty;
import de.famst.data.InstanceRepository;
import de.famst.data.PatientEty;
import de.famst.data.PatientRepository;
import de.famst.data.SeriesEty;
import de.famst.data.SeriesRepository;
import de.famst.data.StudyEty;
import de.famst.data.StudyRepository;
import de.famst.dcm.DcmFile;
import de.famst.dcm.DicomReader;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

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

    @Value("${mupacs.archive}")
    String mupacsArchive;


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

    @Transactional
    public void dicomToDatabase(File dcmFile)
    {
        Attributes dcm = DcmFile.readContent(dcmFile);
        dicomToDatabase(dcm, dcmFile.toPath());
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
        String patientId = dcm.getString(Tag.PatientID);
        String studyInstanceUID = dcm.getString(Tag.StudyInstanceUID);
        String seriesInstanceUID = dcm.getString(Tag.SeriesInstanceUID);
        String sopInstanceUID = dcm.getString(Tag.SOPInstanceUID);

        // Validate required tags
        validateRequiredTags(patientId, studyInstanceUID, seriesInstanceUID, sopInstanceUID);

        // Process Patient level
        PatientEty patient = findOrCreatePatient(dcm, patientId);

        // Process Study level
        StudyEty study = findOrCreateStudy(dcm, studyInstanceUID, patient);

        // Process Series level
        SeriesEty series = findOrCreateSeries(dcm, seriesInstanceUID, study);

        // Process Instance level
        processInstance(dcm, path, sopInstanceUID, study, series);
    }

    /**
     * Validates that all required DICOM tags are present and non-empty.
     */
    private void validateRequiredTags(String patientId, String studyInstanceUID,
                                      String seriesInstanceUID, String sopInstanceUID)
    {
        if (patientId == null || patientId.trim().isEmpty())
        {
            throw new IllegalArgumentException("Patient ID is required but missing or empty");
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
    private PatientEty findOrCreatePatient(Attributes dcm, String patientId)
    {
        PatientEty patient = patientRepository.findByPatientId(patientId);

        if (patient == null)
        {
            LOG.debug("Creating new patient: [{}]", patientId);
            patient = dicomReader.readPatient(dcm);
            patient = patientRepository.save(patient);
            LOG.info("Created new patient: [{}]", patientId);
        }
        else
        {
            LOG.debug("Found existing patient: [{}]", patientId);
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
     * Copies the DICOM file to the archive structure: PatientID/StudyInstanceUID/SeriesInstanceUID/SOPInstanceUID.dcm
     */
    private void processInstance(Attributes dcm, Path path, String sopInstanceUID, StudyEty study, SeriesEty series)
    {
        InstanceEty instance = instanceRepository.findByInstanceUID(sopInstanceUID);

        if (instance != null)
        {
            LOG.debug("Instance already exists, skipping: [{}]", sopInstanceUID);
            return;
        }

        LOG.debug("Creating new instance: [{}]", sopInstanceUID);

        // Copy DICOM file to archive structure
        Path archivePath = copyDicomFileToArchive(dcm, path, sopInstanceUID, study, series);

        instance = dicomReader.readInstance(dcm);
        instance.setPath(archivePath.toAbsolutePath().toString());
        instance.setSeries(series);
        instance = instanceRepository.save(instance);

        series.addInstance(instance);
        // Series save is handled by cascade or can be explicit if needed

        LOG.info("Created new instance: [{}] for series: [{}]",
            sopInstanceUID, series.getSeriesInstanceUID());
    }

    /**
     * Copies the DICOM file to the archive directory structure.
     * Structure: {archive}/PatientID/StudyInstanceUID/SeriesInstanceUID/SOPInstanceUID.dcm
     *
     * @param dcm DICOM attributes
     * @param sourcePath source file path
     * @param sopInstanceUID SOP Instance UID
     * @param series the series entity containing study and patient information
     * @return the destination path where the file was copied
     * @throws RuntimeException if file copy fails
     */
    private Path copyDicomFileToArchive(Attributes dcm, Path sourcePath, String sopInstanceUID, StudyEty study, SeriesEty series)
    {
        try
        {
            String studyInstanceUID = study.getStudyInstanceUID();
            String seriesInstanceUID = series.getSeriesInstanceUID();

            // Build archive path: {archive}/StudyInstanceUID/SeriesInstanceUID/
            Path archiveBase = Paths.get(mupacsArchive);
            Path destinationDir = archiveBase
                    .resolve(studyInstanceUID)
                    .resolve(seriesInstanceUID);

            // Create directories if they don't exist
            if (!Files.exists(destinationDir))
            {
                Files.createDirectories(destinationDir);
                LOG.debug("Created archive directory structure: [{}]", destinationDir);
            }

            // Build destination file path: SOPInstanceUID.dcm
            Path destinationPath = destinationDir.resolve(sopInstanceUID + ".dcm");

            // Copy file to archive location
            Files.copy(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);

            LOG.info("Copied DICOM file from [{}] to archive [{}]",
                    sourcePath.getFileName(), destinationPath);

            return destinationPath;
        }
        catch (IOException e)
        {
            String errorMsg = String.format(
                    "Failed to copy DICOM file [%s] to archive for SOP Instance UID [%s]: %s",
                    sourcePath, sopInstanceUID, e.getMessage());
            LOG.error(errorMsg, e);
            throw new RuntimeException(errorMsg, e);
        }
    }


}
