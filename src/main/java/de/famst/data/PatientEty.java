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

import java.time.LocalDate;
import java.time.LocalTime;
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
 * <p>Standard DICOM Patient Module attributes (from DICOM PS3.3):
 * <ul>
 *   <li>Patient Name (0010,0010) - The name of the patient</li>
 *   <li>Patient ID (0010,0020) - Primary identifier for the patient</li>
 *   <li>Patient Birth Date (0010,0030) - Birth date of the patient</li>
 *   <li>Patient Sex (0010,0040) - Sex of the patient</li>
 *   <li>Patient Birth Time (0010,0032) - Birth time of the patient</li>
 *   <li>Other Patient IDs (0010,1000) - Other identification numbers or codes</li>
 *   <li>Other Patient Names (0010,1001) - Other names for the patient</li>
 *   <li>Ethnic Group (0010,2160) - Ethnic group or race of patient</li>
 *   <li>Patient Comments (0010,4000) - Comments about the patient</li>
 *   <li>Patient's Age (0010,1010) - Age of patient</li>
 *   <li>Patient's Size (0010,1020) - Height or length of patient in meters</li>
 *   <li>Patient's Weight (0010,1030) - Weight of patient in kilograms</li>
 *   <li>Medical Alerts (0010,2000) - Medical alerts for this patient</li>
 *   <li>Allergies (0010,2110) - Patient allergies</li>
 *   <li>Pregnancy Status (0010,21C0) - Pregnancy status of patient</li>
 *   <li>Responsible Person (0010,2297) - Name of person with medical decision authority</li>
 *   <li>Responsible Organization (0010,2299) - Name of organization with medical decision authority</li>
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
            name = "AK_PATIENTID",
            columnNames = {"patientId"})
    })
public class PatientEty
{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "patient", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StudyEty> studies = new ArrayList<>();

    private String patientName;

    @Column(nullable = false, unique = true)
    private String patientId;

    private LocalDate patientBirthDate;

    private String patientSex;

    private LocalTime patientBirthTime;

    @Column(length = 1000)
    private String otherPatientIds;

    @Column(length = 1000)
    private String otherPatientNames;

    private String ethnicGroup;

    @Column(length = 2000)
    private String patientComments;

    private String patientAge;

    private Double patientSize;

    private Double patientWeight;

    @Column(length = 1000)
    private String medicalAlerts;

    @Column(length = 1000)
    private String allergies;

    private Integer pregnancyStatus;

    private String responsiblePerson;

    private String responsibleOrganization;

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
     * @param patientId   the patient ID (unique identifier)
     * @throws IllegalArgumentException if patientId is null or empty
     */
    public PatientEty(String patientName, String patientId)
    {
        this.patientName = patientName;
        setPatientId(patientId);
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
     */
    public void setPatientName(String patientName)
    {
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
     * This is the unique identifier for the patient in this system.
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
     * @throws IllegalArgumentException if patientId is null or empty
     */
    public void setPatientId(String patientId)
    {
        if (patientId == null || patientId.trim().isEmpty())
        {
            throw new IllegalArgumentException("Patient ID cannot be null or empty");
        }
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

    /**
     * Returns the patient's birth date.
     *
     * @return the patient birth date, may be null
     */
    public LocalDate getPatientBirthDate()
    {
        return patientBirthDate;
    }

    /**
     * Sets the patient's birth date.
     *
     * @param patientBirthDate the patient birth date to set
     */
    public void setPatientBirthDate(LocalDate patientBirthDate)
    {
        this.patientBirthDate = patientBirthDate;
    }

    /**
     * Returns the patient's sex.
     *
     * @return the patient sex (M, F, O, or U), may be null
     */
    public String getPatientSex()
    {
        return patientSex;
    }

    /**
     * Sets the patient's sex.
     *
     * @param patientSex the patient sex to set (M=male, F=female, O=other, U=unknown)
     */
    public void setPatientSex(String patientSex)
    {
        this.patientSex = patientSex;
    }

    /**
     * Returns the patient's birth time.
     *
     * @return the patient birth time, may be null
     */
    public LocalTime getPatientBirthTime()
    {
        return patientBirthTime;
    }

    /**
     * Sets the patient's birth time.
     *
     * @param patientBirthTime the patient birth time to set
     */
    public void setPatientBirthTime(LocalTime patientBirthTime)
    {
        this.patientBirthTime = patientBirthTime;
    }

    /**
     * Returns other patient IDs.
     *
     * @return other patient IDs, may be null
     */
    public String getOtherPatientIds()
    {
        return otherPatientIds;
    }

    /**
     * Sets other patient IDs.
     *
     * @param otherPatientIds other patient IDs to set
     */
    public void setOtherPatientIds(String otherPatientIds)
    {
        this.otherPatientIds = otherPatientIds;
    }

    /**
     * Returns other patient names.
     *
     * @return other patient names, may be null
     */
    public String getOtherPatientNames()
    {
        return otherPatientNames;
    }

    /**
     * Sets other patient names.
     *
     * @param otherPatientNames other patient names to set
     */
    public void setOtherPatientNames(String otherPatientNames)
    {
        this.otherPatientNames = otherPatientNames;
    }

    /**
     * Returns the patient's ethnic group.
     *
     * @return the ethnic group, may be null
     */
    public String getEthnicGroup()
    {
        return ethnicGroup;
    }

    /**
     * Sets the patient's ethnic group.
     *
     * @param ethnicGroup the ethnic group to set
     */
    public void setEthnicGroup(String ethnicGroup)
    {
        this.ethnicGroup = ethnicGroup;
    }

    /**
     * Returns patient comments.
     *
     * @return patient comments, may be null
     */
    public String getPatientComments()
    {
        return patientComments;
    }

    /**
     * Sets patient comments.
     *
     * @param patientComments patient comments to set
     */
    public void setPatientComments(String patientComments)
    {
        this.patientComments = patientComments;
    }

    /**
     * Returns the patient's age.
     *
     * @return the patient age as a string (e.g., "042Y"), may be null
     */
    public String getPatientAge()
    {
        return patientAge;
    }

    /**
     * Sets the patient's age.
     *
     * @param patientAge the patient age to set
     */
    public void setPatientAge(String patientAge)
    {
        this.patientAge = patientAge;
    }

    /**
     * Returns the patient's size (height) in meters.
     *
     * @return the patient size in meters, may be null
     */
    public Double getPatientSize()
    {
        return patientSize;
    }

    /**
     * Sets the patient's size (height) in meters.
     *
     * @param patientSize the patient size in meters to set
     */
    public void setPatientSize(Double patientSize)
    {
        this.patientSize = patientSize;
    }

    /**
     * Returns the patient's weight in kilograms.
     *
     * @return the patient weight in kg, may be null
     */
    public Double getPatientWeight()
    {
        return patientWeight;
    }

    /**
     * Sets the patient's weight in kilograms.
     *
     * @param patientWeight the patient weight in kg to set
     */
    public void setPatientWeight(Double patientWeight)
    {
        this.patientWeight = patientWeight;
    }

    /**
     * Returns medical alerts for this patient.
     *
     * @return medical alerts, may be null
     */
    public String getMedicalAlerts()
    {
        return medicalAlerts;
    }

    /**
     * Sets medical alerts for this patient.
     *
     * @param medicalAlerts medical alerts to set
     */
    public void setMedicalAlerts(String medicalAlerts)
    {
        this.medicalAlerts = medicalAlerts;
    }

    /**
     * Returns patient allergies.
     *
     * @return allergies, may be null
     */
    public String getAllergies()
    {
        return allergies;
    }

    /**
     * Sets patient allergies.
     *
     * @param allergies allergies to set
     */
    public void setAllergies(String allergies)
    {
        this.allergies = allergies;
    }

    /**
     * Returns the pregnancy status.
     *
     * @return pregnancy status (1=not pregnant, 2=possibly pregnant, 3=definitely pregnant, 4=unknown), may be null
     */
    public Integer getPregnancyStatus()
    {
        return pregnancyStatus;
    }

    /**
     * Sets the pregnancy status.
     *
     * @param pregnancyStatus pregnancy status to set
     */
    public void setPregnancyStatus(Integer pregnancyStatus)
    {
        this.pregnancyStatus = pregnancyStatus;
    }

    /**
     * Returns the responsible person.
     *
     * @return responsible person name, may be null
     */
    public String getResponsiblePerson()
    {
        return responsiblePerson;
    }

    /**
     * Sets the responsible person.
     *
     * @param responsiblePerson responsible person name to set
     */
    public void setResponsiblePerson(String responsiblePerson)
    {
        this.responsiblePerson = responsiblePerson;
    }

    /**
     * Returns the responsible organization.
     *
     * @return responsible organization name, may be null
     */
    public String getResponsibleOrganization()
    {
        return responsibleOrganization;
    }

    /**
     * Sets the responsible organization.
     *
     * @param responsibleOrganization responsible organization name to set
     */
    public void setResponsibleOrganization(String responsibleOrganization)
    {
        this.responsibleOrganization = responsibleOrganization;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PatientEty that = (PatientEty) o;
        return patientId != null && Objects.equals(patientId, that.patientId);
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(patientId);
    }

    @Override
    public String toString()
    {
        return "PatientEty{" +
            "id=" + id +
            ", patientName='" + patientName + '\'' +
            ", patientId='" + patientId + '\'' +
            ", patientSex='" + patientSex + '\'' +
            ", patientBirthDate=" + patientBirthDate +
            ", studyCount=" + studies.size() +
            '}';
    }
}

