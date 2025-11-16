package de.famst.data;

import de.famst.MuPACSApplication;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

/**
 * Repository integration tests.
 * Tests CRUD operations and custom queries for all repository interfaces.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = {MuPACSApplication.class, RepositoryTest.SpringConfig.class})
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

  @BeforeEach
  public void setUp()
  {
    dbFiller.fillDB(entityManager);
  }

  // Patient

  @Test
  public void canFindPatientByExactName()
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
  public void canGetInstanceByInstanceUID()
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
