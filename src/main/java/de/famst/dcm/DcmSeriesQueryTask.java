package de.famst.dcm;

import de.famst.data.SeriesEty;
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
 * Created by jens on 30/10/2016.
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DcmSeriesQueryTask extends BasicQueryTask
{
    private static Logger LOG = LoggerFactory.getLogger(DcmSeriesQueryTask.class);

    @Inject
    @SuppressWarnings("squid:S3306") // Use constructor injection for this field.
    private PatientStudyFinder patientStudyFinder;

    private List<SeriesEty> currentSeries;
    private int currentIndex;

    public DcmSeriesQueryTask(
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

        currentSeries = patientStudyFinder.findSeries(keys);

        if (currentSeries == null)
        {
            LOG.warn("No series found matching query criteria");
            currentSeries = List.of();
        }
        else
        {
            LOG.info("Found [{}] series matching query criteria", currentSeries.size());
        }

        currentIndex = 0;
    }

    @Override
    protected boolean hasMoreMatches() throws DicomServiceException
    {
        return currentIndex < currentSeries.size();
    }

    @Override
    protected Attributes nextMatch() throws DicomServiceException
    {
        Attributes nextMatch = new Attributes();

        nextMatch.addAll(keys);

        SeriesEty seriesEty = currentSeries.get(currentIndex);

        // Series level attributes
        nextMatch.setString(Tag.SeriesInstanceUID, VR.UI, seriesEty.getSeriesInstanceUID());

        if (seriesEty.getModality() != null)
        {
            nextMatch.setString(Tag.Modality, VR.CS, seriesEty.getModality());
        }
        if (seriesEty.getSeriesNumber() != null)
        {
            nextMatch.setInt(Tag.SeriesNumber, VR.IS, seriesEty.getSeriesNumber());
        }
        if (seriesEty.getSeriesDescription() != null)
        {
            nextMatch.setString(Tag.SeriesDescription, VR.LO, seriesEty.getSeriesDescription());
        }
        if (seriesEty.getSeriesDate() != null)
        {
            nextMatch.setDate(Tag.SeriesDate, VR.DA,
                java.sql.Date.valueOf(seriesEty.getSeriesDate()));
        }
        if (seriesEty.getSeriesTime() != null)
        {
            nextMatch.setDate(Tag.SeriesTime, VR.TM,
                java.sql.Time.valueOf(seriesEty.getSeriesTime()));
        }
        if (seriesEty.getPerformingPhysicianName() != null)
        {
            nextMatch.setString(Tag.PerformingPhysicianName, VR.PN, seriesEty.getPerformingPhysicianName());
        }
        if (seriesEty.getProtocolName() != null)
        {
            nextMatch.setString(Tag.ProtocolName, VR.LO, seriesEty.getProtocolName());
        }
        if (seriesEty.getBodyPartExamined() != null)
        {
            nextMatch.setString(Tag.BodyPartExamined, VR.CS, seriesEty.getBodyPartExamined());
        }
        if (seriesEty.getPatientPosition() != null)
        {
            nextMatch.setString(Tag.PatientPosition, VR.CS, seriesEty.getPatientPosition());
        }
        if (seriesEty.getLaterality() != null)
        {
            nextMatch.setString(Tag.Laterality, VR.CS, seriesEty.getLaterality());
        }
        if (seriesEty.getOperatorsName() != null)
        {
            nextMatch.setString(Tag.OperatorsName, VR.PN, seriesEty.getOperatorsName());
        }

        currentIndex = currentIndex + 1;

        LOG.info("next match \n{}", nextMatch);

        return nextMatch;
    }

}
