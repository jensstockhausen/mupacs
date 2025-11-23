package de.famst.dcm;

import org.dcm4che3.net.ApplicationEntity;
import org.dcm4che3.net.Connection;
import org.dcm4che3.net.Device;
import org.dcm4che3.net.TransferCapability;
import org.dcm4che3.net.service.DicomServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;


/**
 * Created by jens on 30/10/2016.
 */
@Component
public class DcmServiceRegisty
{
    private static final Logger LOG = LoggerFactory.getLogger(DcmServiceRegisty.class);

    private DicomServiceRegistry dicomServiceRegistry;

    private DcmStoreSCP dcmStoreSCP;
    private DcmFindSCP dcmFindSCP;
    private DcmEchoSCP dcmEchoSCP;

    @Value("${mupcas.dicom.scp.port}")
    private Integer port;

    @Value("${mupcas.dicom.scp.host}")
    private String host;

    @Value("${mupcas.dicom.scp.ae-title}")
    private String aeTitle;

    private Device device;

    @Autowired
    public DcmServiceRegisty(
        DcmStoreSCP dcmStoreSCP,
        DcmFindSCP dcmFindSCP,
        DcmEchoSCP dcmEchoSCP)
    {
        LOG.info("initializing");

        this.dcmStoreSCP = dcmStoreSCP;
        this.dcmFindSCP = dcmFindSCP;
        this.dcmEchoSCP = dcmEchoSCP;

        dicomServiceRegistry = new DicomServiceRegistry();

        dicomServiceRegistry.addDicomService(this.dcmEchoSCP);
        dicomServiceRegistry.addDicomService(this.dcmFindSCP);
        dicomServiceRegistry.addDicomService(this.dcmStoreSCP);
    }

    public void start()
    {
        LOG.info("starting DICOM services");

        device = new Device("MUPACS-DEVICE");

        ApplicationEntity ae = new ApplicationEntity(aeTitle);
        ae.setAcceptedCallingAETitles();

        TransferCapability tc = new TransferCapability(
            null, "*", TransferCapability.Role.SCP, "*");

        ae.addTransferCapability(tc);

        device.addApplicationEntity(ae);

        Connection connection = new Connection();
        connection.setPort(port);
        connection.setHostname(host);

        device.addConnection(connection);
        ae.addConnection(connection);
        device.setDimseRQHandler(dicomServiceRegistry);
        ExecutorService executorService = Executors.newCachedThreadPool();
        ScheduledExecutorService scheduledExecutorService =
            Executors.newSingleThreadScheduledExecutor();

        device.setScheduledExecutor(scheduledExecutorService);
        device.setExecutor(executorService);

        try
        {
            device.bindConnections();
            LOG.info("successfully bound to [{}:{}]", connection.getHostname(), connection.getPort());
        }
        catch (IOException e)
        {
            LOG.error("Failed to bind to [{}:{}]: {}", connection.getHostname(), connection.getPort(), e.getMessage(), e);
        }
        catch (GeneralSecurityException e)
        {
            LOG.error("Security error while binding: {}", e.getMessage(), e);
        }

    }

    public void stop()
    {
        LOG.info("stopping DICOM services");

        if (device != null)
        {
            try
            {
                device.unbindConnections();
                LOG.info("device stopped successfully");
            }
            catch (Exception e)
            {
                LOG.error("Error stopping device: {}", e.getMessage(), e);
            }
        }
        else
        {
            LOG.warn("Device is null, cannot stop");
        }
    }

    // Configuration getters for status page

    public String getAeTitle()
    {
        return aeTitle;
    }

    public String getHost()
    {
        return host;
    }

    public Integer getPort()
    {
        return port;
    }

    public boolean isRunning()
    {
        return device != null;
    }

    public String getDeviceName()
    {
        return device != null ? device.getDeviceName() : "N/A";
    }

    public int getRegisteredServicesCount()
    {
        return dicomServiceRegistry != null ? 3 : 0; // Echo, Find, Store
    }
}
