package de.famst.controller;

import de.famst.data.PatientEty;
import de.famst.data.PatientRepository;
import de.famst.data.StudyEty;
import de.famst.data.StudyRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;


/**
 * Created by jens on 09/10/2016.
 */
@WebMvcTest(StudyListController.class)
public class StudyListControllerTest
{
  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private PatientRepository patientRepository;

  @MockitoBean
  private StudyRepository studyRepository;

  @Test
  public void test_study_list() throws Exception
  {
    PatientEty patient = new PatientEty("NAME", "ID");
    given(patientRepository.findById(any())).willReturn(java.util.Optional.of(patient));

    List<StudyEty> studies = new ArrayList<>();
    studies.add(new StudyEty());

    given(studyRepository.findByPatientId(anyLong())).willReturn(studies);


    mockMvc.perform(MockMvcRequestBuilders.get("/studylist?patientId=1"))
      .andExpect(status().isOk())
      .andExpect(model().size(2))
      .andExpect(model().attributeExists("patient"))
      .andExpect(model().attribute("patient", isA(PatientModel.class)))
      .andExpect(model().attribute("patient", hasProperty("patientsName", is("NAME"))))
      .andExpect(model().attribute("patient", hasProperty("numberOfStudies", is(0L))))
      .andExpect(model().attributeExists("studies"))
      .andExpect(model().attributeExists("studies"))
      .andExpect(model().attribute("studies", hasItem(
        allOf(
          hasProperty("id", is(0L)),
          hasProperty("numberOfSeries", is(0L))
        ))
      ))
      .andExpect(view().name("studyList"));
  }

}
