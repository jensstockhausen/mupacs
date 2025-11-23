package de.famst.data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.util.Objects;

/**
 * JPA Entity representing a DICOM Application Entity Title (AET).
 *
 * <p>An AET represents a remote DICOM node that can communicate with this PACS system.
 * Each AET contains the network configuration needed to establish DICOM connections.
 *
 * <p>Key attributes:
 * <ul>
 *   <li>AET - The Application Entity Title (unique identifier)</li>
 *   <li>Host - The hostname or IP address of the remote system</li>
 *   <li>Port - The TCP port number for DICOM communication</li>
 * </ul>
 *
 * @author jens
 * @since 2024-11-23
 */
@Entity
@Table(
    name = "AET",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "AK_AET",
            columnNames = {"aet"})
    })
public class AetEty
{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(nullable = false, unique = true)
    private String aet;

    @Column(nullable = false)
    private String host;

    @Column(nullable = false)
    private int port;

    /**
     * Default constructor required by JPA.
     */
    public AetEty()
    {
        // Required by JPA
    }

    /**
     * Constructor with required fields.
     *
     * @param aet  the Application Entity Title
     * @param host the hostname or IP address
     * @param port the TCP port number
     * @throws IllegalArgumentException if aet or host is null/empty, or port is invalid
     */
    public AetEty(String aet, String host, int port)
    {
        setAet(aet);
        setHost(host);
        setPort(port);
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
     * Returns the Application Entity Title.
     *
     * @return the AET, may be null if not set
     */
    public String getAet()
    {
        return aet;
    }

    /**
     * Sets the Application Entity Title.
     *
     * @param aet the AET to set
     * @throws IllegalArgumentException if aet is null or empty
     */
    public void setAet(String aet)
    {
        if (aet == null || aet.trim().isEmpty())
        {
            throw new IllegalArgumentException("AET cannot be null or empty");
        }
        this.aet = aet.trim();
    }

    /**
     * Returns the hostname or IP address.
     *
     * @return the host, may be null if not set
     */
    public String getHost()
    {
        return host;
    }

    /**
     * Sets the hostname or IP address.
     *
     * @param host the host to set
     * @throws IllegalArgumentException if host is null or empty
     */
    public void setHost(String host)
    {
        if (host == null || host.trim().isEmpty())
        {
            throw new IllegalArgumentException("Host cannot be null or empty");
        }
        this.host = host.trim();
    }

    /**
     * Returns the TCP port number.
     *
     * @return the port number
     */
    public int getPort()
    {
        return port;
    }

    /**
     * Sets the TCP port number.
     *
     * @param port the port to set
     * @throws IllegalArgumentException if port is not in valid range (1-65535)
     */
    public void setPort(int port)
    {
        if (port < 1 || port > 65535)
        {
            throw new IllegalArgumentException("Port must be between 1 and 65535");
        }
        this.port = port;
    }

    /**
     * Returns the connection string in format "aet@host:port".
     *
     * @return the connection string
     */
    public String getConnectionString()
    {
        return aet + "@" + host + ":" + port;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AetEty aetEty = (AetEty) o;
        return aet != null && Objects.equals(aet, aetEty.aet);
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(aet);
    }

    @Override
    public String toString()
    {
        return "AetEty{" +
            "id=" + id +
            ", aet='" + aet + '\'' +
            ", host='" + host + '\'' +
            ", port=" + port +
            '}';
    }
}

