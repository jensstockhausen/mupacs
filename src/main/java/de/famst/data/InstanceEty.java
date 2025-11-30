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

import java.time.LocalDate;
import java.time.LocalTime;
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
 * <p>Standard DICOM Instance/Image Module attributes (from DICOM PS3.3):
 * <ul>
 *   <li>SOP Instance UID (0008,0018) - Unique identifier for the instance</li>
 *   <li>Instance Number (0020,0013) - Number identifying the instance within a series</li>
 *   <li>Content Date (0008,0023) - Date the image pixel data creation started</li>
 *   <li>Content Time (0008,0033) - Time the image pixel data creation started</li>
 *   <li>Image Type (0008,0008) - Image identification characteristics</li>
 *   <li>Acquisition Number (0020,0012) - Number identifying the acquisition</li>
 *   <li>Acquisition Date (0008,0022) - Date the acquisition of data started</li>
 *   <li>Acquisition Time (0008,0032) - Time the acquisition of data started</li>
 *   <li>Image Comments (0020,4000) - User-defined comments about the image</li>
 *   <li>Rows (0028,0010) - Number of rows in the image</li>
 *   <li>Columns (0028,0011) - Number of columns in the image</li>
 *   <li>Bits Allocated (0028,0100) - Number of bits allocated for each pixel sample</li>
 *   <li>Bits Stored (0028,0101) - Number of bits stored for each pixel sample</li>
 *   <li>File Path - Location where the DICOM file is stored (local attribute)</li>
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

    @Column(nullable = false, unique = true)
    private String instanceUID;

    @Column(nullable = false)
    private String path;

    private Integer instanceNumber;

    private LocalDate contentDate;

    private LocalTime contentTime;

    private String imageType;

    private Integer acquisitionNumber;

    private LocalDate acquisitionDate;

    private LocalTime acquisitionTime;

    @Column(length = 2000)
    private String imageComments;

    private Integer rows;

    private Integer columns;

    private Integer bitsAllocated;

    private Integer bitsStored;

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
     * Returns the instance number.
     *
     * @return the instance number, may be null
     */
    public Integer getInstanceNumber()
    {
        return instanceNumber;
    }

    /**
     * Sets the instance number.
     *
     * @param instanceNumber the instance number to set
     */
    public void setInstanceNumber(Integer instanceNumber)
    {
        this.instanceNumber = instanceNumber;
    }

    /**
     * Returns the content date.
     *
     * @return the content date, may be null
     */
    public LocalDate getContentDate()
    {
        return contentDate;
    }

    /**
     * Sets the content date.
     *
     * @param contentDate the content date to set
     */
    public void setContentDate(LocalDate contentDate)
    {
        this.contentDate = contentDate;
    }

    /**
     * Returns the content time.
     *
     * @return the content time, may be null
     */
    public LocalTime getContentTime()
    {
        return contentTime;
    }

    /**
     * Sets the content time.
     *
     * @param contentTime the content time to set
     */
    public void setContentTime(LocalTime contentTime)
    {
        this.contentTime = contentTime;
    }

    /**
     * Returns the image type.
     *
     * @return the image type, may be null
     */
    public String getImageType()
    {
        return imageType;
    }

    /**
     * Sets the image type.
     *
     * @param imageType the image type to set
     */
    public void setImageType(String imageType)
    {
        this.imageType = imageType;
    }

    /**
     * Returns the acquisition number.
     *
     * @return the acquisition number, may be null
     */
    public Integer getAcquisitionNumber()
    {
        return acquisitionNumber;
    }

    /**
     * Sets the acquisition number.
     *
     * @param acquisitionNumber the acquisition number to set
     */
    public void setAcquisitionNumber(Integer acquisitionNumber)
    {
        this.acquisitionNumber = acquisitionNumber;
    }

    /**
     * Returns the acquisition date.
     *
     * @return the acquisition date, may be null
     */
    public LocalDate getAcquisitionDate()
    {
        return acquisitionDate;
    }

    /**
     * Sets the acquisition date.
     *
     * @param acquisitionDate the acquisition date to set
     */
    public void setAcquisitionDate(LocalDate acquisitionDate)
    {
        this.acquisitionDate = acquisitionDate;
    }

    /**
     * Returns the acquisition time.
     *
     * @return the acquisition time, may be null
     */
    public LocalTime getAcquisitionTime()
    {
        return acquisitionTime;
    }

    /**
     * Sets the acquisition time.
     *
     * @param acquisitionTime the acquisition time to set
     */
    public void setAcquisitionTime(LocalTime acquisitionTime)
    {
        this.acquisitionTime = acquisitionTime;
    }

    /**
     * Returns the image comments.
     *
     * @return the image comments, may be null
     */
    public String getImageComments()
    {
        return imageComments;
    }

    /**
     * Sets the image comments.
     *
     * @param imageComments the image comments to set
     */
    public void setImageComments(String imageComments)
    {
        this.imageComments = imageComments;
    }

    /**
     * Returns the number of rows in the image.
     *
     * @return the number of rows, may be null
     */
    public Integer getRows()
    {
        return rows;
    }

    /**
     * Sets the number of rows in the image.
     *
     * @param rows the number of rows to set
     */
    public void setRows(Integer rows)
    {
        this.rows = rows;
    }

    /**
     * Returns the number of columns in the image.
     *
     * @return the number of columns, may be null
     */
    public Integer getColumns()
    {
        return columns;
    }

    /**
     * Sets the number of columns in the image.
     *
     * @param columns the number of columns to set
     */
    public void setColumns(Integer columns)
    {
        this.columns = columns;
    }

    /**
     * Returns the bits allocated per pixel sample.
     *
     * @return the bits allocated, may be null
     */
    public Integer getBitsAllocated()
    {
        return bitsAllocated;
    }

    /**
     * Sets the bits allocated per pixel sample.
     *
     * @param bitsAllocated the bits allocated to set
     */
    public void setBitsAllocated(Integer bitsAllocated)
    {
        this.bitsAllocated = bitsAllocated;
    }

    /**
     * Returns the bits stored per pixel sample.
     *
     * @return the bits stored, may be null
     */
    public Integer getBitsStored()
    {
        return bitsStored;
    }

    /**
     * Sets the bits stored per pixel sample.
     *
     * @param bitsStored the bits stored to set
     */
    public void setBitsStored(Integer bitsStored)
    {
        this.bitsStored = bitsStored;
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
                ", instanceNumber=" + instanceNumber +
                ", path='" + path + '\'' +
                ", rows=" + rows +
                ", columns=" + columns +
                ", seriesUID=" + (series != null ? series.getSeriesInstanceUID() : "null") +
                '}';
    }
}

