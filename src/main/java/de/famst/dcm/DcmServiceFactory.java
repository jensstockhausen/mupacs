package de.famst.dcm;

import org.dcm4che3.net.service.DicomServiceRegistry;
import org.springframework.stereotype.Component;

/**
 * Created by jens on 23/10/2016.
 */
@Component
public class DcmServiceFactory
{
    public DcmStoreSCP createDcmStoreSCPDevice(DicomServiceRegistry dicomServiceRegistry)
    {
        return new DcmStoreSCP(dicomServiceRegistry);
    }

}
