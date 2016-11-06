package de.famst.dcm;

import de.famst.data.PatientEty;
import de.famst.data.PatientRepository;
import de.famst.data.StudyEty;
import de.famst.data.StudyRepository;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jens on 06/11/2016.
 */
@Component
@Transactional
public class PatientStudyFinder
{
    private static Logger LOG = LoggerFactory.getLogger(PatientStudyFinder.class);


    private PatientRepository patientRepository;
    private StudyRepository studyRepository;

    @Inject
    public PatientStudyFinder(PatientRepository patientRepository,
                              StudyRepository studyRepository)
    {
        this.patientRepository = patientRepository;
        this.studyRepository = studyRepository;

        LOG.info("PatientStudyFinder created");
    }

    @Transactional
    public List<PatientEty> findPatients(Attributes keys)
    {
        List<PatientEty> patientEtyList = null;

        String patientName = keys.getString(Tag.PatientName);

        if (null != patientName)
        {
            patientName = patientName.replace('*', '%');
            patientEtyList = patientRepository.findByPatientNameLike(patientName);

            LOG.info("found [{}] patient(s) matching", patientEtyList.size());
        }

        return patientEtyList;
    }


    @Transactional
    public List<StudyEty> findStudies(List<PatientEty> patientEtyList)
    {
        List<StudyEty> studyEtyList = new ArrayList<>();

        if (null == patientEtyList)
        {
            return studyEtyList;
        }

        for(PatientEty patientEty: patientEtyList)
        {
            List<StudyEty> studyRepositoryByPatientId = studyRepository.findByPatientId(patientEty.getId());

            for(StudyEty studyEty: studyRepositoryByPatientId)
            {
                studyEtyList.add(studyEty);
            }
        }

        LOG.info("found [{}] studie(s) matching", studyEtyList.size());

        return studyEtyList;
    }
}
