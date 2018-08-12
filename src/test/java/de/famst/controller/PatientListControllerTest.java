package de.famst.controller;


import de.famst.data.PatientEty;
import de.famst.data.PatientRepository;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.mockito.BDDMockito.given;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@WebMvcTest(PatientListController.class)
public class PatientListControllerTest
{
  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private PatientRepository patientRepository;

  @Test
  public void canGetListOfPatients() throws Exception
  {
    List<PatientEty> patients = new ArrayList<>();
    patients.add(new PatientEty("NAME", "ID"));

    given(patientRepository.findAll()).willReturn(patients);

    mockMvc.perform(MockMvcRequestBuilders.get("/patientlist"))
      .andExpect(status().isOk())
      .andExpect(model().attributeExists("patients"))
      .andExpect(model().attribute("patients", hasSize(1)))
      .andExpect(model().attribute("patients", isA(List.class)))
      .andExpect(model().attribute("patients", hasItem(
        allOf(
          hasProperty("patientsName", is("NAME")),
          hasProperty("numberOfStudies", is(0L)))
        )
      ))
      .andExpect(view().name("patientList"));
  }
}
