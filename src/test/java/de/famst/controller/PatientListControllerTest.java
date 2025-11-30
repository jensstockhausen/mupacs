package de.famst.controller;


import de.famst.data.PatientEty;
import de.famst.data.PatientRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(PatientListController.class)
public class PatientListControllerTest
{
  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private PatientRepository patientRepository;

  @Test
  public void canGetListOfPatients() throws Exception
  {
    List<PatientEty> patients = new ArrayList<>();
    patients.add(new PatientEty("NAME", "ID"));

    // Create a Page object with the patient list
    Page<PatientEty> patientPage = new PageImpl<>(patients, PageRequest.of(0, 10), 1);

    given(patientRepository.findAllWithStudies(any(Pageable.class))).willReturn(patientPage);

    mockMvc.perform(MockMvcRequestBuilders.get("/patientlist"))
      .andExpect(status().isOk())
      .andExpect(model().attributeExists("patients"))
      .andExpect(model().attribute("patients", hasSize(1)))
      .andExpect(model().attribute("patients", isA(List.class)))
      .andExpect(model().attribute("patients", hasItem(
        allOf(
          hasProperty("patientsName", is("NAME")),
          hasProperty("patientId", is("ID")),
          hasProperty("numberOfStudies", is(0L)))
        )
      ))
      .andExpect(model().attributeExists("currentPage"))
      .andExpect(model().attributeExists("totalPages"))
      .andExpect(model().attributeExists("totalItems"))
      .andExpect(model().attributeExists("pageSize"))
      .andExpect(model().attribute("currentPage", is(0)))
      .andExpect(model().attribute("totalPages", is(1)))
      .andExpect(model().attribute("totalItems", is(1L)))
      .andExpect(model().attribute("pageSize", is(10)))
      .andExpect(view().name("patientList"));
  }
}
