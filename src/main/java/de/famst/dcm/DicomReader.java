package de.famst.dcm;

import de.famst.data.InstanceEty;
import de.famst.data.PatientEty;
import de.famst.data.SeriesEty;
import de.famst.data.StudyEty;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.ZoneId;


@Component
public class DicomReader
{
  private static Logger LOG = LoggerFactory.getLogger(DicomReader.class);

  public DicomReader()
  {
  }

  public PatientEty readPatient(Attributes dcm)
  {
    PatientEty patient = new PatientEty();

    patient.setPatientName(dcm.getString(Tag.PatientName));
    patient.setPatientId(dcm.getString(Tag.PatientID));

    return patient;
  }

  public StudyEty readStudy(Attributes dcm)
  {
    StudyEty study = new StudyEty();

    study.setStudyInstanceUID(dcm.getString(Tag.StudyInstanceUID));
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

    return study;
  }

  public SeriesEty readSeries(Attributes dcm)
  {
    SeriesEty series = new SeriesEty();

    series.setSeriesInstanceUID(dcm.getString(Tag.SeriesInstanceUID));

    return series;
  }

  public InstanceEty readInstance(Attributes dcm)
  {
    InstanceEty instance = new InstanceEty();

    instance.setInstanceUID(dcm.getString(Tag.SOPInstanceUID));

    return instance;
  }

}
