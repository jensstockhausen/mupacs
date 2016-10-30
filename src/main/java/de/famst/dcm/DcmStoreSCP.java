package de.famst.dcm;

import de.famst.service.FolderImportManager;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.io.DicomOutputStream;
import org.dcm4che3.net.*;
import org.dcm4che3.net.pdu.PresentationContext;
import org.dcm4che3.net.service.BasicCStoreSCP;
import org.dcm4che3.net.service.DicomServiceException;
import org.dcm4che3.net.service.DicomServiceRegistry;
import org.dcm4che3.util.SafeClose;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Created by jens on 13/10/2016.
 */
@Component
public class DcmStoreSCP extends BasicCStoreSCP
{
    private static Logger LOG = LoggerFactory.getLogger(DcmStoreSCP.class);

    private static final String PART_EXT = ".part";

    @Value("${mupacs.cstore.scp.import}")
    private String importFolder;

    @Value("${mupcas.cstore.scp.port}")
    private Integer port;

    @Value("${mupcas.cstore.scp.host}")
    private String host;

    private Device device;
    private Connection connection;
    private ApplicationEntity ae;

    private FolderImportManager importManager;

    private DicomServiceRegistry dicomServiceRegistry;

    @Inject
    public DcmStoreSCP(FolderImportManager importManager)
    {
        this.importManager = importManager;
    }

    public void setDicomServiceRegistry(DicomServiceRegistry dicomServiceRegistry)
    {
        this.dicomServiceRegistry = dicomServiceRegistry;
    }

    public void start()
    {
        LOG.info("starting C-STORE SCP device");

        device = new Device();

        ae = new ApplicationEntity();
        ae.setAETitle("MUPACS");
        ae.setAcceptedCallingAETitles();

        TransferCapability tc = new TransferCapability(
                null, "*", TransferCapability.Role.SCP, "*");

        ae.addTransferCapability(tc);

        device.addApplicationEntity(ae);

        connection = new Connection();
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
            LOG.info("bound to [{}:{}]", connection.getHostname(), connection.getPort());
        }
        catch (IOException | GeneralSecurityException e)
        {
            LOG.error("[{}]", e);
        }
    }


    public void stop()
    {
        LOG.info("stopping C-STORE SCP device");
        device.unbindConnections();
    }


    @Override
    protected void store(Association as, PresentationContext pc, Attributes rq, PDVInputStream data, Attributes rsp) throws IOException
    {
        LOG.info("C-STORE [{}]", as);

        String cuid = rq.getString(Tag.AffectedSOPClassUID);
        String iuid = rq.getString(Tag.AffectedSOPInstanceUID);
        String tsuid = pc.getTransferSyntax();

        File file = new File(importFolder, iuid + PART_EXT);

        try
        {
            storeTo(as, as.createFileMetaInformation(iuid, cuid, tsuid), data, file);

            Attributes part = DcmFile.readContent(file);

            String path = part.getString(Tag.PatientID);
            path += "/" + part.getString(Tag.StudyInstanceUID);
            path += "/" + part.getString(Tag.SeriesInstanceUID);
            path += "/";

            renameTo(as, file, new File(importFolder, path + iuid));
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
        LOG.info("[{}] writing to [{}]", as, file);

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
        LOG.info("[{}] renaming [{}] to [{}]", as, from, dest);

        if (!dest.getParentFile().mkdirs() && !dest.delete())
        {
            LOG.error("creating/deleting [{}]", dest.getAbsolutePath());
        }

        if (!from.renameTo(dest))
        {
            throw new IOException("Failed to rename " + from + " to " + dest);
        }

        try
        {
            importManager.addImport(dest.toPath());
        }
        catch (InterruptedException e)
        {
            LOG.warn("[{}]", e);
            Thread.currentThread().interrupt();
        }
    }


    private void deleteFile(Association as, File file)
    {
        if (file.delete())
        {
            LOG.info("[{}] deleting [{}]", as, file);
        } else
        {
            LOG.warn("[{}] deleting [{}] failed!", as, file);
        }
    }


}
