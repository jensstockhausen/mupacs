package de.famst.dcm;

import de.famst.data.PatientEty;
import de.famst.data.StudyEty;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4che3.net.Association;
import org.dcm4che3.net.pdu.PresentationContext;
import org.dcm4che3.net.service.BasicQueryTask;
import org.dcm4che3.net.service.DicomServiceException;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.List;

/**
 * Created by jens on 30/10/2016.
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DcmPatientQueryTask extends BasicQueryTask
{
    @Inject
    @SuppressWarnings("squid:S3306") // Use constructor injection for this field.
    private PatientStudyFinder patientStudyFinder;

    private List<StudyEty> currentStudies;
    private long   currentStudyIndex;

    public DcmPatientQueryTask(
            Association as, PresentationContext pc,
            Attributes rq, Attributes keys)
    {
        super(as, pc, rq, keys);
    }

    @PostConstruct
    public void postConstructor()
    {
        List<PatientEty> patientEtyList = patientStudyFinder.findPatients(keys);
        currentStudies = patientStudyFinder.findStudies(patientEtyList);
        currentStudyIndex = 0;
    }


    @Override
    protected boolean hasMoreMatches() throws DicomServiceException
    {
        return currentStudyIndex < currentStudies.size();
    }

    @Override
    protected Attributes nextMatch() throws DicomServiceException
    {
        Attributes returnPatientRecord = new Attributes();

        returnPatientRecord.setString(Tag.PatientName, VR.PN, "DEMO");

        currentStudyIndex = currentStudyIndex + 1;

        return returnPatientRecord;
    }

}
