package de.famst.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.stereotype.Component;

/**
 * Created by jens on 06/11/2016.
 */
@Component
public class DataBaseFiller
{
  private static final Logger LOG = LoggerFactory.getLogger(DataBaseFiller.class);

  private TestEntityManager entityManager;

  /**
   * created data:
   * prefix:   1.2.48
   * patient:  0..9 / Demo_000
   * study:    0..2
   * series:   0..2
   * instance: 0..2 / PATH0
   */
  public void fillDB(TestEntityManager entityManager)
  {
    LOG.info("filling db");

    this.entityManager = entityManager;

    for (int patIdx = 0; patIdx < 10; patIdx++)
    {
      PatientEty patientEty = createPatient(patIdx);

      for (int stdIdx = 0; stdIdx < 2; stdIdx++)
      {
        StudyEty studyEty = createStudy(patientEty, stdIdx);

        for (int serIdx = 0; serIdx < 2; serIdx++)
        {
          SeriesEty seriesEty = createSeries(studyEty, serIdx);

          for (int insIdx = 0; insIdx < 2; insIdx++)
          {
            createInstance(seriesEty, insIdx);
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

    return entityManager.persistFlushFind(patientEty);
  }

  private StudyEty createStudy(PatientEty patientEty, int stdIdx)
  {
    StudyEty studyEty = new StudyEty();

    studyEty.setStudyInstanceUID(String.format("%s.%d", patientEty.getPatientId(), stdIdx));
    studyEty.setAccessionNumber(String.format("%d%d%d", stdIdx, stdIdx, stdIdx));
    studyEty.setPatient(patientEty);

    return entityManager.persistFlushFind(studyEty);
  }

  private SeriesEty createSeries(StudyEty studyEty, int serIdx)
  {
    SeriesEty seriesEty = new SeriesEty();

    seriesEty.setSeriesInstanceUID(String.format("%s.%d", studyEty.getStudyInstanceUID(), serIdx));
    seriesEty.setStudy(studyEty);

    return entityManager.persistFlushFind(seriesEty);
  }

  private InstanceEty createInstance(SeriesEty seriesEty, int insIdx)
  {
    InstanceEty instanceEty = new InstanceEty();

    instanceEty.setInstanceUID(String.format("%s.%d", seriesEty.getSeriesInstanceUID(), insIdx));
    instanceEty.setPath(String.format("PATH%d", insIdx));
    instanceEty.setSeries(seriesEty);

    return entityManager.persistFlushFind(instanceEty);
  }

}
