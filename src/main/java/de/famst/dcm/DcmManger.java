package de.famst.dcm;

import org.dcm4che3.conf.core.api.ConfigurationException;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * Created by jens on 13/10/2016.
 */
@Component
public class DcmManger implements ApplicationListener<ApplicationReadyEvent>
{
    private DcmStoreSCP dcmStoreSCP;

    @Override
    public void onApplicationEvent(final ApplicationReadyEvent event)
    {
        try
        {
            dcmStoreSCP = new DcmStoreSCP();
        }
        catch (ConfigurationException e)
        {
            e.printStackTrace();
        }
    }


}
