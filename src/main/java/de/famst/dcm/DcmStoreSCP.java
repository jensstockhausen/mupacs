package de.famst.dcm;

import org.dcm4che3.conf.api.extensions.CommonAEExtension;
import org.dcm4che3.conf.api.extensions.CommonDeviceExtension;
import org.dcm4che3.conf.core.api.Configuration;
import org.dcm4che3.conf.core.api.ConfigurationException;
import org.dcm4che3.conf.core.api.internal.BeanVitalizer;
import org.dcm4che3.conf.core.api.internal.ConfigurationManager;
import org.dcm4che3.conf.dicom.CommonDicomConfigurationWithHL7;
import org.dcm4che3.conf.dicom.DicomConfigurationBuilder;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.io.DicomOutputStream;
import org.dcm4che3.net.*;
import org.dcm4che3.net.pdu.PresentationContext;
import org.dcm4che3.net.service.BasicCEchoSCP;
import org.dcm4che3.net.service.BasicCStoreSCP;
import org.dcm4che3.net.service.DicomServiceException;
import org.dcm4che3.net.service.DicomServiceRegistry;
import org.dcm4che3.util.SafeClose;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Created by jens on 13/10/2016.
 */
public class DcmStoreSCP extends BasicCStoreSCP
{
    private static Logger LOG = LoggerFactory.getLogger(DcmStoreSCP.class);

    private static final String PART_EXT = ".part";

    private Device device;
    private Connection connection;
    private ApplicationEntity ae;
    private DicomServiceRegistry dicomServiceRegistry;

    public DcmStoreSCP() throws ConfigurationException
    {
        if (System.getProperty("org.dcm4che.conf.filename") == null)
        {
            System.setProperty("org.dcm4che.conf.filename",
                    "src/main/resources/dcmconfig.json");
        }

        DicomConfigurationBuilder builder
                = DicomConfigurationBuilder.newConfigurationBuilder(System.getProperties());


        builder.registerDeviceExtension(CommonDeviceExtension.class);
        builder.registerAEExtension(CommonAEExtension.class);

        CommonDicomConfigurationWithHL7 dicomConfigurationWithHL7 = builder.build();


        device = new Device("store-scp");

        dicomServiceRegistry = new DicomServiceRegistry();
        dicomServiceRegistry.addDicomService(new BasicCEchoSCP());
        dicomServiceRegistry.addDicomService(this);

        device.setDimseRQHandler(dicomServiceRegistry);

        connection = new Connection();
        connection.setHostname("127.0.0.1");
        connection.setPort(8104);

        device.addConnection(connection);

        ae = new ApplicationEntity("*");
        ae.setAssociationAcceptor(true);
        ae.addConnection(connection);
        ae.setAETitle("MUPACS");

        TransferCapability tc = new TransferCapability(
                null, "*", TransferCapability.Role.SCP, "*");

        ae.addTransferCapability(tc);

        device.addApplicationEntity(ae);

        ExecutorService executorService = Executors.newCachedThreadPool();
        ScheduledExecutorService scheduledExecutorService =
                Executors.newSingleThreadScheduledExecutor();

        device.setScheduledExecutor(scheduledExecutorService);
        device.setExecutor(executorService);

        try
        {
            device.bindConnections();
        }
        catch (IOException | GeneralSecurityException e)
        {
            LOG.error("[{}]", e);
        }

    }

    @Override
    protected void store(Association as, PresentationContext pc, Attributes rq, PDVInputStream data, Attributes rsp) throws IOException
    {
        //rsp.setInt(Tag.Status, VR.US, status);

        String cuid = rq.getString(Tag.AffectedSOPClassUID);
        String iuid = rq.getString(Tag.AffectedSOPInstanceUID);
        String tsuid = pc.getTransferSyntax();

        String storageDir = "./build/import";

        File file = new File(storageDir, iuid + PART_EXT);

        try
        {
            storeTo(as,
                    as.createFileMetaInformation(iuid, cuid, tsuid),
                    data,
                    file);

            renameTo(as,
                    file,
                    new File(storageDir, iuid));
        }
        catch (Exception e)
        {
            deleteFile(as, file);
            throw new DicomServiceException(Status.ProcessingFailure, e);
        }
    }


    private void storeTo(Association as, Attributes fmi,
                         PDVInputStream data, File file) throws IOException
    {
        LOG.info("[{}]: M-WRITE [{}]", as, file);
        file.getParentFile().mkdirs();

        DicomOutputStream out = new DicomOutputStream(file);
        try
        {
            out.writeFileMetaInformation(fmi);
            data.copyTo(out);
        }
        finally
        {
            SafeClose.close(out);
        }
    }

    private void renameTo(Association as, File from, File dest)
            throws IOException
    {
        LOG.info("[{}]: M-RENAME [{}] to [{}]", as, from, dest);
        if (!dest.getParentFile().mkdirs())
            dest.delete();
        if (!from.renameTo(dest))
            throw new IOException("Failed to rename " + from + " to "+ dest);
    }


    private void deleteFile(Association as, File file)
    {
        if (file.delete())
            LOG.info("[{}]: M-DELETE [{}]", as, file);
        else
            LOG.warn("[{}]: M-DELETE [{}] failed!", as, file);
    }

}
