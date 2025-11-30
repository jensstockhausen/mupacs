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
 * JPA Entity representing a DICOM Series.
 *
 * <p>A Series is the third level in the DICOM hierarchy:
 * Patient → Study → <strong>Series</strong> → Instance
 *
 * <p>A Series represents a single set of images acquired in a specific sequence
 * or using a specific imaging protocol. For example, a CT scan study might have
 * separate series for axial, sagittal, and coronal reconstructions.
 *
 * <p>Standard DICOM Series Module attributes (from DICOM PS3.3):
 * <ul>
 *   <li>Series Instance UID (0020,000E) - Unique identifier for the series</li>
 *   <li>Series Number (0020,0011) - Number identifying the series within a study</li>
 *   <li>Modality (0008,0060) - The imaging equipment type (CT, MR, XR, etc.)</li>
 *   <li>Series Description (0008,103E) - Description of what the series contains</li>
 *   <li>Series Date (0008,0021) - Date the series was started</li>
 *   <li>Series Time (0008,0031) - Time the series was started</li>
 *   <li>Performing Physician Name (0008,1050) - Name of physician performing the procedure</li>
 *   <li>Protocol Name (0018,1030) - User-defined name of the protocol</li>
 *   <li>Body Part Examined (0018,0015) - Body part examined</li>
 *   <li>Patient Position (0018,5100) - Patient position descriptor</li>
 *   <li>Laterality (0020,0060) - Laterality of body part examined</li>
 *   <li>Operators' Name (0008,1070) - Name of operator(s) of the equipment</li>
 * </ul>
 *
 * @author jens
 * @since 2016-10-05
 */
@Entity
@Table(
    name = "SERIES",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "AK_SERIESUID",
            columnNames = {"seriesInstanceUID"})
    })
public class SeriesEty
{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(nullable = false, unique = true)
    private String seriesInstanceUID;

    private Integer seriesNumber;

    private String modality;

    @Column(length = 1000)
    private String seriesDescription;

    private LocalDate seriesDate;

    private LocalTime seriesTime;

    private String performingPhysicianName;

    private String protocolName;

    private String bodyPartExamined;

    private String patientPosition;

    private String laterality;

    private String operatorsName;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "series", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InstanceEty> instances = new ArrayList<>();


    @ManyToOne(fetch = FetchType.LAZY)
    private StudyEty study;

    /**
     * Default constructor required by JPA.
     */
    public SeriesEty()
    {
        // Required by JPA
    }

    /**
     * Constructor with required fields.
     *
     * @param seriesInstanceUID the unique Series Instance UID
     * @throws IllegalArgumentException if seriesInstanceUID is null or empty
     */
    public SeriesEty(String seriesInstanceUID)
    {
        setSeriesInstanceUID(seriesInstanceUID);
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
     * Returns the Series Instance UID.
     * This is the unique DICOM identifier for the series.
     *
     * @return the Series Instance UID, may be null if not set
     */
    public String getSeriesInstanceUID()
    {
        return seriesInstanceUID;
    }

    /**
     * Sets the Series Instance UID.
     *
     * @param seriesInstanceUID the Series Instance UID to set
     * @throws IllegalArgumentException if seriesInstanceUID is null or empty
     */
    public void setSeriesInstanceUID(String seriesInstanceUID)
    {
        if (seriesInstanceUID == null || seriesInstanceUID.trim().isEmpty())
        {
            throw new IllegalArgumentException("Series Instance UID cannot be null or empty");
        }
        this.seriesInstanceUID = seriesInstanceUID;
    }

    /**
     * Returns the series number.
     *
     * @return the series number, may be null
     */
    public Integer getSeriesNumber()
    {
        return seriesNumber;
    }

    /**
     * Sets the series number.
     *
     * @param seriesNumber the series number to set
     */
    public void setSeriesNumber(Integer seriesNumber)
    {
        this.seriesNumber = seriesNumber;
    }

    /**
     * Returns the modality.
     *
     * @return the modality (CT, MR, XR, etc.), may be null
     */
    public String getModality()
    {
        return modality;
    }

    /**
     * Sets the modality.
     *
     * @param modality the modality to set
     */
    public void setModality(String modality)
    {
        this.modality = modality;
    }

    /**
     * Returns the series description.
     *
     * @return the series description, may be null
     */
    public String getSeriesDescription()
    {
        return seriesDescription;
    }

    /**
     * Sets the series description.
     *
     * @param seriesDescription the series description to set
     */
    public void setSeriesDescription(String seriesDescription)
    {
        this.seriesDescription = seriesDescription;
    }

    /**
     * Returns the series date.
     *
     * @return the series date, may be null
     */
    public LocalDate getSeriesDate()
    {
        return seriesDate;
    }

    /**
     * Sets the series date.
     *
     * @param seriesDate the series date to set
     */
    public void setSeriesDate(LocalDate seriesDate)
    {
        this.seriesDate = seriesDate;
    }

    /**
     * Returns the series time.
     *
     * @return the series time, may be null
     */
    public LocalTime getSeriesTime()
    {
        return seriesTime;
    }

    /**
     * Sets the series time.
     *
     * @param seriesTime the series time to set
     */
    public void setSeriesTime(LocalTime seriesTime)
    {
        this.seriesTime = seriesTime;
    }

    /**
     * Returns the performing physician name.
     *
     * @return the performing physician name, may be null
     */
    public String getPerformingPhysicianName()
    {
        return performingPhysicianName;
    }

    /**
     * Sets the performing physician name.
     *
     * @param performingPhysicianName the performing physician name to set
     */
    public void setPerformingPhysicianName(String performingPhysicianName)
    {
        this.performingPhysicianName = performingPhysicianName;
    }

    /**
     * Returns the protocol name.
     *
     * @return the protocol name, may be null
     */
    public String getProtocolName()
    {
        return protocolName;
    }

    /**
     * Sets the protocol name.
     *
     * @param protocolName the protocol name to set
     */
    public void setProtocolName(String protocolName)
    {
        this.protocolName = protocolName;
    }

    /**
     * Returns the body part examined.
     *
     * @return the body part examined, may be null
     */
    public String getBodyPartExamined()
    {
        return bodyPartExamined;
    }

    /**
     * Sets the body part examined.
     *
     * @param bodyPartExamined the body part examined to set
     */
    public void setBodyPartExamined(String bodyPartExamined)
    {
        this.bodyPartExamined = bodyPartExamined;
    }

    /**
     * Returns the patient position.
     *
     * @return the patient position, may be null
     */
    public String getPatientPosition()
    {
        return patientPosition;
    }

    /**
     * Sets the patient position.
     *
     * @param patientPosition the patient position to set
     */
    public void setPatientPosition(String patientPosition)
    {
        this.patientPosition = patientPosition;
    }

    /**
     * Returns the laterality.
     *
     * @return the laterality (L=left, R=right), may be null
     */
    public String getLaterality()
    {
        return laterality;
    }

    /**
     * Sets the laterality.
     *
     * @param laterality the laterality to set
     */
    public void setLaterality(String laterality)
    {
        this.laterality = laterality;
    }

    /**
     * Returns the operators' name.
     *
     * @return the operators' name, may be null
     */
    public String getOperatorsName()
    {
        return operatorsName;
    }

    /**
     * Sets the operators' name.
     *
     * @param operatorsName the operators' name to set
     */
    public void setOperatorsName(String operatorsName)
    {
        this.operatorsName = operatorsName;
    }

    /**
     * Returns an unmodifiable view of the instances collection.
     *
     * @return unmodifiable collection of instances, never null
     */
    public Collection<InstanceEty> getInstances()
    {
        return Collections.unmodifiableList(instances);
    }

    /**
     * Adds an instance to this series.
     * This method is thread-safe and ensures bidirectional relationship consistency.
     *
     * @param instanceEty the instance to add
     * @throws IllegalArgumentException if instanceEty is null
     */
    public synchronized void addInstance(InstanceEty instanceEty)
    {
        if (instanceEty == null)
        {
            throw new IllegalArgumentException("Instance cannot be null");
        }

        if (instances.contains(instanceEty))
        {
            return;
        }

        instances.add(instanceEty);
        instanceEty.setSeries(this);
    }

    /**
     * Removes an instance from this series.
     *
     * @param instanceEty the instance to remove
     */
    public synchronized void removeInstance(InstanceEty instanceEty)
    {
        if (instanceEty != null)
        {
            instances.remove(instanceEty);
            instanceEty.setSeries(null);
        }
    }

    /**
     * Returns the number of instances in this series.
     *
     * @return the count of instances
     */
    public int getInstanceCount()
    {
        return instances.size();
    }

    /**
     * Sets the study for this series.
     * This method ensures bidirectional relationship consistency.
     *
     * @param study the study to associate with this series
     */
    public void setStudy(StudyEty study)
    {
        this.study = study;
    }

    /**
     * Returns the study associated with this series.
     *
     * @return the study, may be null
     */
    public StudyEty getStudy()
    {
        return study;
    }

    /**
     * Checks if this series has any instances.
     *
     * @return true if at least one instance exists, false otherwise
     */
    public boolean hasInstances()
    {
        return !instances.isEmpty();
    }

    /**
     * Checks if this series is associated with a study.
     *
     * @return true if study is set, false otherwise
     */
    public boolean hasStudy()
    {
        return study != null;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SeriesEty seriesEty = (SeriesEty) o;
        return seriesInstanceUID != null && Objects.equals(seriesInstanceUID, seriesEty.seriesInstanceUID);
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(seriesInstanceUID);
    }

    @Override
    public String toString()
    {
        return "SeriesEty{" +
            "id=" + id +
            ", seriesInstanceUID='" + seriesInstanceUID + '\'' +
            ", seriesNumber=" + seriesNumber +
            ", modality='" + modality + '\'' +
            ", seriesDescription='" + seriesDescription + '\'' +
            ", instanceCount=" + instances.size() +
            ", studyUID=" + (study != null ? study.getStudyInstanceUID() : "null") +
            '}';
    }
}

