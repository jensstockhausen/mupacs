package de.famst.dcm;

import de.famst.service.DicomImportService;
import jakarta.inject.Inject;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.io.DicomOutputStream;
import org.dcm4che3.net.Association;
import org.dcm4che3.net.PDVInputStream;
import org.dcm4che3.net.Status;
import org.dcm4che3.net.pdu.PresentationContext;
import org.dcm4che3.net.service.BasicCStoreSCP;
import org.dcm4che3.net.service.DicomServiceException;
import org.dcm4che3.util.SafeClose;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

/**
 * Created by jens on 13/10/2016.
 */
@Component
public class DcmStoreSCP extends BasicCStoreSCP
{
    private static final Logger LOG = LoggerFactory.getLogger(DcmStoreSCP.class);

    private static final String PART_EXT = ".part";

    private final DicomImportService dicomImportService;

    @Value("${mupacs.cstore.scp.import}")
    private String importFolder;

    @Inject
    public DcmStoreSCP(DicomImportService dicomImportService)
    {
        super("*"); // Accept all SOP Classes
        this.dicomImportService = dicomImportService;
    }


    @Override
    protected void store(Association as, PresentationContext pc,
                         Attributes rq, PDVInputStream data,
                         Attributes rsp) throws IOException
    {
        LOG.info("Received C-STORE request from [{}]", as);

        String cuid = rq.getString(Tag.AffectedSOPClassUID);
        String iuid = rq.getString(Tag.AffectedSOPInstanceUID);
        String tsuid = pc.getTransferSyntax();

        if (iuid == null || iuid.isEmpty())
        {
            LOG.error("Invalid SOP Instance UID received");
            throw new DicomServiceException(Status.ProcessingFailure, "Invalid SOP Instance UID");
        }

        File file = new File(importFolder, iuid + PART_EXT);

        try
        {
            storeTo(as, as.createFileMetaInformation(iuid, cuid, tsuid), data, file);
            LOG.info("Successfully stored DICOM instance [{}] to [{}]", iuid, file.getAbsolutePath());
        }
        catch (Exception e)
        {
            LOG.error("Error storing DICOM instance [{}]: {}", iuid, e.getMessage(), e);
            deleteFile(as, file);
            throw new DicomServiceException(Status.ProcessingFailure, e);
        }

        try
        {
            dicomImportService.dicomToDatabase(file);
        }
        catch (Exception e)
        {
            LOG.error("Unexpected error during import of [{}]: {}", iuid, e.getMessage(), e);
            LOG.error("Cannot Import DICOM file [{}]", file.getAbsolutePath());
            throw new DicomServiceException(Status.ProcessingFailure, e);
        }


        if (!deleteFile(as, file))
        {
            LOG.warn("Failed to delete temporary file [{}] after import", file.getAbsolutePath());
        }
    }


    private void storeTo(Association as, Attributes fmi,
                         PDVInputStream data, File file) throws IOException
    {
        LOG.debug("[{}] Writing DICOM data to [{}]", as, file.getAbsolutePath());

        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists())
        {
            if (!parentDir.mkdirs())
            {
                LOG.error("Failed to create directory: {}", parentDir.getAbsolutePath());
                throw new IOException("Failed to create directory: " + parentDir.getAbsolutePath());
            }
        }

        DicomOutputStream out = null;
        try
        {
            out = new DicomOutputStream(file);
            out.writeFileMetaInformation(fmi);
            data.copyTo(out);
            LOG.debug("[{}] Successfully wrote data to [{}]", as, file.getAbsolutePath());
        }
        finally
        {
            SafeClose.close(out);
        }
    }


    private Boolean deleteFile(Association as, File file)
    {
        if (file != null && file.exists())
        {
            if (file.delete())
            {
                LOG.info("[{}] Successfully deleted file [{}]", as, file.getAbsolutePath());
                return true;
            }
            else
            {
                LOG.warn("[{}] Failed to delete file [{}]", as, file.getAbsolutePath());
            }
        }
        else
        {
            LOG.debug("[{}] File does not exist or is null: [{}]", as, file);
        }

        return false;
    }


}
