package de.famst.dcm;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.UID;
import org.dcm4che3.net.Association;
import org.dcm4che3.net.Status;
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
 * Handles C-FIND SCP requests for Patient, Study, and Series levels.
 * Improved for robustness and maintainability.
 */
@Component
public class DcmFindSCP extends BasicCFindSCP implements ApplicationContextAware
{
    private static final Logger LOG = LoggerFactory.getLogger(DcmFindSCP.class);

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

    /**
     * Calculates matches for C-FIND requests based on QueryRetrieveLevel.
     * Handles PATIENT, STUDY, and SERIES levels. Logs and throws for unsupported levels.
     */
    @Override
    protected QueryTask calculateMatches(Association as, PresentationContext pc, Attributes rq, Attributes keys) throws DicomServiceException
    {
        if (keys == null)
        {
            LOG.warn("C-FIND keys are null");
            throw new DicomServiceException(Status.ProcessingFailure, "C-FIND keys are null");
        }

        LOG.info("C-FIND request  \n{}", rq);
        LOG.info("C-FIND keys     \n{}", keys);

        String queryLevel = keys.getString(Tag.QueryRetrieveLevel);
        if (queryLevel == null)
        {
            LOG.warn("QueryRetrieveLevel is missing in C-FIND keys");
            throw new DicomServiceException(Status.ProcessingFailure, "QueryRetrieveLevel is missing");
        }

        LOG.info("C-FIND level [{}]", queryLevel);

        QueryTask queryTask;
        switch (queryLevel.toUpperCase())
        {
            case "PATIENT":
            {
                queryTask = applicationContext.getBean(DcmPatientQueryTask.class, as, pc, rq, keys);
                break;
            }
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
            case "IMAGE":
            {
                queryTask = applicationContext.getBean(DcmImageQueryTask.class, as, pc, rq, keys);
                break;
            }
            default:
            {
                LOG.warn("Unsupported QueryRetrieveLevel: {}", queryLevel);
                throw new DicomServiceException(Status.ProcessingFailure, "Unsupported QueryRetrieveLevel: " + queryLevel);
            }
        }


        return queryTask;
    }
}
