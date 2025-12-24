package de.famst.dcm;

import de.famst.data.InstanceEty;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4che3.net.Association;
import org.dcm4che3.net.pdu.PresentationContext;
import org.dcm4che3.net.service.BasicQueryTask;
import org.dcm4che3.net.service.DicomServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Handles C-FIND queries at the IMAGE level.
 * Returns matching instances (images) based on query attributes.
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DcmImageQueryTask extends BasicQueryTask
{
    private static final Logger LOG = LoggerFactory.getLogger(DcmImageQueryTask.class);

    @Inject
    @SuppressWarnings("squid:S3306") // Use constructor injection for this field.
    private PatientStudyFinder patientStudyFinder;

    private List<InstanceEty> currentInstances;
    private int currentIndex;

    public DcmImageQueryTask(
            Association as, PresentationContext pc,
            Attributes rq, Attributes keys)
    {
        super(as, pc, rq, keys);
    }

    @PostConstruct
    public void postConstructor()
    {
        String queryLevel = keys.getString(Tag.QueryRetrieveLevel);
        LOG.info("Query level [{}]", queryLevel);

        currentInstances = patientStudyFinder.findInstances(keys);
        currentIndex = 0;

        if (currentInstances == null)
        {
            LOG.warn("No instances found matching query criteria");
            currentInstances = List.of();
        }
        else
        {
            LOG.info("Found [{}] instance(s) matching query criteria", currentInstances.size());
        }
    }

    @Override
    protected boolean hasMoreMatches() throws DicomServiceException
    {
        return currentIndex < currentInstances.size();
    }

    @Override
    protected Attributes nextMatch() throws DicomServiceException
    {
        Attributes nextMatch = new Attributes();
        nextMatch.addAll(keys);

        InstanceEty instanceEty = currentInstances.get(currentIndex);

        nextMatch.setString(Tag.SOPInstanceUID, VR.UI, instanceEty.getInstanceUID());

        if (instanceEty.getInstanceNumber() != null)
        {
            nextMatch.setInt(Tag.InstanceNumber, VR.IS, instanceEty.getInstanceNumber());
        }

        currentIndex = currentIndex + 1;

        LOG.info("next match \n{}", nextMatch);

        return nextMatch;
    }
}

