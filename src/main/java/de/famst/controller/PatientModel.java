package de.famst.controller;

import de.famst.data.PatientEty;

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

    return new PatientModel(
      patientEty.getId(),
      patientEty.getPatientName(),
      patientEty.getPatientId(),
      patientEty.getStudies().size(),
      studies
    );
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

}
