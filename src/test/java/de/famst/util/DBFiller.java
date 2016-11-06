package de.famst.util;

import de.famst.data.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * Created by jens on 06/11/2016.
 */
@Component
public class DBFiller
{
    private static Logger LOG = LoggerFactory.getLogger(DBFiller.class);
    
    @Inject
    PatientRepository patientRepository;

    @Inject
    StudyRepository studyRepository;

    @Inject
    SeriesRepository seriesRepository;

    @Inject
    InstanceRepository instanceRepository;

    public void fillDB()
    {
        LOG.info("filling db");

        for (int patIdx = 0; patIdx<10; patIdx++)
        {
            PatientEty patientEty = createPatient(patIdx);

            for (int stdIdx = 0; stdIdx < 2; stdIdx++)
            {
                StudyEty studyEty = createStudy(patientEty, stdIdx);

                for (int serIdx = 0; serIdx < 2; serIdx++ )
                {
                    SeriesEty seriesEty = createSeries(studyEty, serIdx);

                    for (int insIdx = 0; insIdx < 2; insIdx++ )
                    {
                        InstanceEty instanceEty = createInstance(seriesEty, insIdx);
                    }
                }
            }
        }
    }

    private PatientEty createPatient(int patIdx)
    {
        PatientEty patientEty = new PatientEty();

        patientEty.setPatientId(String.format("1.2.48.%d", patIdx));
        patientEty.setPatientName(String.format("Demo_%03d", patIdx));

        patientRepository.save(patientEty);

        return patientEty;
    }

    private StudyEty createStudy(PatientEty patientEty, int stdIdx)
    {
        StudyEty studyEty = new StudyEty();

        studyEty.setStudyInstanceUID(String.format("%s.%d", patientEty.getPatientId(), stdIdx));
        studyEty.setPatient(patientEty);

        studyRepository.save(studyEty);

        patientEty.addStudy(studyEty);
        patientRepository.save(patientEty);

        return studyEty;
    }

    private  SeriesEty createSeries(StudyEty studyEty, int serIdx)
    {
        SeriesEty seriesEty = new SeriesEty();

        seriesEty.setSeriesInstanceUID(String.format("%s.%d", studyEty.getStudyInstanceUID(), serIdx));
        seriesEty.setStudy(studyEty);

        seriesRepository.save(seriesEty);

        studyEty.addSeries(seriesEty);
        studyRepository.save(studyEty);

        return seriesEty;
    }

    private InstanceEty createInstance(SeriesEty seriesEty, int insIdx)
    {
        InstanceEty instanceEty = new InstanceEty();

        instanceEty.setInstanceUID(String.format("%s.%d", seriesEty.getSeriesInstanceUID(), insIdx));
        instanceEty.setPath("PATH");
        instanceEty.setSeries(seriesEty);

        instanceRepository.save(instanceEty);

        seriesEty.addInstance(instanceEty);
        seriesRepository.save(seriesEty);

        return instanceEty;
    }



}
