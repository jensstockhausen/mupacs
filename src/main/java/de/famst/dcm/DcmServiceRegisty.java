package de.famst.dcm;

import org.dcm4che3.net.service.BasicCEchoSCP;
import org.dcm4che3.net.service.DicomServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * Created by jens on 30/10/2016.
 */
@Component
public class DcmServiceRegisty
{
    private static Logger LOG = LoggerFactory.getLogger(DcmServiceRegisty.class);

    private DicomServiceRegistry dicomServiceRegistry;

    private DcmStoreSCP dcmStoreSCP;
    private DcmFindSCP dcmFindSCP;


    @Autowired
    public DcmServiceRegisty(
            DcmStoreSCP dcmStoreSCP,
            DcmFindSCP dcmFindSCP)
    {
        LOG.info("initializing");

        this.dcmStoreSCP = dcmStoreSCP;
        this.dcmFindSCP = dcmFindSCP;

        dicomServiceRegistry = new DicomServiceRegistry();

        dicomServiceRegistry.addDicomService(new BasicCEchoSCP());

        dcmStoreSCP.setDicomServiceRegistry(dicomServiceRegistry);
        dicomServiceRegistry.addDicomService(dcmStoreSCP);

        dicomServiceRegistry.addDicomService(dcmFindSCP);
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

