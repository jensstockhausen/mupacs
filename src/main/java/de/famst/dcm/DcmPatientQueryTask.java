package de.famst.dcm;

import de.famst.data.PatientEty;
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
 * Handles C-FIND queries at the PATIENT level.
 * Returns matching patients based on query attributes.
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DcmPatientQueryTask extends BasicQueryTask
{
    private static final Logger LOG = LoggerFactory.getLogger(DcmPatientQueryTask.class);

    @Inject
    @SuppressWarnings("squid:S3306") // Use constructor injection for this field.
    private PatientStudyFinder patientStudyFinder;

    private List<PatientEty> currentPatients;
    private int currentIndex;

    public DcmPatientQueryTask(
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

        currentPatients = patientStudyFinder.findPatients(keys);
        currentIndex = 0;

        if (currentPatients == null)
        {
            LOG.warn("No patients found matching query criteria");
            currentPatients = List.of();
        }
        else
        {
            LOG.info("Found [{}] patient(s) matching query criteria", currentPatients.size());
        }
    }

    @Override
    protected boolean hasMoreMatches() throws DicomServiceException
    {
        return currentIndex < currentPatients.size();
    }

    @Override
    protected Attributes nextMatch() throws DicomServiceException
    {
        Attributes nextMatch = new Attributes();
        nextMatch.addAll(keys);

        PatientEty patientEty = currentPatients.get(currentIndex);

        nextMatch.setString(Tag.PatientName, VR.PN, patientEty.getPatientName());
        nextMatch.setString(Tag.PatientID, VR.LO, patientEty.getPatientId());

        if (patientEty.getPatientBirthDate() != null)
        {
            nextMatch.setDate(Tag.PatientBirthDate, VR.DA,
                java.sql.Date.valueOf(patientEty.getPatientBirthDate()));
        }

        if (patientEty.getPatientSex() != null)
        {
            nextMatch.setString(Tag.PatientSex, VR.CS, patientEty.getPatientSex());
        }

        currentIndex = currentIndex + 1;

        LOG.info("next match \n{}", nextMatch);

        return nextMatch;
    }
}

