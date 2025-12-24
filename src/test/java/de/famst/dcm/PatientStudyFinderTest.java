package de.famst.dcm;

import de.famst.data.InstanceEty;
import de.famst.data.InstanceRepository;
import de.famst.data.PatientEty;
import de.famst.data.PatientRepository;
import de.famst.data.SeriesEty;
import de.famst.data.SeriesRepository;
import de.famst.data.StudyEty;
import de.famst.data.StudyRepository;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for PatientStudyFinder methods.
 * Tests all DICOM C-FIND query levels: PATIENT, STUDY, SERIES, IMAGE.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PatientStudyFinder Tests")
class PatientStudyFinderTest
{
    @Mock
    private PatientRepository patientRepository;

    @Mock
    private StudyRepository studyRepository;

    @Mock
    private SeriesRepository seriesRepository;

    @Mock
    private InstanceRepository instanceRepository;

    private PatientStudyFinder patientStudyFinder;

    // Test data for PATIENT level
    private PatientEty patient1;
    private PatientEty patient2;
    private PatientEty patient3;
    private List<PatientEty> allPatients;

    // Test data for STUDY level
    private StudyEty study1;
    private StudyEty study2;
    private StudyEty study3;
    private List<StudyEty> allStudies;

    // Test data for SERIES level
    private SeriesEty series1;
    private SeriesEty series2;
    private SeriesEty series3;
    private List<SeriesEty> allSeries;

    // Test data for IMAGE level
    private InstanceEty instance1;
    private InstanceEty instance2;
    private InstanceEty instance3;
    private List<InstanceEty> allInstances;

    @BeforeEach
    void setUp()
    {
        patientStudyFinder = new PatientStudyFinder(
            patientRepository,
            studyRepository,
            seriesRepository,
            instanceRepository
        );

        setupPatientTestData();
        setupStudyTestData();
        setupSeriesTestData();
        setupInstanceTestData();
    }

    private void setupPatientTestData()
    {
        // Create test patients with diverse attributes
        patient1 = new PatientEty("Doe^John", "PAT001");
        patient1.setPatientBirthDate(LocalDate.of(1980, 5, 15));
        patient1.setPatientSex("M");
        patient1.setPatientBirthTime(LocalTime.of(10, 30, 0));
        patient1.setOtherPatientIds("ALT001");
        patient1.setOtherPatientNames("John Doe");
        patient1.setEthnicGroup("Caucasian");
        patient1.setPatientComments("Regular patient");
        patient1.setPatientAge("043Y");
        patient1.setPatientSize(1.75);
        patient1.setPatientWeight(75.5);
        patient1.setMedicalAlerts("None");
        patient1.setAllergies("Penicillin");
        patient1.setPregnancyStatus(4);
        patient1.setResponsiblePerson("Jane Doe");
        patient1.setResponsibleOrganization("General Hospital");

        patient2 = new PatientEty("Smith^Jane", "PAT002");
        patient2.setPatientBirthDate(LocalDate.of(1990, 8, 22));
        patient2.setPatientSex("F");
        patient2.setPatientBirthTime(LocalTime.of(14, 45, 30));
        patient2.setOtherPatientIds("ALT002");
        patient2.setOtherPatientNames("Jane Smith");
        patient2.setEthnicGroup("Asian");
        patient2.setPatientComments("VIP patient");
        patient2.setPatientAge("033Y");
        patient2.setPatientSize(1.65);
        patient2.setPatientWeight(60.0);
        patient2.setMedicalAlerts("Diabetes");
        patient2.setAllergies("None");
        patient2.setPregnancyStatus(1);
        patient2.setResponsiblePerson("John Smith");
        patient2.setResponsibleOrganization("City Clinic");

        patient3 = new PatientEty("Johnson^Bob", "PAT003");
        patient3.setPatientBirthDate(LocalDate.of(1975, 12, 1));
        patient3.setPatientSex("M");
        patient3.setPatientBirthTime(LocalTime.of(8, 15, 0));
        patient3.setOtherPatientIds("ALT003");
        patient3.setOtherPatientNames("Bob Johnson");
        patient3.setEthnicGroup("African American");
        patient3.setPatientComments("Emergency contact needed");
        patient3.setPatientAge("048Y");
        patient3.setPatientSize(1.80);
        patient3.setPatientWeight(85.0);
        patient3.setMedicalAlerts("Hypertension");
        patient3.setAllergies("Latex");
        patient3.setPregnancyStatus(4);
        patient3.setResponsiblePerson("Mary Johnson");
        patient3.setResponsibleOrganization("General Hospital");

        allPatients = new ArrayList<>(Arrays.asList(patient1, patient2, patient3));
    }

    private void setupStudyTestData()
    {
        study1 = new StudyEty("1.2.3.4.5.6.7.8.1");
        study1.setStudyId("STUDY001");
        study1.setStudyDate(LocalDate.of(2023, 6, 15));
        study1.setStudyTime(LocalTime.of(9, 30, 0));
        study1.setStudyDescription("CT Chest");
        study1.setAccessionNumber("ACC001");
        study1.setModalitiesInStudy("CT");
        study1.setReferringPhysicianName("Dr. Smith");

        study2 = new StudyEty("1.2.3.4.5.6.7.8.2");
        study2.setStudyId("STUDY002");
        study2.setStudyDate(LocalDate.of(2023, 7, 20));
        study2.setStudyTime(LocalTime.of(14, 0, 0));
        study2.setStudyDescription("MRI Brain");
        study2.setAccessionNumber("ACC002");
        study2.setModalitiesInStudy("MR");
        study2.setReferringPhysicianName("Dr. Johnson");

        study3 = new StudyEty("1.2.3.4.5.6.7.8.3");
        study3.setStudyId("STUDY003");
        study3.setStudyDate(LocalDate.of(2023, 8, 10));
        study3.setStudyTime(LocalTime.of(11, 15, 0));
        study3.setStudyDescription("X-Ray Chest");
        study3.setAccessionNumber("ACC003");
        study3.setModalitiesInStudy("CR");
        study3.setReferringPhysicianName("Dr. Smith");

        allStudies = new ArrayList<>(Arrays.asList(study1, study2, study3));
    }

    private void setupSeriesTestData()
    {
        series1 = new SeriesEty("1.2.3.4.5.6.7.8.1.1");
        series1.setSeriesNumber(1);
        series1.setModality("CT");
        series1.setSeriesDescription("Axial");
        series1.setSeriesDate(LocalDate.of(2023, 6, 15));
        series1.setSeriesTime(LocalTime.of(9, 35, 0));
        series1.setPerformingPhysicianName("Dr. Tech1");
        series1.setBodyPartExamined("CHEST");
        series1.setProtocolName("CT_CHEST_ROUTINE");

        series2 = new SeriesEty("1.2.3.4.5.6.7.8.1.2");
        series2.setSeriesNumber(2);
        series2.setModality("CT");
        series2.setSeriesDescription("Coronal");
        series2.setSeriesDate(LocalDate.of(2023, 6, 15));
        series2.setSeriesTime(LocalTime.of(9, 40, 0));
        series2.setPerformingPhysicianName("Dr. Tech1");
        series2.setBodyPartExamined("CHEST");
        series2.setProtocolName("CT_CHEST_ROUTINE");

        series3 = new SeriesEty("1.2.3.4.5.6.7.8.2.1");
        series3.setSeriesNumber(1);
        series3.setModality("MR");
        series3.setSeriesDescription("T1 Weighted");
        series3.setSeriesDate(LocalDate.of(2023, 7, 20));
        series3.setSeriesTime(LocalTime.of(14, 5, 0));
        series3.setPerformingPhysicianName("Dr. Tech2");
        series3.setBodyPartExamined("HEAD");
        series3.setProtocolName("MR_BRAIN_ROUTINE");

        allSeries = new ArrayList<>(Arrays.asList(series1, series2, series3));
    }

    private void setupInstanceTestData()
    {
        instance1 = new InstanceEty("1.2.3.4.5.6.7.8.1.1.1", "/archive/instance1.dcm");
        instance1.setInstanceNumber(1);
        instance1.setContentDate(LocalDate.of(2023, 6, 15));
        instance1.setContentTime(LocalTime.of(9, 35, 30));
        instance1.setImageType("ORIGINAL\\PRIMARY\\AXIAL");
        instance1.setAcquisitionNumber(1);
        instance1.setAcquisitionDate(LocalDate.of(2023, 6, 15));
        instance1.setRows(512);
        instance1.setColumns(512);
        instance1.setBitsAllocated(16);
        instance1.setBitsStored(12);

        instance2 = new InstanceEty("1.2.3.4.5.6.7.8.1.1.2", "/archive/instance2.dcm");
        instance2.setInstanceNumber(2);
        instance2.setContentDate(LocalDate.of(2023, 6, 15));
        instance2.setContentTime(LocalTime.of(9, 35, 31));
        instance2.setImageType("ORIGINAL\\PRIMARY\\AXIAL");
        instance2.setAcquisitionNumber(1);
        instance2.setAcquisitionDate(LocalDate.of(2023, 6, 15));
        instance2.setRows(512);
        instance2.setColumns(512);
        instance2.setBitsAllocated(16);
        instance2.setBitsStored(12);

        instance3 = new InstanceEty("1.2.3.4.5.6.7.8.2.1.1", "/archive/instance3.dcm");
        instance3.setInstanceNumber(1);
        instance3.setContentDate(LocalDate.of(2023, 7, 20));
        instance3.setContentTime(LocalTime.of(14, 5, 30));
        instance3.setImageType("ORIGINAL\\PRIMARY");
        instance3.setAcquisitionNumber(1);
        instance3.setAcquisitionDate(LocalDate.of(2023, 7, 20));
        instance3.setRows(256);
        instance3.setColumns(256);
        instance3.setBitsAllocated(16);
        instance3.setBitsStored(16);

        allInstances = new ArrayList<>(Arrays.asList(instance1, instance2, instance3));
    }

    // ==================== PATIENT LEVEL TESTS ====================

    @Nested
    @DisplayName("findPatients Tests")
    class FindPatientsTests
    {
        @Nested
        @DisplayName("Empty Query Tests")
        class EmptyQueryTests
        {
            @Test
            @DisplayName("Should return all patients when no search keys provided")
            void testFindPatients_EmptyKeys_ReturnsAllPatients()
            {
                // Given
                Attributes keys = new Attributes();
                when(patientRepository.findAll()).thenReturn(allPatients);

                // When
                List<PatientEty> result = patientStudyFinder.findPatients(keys);

                // Then
                assertNotNull(result);
                assertEquals(3, result.size());
                verify(patientRepository, times(1)).findAll();
            }
        }

        @Nested
        @DisplayName("PatientName Search Tests")
        class PatientNameSearchTests
        {
            @Test
            @DisplayName("Should find patient by exact name match")
            void testFindPatients_ExactPatientName()
            {
                // Given
                Attributes keys = new Attributes();
                keys.setString(Tag.PatientName, VR.PN, "Doe^John");
                when(patientRepository.findByPatientName("Doe^John")).thenReturn(patient1);

                // When
                List<PatientEty> result = patientStudyFinder.findPatients(keys);

                // Then
                assertNotNull(result);
                assertEquals(1, result.size());
                assertEquals("Doe^John", result.get(0).getPatientName());
                verify(patientRepository, times(1)).findByPatientName("Doe^John");
            }

            @Test
            @DisplayName("Should find patients by wildcard name pattern")
            void testFindPatients_WildcardPatientName()
            {
            // Given
            Attributes keys = new Attributes();
            keys.setString(Tag.PatientName, VR.PN, "Doe*");
            when(patientRepository.findByPatientNameLike("Doe%")).thenReturn(Collections.singletonList(patient1));

            // When
            List<PatientEty> result = patientStudyFinder.findPatients(keys);

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("Doe^John", result.get(0).getPatientName());
            verify(patientRepository, times(1)).findByPatientNameLike("Doe%");
        }

        @Test
        @DisplayName("Should return empty list when patient name not found")
        void testFindPatients_PatientNameNotFound()
        {
            // Given
            Attributes keys = new Attributes();
            keys.setString(Tag.PatientName, VR.PN, "NonExistent^Patient");
            when(patientRepository.findByPatientName("NonExistent^Patient")).thenReturn(null);

            // When
            List<PatientEty> result = patientStudyFinder.findPatients(keys);

            // Then
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("PatientID Search Tests")
    class PatientIDSearchTests
    {
        @Test
        @DisplayName("Should filter by exact PatientID")
        void testFindPatients_ExactPatientID()
        {
            // Given
            Attributes keys = new Attributes();
            keys.setString(Tag.PatientID, VR.LO, "PAT001");
            when(patientRepository.findAll()).thenReturn(allPatients);

            // When
            List<PatientEty> result = patientStudyFinder.findPatients(keys);

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("PAT001", result.get(0).getPatientId());
        }

        @Test
        @DisplayName("Should filter by wildcard PatientID pattern")
        void testFindPatients_WildcardPatientID()
        {
            // Given
            Attributes keys = new Attributes();
            keys.setString(Tag.PatientID, VR.LO, "PAT00*");
            when(patientRepository.findAll()).thenReturn(allPatients);

            // When
            List<PatientEty> result = patientStudyFinder.findPatients(keys);

            // Then
            assertNotNull(result);
            assertEquals(3, result.size()); // All match PAT00*
        }

        @Test
        @DisplayName("Should filter by single character wildcard in PatientID")
        void testFindPatients_SingleCharWildcardPatientID()
        {
            // Given
            Attributes keys = new Attributes();
            keys.setString(Tag.PatientID, VR.LO, "PAT00?");
            when(patientRepository.findAll()).thenReturn(allPatients);

            // When
            List<PatientEty> result = patientStudyFinder.findPatients(keys);

            // Then
            assertNotNull(result);
            assertEquals(3, result.size()); // All match PAT00?
        }
    }

    @Nested
    @DisplayName("PatientBirthDate Search Tests")
    class PatientBirthDateSearchTests
    {
        @Test
        @DisplayName("Should filter by exact PatientBirthDate")
        void testFindPatients_ExactBirthDate()
        {
            // Given
            Attributes keys = new Attributes();
            keys.setDate(Tag.PatientBirthDate, VR.DA, java.sql.Date.valueOf(LocalDate.of(1980, 5, 15)));
            when(patientRepository.findAll()).thenReturn(allPatients);

            // When
            List<PatientEty> result = patientStudyFinder.findPatients(keys);

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(LocalDate.of(1980, 5, 15), result.get(0).getPatientBirthDate());
        }

        @Test
        @DisplayName("Should return empty list when birth date doesn't match")
        void testFindPatients_BirthDateNotFound()
        {
            // Given
            Attributes keys = new Attributes();
            keys.setDate(Tag.PatientBirthDate, VR.DA, java.sql.Date.valueOf(LocalDate.of(2000, 1, 1)));
            when(patientRepository.findAll()).thenReturn(allPatients);

            // When
            List<PatientEty> result = patientStudyFinder.findPatients(keys);

            // Then
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("PatientSex Search Tests")
    class PatientSexSearchTests
    {
        @Test
        @DisplayName("Should filter by PatientSex male")
        void testFindPatients_PatientSexMale()
        {
            // Given
            Attributes keys = new Attributes();
            keys.setString(Tag.PatientSex, VR.CS, "M");
            when(patientRepository.findAll()).thenReturn(allPatients);

            // When
            List<PatientEty> result = patientStudyFinder.findPatients(keys);

            // Then
            assertNotNull(result);
            assertEquals(2, result.size()); // patient1 and patient3 are male
            assertTrue(result.stream().allMatch(p -> "M".equals(p.getPatientSex())));
        }

        @Test
        @DisplayName("Should filter by PatientSex female")
        void testFindPatients_PatientSexFemale()
        {
            // Given
            Attributes keys = new Attributes();
            keys.setString(Tag.PatientSex, VR.CS, "F");
            when(patientRepository.findAll()).thenReturn(allPatients);

            // When
            List<PatientEty> result = patientStudyFinder.findPatients(keys);

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("F", result.get(0).getPatientSex());
        }
    }

    @Nested
    @DisplayName("Multiple Filters Combined Tests")
    class MultipleFiltersTests
    {
        @Test
        @DisplayName("Should filter by PatientName and PatientSex")
        void testFindPatients_NameAndSex()
        {
            // Given
            Attributes keys = new Attributes();
            keys.setString(Tag.PatientName, VR.PN, "Doe*");
            keys.setString(Tag.PatientSex, VR.CS, "M");
            when(patientRepository.findByPatientNameLike("Doe%")).thenReturn(Collections.singletonList(patient1));

            // When
            List<PatientEty> result = patientStudyFinder.findPatients(keys);

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("Doe^John", result.get(0).getPatientName());
            assertEquals("M", result.get(0).getPatientSex());
        }

        @Test
        @DisplayName("Should filter by PatientID and PatientBirthDate")
        void testFindPatients_IDAndBirthDate()
        {
            // Given
            Attributes keys = new Attributes();
            keys.setString(Tag.PatientID, VR.LO, "PAT001");
            keys.setDate(Tag.PatientBirthDate, VR.DA, java.sql.Date.valueOf(LocalDate.of(1980, 5, 15)));
            when(patientRepository.findAll()).thenReturn(allPatients);

            // When
            List<PatientEty> result = patientStudyFinder.findPatients(keys);

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("PAT001", result.get(0).getPatientId());
            assertEquals(LocalDate.of(1980, 5, 15), result.get(0).getPatientBirthDate());
        }

        @Test
        @DisplayName("Should return empty list when combined filters don't match")
        void testFindPatients_CombinedFiltersNoMatch()
        {
            // Given
            Attributes keys = new Attributes();
            keys.setString(Tag.PatientSex, VR.CS, "M");
            keys.setDate(Tag.PatientBirthDate, VR.DA, java.sql.Date.valueOf(LocalDate.of(1990, 8, 22)));
            when(patientRepository.findAll()).thenReturn(allPatients);

            // When
            List<PatientEty> result = patientStudyFinder.findPatients(keys);

            // Then
            assertNotNull(result);
            assertTrue(result.isEmpty()); // patient2 has this birth date but is female, not male
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests
    {
        @Test
        @DisplayName("Should handle empty PatientName string")
        void testFindPatients_EmptyPatientName()
        {
            // Given
            Attributes keys = new Attributes();
            keys.setString(Tag.PatientName, VR.PN, "");
            when(patientRepository.findAll()).thenReturn(allPatients);

            // When
            List<PatientEty> result = patientStudyFinder.findPatients(keys);

            // Then
            assertNotNull(result);
            assertEquals(3, result.size()); // Empty string is ignored, returns all
            verify(patientRepository, never()).findByPatientName(anyString());
            verify(patientRepository, never()).findByPatientNameLike(anyString());
        }

        @Test
        @DisplayName("Should handle null values in patient attributes")
        void testFindPatients_PatientWithNullAttributes()
        {
            // Given
            PatientEty patientWithNulls = new PatientEty("Minimal^Patient", "PAT999");
            // Don't set any optional attributes

            Attributes keys = new Attributes();
            keys.setString(Tag.PatientSex, VR.CS, "M");
            when(patientRepository.findAll()).thenReturn(Collections.singletonList(patientWithNulls));

            // When
            List<PatientEty> result = patientStudyFinder.findPatients(keys);

            // Then
            assertNotNull(result);
            assertTrue(result.isEmpty()); // Patient has null sex, doesn't match "M"
        }

        @Test
        @DisplayName("Should handle repository returning empty list")
        void testFindPatients_EmptyRepository()
        {
            // Given
            Attributes keys = new Attributes();
            keys.setString(Tag.PatientName, VR.PN, "Any*");
            when(patientRepository.findByPatientNameLike("Any%")).thenReturn(Collections.emptyList());

            // When
            List<PatientEty> result = patientStudyFinder.findPatients(keys);

            // Then
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("Wildcard Pattern Matching Tests")
    class WildcardPatternTests
    {
        @Test
        @DisplayName("Should match pattern with question mark in PatientSex")
        void testMatchesPattern_QuestionMark()
        {
            // Given
            Attributes keys = new Attributes();
            keys.setString(Tag.PatientSex, VR.CS, "?");
            when(patientRepository.findAll()).thenReturn(allPatients);

            // When
            List<PatientEty> result = patientStudyFinder.findPatients(keys);

            // Then
            assertNotNull(result);
            assertEquals(3, result.size()); // All patients have single-character sex
        }

        @Test
        @DisplayName("Should match PatientID pattern with wildcard")
        void testMatchesPattern_PatientIDWildcard()
        {
            // Given
            Attributes keys = new Attributes();
            keys.setString(Tag.PatientID, VR.LO, "PAT*");
            when(patientRepository.findAll()).thenReturn(allPatients);

            // When
            List<PatientEty> result = patientStudyFinder.findPatients(keys);

            // Then
            assertNotNull(result);
            assertEquals(3, result.size()); // All patients match PAT*
        }
    }
    } // End of FindPatientsTests

    // ==================== STUDY LEVEL TESTS ====================

    @Nested
    @DisplayName("findStudies Tests")
    class FindStudiesTests
    {
        @Nested
        @DisplayName("Empty Query Tests")
        class StudyEmptyQueryTests
        {
            @Test
            @DisplayName("Should return all studies when no search keys provided")
            void testFindStudies_EmptyKeys_ReturnsAllStudies()
            {
                // Given
                Attributes keys = new Attributes();
                when(studyRepository.findAll()).thenReturn(allStudies);

                // When
                List<StudyEty> result = patientStudyFinder.findStudies(keys);

                // Then
                assertNotNull(result);
                assertEquals(3, result.size());
                verify(studyRepository, times(1)).findAll();
            }
        }

        @Nested
        @DisplayName("StudyInstanceUID Search Tests")
        class StudyInstanceUIDSearchTests
        {
            @Test
            @DisplayName("Should filter by exact StudyInstanceUID")
            void testFindStudies_ExactStudyInstanceUID()
            {
                // Given
                Attributes keys = new Attributes();
                keys.setString(Tag.StudyInstanceUID, VR.UI, "1.2.3.4.5.6.7.8.1");
                when(studyRepository.findAll()).thenReturn(allStudies);

                // When
                List<StudyEty> result = patientStudyFinder.findStudies(keys);

                // Then
                assertNotNull(result);
                assertEquals(1, result.size());
                assertEquals("1.2.3.4.5.6.7.8.1", result.get(0).getStudyInstanceUID());
            }

            @Test
            @DisplayName("Should filter by wildcard StudyInstanceUID pattern")
            void testFindStudies_WildcardStudyInstanceUID()
            {
                // Given
                Attributes keys = new Attributes();
                keys.setString(Tag.StudyInstanceUID, VR.UI, "1.2.3.4.5.6.7.8.*");
                when(studyRepository.findAll()).thenReturn(allStudies);

                // When
                List<StudyEty> result = patientStudyFinder.findStudies(keys);

                // Then
                assertNotNull(result);
                assertEquals(3, result.size());
            }
        }

        @Nested
        @DisplayName("StudyDate Search Tests")
        class StudyDateSearchTests
        {
            @Test
            @DisplayName("Should filter by exact StudyDate")
            void testFindStudies_ExactStudyDate()
            {
                // Given
                Attributes keys = new Attributes();
                keys.setDate(Tag.StudyDate, VR.DA, java.sql.Date.valueOf(LocalDate.of(2023, 6, 15)));
                when(studyRepository.findAll()).thenReturn(allStudies);

                // When
                List<StudyEty> result = patientStudyFinder.findStudies(keys);

                // Then
                assertNotNull(result);
                assertEquals(1, result.size());
                assertEquals(LocalDate.of(2023, 6, 15), result.get(0).getStudyDate());
            }
        }

        @Nested
        @DisplayName("StudyDescription Search Tests")
        class StudyDescriptionSearchTests
        {
            @Test
            @DisplayName("Should filter by StudyDescription with wildcard")
            void testFindStudies_WildcardStudyDescription()
            {
                // Given
                Attributes keys = new Attributes();
                keys.setString(Tag.StudyDescription, VR.LO, "CT*");
                when(studyRepository.findAll()).thenReturn(allStudies);

                // When
                List<StudyEty> result = patientStudyFinder.findStudies(keys);

                // Then
                assertNotNull(result);
                assertEquals(1, result.size());
                assertTrue(result.get(0).getStudyDescription().startsWith("CT"));
            }
        }

        @Nested
        @DisplayName("AccessionNumber Search Tests")
        class AccessionNumberSearchTests
        {
            @Test
            @DisplayName("Should filter by exact AccessionNumber")
            void testFindStudies_ExactAccessionNumber()
            {
                // Given
                Attributes keys = new Attributes();
                keys.setString(Tag.AccessionNumber, VR.SH, "ACC002");
                when(studyRepository.findAll()).thenReturn(allStudies);

                // When
                List<StudyEty> result = patientStudyFinder.findStudies(keys);

                // Then
                assertNotNull(result);
                assertEquals(1, result.size());
                assertEquals("ACC002", result.get(0).getAccessionNumber());
            }
        }

        @Nested
        @DisplayName("ModalitiesInStudy Search Tests")
        class ModalitiesInStudySearchTests
        {
            @Test
            @DisplayName("Should filter by ModalitiesInStudy")
            void testFindStudies_ModalitiesInStudy()
            {
                // Given
                Attributes keys = new Attributes();
                keys.setString(Tag.ModalitiesInStudy, VR.CS, "CT");
                when(studyRepository.findAll()).thenReturn(allStudies);

                // When
                List<StudyEty> result = patientStudyFinder.findStudies(keys);

                // Then
                assertNotNull(result);
                assertEquals(1, result.size());
                assertTrue(result.get(0).getModalitiesInStudy().contains("CT"));
            }
        }

        @Nested
        @DisplayName("ReferringPhysicianName Search Tests")
        class ReferringPhysicianNameSearchTests
        {
            @Test
            @DisplayName("Should filter by ReferringPhysicianName with wildcard")
            void testFindStudies_ReferringPhysicianNameWildcard()
            {
                // Given
                Attributes keys = new Attributes();
                keys.setString(Tag.ReferringPhysicianName, VR.PN, "Dr. Smith*");
                when(studyRepository.findAll()).thenReturn(allStudies);

                // When
                List<StudyEty> result = patientStudyFinder.findStudies(keys);

                // Then
                assertNotNull(result);
                assertEquals(2, result.size()); // study1 and study3 have Dr. Smith
            }
        }

        @Nested
        @DisplayName("Multiple Study Filters Tests")
        class MultipleStudyFiltersTests
        {
            @Test
            @DisplayName("Should filter by StudyDate and ModalitiesInStudy")
            void testFindStudies_DateAndModality()
            {
                // Given
                Attributes keys = new Attributes();
                keys.setDate(Tag.StudyDate, VR.DA, java.sql.Date.valueOf(LocalDate.of(2023, 6, 15)));
                keys.setString(Tag.ModalitiesInStudy, VR.CS, "CT");
                when(studyRepository.findAll()).thenReturn(allStudies);

                // When
                List<StudyEty> result = patientStudyFinder.findStudies(keys);

                // Then
                assertNotNull(result);
                assertEquals(1, result.size());
                assertEquals("CT Chest", result.get(0).getStudyDescription());
            }
        }
    }

    // ==================== SERIES LEVEL TESTS ====================

    @Nested
    @DisplayName("findSeries Tests")
    class FindSeriesTests
    {
        @Nested
        @DisplayName("Empty Query Tests")
        class SeriesEmptyQueryTests
        {
            @Test
            @DisplayName("Should return all series when no search keys provided")
            void testFindSeries_EmptyKeys_ReturnsAllSeries()
            {
                // Given
                Attributes keys = new Attributes();
                when(seriesRepository.findAll()).thenReturn(allSeries);

                // When
                List<SeriesEty> result = patientStudyFinder.findSeries(keys);

                // Then
                assertNotNull(result);
                assertEquals(3, result.size());
            }
        }

        @Nested
        @DisplayName("StudyInstanceUID Filter Tests")
        class SeriesStudyInstanceUIDTests
        {
            @Test
            @DisplayName("Should filter series by StudyInstanceUID")
            void testFindSeries_ByStudyInstanceUID()
            {
                // Given
                Attributes keys = new Attributes();
                keys.setString(Tag.StudyInstanceUID, VR.UI, "1.2.3.4.5.6.7.8.1");
                when(studyRepository.findByStudyInstanceUID("1.2.3.4.5.6.7.8.1")).thenReturn(study1);
                when(seriesRepository.findByStudyId(anyLong())).thenReturn(Arrays.asList(series1, series2));

                // When
                List<SeriesEty> result = patientStudyFinder.findSeries(keys);

                // Then
                assertNotNull(result);
                assertEquals(2, result.size());
            }
        }

        @Nested
        @DisplayName("Modality Search Tests")
        class ModalitySearchTests
        {
            @Test
            @DisplayName("Should filter by exact Modality")
            void testFindSeries_ExactModality()
            {
                // Given
                Attributes keys = new Attributes();
                keys.setString(Tag.Modality, VR.CS, "CT");
                when(seriesRepository.findAll()).thenReturn(allSeries);

                // When
                List<SeriesEty> result = patientStudyFinder.findSeries(keys);

                // Then
                assertNotNull(result);
                assertEquals(2, result.size()); // series1 and series2 are CT
                assertTrue(result.stream().allMatch(s -> "CT".equals(s.getModality())));
            }

            @Test
            @DisplayName("Should filter by MR Modality")
            void testFindSeries_MRModality()
            {
                // Given
                Attributes keys = new Attributes();
                keys.setString(Tag.Modality, VR.CS, "MR");
                when(seriesRepository.findAll()).thenReturn(allSeries);

                // When
                List<SeriesEty> result = patientStudyFinder.findSeries(keys);

                // Then
                assertNotNull(result);
                assertEquals(1, result.size());
                assertEquals("MR", result.get(0).getModality());
            }
        }

        @Nested
        @DisplayName("SeriesNumber Search Tests")
        class SeriesNumberSearchTests
        {
            @Test
            @DisplayName("Should filter by SeriesNumber")
            void testFindSeries_SeriesNumber()
            {
                // Given
                Attributes keys = new Attributes();
                keys.setInt(Tag.SeriesNumber, VR.IS, 1);
                when(seriesRepository.findAll()).thenReturn(allSeries);

                // When
                List<SeriesEty> result = patientStudyFinder.findSeries(keys);

                // Then
                assertNotNull(result);
                assertEquals(2, result.size()); // series1 and series3 have SeriesNumber=1
            }
        }

        @Nested
        @DisplayName("SeriesDescription Search Tests")
        class SeriesDescriptionSearchTests
        {
            @Test
            @DisplayName("Should filter by SeriesDescription with wildcard")
            void testFindSeries_WildcardSeriesDescription()
            {
                // Given
                Attributes keys = new Attributes();
                keys.setString(Tag.SeriesDescription, VR.LO, "*Weighted");
                when(seriesRepository.findAll()).thenReturn(allSeries);

                // When
                List<SeriesEty> result = patientStudyFinder.findSeries(keys);

                // Then
                assertNotNull(result);
                assertEquals(1, result.size());
                assertTrue(result.get(0).getSeriesDescription().endsWith("Weighted"));
            }
        }

        @Nested
        @DisplayName("BodyPartExamined Search Tests")
        class BodyPartExaminedSearchTests
        {
            @Test
            @DisplayName("Should filter by BodyPartExamined")
            void testFindSeries_BodyPartExamined()
            {
                // Given
                Attributes keys = new Attributes();
                keys.setString(Tag.BodyPartExamined, VR.CS, "CHEST");
                when(seriesRepository.findAll()).thenReturn(allSeries);

                // When
                List<SeriesEty> result = patientStudyFinder.findSeries(keys);

                // Then
                assertNotNull(result);
                assertEquals(2, result.size()); // series1 and series2 are CHEST
            }
        }

        @Nested
        @DisplayName("Multiple Series Filters Tests")
        class MultipleSeriesFiltersTests
        {
            @Test
            @DisplayName("Should filter by Modality and BodyPartExamined")
            void testFindSeries_ModalityAndBodyPart()
            {
                // Given
                Attributes keys = new Attributes();
                keys.setString(Tag.Modality, VR.CS, "CT");
                keys.setString(Tag.BodyPartExamined, VR.CS, "CHEST");
                when(seriesRepository.findAll()).thenReturn(allSeries);

                // When
                List<SeriesEty> result = patientStudyFinder.findSeries(keys);

                // Then
                assertNotNull(result);
                assertEquals(2, result.size());
            }
        }
    }

    // ==================== IMAGE/INSTANCE LEVEL TESTS ====================

    @Nested
    @DisplayName("findInstances Tests")
    class FindInstancesTests
    {
        @Nested
        @DisplayName("Empty Query Tests")
        class InstanceEmptyQueryTests
        {
            @Test
            @DisplayName("Should return all instances when no search keys provided")
            void testFindInstances_EmptyKeys_ReturnsAllInstances()
            {
                // Given
                Attributes keys = new Attributes();
                when(instanceRepository.findAll()).thenReturn(allInstances);

                // When
                List<InstanceEty> result = patientStudyFinder.findInstances(keys);

                // Then
                assertNotNull(result);
                assertEquals(3, result.size());
            }
        }

        @Nested
        @DisplayName("SeriesInstanceUID Filter Tests")
        class InstanceSeriesInstanceUIDTests
        {
            @Test
            @DisplayName("Should filter instances by SeriesInstanceUID")
            void testFindInstances_BySeriesInstanceUID()
            {
                // Given
                Attributes keys = new Attributes();
                keys.setString(Tag.SeriesInstanceUID, VR.UI, "1.2.3.4.5.6.7.8.1.1");
                when(seriesRepository.findBySeriesInstanceUID("1.2.3.4.5.6.7.8.1.1")).thenReturn(series1);
                when(instanceRepository.findBySeriesId(anyLong())).thenReturn(Arrays.asList(instance1, instance2));

                // When
                List<InstanceEty> result = patientStudyFinder.findInstances(keys);

                // Then
                assertNotNull(result);
                assertEquals(2, result.size());
            }
        }

        @Nested
        @DisplayName("SOPInstanceUID Search Tests")
        class SOPInstanceUIDSearchTests
        {
            @Test
            @DisplayName("Should filter by exact SOPInstanceUID")
            void testFindInstances_ExactSOPInstanceUID()
            {
                // Given
                Attributes keys = new Attributes();
                keys.setString(Tag.SOPInstanceUID, VR.UI, "1.2.3.4.5.6.7.8.1.1.1");
                when(instanceRepository.findAll()).thenReturn(allInstances);

                // When
                List<InstanceEty> result = patientStudyFinder.findInstances(keys);

                // Then
                assertNotNull(result);
                assertEquals(1, result.size());
                assertEquals("1.2.3.4.5.6.7.8.1.1.1", result.get(0).getInstanceUID());
            }

            @Test
            @DisplayName("Should filter by wildcard SOPInstanceUID pattern")
            void testFindInstances_WildcardSOPInstanceUID()
            {
                // Given
                Attributes keys = new Attributes();
                keys.setString(Tag.SOPInstanceUID, VR.UI, "1.2.3.4.5.6.7.8.1.1.*");
                when(instanceRepository.findAll()).thenReturn(allInstances);

                // When
                List<InstanceEty> result = patientStudyFinder.findInstances(keys);

                // Then
                assertNotNull(result);
                assertEquals(2, result.size()); // instance1 and instance2
            }
        }

        @Nested
        @DisplayName("InstanceNumber Search Tests")
        class InstanceNumberSearchTests
        {
            @Test
            @DisplayName("Should filter by InstanceNumber")
            void testFindInstances_InstanceNumber()
            {
                // Given
                Attributes keys = new Attributes();
                keys.setInt(Tag.InstanceNumber, VR.IS, 1);
                when(instanceRepository.findAll()).thenReturn(allInstances);

                // When
                List<InstanceEty> result = patientStudyFinder.findInstances(keys);

                // Then
                assertNotNull(result);
                assertEquals(2, result.size()); // instance1 and instance3 have InstanceNumber=1
            }
        }

        @Nested
        @DisplayName("ContentDate Search Tests")
        class ContentDateSearchTests
        {
            @Test
            @DisplayName("Should filter by ContentDate")
            void testFindInstances_ContentDate()
            {
                // Given
                Attributes keys = new Attributes();
                keys.setDate(Tag.ContentDate, VR.DA, java.sql.Date.valueOf(LocalDate.of(2023, 6, 15)));
                when(instanceRepository.findAll()).thenReturn(allInstances);

                // When
                List<InstanceEty> result = patientStudyFinder.findInstances(keys);

                // Then
                assertNotNull(result);
                assertEquals(2, result.size()); // instance1 and instance2
            }
        }

        @Nested
        @DisplayName("ImageType Search Tests")
        class ImageTypeSearchTests
        {
            @Test
            @DisplayName("Should filter by ImageType containing value")
            void testFindInstances_ImageType()
            {
                // Given
                Attributes keys = new Attributes();
                keys.setString(Tag.ImageType, VR.CS, "AXIAL");
                when(instanceRepository.findAll()).thenReturn(allInstances);

                // When
                List<InstanceEty> result = patientStudyFinder.findInstances(keys);

                // Then
                assertNotNull(result);
                assertEquals(2, result.size()); // instance1 and instance2 contain AXIAL
            }
        }

        @Nested
        @DisplayName("Rows and Columns Search Tests")
        class RowsColumnsSearchTests
        {
            @Test
            @DisplayName("Should filter by Rows")
            void testFindInstances_Rows()
            {
                // Given
                Attributes keys = new Attributes();
                keys.setInt(Tag.Rows, VR.US, 512);
                when(instanceRepository.findAll()).thenReturn(allInstances);

                // When
                List<InstanceEty> result = patientStudyFinder.findInstances(keys);

                // Then
                assertNotNull(result);
                assertEquals(2, result.size()); // instance1 and instance2 have 512 rows
            }

            @Test
            @DisplayName("Should filter by Columns")
            void testFindInstances_Columns()
            {
                // Given
                Attributes keys = new Attributes();
                keys.setInt(Tag.Columns, VR.US, 256);
                when(instanceRepository.findAll()).thenReturn(allInstances);

                // When
                List<InstanceEty> result = patientStudyFinder.findInstances(keys);

                // Then
                assertNotNull(result);
                assertEquals(1, result.size()); // only instance3 has 256 columns
            }
        }

        @Nested
        @DisplayName("Multiple Instance Filters Tests")
        class MultipleInstanceFiltersTests
        {
            @Test
            @DisplayName("Should filter by ContentDate and Rows")
            void testFindInstances_DateAndRows()
            {
                // Given
                Attributes keys = new Attributes();
                keys.setDate(Tag.ContentDate, VR.DA, java.sql.Date.valueOf(LocalDate.of(2023, 6, 15)));
                keys.setInt(Tag.Rows, VR.US, 512);
                when(instanceRepository.findAll()).thenReturn(allInstances);

                // When
                List<InstanceEty> result = patientStudyFinder.findInstances(keys);

                // Then
                assertNotNull(result);
                assertEquals(2, result.size());
            }

            @Test
            @DisplayName("Should return empty when combined filters don't match")
            void testFindInstances_NoMatch()
            {
                // Given
                Attributes keys = new Attributes();
                keys.setDate(Tag.ContentDate, VR.DA, java.sql.Date.valueOf(LocalDate.of(2023, 7, 20)));
                keys.setInt(Tag.Rows, VR.US, 512);
                when(instanceRepository.findAll()).thenReturn(allInstances);

                // When
                List<InstanceEty> result = patientStudyFinder.findInstances(keys);

                // Then
                assertNotNull(result);
                assertTrue(result.isEmpty()); // instance3 has 2023-07-20 but 256 rows, not 512
            }
        }
    }
}
