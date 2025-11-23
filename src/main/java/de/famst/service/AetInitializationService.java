package de.famst.service;

import de.famst.data.AetEty;
import de.famst.data.AetRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Service for initializing and managing DICOM Application Entity Titles (AETs).
 *
 * <p>This service reads AET configurations from application.properties on startup
 * and populates the database with the configured remote DICOM nodes.
 *
 * <p>Property format: {@code mupacs.dicom.aet=aet@host:port;aet@host:port;...}
 *
 * @author jens
 * @since 2024-11-23
 */
@Service
public class AetInitializationService
{
    private static final Logger LOG = LoggerFactory.getLogger(AetInitializationService.class);

    private final AetRepository aetRepository;

    @Value("${mupacs.dicom.aet:}")
    private String aetConfigString;

    public AetInitializationService(AetRepository aetRepository)
    {
        this.aetRepository = aetRepository;
    }

    /**
     * Initializes the AET table from application.properties after bean construction.
     * This method is automatically called by Spring after the bean is fully initialized.
     */
    @PostConstruct
    public void initializeAets()
    {
        LOG.info("Initializing DICOM AETs from configuration");

        if (aetConfigString == null || aetConfigString.trim().isEmpty())
        {
            LOG.info("No AETs configured in application.properties (mupacs.dicom.aet is empty)");
            return;
        }

        List<AetEty> aetsToCreate = parseAetConfiguration(aetConfigString);

        if (aetsToCreate.isEmpty())
        {
            LOG.warn("No valid AETs found in configuration string: [{}]", aetConfigString);
            return;
        }

        int created = 0;
        int skipped = 0;
        int failed = 0;

        for (AetEty aet : aetsToCreate)
        {
            try
            {
                if (aetRepository.existsByAet(aet.getAet()))
                {
                    LOG.debug("AET [{}] already exists, skipping", aet.getAet());
                    skipped++;
                    continue;
                }

                aetRepository.save(aet);
                LOG.info("Created AET: [{}]", aet.getConnectionString());
                created++;
            }
            catch (Exception e)
            {
                LOG.error("Failed to create AET [{}]: {}", aet.getAet(), e.getMessage(), e);
                failed++;
            }
        }

        LOG.info("AET initialization complete: {} created, {} skipped, {} failed", created, skipped, failed);
    }

    /**
     * Parses the AET configuration string into a list of AetEty objects.
     *
     * <p>Format: {@code aet@host:port;aet@host:port;...}
     *
     * <p>Example: {@code PACS1@192.168.1.100:104;PACS2@pacs.hospital.org:11112}
     *
     * @param configString the configuration string from application.properties
     * @return list of parsed AetEty objects (may be empty if parsing fails)
     */
    List<AetEty> parseAetConfiguration(String configString)
    {
        List<AetEty> aets = new ArrayList<>();

        if (configString == null || configString.trim().isEmpty())
        {
            return aets;
        }

        String[] entries = configString.split(";");

        for (String entry : entries)
        {
            String trimmedEntry = entry.trim();
            if (trimmedEntry.isEmpty())
            {
                continue;
            }

            try
            {
                AetEty aet = parseAetEntry(trimmedEntry);
                aets.add(aet);
            }
            catch (Exception e)
            {
                LOG.warn("Failed to parse AET entry [{}]: {}", trimmedEntry, e.getMessage());
            }
        }

        return aets;
    }

    /**
     * Parses a single AET entry in format: aet@host:port
     *
     * @param entry the entry string to parse
     * @return the parsed AetEty object
     * @throws IllegalArgumentException if the entry format is invalid
     */
    private AetEty parseAetEntry(String entry) throws IllegalArgumentException
    {
        // Expected format: aet@host:port
        int atIndex = entry.indexOf('@');
        if (atIndex <= 0)
        {
            throw new IllegalArgumentException("Invalid format: missing '@' separator or empty AET name");
        }

        String aet = entry.substring(0, atIndex).trim();
        String hostPort = entry.substring(atIndex + 1).trim();

        int colonIndex = hostPort.lastIndexOf(':');
        if (colonIndex <= 0)
        {
            throw new IllegalArgumentException("Invalid format: missing ':' separator or empty host");
        }

        String host = hostPort.substring(0, colonIndex).trim();
        String portStr = hostPort.substring(colonIndex + 1).trim();

        if (aet.isEmpty())
        {
            throw new IllegalArgumentException("AET name cannot be empty");
        }

        if (host.isEmpty())
        {
            throw new IllegalArgumentException("Host cannot be empty");
        }

        int port;
        try
        {
            port = Integer.parseInt(portStr);
        }
        catch (NumberFormatException e)
        {
            throw new IllegalArgumentException("Invalid port number: " + portStr);
        }

        return new AetEty(aet, host, port);
    }
}

