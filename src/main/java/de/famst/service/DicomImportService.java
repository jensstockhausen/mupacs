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

import javax.inject.Inject;
import java.nio.file.Path;

/**
 * Created by jens on 08/10/2016.
 */
@Service
public class DicomImportService
{
  private static Logger LOG = LoggerFactory.getLogger(DicomImportService.class);

  private final DicomReader dicomReader;

  private final InstanceRepository instanceRepository;
  private final SeriesRepository seriesRepository;
  private final StudyRepository studyRepository;
  private final PatientRepository patientRepository;

  @Inject
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
  public void dicomToDatabase(Attributes dcm, Path path)
  {
    PatientEty patient;
    String patientName = dcm.getString(Tag.PatientName);

    patient = patientRepository.findByPatientName(patientName);

    if (null == patient)
    {
      patient = dicomReader.readPatient(dcm);
      patientRepository.save(patient);
    }

    StudyEty study;
    String studyInstanceUID = dcm.getString(Tag.StudyInstanceUID);

    study = studyRepository.findByStudyInstanceUID(studyInstanceUID);
    if (null == study)
    {
      study = dicomReader.readStudy(dcm);
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
      series = dicomReader.readSeries(dcm);
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
      instance = dicomReader.readInstance(dcm);
      instance.setPath(path.toAbsolutePath().toString());
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
