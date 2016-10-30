package de.famst.service;

import de.famst.data.*;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;

/**
 * Created by jens on 08/10/2016.
 */
@Service
public class DicomImportService
{
    private static Logger LOG = LoggerFactory.getLogger(DicomImportService.class);

    @Inject
    private InstanceRepository instanceRepository;

    @Inject
    private SeriesRepository seriesRepository;

    @Inject
    private StudyRepository studyRepository;

    @Inject
    private PatientRepository patientRepository;

    private DicomImportService()
    {

    }


    @Transactional
    public void dicomToDatabase(Attributes dcm)
    {
        PatientEty patient;
        String patientName = dcm.getString(Tag.PatientName);

        patient = patientRepository.findByPatientName(patientName);
        if (null == patient)
        {
            LOG.info("create patient [{}]", patientName);

            patient = new PatientEty();
            patient.setPatientName(patientName);
            patientRepository.save(patient);
        }
        StudyEty study;
        String studyInstanceUID = dcm.getString(Tag.StudyInstanceUID);


        study = studyRepository.findByStudyInstanceUID(studyInstanceUID);
        if (null == study)
        {
            LOG.info("create study [{}]", studyInstanceUID);
            study = new StudyEty();
            study.setStudyInstanceUID(studyInstanceUID);
            study.setPatient(patient);
            studyRepository.save(study);

            patient.addStudy(study);
            patientRepository.save(patient);
        }

        SeriesEty series;
        String seriesInstanceUID = dcm.getString(Tag.SeriesInstanceUID);

        series = seriesRepository.findBySeriesInstanceUID(seriesInstanceUID);
        if (null == series)
        {
            LOG.info("create series [{}]", seriesInstanceUID);
            series = new SeriesEty();
            series.setSeriesInstanceUID(seriesInstanceUID);
            series.setStudy(study);
            seriesRepository.save(series);

            study.addSeries(series);
            studyRepository.save(study);
        }

        InstanceEty instance;
        String sopInstanceUID = dcm.getString(Tag.SOPInstanceUID);

        instance = instanceRepository.findByInstanceUID(sopInstanceUID);
        if (null == instance)
        {
            LOG.info("create instance [{}]", sopInstanceUID);
            instance = new InstanceEty();
            instance.setInstanceUID(sopInstanceUID);
            instance.setSeries(series);
            instanceRepository.save(instance);

            LOG.info("add instance to series [{}]", series.getSeriesInstanceUID());
            series.addInstance(instance);

            LOG.info("store series [{}]", series.getSeriesInstanceUID());
            seriesRepository.save(series);
        }
        else
        {
            LOG.info("ignoring imported instance [{}]", sopInstanceUID);
        }

    }




}
