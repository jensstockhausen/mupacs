package de.famst.service;

import de.famst.data.*;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.nio.file.Path;
import java.time.ZoneId;

/**
 * Created by jens on 08/10/2016.
 */
@Service
public class DicomImportService
{
    private static Logger LOG = LoggerFactory.getLogger(DicomImportService.class);

    private InstanceRepository instanceRepository;
    private SeriesRepository seriesRepository;
    private StudyRepository studyRepository;
    private PatientRepository patientRepository;

    @Inject
    public DicomImportService(
        InstanceRepository instanceRepository,
        SeriesRepository seriesRepository,
        StudyRepository studyRepository,
        PatientRepository patientRepository)
    {
        this.instanceRepository = instanceRepository;
        this.seriesRepository = seriesRepository;
        this.studyRepository = studyRepository;
        this.patientRepository = patientRepository;
    }

    @Transactional
    public void dicomToDatabase(Attributes dcm, Path path)
    {
        PatientEty patient;
        String patientName = dcm.getString(Tag.PatientName);

        patient = patientRepository.findByPatientName(patientName);

        if (null == patient)
        {
            patient = createPatientEty(dcm, patientName);

            patientRepository.save(patient);
        }

        StudyEty study;
        String studyInstanceUID = dcm.getString(Tag.StudyInstanceUID);

        study = studyRepository.findByStudyInstanceUID(studyInstanceUID);
        if (null == study)
        {
            study = createStudyEty(dcm, studyInstanceUID, patient);

            patient.addStudy(study);
            patientRepository.save(patient);
        }

        SeriesEty series;
        String seriesInstanceUID = dcm.getString(Tag.SeriesInstanceUID);

        series = seriesRepository.findBySeriesInstanceUID(seriesInstanceUID);
        if (null == series)
        {
            series = createSeriesEty(study, seriesInstanceUID);

            study.addSeries(series);
            studyRepository.save(study);
        }

        InstanceEty instance;
        String sopInstanceUID = dcm.getString(Tag.SOPInstanceUID);

        instance = instanceRepository.findByInstanceUID(sopInstanceUID);
        if (null == instance)
        {
            instance = createInstanceEty(path, series, sopInstanceUID);

            instanceRepository.save(instance);

            LOG.info("add instance to series [{}]", series.getSeriesInstanceUID());
            series.addInstance(instance);

            LOG.info("store series [{}]", series.getSeriesInstanceUID());
            seriesRepository.save(series);
        } else
        {
            LOG.info("ignoring imported instance [{}]", sopInstanceUID);
        }

    }


    private PatientEty createPatientEty(Attributes dcm, String patientName)
    {
        PatientEty patient;
        LOG.info("create patient [{}]", patientName);

        patient = new PatientEty();
        patient.setPatientName(patientName);
        patient.setPatientId(dcm.getString(Tag.PatientID));
        return patient;
    }


    private StudyEty createStudyEty(Attributes dcm, String studyInstanceUID, PatientEty patient)
    {
        StudyEty study;
        LOG.info("create study [{}]", studyInstanceUID);
        study = new StudyEty();
        study.setStudyInstanceUID(studyInstanceUID);
        study.setStudyId(dcm.getString(Tag.StudyID));
        study.setStudyDescription(dcm.getString(Tag.StudyDescription));

        if (dcm.contains(Tag.StudyDate))
        {
            study.setStudyDate(
                dcm.getDate(Tag.StudyDate)
                    .toInstant().atZone(ZoneId.systemDefault())
                    .toLocalDate());
        }

        if (dcm.contains(Tag.StudyTime))
        {
            study.setStudyTime(
                dcm.getDate(Tag.StudyTime)
                    .toInstant().atZone(ZoneId.systemDefault())
                    .toLocalTime());
        }

        study.setAccessionNumber(dcm.getString(Tag.AccessionNumber));
        study.setModalitiesInStudy(dcm.getString(Tag.ModalitiesInStudy));
        study.setReferringPhysicianName(dcm.getString(Tag.ReferringPhysicianName));

        study.setPatient(patient);
        studyRepository.save(study);
        return study;
    }


    private SeriesEty createSeriesEty(StudyEty study, String seriesInstanceUID)
    {
        SeriesEty series;
        LOG.info("create series [{}]", seriesInstanceUID);
        series = new SeriesEty();
        series.setSeriesInstanceUID(seriesInstanceUID);
        series.setStudy(study);
        seriesRepository.save(series);
        return series;
    }


    private InstanceEty createInstanceEty(Path path, SeriesEty series, String sopInstanceUID)
    {
        InstanceEty instance;
        LOG.info("create instance [{}]", sopInstanceUID);
        instance = new InstanceEty();
        instance.setInstanceUID(sopInstanceUID);
        instance.setPath(path.toAbsolutePath().toString());
        instance.setSeries(series);
        return instance;
    }


}
