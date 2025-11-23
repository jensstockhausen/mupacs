package de.famst.dcm;

import org.dcm4che3.net.Association;
import org.dcm4che3.net.pdu.PresentationContext;
import org.dcm4che3.net.service.BasicCEchoSCP;
import org.dcm4che3.net.service.DicomServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Custom C-ECHO SCP (Service Class Provider) implementation for DICOM verification.
 *
 * <p>The C-ECHO service is used to verify end-to-end communications with another
 * DICOM Application Entity (AE). This is essentially a "ping" operation in DICOM.
 *
 * @author jens
 * @since 2024-11-23
 */
@Component
public class DcmEchoSCP extends BasicCEchoSCP
{
    private static final Logger LOG = LoggerFactory.getLogger(DcmEchoSCP.class);

    /**
     * Default constructor.
     */
    public DcmEchoSCP()
    {
        super();
        LOG.info("DcmEchoSCP initialized");
    }

    @Override
    public void onDimseRQ(Association as, PresentationContext pc,
                          org.dcm4che3.net.Dimse dimse,
                          org.dcm4che3.data.Attributes cmd,
                          org.dcm4che3.net.PDVInputStream data)
        throws java.io.IOException
    {
        LOG.info("Received C-ECHO request from [{}]", as.getRemoteAET());

        try
        {
            super.onDimseRQ(as, pc, dimse, cmd, data);
            LOG.info("C-ECHO successful for [{}]", as.getRemoteAET());
        }
        catch (DicomServiceException e)
        {
            LOG.error("C-ECHO failed for [{}]: {}", as.getRemoteAET(), e.getMessage(), e);
            throw e;
        }
        catch (Exception e)
        {
            LOG.error("Unexpected error during C-ECHO from [{}]: {}", as.getRemoteAET(), e.getMessage(), e);
            throw e;
        }
    }
}

