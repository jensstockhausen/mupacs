package de.famst.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for AetEty entities.
 *
 * <p>Provides CRUD operations and query methods for managing
 * DICOM Application Entity Titles (AETs) in the database.
 *
 * @author jens
 * @since 2024-11-23
 */
@RepositoryRestResource(collectionResourceRel = "aets", path = "aets")
public interface AetRepository extends JpaRepository<AetEty, Long>
{
    /**
     * Finds an AET by its Application Entity Title.
     *
     * @param aet the Application Entity Title
     * @return Optional containing the AET if found, empty otherwise
     */
    Optional<AetEty> findByAet(@Param("aet") String aet);

    /**
     * Finds all AETs with a specific hostname.
     *
     * @param host the hostname or IP address
     * @return list of AETs with the specified host
     */
    List<AetEty> findByHost(@Param("host") String host);

    /**
     * Finds all AETs using a specific port.
     *
     * @param port the TCP port number
     * @return list of AETs using the specified port
     */
    List<AetEty> findByPort(@Param("port") int port);

    /**
     * Finds an AET by host and port combination.
     *
     * @param host the hostname or IP address
     * @param port the TCP port number
     * @return Optional containing the AET if found, empty otherwise
     */
    Optional<AetEty> findByHostAndPort(@Param("host") String host, @Param("port") int port);

    /**
     * Checks if an AET with the given Application Entity Title exists.
     *
     * @param aet the Application Entity Title
     * @return true if an AET exists, false otherwise
     */
    boolean existsByAet(@Param("aet") String aet);
}

