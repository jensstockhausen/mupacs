package de.famst.dcm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * Created by jens on 13/10/2016.
 */
@Component
public class DcmManger implements ApplicationListener<ApplicationReadyEvent>
{
    private static Logger LOG = LoggerFactory.getLogger(DcmManger.class);

    private DcmServiceRegisty dcmServiceRegisty;

    @Inject
    public DcmManger(DcmServiceRegisty dcmServiceRegisty)
    {
        LOG.info("initialize DICOM services");
        this.dcmServiceRegisty = dcmServiceRegisty;
    }

    @Override
    public void onApplicationEvent(final ApplicationReadyEvent event)
    {
        startServices();
    }

    public void startServices()
    {
        LOG.info("starting DICOM services");
        dcmServiceRegisty.start();
    }

    public void stopServices()
    {
        LOG.info("stopping DICOM services");
        dcmServiceRegisty.stop();
    }


}
