package de.famst.controller;

import de.famst.data.InstanceEty;
import de.famst.data.PatientEty;
import de.famst.data.SeriesEty;
import de.famst.data.StudyEty;
import de.famst.dcm.PatientStudyFinder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit tests for DicomWebQidoController.
 *
 * <p>Tests QIDO-RS endpoint functionality including:
 * <ul>
 *   <li>Study queries</li>
 *   <li>Series queries</li>
 *   <li>Instance queries</li>
 *   <li>Query parameter handling</li>
 * </ul>
 *
 * @author jens
 * @since 2026-04-24
 */
@WebMvcTest(DicomWebQidoController.class)
class DicomWebQidoControllerTest
{
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PatientStudyFinder patientStudyFinder;

    private PatientEty patient;
    private StudyEty study;
    private SeriesEty series;
    private InstanceEty instance;

    @BeforeEach
    void setUp()
    {
        // Create test entities (constructor is PatientEty(patientName, patientId))
        patient = new PatientEty("Test^Patient", "12345");
        patient.setPatientBirthDate(LocalDate.of(1980, 1, 15));
        patient.setPatientSex("M");

        study = new StudyEty("1.2.3.4.5");
        study.setStudyId("STUDY001");
        study.setStudyDate(LocalDate.of(2024, 1, 10));
        study.setStudyTime(LocalTime.of(10, 30, 0));
        study.setStudyDescription("Test Study");
        study.setAccessionNumber("ACC123");

        series = new SeriesEty("1.2.3.4.5.6");
        series.setModality("CT");
        series.setSeriesNumber(1);
        series.setSeriesDescription("Test Series");

        instance = new InstanceEty("1.2.3.4.5.6.7", "/path/to/file.dcm");
        instance.setInstanceNumber(1);

        // Setup relationships
        patient.addStudy(study);
        study.addSeries(series);
        series.addInstance(instance);
    }

    @Test
    @DisplayName("Should search for studies and return JSON array")
    void shouldSearchStudies() throws Exception
    {
        // Given
        when(patientStudyFinder.findStudies(any())).thenReturn(List.of(study));

        // When/Then
        mockMvc.perform(get("/qido-rs/studies")
                        .param("PatientID", "12345")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].0020000D.Value[0]").value("1.2.3.4.5"));
    }

    @Test
    @DisplayName("Should search for studies by patient name")
    void shouldSearchStudiesByPatientName() throws Exception
    {
        // Given
        when(patientStudyFinder.findStudies(any())).thenReturn(List.of(study));

        // When/Then
        mockMvc.perform(get("/qido-rs/studies")
                        .param("PatientName", "Test*"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].00100010.Value[0]").value("Test^Patient"));
    }

    @Test
    @DisplayName("Should search for series and return JSON array")
    void shouldSearchSeries() throws Exception
    {
        // Given
        when(patientStudyFinder.findSeries(any())).thenReturn(List.of(series));

        // When/Then
        mockMvc.perform(get("/qido-rs/series")
                        .param("StudyInstanceUID", "1.2.3.4.5"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].0020000E.Value[0]").value("1.2.3.4.5.6"))
                .andExpect(jsonPath("$[0].00080060.Value[0]").value("CT"));
    }

    @Test
    @DisplayName("Should search for series by modality")
    void shouldSearchSeriesByModality() throws Exception
    {
        // Given
        when(patientStudyFinder.findSeries(any())).thenReturn(List.of(series));

        // When/Then
        mockMvc.perform(get("/qido-rs/series")
                        .param("Modality", "CT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].00080060.Value[0]").value("CT"));
    }

    @Test
    @DisplayName("Should search for instances and return JSON array")
    void shouldSearchInstances() throws Exception
    {
        // Given
        when(patientStudyFinder.findInstances(any())).thenReturn(List.of(instance));

        // When/Then
        mockMvc.perform(get("/qido-rs/instances")
                        .param("SeriesInstanceUID", "1.2.3.4.5.6"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].00080018.Value[0]").value("1.2.3.4.5.6.7"));
    }

    @Test
    @DisplayName("Should return empty array when no studies found")
    void shouldReturnEmptyArrayWhenNoStudiesFound() throws Exception
    {
        // Given
        when(patientStudyFinder.findStudies(any())).thenReturn(List.of());

        // When/Then
        mockMvc.perform(get("/qido-rs/studies")
                        .param("PatientID", "NONEXISTENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("Should return empty array when no series found")
    void shouldReturnEmptyArrayWhenNoSeriesFound() throws Exception
    {
        // Given
        when(patientStudyFinder.findSeries(any())).thenReturn(List.of());

        // When/Then
        mockMvc.perform(get("/qido-rs/series")
                        .param("StudyInstanceUID", "NONEXISTENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("Should return empty array when no instances found")
    void shouldReturnEmptyArrayWhenNoInstancesFound() throws Exception
    {
        // Given
        when(patientStudyFinder.findInstances(any())).thenReturn(List.of());

        // When/Then
        mockMvc.perform(get("/qido-rs/instances")
                        .param("SeriesInstanceUID", "NONEXISTENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("Should handle multiple query parameters for studies")
    void shouldHandleMultipleQueryParametersForStudies() throws Exception
    {
        // Given
        when(patientStudyFinder.findStudies(any())).thenReturn(List.of(study));

        // When/Then
        mockMvc.perform(get("/qido-rs/studies")
                        .param("PatientID", "12345")
                        .param("StudyDate", "20240110")
                        .param("Modality", "CT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}

