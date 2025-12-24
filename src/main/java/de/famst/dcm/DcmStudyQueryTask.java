package de.famst.dcm;

import de.famst.data.PatientEty;
import de.famst.data.StudyEty;
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
public class DcmStudyQueryTask extends BasicQueryTask
{
    private static Logger LOG = LoggerFactory.getLogger(DcmStudyQueryTask.class);

    @Inject
    @SuppressWarnings("squid:S3306") // Use constructor injection for this field.
    private PatientStudyFinder patientStudyFinder;

    private List<StudyEty> currentStudies;
    private int currentIndex;

    private String queryLevel;

    public DcmStudyQueryTask(
            Association as, PresentationContext pc,
            Attributes rq, Attributes keys)
    {
        super(as, pc, rq, keys);
    }

    @PostConstruct
    public void postConstructor()
    {
        queryLevel = keys.getString(Tag.QueryRetrieveLevel);

        LOG.info("Query level [{}]", queryLevel);

        currentStudies = patientStudyFinder.findStudies(keys);

        if (currentStudies == null)
        {
            LOG.warn("No studies found matching query criteria");
            currentStudies = List.of();
        }
        else
        {
            LOG.info("Found [{}] study(ies) matching query criteria", currentStudies.size());
        }

        currentIndex = 0;
    }


    @Override
    protected boolean hasMoreMatches() throws DicomServiceException
    {
        return currentIndex < currentStudies.size();
    }

    @Override
    protected Attributes nextMatch() throws DicomServiceException
    {
        Attributes nextMatch = new Attributes();

        nextMatch.addAll(keys);

        StudyEty studyEty = currentStudies.get(currentIndex);
        PatientEty patientEty = studyEty.getPatient();

        // Patient level attributes
        if (patientEty != null)
        {
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
        }

        // Study level attributes
        nextMatch.setString(Tag.StudyInstanceUID, VR.UI, studyEty.getStudyInstanceUID());

        if (studyEty.getStudyId() != null)
        {
            nextMatch.setString(Tag.StudyID, VR.SH, studyEty.getStudyId());
        }
        if (studyEty.getStudyDate() != null)
        {
            nextMatch.setDate(Tag.StudyDate, VR.DA,
                java.sql.Date.valueOf(studyEty.getStudyDate()));
        }
        if (studyEty.getStudyTime() != null)
        {
            nextMatch.setDate(Tag.StudyTime, VR.TM,
                java.sql.Time.valueOf(studyEty.getStudyTime()));
        }
        if (studyEty.getStudyDescription() != null)
        {
            nextMatch.setString(Tag.StudyDescription, VR.LO, studyEty.getStudyDescription());
        }
        if (studyEty.getAccessionNumber() != null)
        {
            nextMatch.setString(Tag.AccessionNumber, VR.SH, studyEty.getAccessionNumber());
        }
        if (studyEty.getModalitiesInStudy() != null)
        {
            nextMatch.setString(Tag.ModalitiesInStudy, VR.CS, studyEty.getModalitiesInStudy());
        }
        if (studyEty.getReferringPhysicianName() != null)
        {
            nextMatch.setString(Tag.ReferringPhysicianName, VR.PN, studyEty.getReferringPhysicianName());
        }

        currentIndex = currentIndex + 1;

        LOG.info("next match \n{}", nextMatch);

        return nextMatch;
    }

}
