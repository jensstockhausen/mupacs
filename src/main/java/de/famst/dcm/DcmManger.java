package de.famst.dcm;

import org.dcm4che3.net.service.AbstractDicomService;
import org.dcm4che3.net.service.BasicCEchoSCP;
import org.dcm4che3.net.service.DicomServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jens on 13/10/2016.
 */
@Component
public class DcmManger implements ApplicationListener<ApplicationReadyEvent>
{
    private static Logger LOG = LoggerFactory.getLogger(DcmManger.class);

    @Inject
    private DcmServiceFactory dcmServiceFactory;

    private DicomServiceRegistry dicomServiceRegistry;
    private List<AbstractDicomService> services;

    public DcmManger()
    {
        dicomServiceRegistry = new DicomServiceRegistry();
        services = new ArrayList<>();
    }

    @Override
    public void onApplicationEvent(final ApplicationReadyEvent event)
    {
        startServices();
    }

    public void startServices()
    {
        LOG.info("starting DCM services");

        services.add(new BasicCEchoSCP());

        services.add(dcmServiceFactory.createDcmStoreSCPDevice(dicomServiceRegistry));

        for (AbstractDicomService service: services)
        {
            dicomServiceRegistry.addDicomService(service);
        }
    }

    public void stopServices()
    {
        LOG.info("stopping DCM services");

        List<AbstractDicomService> toDelete = new ArrayList<>();

        for (AbstractDicomService service: services)
        {
            dicomServiceRegistry.removeDicomService(service);
            toDelete.add(service);
        }

        services.removeAll(toDelete);
    }


}
