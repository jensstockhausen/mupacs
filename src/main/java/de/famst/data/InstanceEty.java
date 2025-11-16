package de.famst.data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.util.Objects;

/**
 * JPA Entity representing a DICOM Instance.
 *
 * <p>An Instance is the bottom level in the DICOM hierarchy:
 * Patient → Study → Series → <strong>Instance</strong>
 *
 * <p>An Instance represents a single DICOM object, typically a single image
 * or a single frame in a multi-frame image. Each instance is uniquely identified
 * by its SOP Instance UID and has a file path where the DICOM file is stored.
 *
 * <p>Key DICOM attributes:
 * <ul>
 *   <li>SOP Instance UID - Unique identifier for the instance</li>
 *   <li>Instance Number - Number identifying the instance within a series</li>
 *   <li>File Path - Location where the DICOM file is stored</li>
 * </ul>
 *
 * @author jens
 * @since 2016-10-03
 */
@Entity
@Table(
        name = "INSTANCE",
        uniqueConstraints={
                @UniqueConstraint(
                        name="AK_INSTANCEUID",
                        columnNames={"instanceUID"})
        })
public class InstanceEty
{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(nullable = false)
    private String instanceUID;

    @Column(nullable = false)
    private String path;

    @ManyToOne(fetch = FetchType.LAZY)
    private SeriesEty series;

    /**
     * Default constructor required by JPA.
     */
    public InstanceEty()
    {
        // Required by JPA
    }

    /**
     * Constructor with required fields.
     *
     * @param instanceUID the unique SOP Instance UID
     * @param path the file path where the DICOM file is stored
     * @throws IllegalArgumentException if instanceUID or path is null or empty
     */
    public InstanceEty(String instanceUID, String path)
    {
        setInstanceUID(instanceUID);
        setPath(path);
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
     * Returns the SOP Instance UID.
     * This is the unique DICOM identifier for the instance.
     *
     * @return the SOP Instance UID, may be null if not set
     */
    public String getInstanceUID()
    {
        return instanceUID;
    }

    /**
     * Sets the SOP Instance UID.
     *
     * @param instanceUID the SOP Instance UID to set
     * @throws IllegalArgumentException if instanceUID is null or empty
     */
    public void setInstanceUID(String instanceUID)
    {
        if (instanceUID == null || instanceUID.trim().isEmpty())
        {
            throw new IllegalArgumentException("Instance UID cannot be null or empty");
        }
        this.instanceUID = instanceUID;
    }

    /**
     * Returns the series associated with this instance.
     *
     * @return the series, may be null
     */
    public SeriesEty getSeries()
    {
        return series;
    }

    /**
     * Sets the series for this instance.
     * This method ensures bidirectional relationship consistency.
     *
     * @param series the series to associate with this instance
     */
    public void setSeries(SeriesEty series)
    {
        this.series = series;
    }

    /**
     * Returns the file path where the DICOM file is stored.
     *
     * @return the file path, may be null if not set
     */
    public String getPath()
    {
        return path;
    }

    /**
     * Sets the file path for this instance.
     *
     * @param path the file path to set
     * @throws IllegalArgumentException if path is null or empty
     */
    public void setPath(String path)
    {
        if (path == null || path.trim().isEmpty())
        {
            throw new IllegalArgumentException("Path cannot be null or empty");
        }
        this.path = path;
    }

    /**
     * Checks if this instance is associated with a series.
     *
     * @return true if series is set, false otherwise
     */
    public boolean hasSeries()
    {
        return series != null;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InstanceEty that = (InstanceEty) o;
        return instanceUID != null && Objects.equals(instanceUID, that.instanceUID);
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(instanceUID);
    }

    @Override
    public String toString()
    {
        return "InstanceEty{" +
                "id=" + id +
                ", instanceUID='" + instanceUID + '\'' +
                ", path='" + path + '\'' +
                ", seriesUID=" + (series != null ? series.getSeriesInstanceUID() : "null") +
                '}';
    }
}

