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
 * <p>Key DICOM attributes:
 * <ul>
 *   <li>Series Instance UID - Unique identifier for the series</li>
 *   <li>Series Number - Number identifying the series within a study</li>
 *   <li>Modality - The imaging equipment type (CT, MR, XR, etc.)</li>
 *   <li>Series Description - Description of what the series contains</li>
 * </ul>
 *
 * @author jens
 * @since 2016-10-05
 */
@Entity
@Table(
        name = "SERIES",
        uniqueConstraints={
                @UniqueConstraint(
                        name="AK_SERIESUID",
                        columnNames={"seriesInstanceUID"})
        })
public class SeriesEty
{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(nullable = false, unique = true)
    private String seriesInstanceUID;

    @OneToMany(fetch = FetchType.LAZY, mappedBy="series", cascade = CascadeType.ALL, orphanRemoval = true)
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
                ", instanceCount=" + instances.size() +
                ", studyUID=" + (study != null ? study.getStudyInstanceUID() : "null") +
                '}';
    }
}

