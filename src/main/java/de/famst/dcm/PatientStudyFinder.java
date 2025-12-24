package de.famst.dcm;

import de.famst.data.InstanceEty;
import de.famst.data.InstanceRepository;
import de.famst.data.PatientEty;
import de.famst.data.PatientRepository;
import de.famst.data.SeriesEty;
import de.famst.data.SeriesRepository;
import de.famst.data.StudyEty;
import de.famst.data.StudyRepository;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jens on 06/11/2016.
 */
@Component
@Transactional
public class PatientStudyFinder
{
    private static Logger LOG = LoggerFactory.getLogger(PatientStudyFinder.class);

    private PatientRepository patientRepository;
    private StudyRepository studyRepository;
    private SeriesRepository seriesRepository;
    private InstanceRepository instanceRepository;

    @Autowired
    public PatientStudyFinder(PatientRepository patientRepository,
                              StudyRepository studyRepository,
                              SeriesRepository seriesRepository,
                              InstanceRepository instanceRepository)
    {
        this.patientRepository = patientRepository;
        this.studyRepository = studyRepository;
        this.seriesRepository = seriesRepository;
        this.instanceRepository = instanceRepository;

        LOG.info("PatientStudyFinder created");
    }

    @Transactional
    public List<PatientEty> findPatients(Attributes keys)
    {
        // Get all patients initially
        List<PatientEty> patientEtyList = patientRepository.findAll();

        // Filter by PatientName if provided
        String patientName = keys.getString(Tag.PatientName);
        if (null != patientName && !patientName.isEmpty())
        {
            String namePattern = patientName.replace('*', '%');
            if (namePattern.contains("%"))
            {
                // Use LIKE query for wildcards
                patientEtyList = patientRepository.findByPatientNameLike(namePattern);
            }
            else
            {
                // Exact match
                PatientEty patient = patientRepository.findByPatientName(patientName);
                patientEtyList = patient != null ? List.of(patient) : List.of();
            }
        }

        // Filter by PatientID if provided
        String patientId = keys.getString(Tag.PatientID);
        if (null != patientId && !patientId.isEmpty())
        {
            patientEtyList = patientEtyList.stream()
                .filter(p -> p.getPatientId() != null && matchesPattern(p.getPatientId(), patientId))
                .toList();
        }

        // Filter by PatientBirthDate if provided
        if (keys.contains(Tag.PatientBirthDate))
        {
            java.util.Date birthDate = keys.getDate(Tag.PatientBirthDate);
            if (birthDate != null)
            {
                java.time.LocalDate localBirthDate = new java.sql.Date(birthDate.getTime()).toLocalDate();
                patientEtyList = patientEtyList.stream()
                    .filter(p -> p.getPatientBirthDate() != null && p.getPatientBirthDate().equals(localBirthDate))
                    .toList();
            }
        }

        // Filter by PatientSex if provided
        String patientSex = keys.getString(Tag.PatientSex);
        if (null != patientSex && !patientSex.isEmpty())
        {
            patientEtyList = patientEtyList.stream()
                .filter(p -> p.getPatientSex() != null && matchesPattern(p.getPatientSex(), patientSex))
                .toList();
        }

        if (patientEtyList != null && !patientEtyList.isEmpty())
        {
            LOG.info("found [{}] patient(s) matching query criteria", patientEtyList.size());
        }
        else
        {
            LOG.info("found no patients matching query criteria");
            patientEtyList = List.of();
        }

        return patientEtyList;
    }

    /**
     * Matches a value against a pattern that may contain DICOM wildcards (* or ?).
     * * matches any sequence of characters
     * ? matches any single character
     *
     * @param value the value to match
     * @param pattern the pattern with optional wildcards
     * @return true if the value matches the pattern
     */
    private boolean matchesPattern(String value, String pattern)
    {
        if (pattern == null || value == null)
        {
            return false;
        }

        // If no wildcards, do exact match
        if (!pattern.contains("*") && !pattern.contains("?"))
        {
            return value.equals(pattern);
        }

        // Convert DICOM wildcards to regex
        String regex = pattern
            .replace(".", "\\.")
            .replace("*", ".*")
            .replace("?", ".");

        return value.matches(regex);
    }


    @Transactional
    public List<SeriesEty> findSeries(Attributes keys)
    {
        List<SeriesEty> seriesEtyList = new ArrayList<>();

        // First, filter by StudyInstanceUID if provided (required for SERIES level query)
        String studyInstanceUID = keys.getString(Tag.StudyInstanceUID);
        if (null != studyInstanceUID && !studyInstanceUID.isEmpty())
        {
            StudyEty studyEty = studyRepository.findByStudyInstanceUID(studyInstanceUID);
            if (null != studyEty)
            {
                seriesEtyList = new ArrayList<>(seriesRepository.findByStudyId(studyEty.getId()));
            }
        }
        else
        {
            // If no StudyInstanceUID, get all series
            seriesEtyList = new ArrayList<>(seriesRepository.findAll());
        }

        // Filter by SeriesInstanceUID if provided
        String seriesInstanceUID = keys.getString(Tag.SeriesInstanceUID);
        if (null != seriesInstanceUID && !seriesInstanceUID.isEmpty())
        {
            seriesEtyList = seriesEtyList.stream()
                .filter(s -> s.getSeriesInstanceUID() != null && matchesPattern(s.getSeriesInstanceUID(), seriesInstanceUID))
                .toList();
        }

        // Filter by Modality if provided
        String modality = keys.getString(Tag.Modality);
        if (null != modality && !modality.isEmpty())
        {
            seriesEtyList = seriesEtyList.stream()
                .filter(s -> s.getModality() != null && matchesPattern(s.getModality(), modality))
                .toList();
        }

        // Filter by SeriesNumber if provided
        if (keys.contains(Tag.SeriesNumber))
        {
            int seriesNumber = keys.getInt(Tag.SeriesNumber, -1);
            if (seriesNumber >= 0)
            {
                seriesEtyList = seriesEtyList.stream()
                    .filter(s -> s.getSeriesNumber() != null && s.getSeriesNumber() == seriesNumber)
                    .toList();
            }
        }

        // Filter by SeriesDescription if provided
        String seriesDescription = keys.getString(Tag.SeriesDescription);
        if (null != seriesDescription && !seriesDescription.isEmpty())
        {
            seriesEtyList = seriesEtyList.stream()
                .filter(s -> s.getSeriesDescription() != null && matchesPattern(s.getSeriesDescription(), seriesDescription))
                .toList();
        }

        // Filter by SeriesDate if provided
        if (keys.contains(Tag.SeriesDate))
        {
            java.util.Date seriesDate = keys.getDate(Tag.SeriesDate);
            if (seriesDate != null)
            {
                java.time.LocalDate localSeriesDate = new java.sql.Date(seriesDate.getTime()).toLocalDate();
                seriesEtyList = seriesEtyList.stream()
                    .filter(s -> s.getSeriesDate() != null && s.getSeriesDate().equals(localSeriesDate))
                    .toList();
            }
        }

        // Filter by PerformingPhysicianName if provided
        String performingPhysicianName = keys.getString(Tag.PerformingPhysicianName);
        if (null != performingPhysicianName && !performingPhysicianName.isEmpty())
        {
            seriesEtyList = seriesEtyList.stream()
                .filter(s -> s.getPerformingPhysicianName() != null && matchesPattern(s.getPerformingPhysicianName(), performingPhysicianName))
                .toList();
        }

        // Filter by BodyPartExamined if provided
        String bodyPartExamined = keys.getString(Tag.BodyPartExamined);
        if (null != bodyPartExamined && !bodyPartExamined.isEmpty())
        {
            seriesEtyList = seriesEtyList.stream()
                .filter(s -> s.getBodyPartExamined() != null && matchesPattern(s.getBodyPartExamined(), bodyPartExamined))
                .toList();
        }

        if (!seriesEtyList.isEmpty())
        {
            LOG.info("found [{}] series matching query criteria", seriesEtyList.size());
        }
        else
        {
            LOG.info("found no series matching query criteria");
        }

        return seriesEtyList;
    }


    /**
     * Finds studies matching the given DICOM query keys.
     * Supports filtering by PatientID, PatientName, StudyInstanceUID, StudyID,
     * StudyDate, StudyDescription, AccessionNumber, and ModalitiesInStudy.
     *
     * @param keys the DICOM attributes containing search criteria
     * @return list of matching studies
     */
    @Transactional
    public List<StudyEty> findStudies(Attributes keys)
    {
        List<StudyEty> studyEtyList = new ArrayList<>();

        // First check if we should filter by patient criteria
        String patientId = keys.getString(Tag.PatientID);
        String patientName = keys.getString(Tag.PatientName);

        if ((null != patientId && !patientId.isEmpty()) || (null != patientName && !patientName.isEmpty()))
        {
            // Get patients matching criteria first
            List<PatientEty> patientEtyList = findPatients(keys);
            for (PatientEty patientEty : patientEtyList)
            {
                studyEtyList.addAll(studyRepository.findByPatientId(patientEty.getId()));
            }
        }
        else
        {
            // No patient filter, get all studies
            studyEtyList = new ArrayList<>(studyRepository.findAll());
        }

        // Filter by StudyInstanceUID if provided
        String studyInstanceUID = keys.getString(Tag.StudyInstanceUID);
        if (null != studyInstanceUID && !studyInstanceUID.isEmpty())
        {
            studyEtyList = studyEtyList.stream()
                .filter(s -> s.getStudyInstanceUID() != null && matchesPattern(s.getStudyInstanceUID(), studyInstanceUID))
                .toList();
        }

        // Filter by StudyID if provided
        String studyId = keys.getString(Tag.StudyID);
        if (null != studyId && !studyId.isEmpty())
        {
            studyEtyList = studyEtyList.stream()
                .filter(s -> s.getStudyId() != null && matchesPattern(s.getStudyId(), studyId))
                .toList();
        }

        // Filter by StudyDate if provided
        if (keys.contains(Tag.StudyDate))
        {
            java.util.Date studyDate = keys.getDate(Tag.StudyDate);
            if (studyDate != null)
            {
                java.time.LocalDate localStudyDate = new java.sql.Date(studyDate.getTime()).toLocalDate();
                studyEtyList = studyEtyList.stream()
                    .filter(s -> s.getStudyDate() != null && s.getStudyDate().equals(localStudyDate))
                    .toList();
            }
        }

        // Filter by StudyDescription if provided
        String studyDescription = keys.getString(Tag.StudyDescription);
        if (null != studyDescription && !studyDescription.isEmpty())
        {
            studyEtyList = studyEtyList.stream()
                .filter(s -> s.getStudyDescription() != null && matchesPattern(s.getStudyDescription(), studyDescription))
                .toList();
        }

        // Filter by AccessionNumber if provided
        String accessionNumber = keys.getString(Tag.AccessionNumber);
        if (null != accessionNumber && !accessionNumber.isEmpty())
        {
            studyEtyList = studyEtyList.stream()
                .filter(s -> s.getAccessionNumber() != null && matchesPattern(s.getAccessionNumber(), accessionNumber))
                .toList();
        }

        // Filter by ModalitiesInStudy if provided
        String modalitiesInStudy = keys.getString(Tag.ModalitiesInStudy);
        if (null != modalitiesInStudy && !modalitiesInStudy.isEmpty())
        {
            studyEtyList = studyEtyList.stream()
                .filter(s -> s.getModalitiesInStudy() != null && s.getModalitiesInStudy().contains(modalitiesInStudy))
                .toList();
        }

        // Filter by ReferringPhysicianName if provided
        String referringPhysicianName = keys.getString(Tag.ReferringPhysicianName);
        if (null != referringPhysicianName && !referringPhysicianName.isEmpty())
        {
            studyEtyList = studyEtyList.stream()
                .filter(s -> s.getReferringPhysicianName() != null && matchesPattern(s.getReferringPhysicianName(), referringPhysicianName))
                .toList();
        }

        if (!studyEtyList.isEmpty())
        {
            LOG.info("found [{}] study(ies) matching query criteria", studyEtyList.size());
        }
        else
        {
            LOG.info("found no studies matching query criteria");
        }

        return studyEtyList;
    }

    @Transactional
    public List<StudyEty> getStudiesForPatient(List<PatientEty> patientEtyList)
    {
        List<StudyEty> studyEtyList = new ArrayList<>();

        if (null == patientEtyList)
        {
            return studyEtyList;
        }

        for(PatientEty patientEty: patientEtyList)
        {
            List<StudyEty> studyRepositoryByPatientId = studyRepository.findByPatientId(patientEty.getId());

            for(StudyEty studyEty: studyRepositoryByPatientId)
            {
                studyEtyList.add(studyEty);
            }
        }

        LOG.info("found [{}] studie(s) matching", studyEtyList.size());

        return studyEtyList;
    }

    /**
     * Finds instances (images) matching the given DICOM query keys.
     * Supports filtering by SeriesInstanceUID, SOPInstanceUID, InstanceNumber,
     * ContentDate, and ImageType.
     *
     * @param keys the DICOM attributes containing search criteria
     * @return list of matching instances
     */
    @Transactional
    public List<InstanceEty> findInstances(Attributes keys)
    {
        List<InstanceEty> instanceEtyList = new ArrayList<>();

        // First, filter by SeriesInstanceUID if provided (required for IMAGE level query)
        String seriesInstanceUID = keys.getString(Tag.SeriesInstanceUID);
        if (null != seriesInstanceUID && !seriesInstanceUID.isEmpty())
        {
            SeriesEty seriesEty = seriesRepository.findBySeriesInstanceUID(seriesInstanceUID);
            if (null != seriesEty)
            {
                instanceEtyList = new ArrayList<>(instanceRepository.findBySeriesId(seriesEty.getId()));
            }
        }
        else
        {
            // If no SeriesInstanceUID, get all instances
            instanceEtyList = new ArrayList<>(instanceRepository.findAll());
        }

        // Filter by SOPInstanceUID if provided
        String sopInstanceUID = keys.getString(Tag.SOPInstanceUID);
        if (null != sopInstanceUID && !sopInstanceUID.isEmpty())
        {
            instanceEtyList = instanceEtyList.stream()
                .filter(i -> i.getInstanceUID() != null && matchesPattern(i.getInstanceUID(), sopInstanceUID))
                .toList();
        }

        // Filter by InstanceNumber if provided
        if (keys.contains(Tag.InstanceNumber))
        {
            int instanceNumber = keys.getInt(Tag.InstanceNumber, -1);
            if (instanceNumber >= 0)
            {
                instanceEtyList = instanceEtyList.stream()
                    .filter(i -> i.getInstanceNumber() != null && i.getInstanceNumber() == instanceNumber)
                    .toList();
            }
        }

        // Filter by ContentDate if provided
        if (keys.contains(Tag.ContentDate))
        {
            java.util.Date contentDate = keys.getDate(Tag.ContentDate);
            if (contentDate != null)
            {
                java.time.LocalDate localContentDate = new java.sql.Date(contentDate.getTime()).toLocalDate();
                instanceEtyList = instanceEtyList.stream()
                    .filter(i -> i.getContentDate() != null && i.getContentDate().equals(localContentDate))
                    .toList();
            }
        }

        // Filter by AcquisitionNumber if provided
        if (keys.contains(Tag.AcquisitionNumber))
        {
            int acquisitionNumber = keys.getInt(Tag.AcquisitionNumber, -1);
            if (acquisitionNumber >= 0)
            {
                instanceEtyList = instanceEtyList.stream()
                    .filter(i -> i.getAcquisitionNumber() != null && i.getAcquisitionNumber() == acquisitionNumber)
                    .toList();
            }
        }

        // Filter by AcquisitionDate if provided
        if (keys.contains(Tag.AcquisitionDate))
        {
            java.util.Date acquisitionDate = keys.getDate(Tag.AcquisitionDate);
            if (acquisitionDate != null)
            {
                java.time.LocalDate localAcquisitionDate = new java.sql.Date(acquisitionDate.getTime()).toLocalDate();
                instanceEtyList = instanceEtyList.stream()
                    .filter(i -> i.getAcquisitionDate() != null && i.getAcquisitionDate().equals(localAcquisitionDate))
                    .toList();
            }
        }

        // Filter by ImageType if provided
        String imageType = keys.getString(Tag.ImageType);
        if (null != imageType && !imageType.isEmpty())
        {
            instanceEtyList = instanceEtyList.stream()
                .filter(i -> i.getImageType() != null && i.getImageType().contains(imageType))
                .toList();
        }

        // Filter by Rows if provided
        if (keys.contains(Tag.Rows))
        {
            int rows = keys.getInt(Tag.Rows, -1);
            if (rows >= 0)
            {
                instanceEtyList = instanceEtyList.stream()
                    .filter(i -> i.getRows() != null && i.getRows() == rows)
                    .toList();
            }
        }

        // Filter by Columns if provided
        if (keys.contains(Tag.Columns))
        {
            int columns = keys.getInt(Tag.Columns, -1);
            if (columns >= 0)
            {
                instanceEtyList = instanceEtyList.stream()
                    .filter(i -> i.getColumns() != null && i.getColumns() == columns)
                    .toList();
            }
        }

        if (!instanceEtyList.isEmpty())
        {
            LOG.info("found [{}] instance(s) matching query criteria", instanceEtyList.size());
        }
        else
        {
            LOG.info("found no instances matching query criteria");
        }

        return instanceEtyList;
    }

}
