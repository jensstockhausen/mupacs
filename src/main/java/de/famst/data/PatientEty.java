package de.famst.data;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by jens on 05/10/2016.
 */
@Entity
@Table(
  name = "PATIENT",
  uniqueConstraints = {
    @UniqueConstraint(
      name = "AK_PATIENTNAME",
      columnNames = {"patientName"})
  })
public class PatientEty
{
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private long id;

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "patient")
  private List<StudyEty> studies;

  private String patientName;

  private String patientId;

  public PatientEty()
  {
    studies = new ArrayList<>();
  }

  public PatientEty(String patientName, String patientId)
  {
    studies = new ArrayList<>();

    this.patientName = patientName;
    this.patientId = patientId;
  }

  public long getId()
  {
    return id;
  }

  public String getPatientName()
  {
    return patientName;
  }

  public void setPatientName(String patientName)
  {
    this.patientName = patientName;
  }

  public Collection<StudyEty> getStudies()
  {
    return studies;
  }

  public void addStudy(StudyEty study)
  {
    if (studies.contains(study))
    {
      return;
    }

    studies.add(study);
  }

  public String getPatientId()
  {
    return patientId;
  }

  public void setPatientId(String patientId)
  {
    this.patientId = patientId;
  }
}
