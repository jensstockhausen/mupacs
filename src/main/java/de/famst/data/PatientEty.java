package de.famst.data;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * JPA Entity representing a DICOM Patient.
 *
 * <p>A Patient is the top level in the DICOM hierarchy:
 * <strong>Patient</strong> → Study → Series → Instance
 *
 * <p>A Patient represents an individual person who is the subject of
 * medical imaging studies. A patient can have multiple studies associated
 * with them over time.
 *
 * <p>Key DICOM attributes:
 * <ul>
 *   <li>Patient Name - The name of the patient (unique in this system)</li>
 *   <li>Patient ID - An identifier for the patient from the ordering system</li>
 * </ul>
 *
 * @author jens
 * @since 2016-10-05
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

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "patient", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<StudyEty> studies = new ArrayList<>();

  @Column(nullable = false, unique = true)
  private String patientName;

  private String patientId;

  /**
   * Default constructor required by JPA.
   */
  public PatientEty()
  {
    // Required by JPA
  }

  /**
   * Constructor with required fields.
   *
   * @param patientName the patient name
   * @param patientId the patient ID
   * @throws IllegalArgumentException if patientName is null or empty
   */
  public PatientEty(String patientName, String patientId)
  {
    setPatientName(patientName);
    this.patientId = patientId;
  }

  /**
   * Returns the database primary key.
   *
   * @return the entity ID
   */
  public long getId()
  {
    return id;
  }

  /**
   * Returns the patient name.
   * This is the unique identifier for the patient in this system.
   *
   * @return the patient name, may be null if not set
   */
  public String getPatientName()
  {
    return patientName;
  }

  /**
   * Sets the patient name.
   *
   * @param patientName the patient name to set
   * @throws IllegalArgumentException if patientName is null or empty
   */
  public void setPatientName(String patientName)
  {
    if (patientName == null || patientName.trim().isEmpty())
    {
      throw new IllegalArgumentException("Patient name cannot be null or empty");
    }
    this.patientName = patientName;
  }

  /**
   * Returns an unmodifiable view of the studies collection.
   *
   * @return unmodifiable collection of studies, never null
   */
  public Collection<StudyEty> getStudies()
  {
    return Collections.unmodifiableList(studies);
  }

  /**
   * Adds a study to this patient.
   * This method ensures bidirectional relationship consistency.
   *
   * @param study the study to add
   * @throws IllegalArgumentException if study is null
   */
  public void addStudy(StudyEty study)
  {
    if (study == null)
    {
      throw new IllegalArgumentException("Study cannot be null");
    }

    if (studies.contains(study))
    {
      return;
    }

    studies.add(study);
    study.setPatient(this);
  }

  /**
   * Removes a study from this patient.
   *
   * @param study the study to remove
   */
  public void removeStudy(StudyEty study)
  {
    if (study != null)
    {
      studies.remove(study);
      study.setPatient(null);
    }
  }

  /**
   * Returns the number of studies for this patient.
   *
   * @return the count of studies
   */
  public int getStudyCount()
  {
    return studies.size();
  }

  /**
   * Returns the patient ID from the ordering system.
   *
   * @return the patient ID, may be null
   */
  public String getPatientId()
  {
    return patientId;
  }

  /**
   * Sets the patient ID.
   *
   * @param patientId the patient ID to set
   */
  public void setPatientId(String patientId)
  {
    this.patientId = patientId;
  }

  /**
   * Checks if this patient has any studies.
   *
   * @return true if at least one study exists, false otherwise
   */
  public boolean hasStudies()
  {
    return !studies.isEmpty();
  }

  @Override
  public boolean equals(Object o)
  {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    PatientEty that = (PatientEty) o;
    return patientName != null && Objects.equals(patientName, that.patientName);
  }

  @Override
  public int hashCode()
  {
    return Objects.hashCode(patientName);
  }

  @Override
  public String toString()
  {
    return "PatientEty{" +
            "id=" + id +
            ", patientName='" + patientName + '\'' +
            ", patientId='" + patientId + '\'' +
            ", studyCount=" + studies.size() +
            '}';
  }
}

