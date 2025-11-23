package de.famst.service;

import de.famst.data.AetEty;
import de.famst.data.AetRepository;
import de.famst.dcm.DcmClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Optional;

/**
 * Service for performing DICOM client operations using configured AETs.
 *
 * <p>This service acts as a bridge between the DcmClient and the AET repository,
 * allowing operations to be performed using AET names from the database.
 *
 * @author jens
 * @since 2024-11-23
 */
@Service
public class DcmClientService
{
    private static final Logger LOG = LoggerFactory.getLogger(DcmClientService.class);

    private final DcmClient dcmClient;
    private final AetRepository aetRepository;

    public DcmClientService(DcmClient dcmClient, AetRepository aetRepository)
    {
        this.dcmClient = dcmClient;
        this.aetRepository = aetRepository;
    }

    public String getLastMessage()
    {
        return dcmClient.getLastMessage();
    }

    /**
     * Performs a C-ECHO operation to a remote AET by name.
     *
     * @param aetName the name of the AET from the database
     * @return true if echo was successful, false otherwise
     */
    public boolean echo(String aetName)
    {
        LOG.info("Performing C-ECHO to AET: {}", aetName);

        Optional<AetEty> aet = aetRepository.findByAet(aetName);
        if (aet.isEmpty())
        {
            LOG.error("AET not found: {}", aetName);
            return false;
        }

        AetEty remote = aet.get();
        return dcmClient.echo(remote.getAet(), remote.getHost(), remote.getPort());
    }

    /**
     * Performs a C-ECHO operation to a remote AET by ID.
     *
     * @param aetId the ID of the AET from the database
     * @return true if echo was successful, false otherwise
     */
    public boolean echoById(Long aetId)
    {
        LOG.info("Performing C-ECHO to AET ID: {}", aetId);

        Optional<AetEty> aet = aetRepository.findById(aetId);
        if (aet.isEmpty())
        {
            LOG.error("AET not found with ID: {}", aetId);
            return false;
        }

        AetEty remote = aet.get();
        return dcmClient.echo(remote.getAet(), remote.getHost(), remote.getPort());
    }

    /**
     * Performs a C-STORE operation to send a DICOM file to a remote AET by name.
     *
     * @param aetName the name of the AET from the database
     * @param dicomFile the DICOM file to send
     * @return true if store was successful, false otherwise
     */
    public boolean store(String aetName, File dicomFile)
    {
        LOG.info("Performing C-STORE to AET: {} for file: {}", aetName, dicomFile.getName());

        Optional<AetEty> aet = aetRepository.findByAet(aetName);
        if (aet.isEmpty())
        {
            LOG.error("AET not found: {}", aetName);
            return false;
        }

        AetEty remote = aet.get();
        return dcmClient.store(remote.getAet(), remote.getHost(), remote.getPort(), dicomFile);
    }

    /**
     * Performs a C-STORE operation to send a DICOM file to a remote AET by ID.
     *
     * @param aetId the ID of the AET from the database
     * @param dicomFile the DICOM file to send
     * @return true if store was successful, false otherwise
     */
    public boolean storeById(Long aetId, File dicomFile)
    {
        LOG.info("Performing C-STORE to AET ID: {} for file: {}", aetId, dicomFile.getName());

        Optional<AetEty> aet = aetRepository.findById(aetId);
        if (aet.isEmpty())
        {
            LOG.error("AET not found with ID: {}", aetId);
            return false;
        }

        AetEty remote = aet.get();
        return dcmClient.store(remote.getAet(), remote.getHost(), remote.getPort(), dicomFile);
    }

    /**
     * Performs C-STORE operations to send all DICOM files in a directory to a remote AET by name.
     *
     * @param aetName the name of the AET from the database
     * @param directory the directory containing DICOM files
     * @return the number of files successfully stored
     */
    public int storeDirectory(String aetName, File directory)
    {
        LOG.info("Performing C-STORE directory to AET: {} for directory: {}",
            aetName, directory.getAbsolutePath());

        Optional<AetEty> aet = aetRepository.findByAet(aetName);
        if (aet.isEmpty())
        {
            LOG.error("AET not found: {}", aetName);
            return 0;
        }

        AetEty remote = aet.get();
        return dcmClient.storeDirectory(remote.getAet(), remote.getHost(), remote.getPort(), directory);
    }

    /**
     * Performs C-STORE operations to send all DICOM files in a directory to a remote AET by ID.
     *
     * @param aetId the ID of the AET from the database
     * @param directory the directory containing DICOM files
     * @return the number of files successfully stored
     */
    public int storeDirectoryById(Long aetId, File directory)
    {
        LOG.info("Performing C-STORE directory to AET ID: {} for directory: {}",
            aetId, directory.getAbsolutePath());

        Optional<AetEty> aet = aetRepository.findById(aetId);
        if (aet.isEmpty())
        {
            LOG.error("AET not found with ID: {}", aetId);
            return 0;
        }

        AetEty remote = aet.get();
        return dcmClient.storeDirectory(remote.getAet(), remote.getHost(), remote.getPort(), directory);
    }

    /**
     * Performs a direct C-ECHO without using the AET repository.
     *
     * @param remoteAeTitle the AE Title of the remote node
     * @param remoteHost the hostname or IP address
     * @param remotePort the port number
     * @return true if echo was successful, false otherwise
     */
    public boolean echoDirect(String remoteAeTitle, String remoteHost, int remotePort)
    {
        return dcmClient.echo(remoteAeTitle, remoteHost, remotePort);
    }

    /**
     * Performs a direct C-STORE without using the AET repository.
     *
     * @param remoteAeTitle the AE Title of the remote node
     * @param remoteHost the hostname or IP address
     * @param remotePort the port number
     * @param dicomFile the DICOM file to send
     * @return true if store was successful, false otherwise
     */
    public boolean storeDirect(String remoteAeTitle, String remoteHost, int remotePort, File dicomFile)
    {
        return dcmClient.store(remoteAeTitle, remoteHost, remotePort, dicomFile);
    }
}

