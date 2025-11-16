package de.famst.dcm;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.net.Association;
import org.dcm4che3.net.pdu.PresentationContext;
import org.dcm4che3.net.service.BasicCFindSCP;
import org.dcm4che3.net.service.DicomServiceException;
import org.dcm4che3.net.service.QueryTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Created by jens on 30/10/2016.
 */
@Component
public class DcmFindSCP extends BasicCFindSCP implements ApplicationContextAware
{
    private static Logger LOG = LoggerFactory.getLogger(DcmFindSCP.class);

    private ApplicationContext applicationContext;


    public DcmFindSCP()
    {
        super(UID.PatientRootQueryRetrieveInformationModelFind,
                UID.StudyRootQueryRetrieveInformationModelFind);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext)
    {
        this.applicationContext = applicationContext;
    }

    @Override
    protected QueryTask calculateMatches(Association as, PresentationContext pc, Attributes rq, Attributes keys) throws DicomServiceException
    {
        LOG.info("C-FIND request  \n{}", rq);
        LOG.info("C-FIND keys     \n{}", keys);

        String queryLevel = keys.getString(Tag.QueryRetrieveLevel);

        LOG.info("C-FIND level [{}]", queryLevel);

        QueryTask queryTask = null;

        switch (queryLevel)
        {
            /*
            case "PATIENT":
            {
                queryTask = applicationContext.getBean(DcmPatientQueryTask.class, as, pc, rq, keys);
                break;
            }
            */
            case "STUDY":
            {
                queryTask = applicationContext.getBean(DcmStudyQueryTask.class, as, pc, rq, keys);
                break;
            }
            case "SERIES":
            {
                queryTask = applicationContext.getBean(DcmSeriesQueryTask.class, as, pc, rq, keys);
                break;
            }
            default:
            {
                // nop
            }
        }

        return queryTask;
    }


}
