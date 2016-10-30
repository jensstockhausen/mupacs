package de.famst.dcm;

import org.dcm4che3.net.service.BasicCEchoSCP;
import org.dcm4che3.net.service.DicomServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * Created by jens on 30/10/2016.
 */
@Component
public class DcmServiceRegisty
{
    private static Logger LOG = LoggerFactory.getLogger(DcmServiceRegisty.class);

    private DicomServiceRegistry dicomServiceRegistry;

    private DcmStoreSCP dcmStoreSCP;

    @Inject
    public DcmServiceRegisty(DcmStoreSCP dcmStoreSCP)
    {
        LOG.info("initializing");

        this.dcmStoreSCP = dcmStoreSCP;

        dicomServiceRegistry = new DicomServiceRegistry();

        dcmStoreSCP.setDicomServiceRegistry(dicomServiceRegistry);

        dicomServiceRegistry.addDicomService(new BasicCEchoSCP());
        dicomServiceRegistry.addDicomService(dcmStoreSCP);
    }

    public void start()
    {
        LOG.info("starting DICOM services");
        dcmStoreSCP.start();
    }

    public void stop()
    {
        LOG.info("stopping DICOM services");
        dcmStoreSCP.stop();
    }





}

