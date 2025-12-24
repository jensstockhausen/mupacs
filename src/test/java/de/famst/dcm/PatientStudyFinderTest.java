package de.famst.dcm;

import de.famst.data.InstanceRepository;
import de.famst.data.PatientEty;
import de.famst.data.PatientRepository;
import de.famst.data.SeriesRepository;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for PatientStudyFinder.findPatients() method.
 * Tests all DICOM patient-level search key filtering scenarios.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PatientStudyFinder.findPatients Tests")
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

    private PatientEty patient1;
    private PatientEty patient2;
    private PatientEty patient3;
    private List<PatientEty> allPatients;

    @BeforeEach
    void setUp()
    {
        patientStudyFinder = new PatientStudyFinder(
            patientRepository,
            studyRepository,
            seriesRepository,
            instanceRepository
        );

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
        patient1.setPregnancyStatus(4); // Not applicable
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
        patient2.setPregnancyStatus(1); // Not pregnant
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
        patient3.setPregnancyStatus(4); // Not applicable
        patient3.setResponsiblePerson("Mary Johnson");
        patient3.setResponsibleOrganization("General Hospital");

        allPatients = new ArrayList<>(Arrays.asList(patient1, patient2, patient3));
    }

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
}

