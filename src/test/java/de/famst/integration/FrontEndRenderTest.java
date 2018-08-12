package de.famst.integration;


import de.famst.data.InstanceEty;
import de.famst.data.InstanceRepository;
import de.famst.data.PatientEty;
import de.famst.data.PatientRepository;
import de.famst.data.SeriesEty;
import de.famst.data.SeriesRepository;
import de.famst.data.StudyEty;
import de.famst.data.StudyRepository;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyLong;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class FrontEndRenderTest
{
  @MockBean
  private PatientRepository patientRepository;

  @MockBean
  private StudyRepository studyRepository;

  @MockBean
  private SeriesRepository seriesRepository;

  @MockBean
  private InstanceRepository instanceRepository;

  @Autowired
  private TestRestTemplate restTemplate;

  @Test
  public void canRenderPatientList()
  {
    List<PatientEty> patients = new ArrayList<>();
    patients.add(new PatientEty("A_PATIENT_NAME", "A_PATIENT_ID"));

    given(patientRepository.findAll()).willReturn(patients);


    ResponseEntity<String> patientsListResponse
      = restTemplate.getForEntity("/patientlist", String.class);

    assertThat(patientsListResponse.getBody(), containsString("List of Patients"));
    assertThat(patientsListResponse.getBody(), containsString("A_PATIENT_NAME"));
  }

  @Test
  public void canRenderStudiesList()
  {
    StudyEty studyEty = new StudyEty();
    studyEty.setStudyId("A_STUDY_ID");

    List<StudyEty> studies = new ArrayList<>();
    studies.add(studyEty);

    given(studyRepository.findByPatientId(anyLong())).willReturn(studies);

    ResponseEntity<String> studiesListResponse
      = restTemplate.getForEntity("/studylist?patientId=1", String.class);

    assertThat(studiesListResponse.getBody(), containsString("List of Studies"));
    assertThat(studiesListResponse.getBody(), containsString("A_STUDY_ID"));
  }

  @Test
  public void canRenderSeriesList()
  {
    SeriesEty seriesEty = new SeriesEty();
    seriesEty.setSeriesInstanceUID("A_SERIES_INSTANCE_UID");

    List<SeriesEty> series = new ArrayList<>();
    series.add(seriesEty);

    given(seriesRepository.findByStudyId(anyLong())).willReturn(series);

    ResponseEntity<String> seriesListResponse
      = restTemplate.getForEntity("/serieslist?studyId=1", String.class);

    assertThat(seriesListResponse.getBody(), containsString("List of Series"));
    assertThat(seriesListResponse.getBody(), containsString("A_SERIES_INSTANCE_UID"));
  }

  @Test
  public void canRenderInstanceList()
  {
    InstanceEty instanceEty = new InstanceEty();
    instanceEty.setInstanceUID("A_SOP_INSTANCE_UID");

    List<InstanceEty> instances = new ArrayList<>();
    instances.add(instanceEty);

    given(instanceRepository.findBySeriesId(anyLong())).willReturn(instances);

    ResponseEntity<String> instanceListResponse
      = restTemplate.getForEntity("/instancelist?seriesId=1", String.class);

    assertThat(instanceListResponse.getBody(), containsString("List of Instances"));
    assertThat(instanceListResponse.getBody(), containsString("A_SOP_INSTANCE_UID"));
  }

}
