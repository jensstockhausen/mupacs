package de.famst.data;

import org.assertj.core.api.Assertions;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

/**
 * Created by jens on 31/10/2016.
 */
@RunWith(SpringRunner.class)
@DataJpaTest
@ContextConfiguration
public class RepositoryTest
{

  // add DBFiller as component
  @Configuration
  @ComponentScan("de.famst.data")
  public static class SpringConfig
  {
  }

  @Autowired
  private DataBaseFiller dbFiller;

  @Autowired
  private PatientRepository patientRepository;

  @Autowired
  private StudyRepository studyRepository;

  @Autowired
  private SeriesRepository seriesRepository;

  @Autowired
  private InstanceRepository instanceRepository;

  @Autowired
  private TestEntityManager entityManager;

  @Before
  public void setUp()
  {
    dbFiller.fillDB(entityManager);
  }

  // Patient

  @Test
  public void canFindPatientByExactName() throws Exception
  {
    PatientEty patientEty = patientRepository.findByPatientName("Demo_005");

    assertThat(patientEty, is(notNullValue()));
    assertThat(patientEty.getPatientName(), is(equalTo("Demo_005")));
  }

  @Test
  public void canFindPatientByPartialName()
  {
    List<PatientEty> patients = patientRepository.findByPatientNameLike("Demo%5");

    assertThat(patients, hasSize(1));
    assertThat(patients.get(0), is(notNullValue()));
    assertThat(patients.get(0).getPatientName(), is(equalTo("Demo_005")));
  }

  @Test
  public void canFindPatientByPatientId()
  {
    List<PatientEty> patients = patientRepository.findByPatientId("1.2.48.3");

    assertThat(patients, hasSize(1));
    assertThat(patients.get(0).getPatientName(), is(equalTo("Demo_003")));
  }

  // Study

  @Test
  public void canFindStudyByStudyInstanceUID()
  {
    StudyEty studyEty = studyRepository.findByStudyInstanceUID("1.2.48.5.1");

    assertThat(studyEty.getStudyInstanceUID(), is(equalTo("1.2.48.5.1")));
    assertThat(studyEty.getAccessionNumber(), is(equalTo("111")));
  }

  @Test
  public void canFindStudyByPatientId()
  {
    PatientEty patientEty = patientRepository.findByPatientName("Demo_005");
    List<StudyEty> studies = studyRepository.findByPatientId(patientEty.getId());

    assertThat(studies, hasSize(2));

    StudyEty studyEty = studies.get(0);
    assertThat(studyEty.getStudyInstanceUID(), is(equalTo("1.2.48.5.0")));
    assertThat(studyEty.getAccessionNumber(), is(equalTo("000")));

    assertThat(studyEty.getPatient(), is(equalTo(patientEty)));
    assertThat(patientEty.getStudies(), hasItem(studyEty));
  }

  // Series

  @Test
  public void canFindSeriesBySeriesInstanceUID()
  {
    SeriesEty seriesEty = seriesRepository.findBySeriesInstanceUID("1.2.48.3.1.1");
    assertThat(seriesEty.getSeriesInstanceUID(), is(equalTo("1.2.48.3.1.1")));
  }

  @Test
  public void canFindSeriesByStudyId()
  {
    StudyEty studyEty = studyRepository.findByStudyInstanceUID("1.2.48.3.1");
    List<SeriesEty> series = seriesRepository.findByStudyId(studyEty.getId());

    assertThat(series, hasSize(2));

    SeriesEty seriesEty = series.get(0);
    assertThat(seriesEty.getSeriesInstanceUID(), is(equalTo("1.2.48.3.1.0")));

    assertThat(seriesEty.getStudy(), is(equalTo(studyEty)));
    assertThat(studyEty.getSeries(), hasItem(seriesEty));
  }

  // Instance

  @Test
  public void canGetInstanceByInstanceUID() throws Exception
  {
    InstanceEty instanceEty = instanceRepository.findByInstanceUID("1.2.48.3.0.0.0");

    assertThat(instanceEty.getInstanceUID(), is(equalTo("1.2.48.3.0.0.0")));
    assertThat(instanceEty.getPath(), is(equalTo("PATH0")));
  }

  @Test
  public void canGetInstancesBySeriesId()
  {
    SeriesEty seriesEty = seriesRepository.findBySeriesInstanceUID("1.2.48.3.1.1");
    List<InstanceEty> instances = instanceRepository.findBySeriesId(seriesEty.getId());

    Assertions.assertThat(instances).hasSize(2);
    InstanceEty instanceEty = instances.get(0);

    assertThat(instanceEty.getInstanceUID(), is(equalTo("1.2.48.3.1.1.0")));
    assertThat(instanceEty.getPath(), is(equalTo("PATH0")));

    assertThat(instanceEty.getSeries(), is(equalTo(seriesEty)));
    assertThat(seriesEty.getInstances(), hasItem(instanceEty));
  }


}
