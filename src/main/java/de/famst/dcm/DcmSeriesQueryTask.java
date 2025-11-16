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
        List<SeriesEty> seriesEtyList = patientStudyFinder.findSeries(keys);
        currentSeries = seriesEtyList;
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

        nextMatch.setString(Tag.SeriesInstanceUID, VR.UI, seriesEty.getSeriesInstanceUID());
        nextMatch.setString(Tag.Modality, VR.CS, "US");
        nextMatch.setInt(Tag.SeriesNumber, VR.IS, 0);

        currentIndex = currentIndex + 1;

        LOG.info("next match \n{}", nextMatch);

        return nextMatch;
    }

}
