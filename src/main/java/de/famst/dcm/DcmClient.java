package de.famst.dcm;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Association;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.DataWriterAdapter;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.DimseRSP;
import org.dcm4che3.net.IncompatibleConnectionException;
import org.dcm4che3.net.Priority;
import org.dcm4che3.net.Status;
import org.dcm4che3.net.pdu.AAssociateRQ;
import org.dcm4che3.net.pdu.PresentationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * DICOM Client (SCU - Service Class User) for sending DICOM operations to remote nodes.
 *
 * <p>Supports the following DICOM operations:
 * <ul>
 *   <li>C-ECHO - Verification service to test connectivity</li>
 *   <li>C-STORE - Storage service to send DICOM instances</li>
 * </ul>
 *
 * @author jens
 * @since 2024-11-23
 */
@Component
public class DcmClient
{
    private static final Logger LOG = LoggerFactory.getLogger(DcmClient.class);

    @Value("${mupacs.dicom.ae-title}")
    private String localAeTitle;

    @Value("${mupacs.dicom.scu.connect-timeout}")
    private int connectTimeout;

    @Value("${mupacs.dicom.scu.response-timeout}")
    private int responseTimeout;

    private Device device;
    private ApplicationEntity applicationEntity;
    private Connection connection;
    private boolean initialized = false;

    private String lastMessage = "";

    /**
     * Constructs a new DcmClient.
     * Device initialization is deferred until first use.
     */
    public DcmClient()
    {
        LOG.debug("DcmClient instance created");
    }

    /**
     * Initializes the DICOM client device, application entity, and connection.
     * This method should be called before performing any DICOM operations.
     * Multiple calls to this method are safe - subsequent calls will be ignored.
     *
     * @throws IllegalStateException if initialization fails
     */
    public synchronized void initialize()
    {
        if (initialized)
        {
            LOG.debug("DICOM Client already initialized, skipping");
            return;
        }

        LOG.info("Initializing DICOM Client (SCU)");

        try
        {
            // Create device
            device = new Device("MUPACS-CLIENT");

            // Create application entity
            applicationEntity = new ApplicationEntity(localAeTitle);
            device.addApplicationEntity(applicationEntity);

            // Create connection
            connection = new Connection();
            device.addConnection(connection);
            applicationEntity.addConnection(connection);

            // Set executors
            ExecutorService executorService = Executors.newCachedThreadPool();
            ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
            device.setExecutor(executorService);
            device.setScheduledExecutor(scheduledExecutorService);

            initialized = true;
            LOG.info("DICOM Client initialized successfully with AE Title: {}", localAeTitle);
        }
        catch (Exception e)
        {
            LOG.error("Failed to initialize DICOM Client: {}", e.getMessage(), e);
            throw new IllegalStateException("Failed to initialize DICOM Client", e);
        }
    }

    /**
     * Checks if the client is initialized and initializes it if needed.
     *
     * @throws IllegalStateException if initialization fails
     */
    private void ensureInitialized()
    {
        if (!initialized)
        {
            initialize();
        }
    }

    public String getLastMessage()
    {
        return lastMessage;
    }

    /**
     * Performs a C-ECHO operation to verify connectivity with a remote DICOM node.
     *
     * @param remoteAeTitle the Application Entity Title of the remote node
     * @param remoteHost the hostname or IP address of the remote node
     * @param remotePort the port number of the remote node
     * @return true if echo was successful, false otherwise
     */
    public boolean echo(String remoteAeTitle, String remoteHost, int remotePort)
    {
        ensureInitialized();

        LOG.info("Sending C-ECHO to {}@{}:{}", remoteAeTitle, remoteHost, remotePort);

        Association association = null;
        try
        {
            association = connect(remoteAeTitle, remoteHost, remotePort, UID.Verification);

            // Send C-ECHO request
            DimseRSP response = association.cecho();
            response.next();

            int status = response.getCommand().getInt(Tag.Status, -1);
            boolean success = status == Status.Success;

            if (success)
            {
                lastMessage = "C-ECHO successful to " + remoteAeTitle + "@" + remoteHost + ":" + remotePort;
                LOG.info(lastMessage);
            }
            else
            {
                lastMessage = "C-ECHO failed with status: 0x" + Integer.toHexString(status) + " to " +
                    remoteAeTitle + "@" + remoteHost + ":" + remotePort;
                LOG.warn(lastMessage);
            }

            return success;
        }
        catch (Exception e)
        {
            lastMessage = "C-ECHO failed to " + remoteAeTitle + "@" + remoteHost + ":" + remotePort +
                ": " + e.getMessage();
            LOG.error(lastMessage);

            return false;
        }
        finally
        {
            if (association != null)
            {
                try
                {
                    association.release();
                }
                catch (IOException e)
                {
                    LOG.warn("Error releasing association: {}", e.getMessage());
                }
            }
        }
    }

    /**
     * Performs a C-STORE operation to send a DICOM instance to a remote node.
     *
     * @param remoteAeTitle the Application Entity Title of the remote node
     * @param remoteHost the hostname or IP address of the remote node
     * @param remotePort the port number of the remote node
     * @param dicomFile the DICOM file to send
     * @return true if store was successful, false otherwise
     */
    public boolean store(String remoteAeTitle, String remoteHost, int remotePort, File dicomFile)
    {
        ensureInitialized();

        LOG.info("Sending C-STORE to {}@{}:{} for file: {}",
            remoteAeTitle, remoteHost, remotePort, dicomFile.getName());

        if (!dicomFile.exists() || !dicomFile.isFile())
        {
            lastMessage = "DICOM file does not exist or is not a file: " + dicomFile.getAbsolutePath();
            LOG.error(lastMessage);
            return false;
        }

        Association association = null;
        try
        {
            // Read DICOM file to get SOP Class UID
            Attributes fileMetaInfo;
            Attributes dataset;
            try (DicomInputStream dis = new DicomInputStream(dicomFile))
            {
                fileMetaInfo = dis.readFileMetaInformation();
                dataset = dis.readDataset();
            }

            String sopClassUID = fileMetaInfo.getString(Tag.MediaStorageSOPClassUID);
            String sopInstanceUID = fileMetaInfo.getString(Tag.MediaStorageSOPInstanceUID);
            String transferSyntaxUID = fileMetaInfo.getString(Tag.TransferSyntaxUID);

            if (sopClassUID == null || sopInstanceUID == null)
            {
                LOG.error("Missing SOP Class UID or SOP Instance UID in file: {}", dicomFile.getName());
                return false;
            }

            LOG.debug("File metadata - SOP Class: {}, SOP Instance: {}, Transfer Syntax: {}",
                sopClassUID, sopInstanceUID, transferSyntaxUID);

            // Connect with appropriate SOP Class
            association = connect(remoteAeTitle, remoteHost, remotePort, sopClassUID);

            // Send C-STORE request
            DimseRSP response = association.cstore(
                sopClassUID,
                sopInstanceUID,
                Priority.NORMAL,
                new DataWriterAdapter(dataset),
                transferSyntaxUID != null ? transferSyntaxUID : UID.ExplicitVRLittleEndian
            );

            response.next();

            int status = response.getCommand().getInt(Tag.Status, -1);
            boolean success = status == Status.Success;

            if (success)
            {
                LOG.info("C-STORE successful to {}@{}:{} for file: {}",
                    remoteAeTitle, remoteHost, remotePort, dicomFile.getName());
            }
            else
            {
                LOG.warn("C-STORE failed with status: 0x{} to {}@{}:{} for file: {}",
                    Integer.toHexString(status), remoteAeTitle, remoteHost, remotePort, dicomFile.getName());
            }

            return success;
        }
        catch (Exception e)
        {
            LOG.error("C-STORE failed to {}@{}:{} for file {}: {}",
                remoteAeTitle, remoteHost, remotePort, dicomFile.getName(), e.getMessage(), e);
            return false;
        }
        finally
        {
            if (association != null)
            {
                try
                {
                    association.release();
                }
                catch (IOException e)
                {
                    LOG.warn("Error releasing association: {}", e.getMessage());
                }
            }
        }
    }

    /**
     * Stores all DICOM files in a directory to a remote node.
     *
     * @param remoteAeTitle the Application Entity Title of the remote node
     * @param remoteHost the hostname or IP address of the remote node
     * @param remotePort the port number of the remote node
     * @param directory the directory containing DICOM files
     * @return the number of files successfully stored
     */
    public int storeDirectory(String remoteAeTitle, String remoteHost, int remotePort, File directory)
    {
        ensureInitialized();

        LOG.info("Sending C-STORE for directory to {}@{}:{}: {}",
            remoteAeTitle, remoteHost, remotePort, directory.getAbsolutePath());

        if (!directory.exists() || !directory.isDirectory())
        {
            LOG.error("Directory does not exist or is not a directory: {}", directory.getAbsolutePath());
            return 0;
        }

        int successCount = 0;
        int failureCount = 0;

        File[] files = directory.listFiles((dir, name) -> name.toLowerCase().endsWith(".dcm"));
        if (files == null || files.length == 0)
        {
            LOG.warn("No DICOM files found in directory: {}", directory.getAbsolutePath());
            return 0;
        }

        LOG.info("Found {} DICOM files to send", files.length);

        for (File file : files)
        {
            if (store(remoteAeTitle, remoteHost, remotePort, file))
            {
                successCount++;
            }
            else
            {
                failureCount++;
            }
        }

        LOG.info("C-STORE directory complete: {} successful, {} failed", successCount, failureCount);
        return successCount;
    }

    /**
     * Establishes a connection and association with a remote DICOM node.
     *
     * @param remoteAeTitle the Application Entity Title of the remote node
     * @param remoteHost the hostname or IP address
     * @param remotePort the port number
     * @param sopClassUID the SOP Class UID for presentation context
     * @return the established Association
     * @throws IOException if connection fails
     * @throws InterruptedException if connection is interrupted
     * @throws IncompatibleConnectionException if connection parameters are incompatible
     * @throws GeneralSecurityException if security setup fails
     */
    private Association connect(String remoteAeTitle, String remoteHost, int remotePort, String sopClassUID)
        throws IOException, InterruptedException, IncompatibleConnectionException, GeneralSecurityException
    {
        LOG.debug("Connecting to {}@{}:{}", remoteAeTitle, remoteHost, remotePort);

        // Create remote connection
        Connection remoteConnection = new Connection();
        remoteConnection.setHostname(remoteHost);
        remoteConnection.setPort(remotePort);
        remoteConnection.setConnectTimeout(connectTimeout);

        // Create association request
        AAssociateRQ associationRequest = new AAssociateRQ();
        associationRequest.setCalledAET(remoteAeTitle);
        associationRequest.setCallingAET(localAeTitle);

        // Add presentation context
        associationRequest.addPresentationContext(
            new PresentationContext(
                1,
                sopClassUID,
                UID.ExplicitVRLittleEndian,
                UID.ImplicitVRLittleEndian
            )
        );

        // Open association
        Association association = applicationEntity.connect(remoteConnection, associationRequest);

        LOG.debug("Association established with {}@{}:{}", remoteAeTitle, remoteHost, remotePort);
        return association;
    }

    /**
     * Shuts down the DICOM client and releases resources.
     * After shutdown, the client can be reinitialized by calling initialize().
     */
    public synchronized void shutdown()
    {
        if (!initialized)
        {
            LOG.debug("DICOM Client not initialized, nothing to shutdown");
            return;
        }

        LOG.info("Shutting down DICOM Client");
        try
        {
            if (device != null && device.getExecutor() instanceof ExecutorService)
            {
                ((ExecutorService) device.getExecutor()).shutdown();
            }
            if (device != null && device.getScheduledExecutor() != null)
            {
                device.getScheduledExecutor().shutdown();
            }

            initialized = false;
            LOG.info("DICOM Client shutdown complete");
        }
        catch (Exception e)
        {
            LOG.error("Error during DICOM Client shutdown: {}", e.getMessage(), e);
        }
    }
}

