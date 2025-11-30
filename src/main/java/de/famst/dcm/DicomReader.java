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

    // Basic patient identification
    patient.setPatientName(dcm.getString(Tag.PatientName));
    patient.setPatientId(dcm.getString(Tag.PatientID));

    // Patient demographic information
    if (dcm.contains(Tag.PatientBirthDate) && null != dcm.getDate(Tag.PatientBirthDate))
    {
      patient.setPatientBirthDate(
        dcm.getDate(Tag.PatientBirthDate)
          .toInstant().atZone(ZoneId.systemDefault())
          .toLocalDate());
    }

    patient.setPatientSex(dcm.getString(Tag.PatientSex));

    if (dcm.contains(Tag.PatientBirthTime) && null != dcm.getDate(Tag.PatientBirthTime))
    {
      patient.setPatientBirthTime(
        dcm.getDate(Tag.PatientBirthTime)
          .toInstant().atZone(ZoneId.systemDefault())
          .toLocalTime());
    }

    // Additional patient identifiers
    patient.setOtherPatientIds(dcm.getString(Tag.OtherPatientIDs));
    patient.setOtherPatientNames(dcm.getString(Tag.OtherPatientNames));

    // Patient characteristics
    patient.setEthnicGroup(dcm.getString(Tag.EthnicGroup));
    patient.setPatientComments(dcm.getString(Tag.PatientComments));

    // Patient measurements and age
    patient.setPatientAge(dcm.getString(Tag.PatientAge));

    if (dcm.contains(Tag.PatientSize))
    {
      patient.setPatientSize(dcm.getDouble(Tag.PatientSize, 0.0));
    }

    if (dcm.contains(Tag.PatientWeight))
    {
      patient.setPatientWeight(dcm.getDouble(Tag.PatientWeight, 0.0));
    }

    // Medical information
    patient.setMedicalAlerts(dcm.getString(Tag.MedicalAlerts));
    patient.setAllergies(dcm.getString(Tag.Allergies));

    if (dcm.contains(Tag.PregnancyStatus))
    {
      patient.setPregnancyStatus(dcm.getInt(Tag.PregnancyStatus, 0));
    }

    // Responsible parties
    patient.setResponsiblePerson(dcm.getString(Tag.ResponsiblePerson));
    patient.setResponsibleOrganization(dcm.getString(Tag.ResponsibleOrganization));

    return patient;
  }

  public StudyEty readStudy(Attributes dcm)
  {
    StudyEty study = new StudyEty();

    study.setStudyInstanceUID(dcm.getString(Tag.StudyInstanceUID));
    study.setStudyId(dcm.getString(Tag.StudyID));
    study.setStudyDescription(dcm.getString(Tag.StudyDescription));

    if ( (dcm.contains(Tag.StudyDate)) && (null != dcm.getDate(Tag.StudyDate)))
    {
      study.setStudyDate(
        dcm.getDate(Tag.StudyDate)
          .toInstant().atZone(ZoneId.systemDefault())
          .toLocalDate());
    }

    if ((dcm.contains(Tag.StudyTime)) && (null != dcm.getDate(Tag.StudyTime)))
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

    // Series identification
    series.setSeriesInstanceUID(dcm.getString(Tag.SeriesInstanceUID));

    if (dcm.contains(Tag.SeriesNumber))
    {
      series.setSeriesNumber(dcm.getInt(Tag.SeriesNumber, 0));
    }

    // Series characteristics
    series.setModality(dcm.getString(Tag.Modality));
    series.setSeriesDescription(dcm.getString(Tag.SeriesDescription));

    // Series date and time
    if (dcm.contains(Tag.SeriesDate) && null != dcm.getDate(Tag.SeriesDate))
    {
      series.setSeriesDate(
        dcm.getDate(Tag.SeriesDate)
          .toInstant().atZone(ZoneId.systemDefault())
          .toLocalDate());
    }

    if (dcm.contains(Tag.SeriesTime) && null != dcm.getDate(Tag.SeriesTime))
    {
      series.setSeriesTime(
        dcm.getDate(Tag.SeriesTime)
          .toInstant().atZone(ZoneId.systemDefault())
          .toLocalTime());
    }

    // Personnel and protocol
    series.setPerformingPhysicianName(dcm.getString(Tag.PerformingPhysicianName));
    series.setProtocolName(dcm.getString(Tag.ProtocolName));
    series.setOperatorsName(dcm.getString(Tag.OperatorsName));

    // Anatomical information
    series.setBodyPartExamined(dcm.getString(Tag.BodyPartExamined));
    series.setPatientPosition(dcm.getString(Tag.PatientPosition));
    series.setLaterality(dcm.getString(Tag.Laterality));

    return series;
  }

  public InstanceEty readInstance(Attributes dcm)
  {
    InstanceEty instance = new InstanceEty();

    // Instance identification
    instance.setInstanceUID(dcm.getString(Tag.SOPInstanceUID));

    if (dcm.contains(Tag.InstanceNumber))
    {
      instance.setInstanceNumber(dcm.getInt(Tag.InstanceNumber, 0));
    }

    // Content date and time
    if (dcm.contains(Tag.ContentDate) && null != dcm.getDate(Tag.ContentDate))
    {
      instance.setContentDate(
        dcm.getDate(Tag.ContentDate)
          .toInstant().atZone(ZoneId.systemDefault())
          .toLocalDate());
    }

    if (dcm.contains(Tag.ContentTime) && null != dcm.getDate(Tag.ContentTime))
    {
      instance.setContentTime(
        dcm.getDate(Tag.ContentTime)
          .toInstant().atZone(ZoneId.systemDefault())
          .toLocalTime());
    }

    // Image characteristics
    instance.setImageType(dcm.getString(Tag.ImageType));

    // Acquisition information
    if (dcm.contains(Tag.AcquisitionNumber))
    {
      instance.setAcquisitionNumber(dcm.getInt(Tag.AcquisitionNumber, 0));
    }

    if (dcm.contains(Tag.AcquisitionDate) && null != dcm.getDate(Tag.AcquisitionDate))
    {
      instance.setAcquisitionDate(
        dcm.getDate(Tag.AcquisitionDate)
          .toInstant().atZone(ZoneId.systemDefault())
          .toLocalDate());
    }

    if (dcm.contains(Tag.AcquisitionTime) && null != dcm.getDate(Tag.AcquisitionTime))
    {
      instance.setAcquisitionTime(
        dcm.getDate(Tag.AcquisitionTime)
          .toInstant().atZone(ZoneId.systemDefault())
          .toLocalTime());
    }

    instance.setImageComments(dcm.getString(Tag.ImageComments));

    // Image pixel information
    if (dcm.contains(Tag.Rows))
    {
      instance.setRows(dcm.getInt(Tag.Rows, 0));
    }

    if (dcm.contains(Tag.Columns))
    {
      instance.setColumns(dcm.getInt(Tag.Columns, 0));
    }

    if (dcm.contains(Tag.BitsAllocated))
    {
      instance.setBitsAllocated(dcm.getInt(Tag.BitsAllocated, 0));
    }

    if (dcm.contains(Tag.BitsStored))
    {
      instance.setBitsStored(dcm.getInt(Tag.BitsStored, 0));
    }

    return instance;
  }

}
