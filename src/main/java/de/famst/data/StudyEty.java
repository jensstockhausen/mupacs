package de.famst.data;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * JPA Entity representing a DICOM Study.
 *
 * <p>A Study is the second level in the DICOM hierarchy:
 * Patient → <strong>Study</strong> → Series → Instance
 *
 * <p>A Study represents a single imaging examination of a patient, such as
 * a CT scan, MRI scan, or X-ray examination. It is uniquely identified by
 * the Study Instance UID and can contain multiple Series.
 *
 * <p>Key DICOM attributes:
 * <ul>
 *   <li>Study Instance UID - Unique identifier for the study</li>
 *   <li>Study Date/Time - When the study was performed</li>
 *   <li>Study Description - Description of the examination</li>
 *   <li>Accession Number - Identifier from the ordering system</li>
 *   <li>Modalities - Imaging modalities used (CT, MR, XR, etc.)</li>
 * </ul>
 *
 * @author jens
 * @since 2016-10-03
 */
@Entity
@Table(
  name = "STUDY",
  uniqueConstraints = {
    @UniqueConstraint(
      name = "AK_STUDYUID",
      columnNames = {"studyInstanceUID"})
  })
public class StudyEty
{
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private long id;

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "study", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<SeriesEty> series = new ArrayList<>();

  @ManyToOne(fetch = FetchType.LAZY)
  private PatientEty patient;

  @Column(nullable = false, unique = true)
  private String studyInstanceUID;

  private String studyId;
  private String studyDescription;
  private LocalDate studyDate;
  private LocalTime studyTime;
  private String accessionNumber;
  private String modalitiesInStudy;
  private String referringPhysicianName;

  /**
   * Default constructor required by JPA.
   */
  public StudyEty()
  {
    // Required by JPA
  }

  /**
   * Constructor with required fields.
   *
   * @param studyInstanceUID the unique Study Instance UID
   * @throws IllegalArgumentException if studyInstanceUID is null or empty
   */
  public StudyEty(String studyInstanceUID)
  {
    setStudyInstanceUID(studyInstanceUID);
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
   * Returns the Study Instance UID.
   * This is the unique DICOM identifier for the study.
   *
   * @return the Study Instance UID, may be null if not set
   */
  public String getStudyInstanceUID()
  {
    return studyInstanceUID;
  }

  /**
   * Sets the Study Instance UID.
   *
   * @param studyInstanceUID the Study Instance UID to set
   * @throws IllegalArgumentException if studyInstanceUID is null or empty
   */
  public void setStudyInstanceUID(String studyInstanceUID)
  {
    if (studyInstanceUID == null || studyInstanceUID.trim().isEmpty())
    {
      throw new IllegalArgumentException("Study Instance UID cannot be null or empty");
    }
    this.studyInstanceUID = studyInstanceUID;
  }

  /**
   * Returns an unmodifiable view of the series collection.
   *
   * @return unmodifiable collection of series, never null
   */
  public Collection<SeriesEty> getSeries()
  {
    return Collections.unmodifiableList(series);
  }

  /**
   * Adds a series to this study.
   * This method ensures bidirectional relationship consistency.
   *
   * @param seriesEty the series to add
   * @throws IllegalArgumentException if seriesEty is null
   */
  public void addSeries(SeriesEty seriesEty)
  {
    if (seriesEty == null)
    {
      throw new IllegalArgumentException("Series cannot be null");
    }

    if (series.contains(seriesEty))
    {
      return;
    }

    series.add(seriesEty);
    seriesEty.setStudy(this);
  }

  /**
   * Removes a series from this study.
   *
   * @param seriesEty the series to remove
   */
  public void removeSeries(SeriesEty seriesEty)
  {
    if (seriesEty != null)
    {
      series.remove(seriesEty);
      seriesEty.setStudy(null);
    }
  }

  /**
   * Returns the number of series in this study.
   *
   * @return the count of series
   */
  public int getSeriesCount()
  {
    return series.size();
  }

  /**
   * Returns the patient associated with this study.
   *
   * @return the patient, may be null
   */
  public PatientEty getPatient()
  {
    return patient;
  }

  /**
   * Sets the patient for this study.
   * This method ensures bidirectional relationship consistency.
   *
   * @param patient the patient to associate with this study
   */
  public void setPatient(PatientEty patient)
  {
    this.patient = patient;
  }

  public LocalDate getStudyDate()
  {
    return studyDate;
  }

  public void setStudyDate(LocalDate studyDate)
  {
    this.studyDate = studyDate;
  }

  public LocalTime getStudyTime()
  {
    return studyTime;
  }

  public void setStudyTime(LocalTime studyTime)
  {
    this.studyTime = studyTime;
  }

  public String getAccessionNumber()
  {
    return accessionNumber;
  }

  public void setAccessionNumber(String accessionNumber)
  {
    this.accessionNumber = accessionNumber;
  }

  public String getModalitiesInStudy()
  {
    return modalitiesInStudy;
  }

  public void setModalitiesInStudy(String modalitiesInStudy)
  {
    this.modalitiesInStudy = modalitiesInStudy;
  }

  public String getReferringPhysicianName()
  {
    return referringPhysicianName;
  }

  public void setReferringPhysicianName(String referringPhysicianName)
  {
    this.referringPhysicianName = referringPhysicianName;
  }

  public String getStudyId()
  {
    return studyId;
  }

  public void setStudyId(String studyId)
  {
    this.studyId = studyId;
  }

  public void setStudyDescription(String studyDescription)
  {
    this.studyDescription = studyDescription;
  }

  public String getStudyDescription()
  {
    return studyDescription;
  }

  /**
   * Checks if this study has any series.
   *
   * @return true if at least one series exists, false otherwise
   */
  public boolean hasSeries()
  {
    return !series.isEmpty();
  }

  /**
   * Checks if this study is associated with a patient.
   *
   * @return true if patient is set, false otherwise
   */
  public boolean hasPatient()
  {
    return patient != null;
  }

  @Override
  public boolean equals(Object o)
  {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    StudyEty studyEty = (StudyEty) o;
    return studyInstanceUID != null && Objects.equals(studyInstanceUID, studyEty.studyInstanceUID);
  }

  @Override
  public int hashCode()
  {
    return Objects.hashCode(studyInstanceUID);
  }

  @Override
  public String toString()
  {
    return "StudyEty{" +
            "id=" + id +
            ", studyInstanceUID='" + studyInstanceUID + '\'' +
            ", studyId='" + studyId + '\'' +
            ", studyDescription='" + studyDescription + '\'' +
            ", studyDate=" + studyDate +
            ", seriesCount=" + series.size() +
            ", patientName=" + (patient != null ? patient.getPatientName() : "null") +
            '}';
  }

}
