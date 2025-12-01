package de.famst.controller;

import de.famst.data.PatientEty;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by jens on 10/10/2016.
 */
public class PatientModel
{
  private long id;
  private String patientsName;
  private String patientId;
  private long numberOfStudies;
  private List<StudyModel> studies;

  // Additional DICOM Patient Module attributes
  private LocalDate patientBirthDate;
  private String patientSex;
  private LocalTime patientBirthTime;
  private String otherPatientIds;
  private String otherPatientNames;
  private String ethnicGroup;
  private String patientComments;
  private String patientAge;
  private Double patientSize;
  private Double patientWeight;
  private String medicalAlerts;
  private String allergies;
  private Integer pregnancyStatus;
  private String responsiblePerson;
  private String responsibleOrganization;

  public PatientModel()
  {
    id = -1;
    patientsName = "";
    patientId = "";
    numberOfStudies = 0;
    studies = new ArrayList<>();
  }

  public PatientModel(long id, String patientsName, String patientId, long numberOfStudies, List<StudyModel> studies)
  {
    this.id = id;
    this.patientsName = patientsName;
    this.patientId = patientId;
    this.numberOfStudies = numberOfStudies;
    this.studies = studies;
  }

  public static PatientModel fromPatientEty(PatientEty patientEty)
  {
    if (null == patientEty)
    {
      return new PatientModel();
    }

    List<StudyModel> studies = patientEty.getStudies().stream()
        .map(StudyModel::fromStudyEty)
        .collect(Collectors.toList());

    PatientModel model = new PatientModel(
      patientEty.getId(),
      patientEty.getPatientName(),
      patientEty.getPatientId(),
      patientEty.getStudies().size(),
      studies
    );

    // Set additional DICOM Patient Module attributes
    model.patientBirthDate = patientEty.getPatientBirthDate();
    model.patientSex = patientEty.getPatientSex();
    model.patientBirthTime = patientEty.getPatientBirthTime();
    model.otherPatientIds = patientEty.getOtherPatientIds();
    model.otherPatientNames = patientEty.getOtherPatientNames();
    model.ethnicGroup = patientEty.getEthnicGroup();
    model.patientComments = patientEty.getPatientComments();
    model.patientAge = patientEty.getPatientAge();
    model.patientSize = patientEty.getPatientSize();
    model.patientWeight = patientEty.getPatientWeight();
    model.medicalAlerts = patientEty.getMedicalAlerts();
    model.allergies = patientEty.getAllergies();
    model.pregnancyStatus = patientEty.getPregnancyStatus();
    model.responsiblePerson = patientEty.getResponsiblePerson();
    model.responsibleOrganization = patientEty.getResponsibleOrganization();

    return model;
  }


  public long getId()
  {
    return id;
  }

  public String getPatientsName()
  {
    return patientsName;
  }

  public String getPatientId()
  {
    return patientId;
  }

  public long getNumberOfStudies()
  {
    return numberOfStudies;
  }

  public List<StudyModel> getStudies()
  {
    return studies;
  }

  public LocalDate getPatientBirthDate()
  {
    return patientBirthDate;
  }

  public String getPatientSex()
  {
    return patientSex;
  }

  public LocalTime getPatientBirthTime()
  {
    return patientBirthTime;
  }

  public String getOtherPatientIds()
  {
    return otherPatientIds;
  }

  public String getOtherPatientNames()
  {
    return otherPatientNames;
  }

  public String getEthnicGroup()
  {
    return ethnicGroup;
  }

  public String getPatientComments()
  {
    return patientComments;
  }

  public String getPatientAge()
  {
    return patientAge;
  }

  public Double getPatientSize()
  {
    return patientSize;
  }

  public Double getPatientWeight()
  {
    return patientWeight;
  }

  public String getMedicalAlerts()
  {
    return medicalAlerts;
  }

  public String getAllergies()
  {
    return allergies;
  }

  public Integer getPregnancyStatus()
  {
    return pregnancyStatus;
  }

  public String getResponsiblePerson()
  {
    return responsiblePerson;
  }

  public String getResponsibleOrganization()
  {
    return responsibleOrganization;
  }

}
