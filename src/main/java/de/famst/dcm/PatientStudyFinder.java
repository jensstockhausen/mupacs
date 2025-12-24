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

        // Filter by PatientBirthTime if provided
        if (keys.contains(Tag.PatientBirthTime))
        {
            java.util.Date birthTime = keys.getDate(Tag.PatientBirthTime);
            if (birthTime != null)
            {
                java.time.LocalTime localBirthTime = new java.sql.Time(birthTime.getTime()).toLocalTime();
                patientEtyList = patientEtyList.stream()
                    .filter(p -> p.getPatientBirthTime() != null && p.getPatientBirthTime().equals(localBirthTime))
                    .toList();
            }
        }

        // Filter by OtherPatientIDs if provided
        String otherPatientIds = keys.getString(Tag.OtherPatientIDs);
        if (null != otherPatientIds && !otherPatientIds.isEmpty())
        {
            patientEtyList = patientEtyList.stream()
                .filter(p -> p.getOtherPatientIds() != null && matchesPattern(p.getOtherPatientIds(), otherPatientIds))
                .toList();
        }

        // Filter by OtherPatientNames if provided
        String otherPatientNames = keys.getString(Tag.OtherPatientNames);
        if (null != otherPatientNames && !otherPatientNames.isEmpty())
        {
            patientEtyList = patientEtyList.stream()
                .filter(p -> p.getOtherPatientNames() != null && matchesPattern(p.getOtherPatientNames(), otherPatientNames))
                .toList();
        }

        // Filter by EthnicGroup if provided
        String ethnicGroup = keys.getString(Tag.EthnicGroup);
        if (null != ethnicGroup && !ethnicGroup.isEmpty())
        {
            patientEtyList = patientEtyList.stream()
                .filter(p -> p.getEthnicGroup() != null && matchesPattern(p.getEthnicGroup(), ethnicGroup))
                .toList();
        }

        // Filter by PatientComments if provided
        String patientComments = keys.getString(Tag.PatientComments);
        if (null != patientComments && !patientComments.isEmpty())
        {
            patientEtyList = patientEtyList.stream()
                .filter(p -> p.getPatientComments() != null && p.getPatientComments().contains(patientComments))
                .toList();
        }

        // Filter by PatientAge if provided
        String patientAge = keys.getString(Tag.PatientAge);
        if (null != patientAge && !patientAge.isEmpty())
        {
            patientEtyList = patientEtyList.stream()
                .filter(p -> p.getPatientAge() != null && matchesPattern(p.getPatientAge(), patientAge))
                .toList();
        }

        // Filter by PatientSize if provided
        if (keys.contains(Tag.PatientSize))
        {
            double patientSize = keys.getDouble(Tag.PatientSize, -1.0);
            if (patientSize >= 0.0)
            {
                patientEtyList = patientEtyList.stream()
                    .filter(p -> p.getPatientSize() != null && Math.abs(p.getPatientSize() - patientSize) < 0.001)
                    .toList();
            }
        }

        // Filter by PatientWeight if provided
        if (keys.contains(Tag.PatientWeight))
        {
            double patientWeight = keys.getDouble(Tag.PatientWeight, -1.0);
            if (patientWeight >= 0.0)
            {
                patientEtyList = patientEtyList.stream()
                    .filter(p -> p.getPatientWeight() != null && Math.abs(p.getPatientWeight() - patientWeight) < 0.001)
                    .toList();
            }
        }

        // Filter by MedicalAlerts if provided
        String medicalAlerts = keys.getString(Tag.MedicalAlerts);
        if (null != medicalAlerts && !medicalAlerts.isEmpty())
        {
            patientEtyList = patientEtyList.stream()
                .filter(p -> p.getMedicalAlerts() != null && p.getMedicalAlerts().contains(medicalAlerts))
                .toList();
        }

        // Filter by Allergies if provided
        String allergies = keys.getString(Tag.Allergies);
        if (null != allergies && !allergies.isEmpty())
        {
            patientEtyList = patientEtyList.stream()
                .filter(p -> p.getAllergies() != null && p.getAllergies().contains(allergies))
                .toList();
        }

        // Filter by PregnancyStatus if provided
        if (keys.contains(Tag.PregnancyStatus))
        {
            int pregnancyStatus = keys.getInt(Tag.PregnancyStatus, -1);
            if (pregnancyStatus >= 0)
            {
                patientEtyList = patientEtyList.stream()
                    .filter(p -> p.getPregnancyStatus() != null && p.getPregnancyStatus() == pregnancyStatus)
                    .toList();
            }
        }

        // Filter by ResponsiblePerson if provided
        String responsiblePerson = keys.getString(Tag.ResponsiblePerson);
        if (null != responsiblePerson && !responsiblePerson.isEmpty())
        {
            patientEtyList = patientEtyList.stream()
                .filter(p -> p.getResponsiblePerson() != null && matchesPattern(p.getResponsiblePerson(), responsiblePerson))
                .toList();
        }

        // Filter by ResponsibleOrganization if provided
        String responsibleOrganization = keys.getString(Tag.ResponsibleOrganization);
        if (null != responsibleOrganization && !responsibleOrganization.isEmpty())
        {
            patientEtyList = patientEtyList.stream()
                .filter(p -> p.getResponsibleOrganization() != null && matchesPattern(p.getResponsibleOrganization(), responsibleOrganization))
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
        List<SeriesEty> seriesEtyList = null;

        String studyInstanceUID = keys.getString(Tag.StudyInstanceUID);

        if (null != studyInstanceUID)
        {
            StudyEty studyEty = studyRepository.findByStudyInstanceUID(studyInstanceUID);

            if (null != studyEty)
            {
                seriesEtyList = seriesRepository.findByStudyId(studyEty.getId());
            }
        }

        return seriesEtyList;
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

    @Transactional
    public List<InstanceEty> findInstances(Attributes keys)
    {
        List<InstanceEty> instanceEtyList = null;

        String seriesInstanceUID = keys.getString(Tag.SeriesInstanceUID);

        if (null != seriesInstanceUID)
        {
            SeriesEty seriesEty = seriesRepository.findBySeriesInstanceUID(seriesInstanceUID);

            if (null != seriesEty)
            {
                instanceEtyList = instanceRepository.findBySeriesId(seriesEty.getId());
                LOG.info("found [{}] instance(s) matching", instanceEtyList.size());
            }
        }

        return instanceEtyList;
    }

}
