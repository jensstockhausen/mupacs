package de.famst.service;

import de.famst.data.InstanceEty;
import de.famst.data.InstanceRepository;
import de.famst.data.PatientEty;
import de.famst.data.PatientRepository;
import de.famst.data.SeriesEty;
import de.famst.data.SeriesRepository;
import de.famst.data.StudyEty;
import de.famst.data.StudyRepository;
import de.famst.dcm.DicomReader;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for DicomImportService.
 * Tests the DICOM import logic with mocked repositories.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DicomImportService Tests")
class DicomImportServiceTest
{
    @Mock
    private InstanceRepository instanceRepository;

    @Mock
    private SeriesRepository seriesRepository;

    @Mock
    private StudyRepository studyRepository;

    @Mock
    private PatientRepository patientRepository;

    private DicomReader dicomReader;

    private Attributes dcmAttributes;

    private DicomImportService dicomImportService;

    @TempDir
    private Path tempDir;
    private Path testPath;

    private static final String PATIENT_NAME = "Doe^John";
    private static final String PATIENT_ID = "12345";
    private static final String STUDY_UID = "1.2.840.113619.2.1.1";
    private static final String SERIES_UID = "1.2.840.113619.2.1.2";
    private static final String INSTANCE_UID = "1.2.840.113619.2.1.3";

    @BeforeEach
    void setUp() throws IOException
    {
        testPath = tempDir.resolve("import/instance.part");
        Files.createDirectories(testPath);

        dicomReader = new DicomReader();
        //dcmAttributes = org.mockito.Mockito.mock(Attributes.class);

        dicomImportService = new DicomImportService(instanceRepository, seriesRepository,
            studyRepository, patientRepository, dicomReader);

        ReflectionTestUtils.setField(dicomImportService,"mupacsArchive", tempDir.resolve("archive").toString());
    }

    @DisplayName("Should throw exception when DICOM attributes are null")
    @Test
    void testDicomToDatabase_WithNullAttributes_ThrowsException()
    {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> dicomImportService.dicomToDatabase(null, testPath)
        );

        assertEquals("DICOM attributes cannot be null", exception.getMessage());
    }

    @DisplayName("Should throw exception when file path is null")
    @Test
    void testDicomToDatabase_WithNullPath_ThrowsException()
    {
        // Given
        dcmAttributes = new Attributes();

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> dicomImportService.dicomToDatabase(dcmAttributes, null)
        );

        assertEquals("File path cannot be null", exception.getMessage());
    }

    @DisplayName("Should throw exception when Patient ID is missing")
    @Test
    void testDicomToDatabase_WithMissingPatientID_ThrowsException()
    {
        // Given
        dcmAttributes = new Attributes();
        dcmAttributes.setString(Tag.PatientID, VR.LO, (String) null);

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> dicomImportService.dicomToDatabase(dcmAttributes, testPath)
        );

        assertEquals("Patient ID is required but missing or empty", exception.getMessage());
    }

    @DisplayName("Should throw exception when Patient ID is empty")
    @Test
    void testDicomToDatabase_WithEmptyPatientID_ThrowsException()
    {
        // Given
        dcmAttributes = new Attributes();
        dcmAttributes.setString(Tag.PatientID, VR.LO, "   ");

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> dicomImportService.dicomToDatabase(dcmAttributes, testPath)
        );

        assertEquals("Patient ID is required but missing or empty", exception.getMessage());
    }

    @DisplayName("Should throw exception when Study Instance UID is missing")
    @Test
    void testDicomToDatabase_WithMissingStudyInstanceUID_ThrowsException()
    {
        // Given
        dcmAttributes = new Attributes();
        dcmAttributes.setString(Tag.PatientID, VR.LO, PATIENT_ID);
        dcmAttributes.setString(Tag.StudyInstanceUID, VR.PN, (String) null);

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> dicomImportService.dicomToDatabase(dcmAttributes, testPath)
        );

        assertEquals("Study Instance UID is required but missing or empty", exception.getMessage());
    }

    @DisplayName("Should throw exception when Series Instance UID is missing")
    @Test
    void testDicomToDatabase_WithMissingSeriesInstanceUID_ThrowsException()
    {
        // Given
        dcmAttributes = new Attributes();
        dcmAttributes.setString(Tag.PatientID, VR.LO, PATIENT_ID);
        dcmAttributes.setString(Tag.StudyInstanceUID, VR.PN, STUDY_UID);
        dcmAttributes.setString(Tag.SeriesInstanceUID, VR.PN, (String) null);

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> dicomImportService.dicomToDatabase(dcmAttributes, testPath)
        );

        assertEquals("Series Instance UID is required but missing or empty", exception.getMessage());
    }

    @DisplayName("Should throw exception when SOP Instance UID is missing")
    @Test
    void testDicomToDatabase_WithMissingSOPInstanceUID_ThrowsException()
    {
        // Given
        dcmAttributes = new Attributes();
        dcmAttributes.setString(Tag.PatientID, VR.LO, PATIENT_ID);
        dcmAttributes.setString(Tag.StudyInstanceUID, VR.PN, STUDY_UID);
        dcmAttributes.setString(Tag.SeriesInstanceUID, VR.PN, SERIES_UID);
        dcmAttributes.setString(Tag.SOPInstanceUID, VR.PN, (String) null);


        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> dicomImportService.dicomToDatabase(dcmAttributes, testPath)
        );

        assertEquals("SOP Instance UID is required but missing or empty", exception.getMessage());
    }

    @DisplayName("Should create all entities when data is completely new")
    @Test
    void testDicomToDatabase_WithCompletelyNewData_CreatesAllEntities()
    {
        // Given
        dcmAttributes = setupValidDicomAttributes();

        PatientEty patient = new PatientEty(PATIENT_NAME, PATIENT_ID);
        StudyEty study = new StudyEty(STUDY_UID);
        SeriesEty series = new SeriesEty(SERIES_UID);
        InstanceEty instance = new InstanceEty(INSTANCE_UID, testPath.toString());

        when(patientRepository.findByPatientId(PATIENT_ID)).thenReturn(null);
        when(studyRepository.findByStudyInstanceUID(STUDY_UID)).thenReturn(null);
        when(seriesRepository.findBySeriesInstanceUID(SERIES_UID)).thenReturn(null);
        when(instanceRepository.findByInstanceUID(INSTANCE_UID)).thenReturn(null);

        when(patientRepository.save(any(PatientEty.class))).thenReturn(patient);
        when(studyRepository.save(any(StudyEty.class))).thenReturn(study);
        when(seriesRepository.save(any(SeriesEty.class))).thenReturn(series);
        when(instanceRepository.save(any(InstanceEty.class))).thenReturn(instance);

        // When
        dicomImportService.dicomToDatabase(dcmAttributes, testPath);

        // Then
        verify(patientRepository).findByPatientId(PATIENT_ID);
        verify(patientRepository).save(any(PatientEty.class));

        verify(studyRepository).findByStudyInstanceUID(STUDY_UID);
        verify(studyRepository).save(any(StudyEty.class));

        verify(seriesRepository).findBySeriesInstanceUID(SERIES_UID);
        verify(seriesRepository).save(any(SeriesEty.class));

        verify(instanceRepository).findByInstanceUID(INSTANCE_UID);
        verify(instanceRepository).save(any(InstanceEty.class));
    }

    @DisplayName("Should reuse existing patient when patient already exists")
    @Test
    void testDicomToDatabase_WithExistingPatient_ReusesPatient()
    {
        // Given
        dcmAttributes = setupValidDicomAttributes();

        PatientEty existingPatient = new PatientEty(PATIENT_NAME, PATIENT_ID);
        StudyEty study = new StudyEty(STUDY_UID);
        SeriesEty series = new SeriesEty(SERIES_UID);
        InstanceEty instance = new InstanceEty(INSTANCE_UID, testPath.toString());

        when(patientRepository.findByPatientId(PATIENT_ID)).thenReturn(existingPatient);
        when(studyRepository.findByStudyInstanceUID(STUDY_UID)).thenReturn(null);
        when(seriesRepository.findBySeriesInstanceUID(SERIES_UID)).thenReturn(null);
        when(instanceRepository.findByInstanceUID(INSTANCE_UID)).thenReturn(null);

        when(studyRepository.save(any(StudyEty.class))).thenReturn(study);
        when(seriesRepository.save(any(SeriesEty.class))).thenReturn(series);
        when(instanceRepository.save(any(InstanceEty.class))).thenReturn(instance);

        // When
        dicomImportService.dicomToDatabase(dcmAttributes, testPath);

        // Then
        verify(patientRepository).findByPatientId(PATIENT_ID);
        verify(patientRepository, never()).save(any(PatientEty.class));
        verify(studyRepository).save(any(StudyEty.class));
        verify(seriesRepository).save(any(SeriesEty.class));
        verify(instanceRepository).save(any(InstanceEty.class));
    }

    @DisplayName("Should reuse existing study when study already exists")
    @Test
    void testDicomToDatabase_WithExistingStudy_ReusesStudy()
    {
        // Given
        dcmAttributes = setupValidDicomAttributes();

        PatientEty patient = new PatientEty(PATIENT_NAME, PATIENT_ID);
        StudyEty existingStudy = new StudyEty(STUDY_UID);
        SeriesEty series = new SeriesEty(SERIES_UID);
        InstanceEty instance = new InstanceEty(INSTANCE_UID, testPath.toString());

        when(patientRepository.findByPatientId(PATIENT_ID)).thenReturn(patient);
        when(studyRepository.findByStudyInstanceUID(STUDY_UID)).thenReturn(existingStudy);
        when(seriesRepository.findBySeriesInstanceUID(SERIES_UID)).thenReturn(null);
        when(instanceRepository.findByInstanceUID(INSTANCE_UID)).thenReturn(null);


        when(seriesRepository.save(any(SeriesEty.class))).thenReturn(series);
        when(instanceRepository.save(any(InstanceEty.class))).thenReturn(instance);

        // When
        dicomImportService.dicomToDatabase(dcmAttributes, testPath);

        // Then
        verify(studyRepository).findByStudyInstanceUID(STUDY_UID);
        verify(studyRepository, never()).save(any(StudyEty.class));
        verify(seriesRepository).save(any(SeriesEty.class));
        verify(instanceRepository).save(any(InstanceEty.class));
    }

    @DisplayName("Should reuse existing series when series already exists")
    @Test
    void testDicomToDatabase_WithExistingSeries_ReusesSeries()
    {
        // Given
        dcmAttributes = setupValidDicomAttributes();

        PatientEty patient = new PatientEty(PATIENT_NAME, PATIENT_ID);
        StudyEty study = new StudyEty(STUDY_UID);
        SeriesEty existingSeries = new SeriesEty(SERIES_UID);
        InstanceEty instance = new InstanceEty(INSTANCE_UID, testPath.toString());

        when(patientRepository.findByPatientId(PATIENT_ID)).thenReturn(patient);
        when(studyRepository.findByStudyInstanceUID(STUDY_UID)).thenReturn(study);
        when(seriesRepository.findBySeriesInstanceUID(SERIES_UID)).thenReturn(existingSeries);
        when(instanceRepository.findByInstanceUID(INSTANCE_UID)).thenReturn(null);

        when(instanceRepository.save(any(InstanceEty.class))).thenReturn(instance);

        // When
        dicomImportService.dicomToDatabase(dcmAttributes, testPath);

        // Then
        verify(seriesRepository).findBySeriesInstanceUID(SERIES_UID);
        verify(seriesRepository, never()).save(any(SeriesEty.class));
        verify(instanceRepository).save(any(InstanceEty.class));
    }

    @DisplayName("Should skip instance creation when instance already exists")
    @Test
    void testDicomToDatabase_WithExistingInstance_SkipsInstanceCreation()
    {
        // Given
        dcmAttributes = setupValidDicomAttributes();

        PatientEty patient = new PatientEty(PATIENT_NAME, PATIENT_ID);
        StudyEty study = new StudyEty(STUDY_UID);
        SeriesEty series = new SeriesEty(SERIES_UID);
        InstanceEty existingInstance = new InstanceEty(INSTANCE_UID, testPath.toString());

        when(patientRepository.findByPatientId(PATIENT_ID)).thenReturn(patient);
        when(studyRepository.findByStudyInstanceUID(STUDY_UID)).thenReturn(study);
        when(seriesRepository.findBySeriesInstanceUID(SERIES_UID)).thenReturn(series);
        when(instanceRepository.findByInstanceUID(INSTANCE_UID)).thenReturn(existingInstance);

        // When
        dicomImportService.dicomToDatabase(dcmAttributes, testPath);

        // Then
        verify(instanceRepository).findByInstanceUID(INSTANCE_UID);
        verify(instanceRepository, never()).save(any(InstanceEty.class));
    }

    @DisplayName("Should not create any entities when all already exist")
    @Test
    void testDicomToDatabase_WithAllExistingEntities_NoEntitiesCreated()
    {
        // Given
        dcmAttributes = setupValidDicomAttributes();

        PatientEty existingPatient = new PatientEty(PATIENT_NAME, PATIENT_ID);
        StudyEty existingStudy = new StudyEty(STUDY_UID);
        SeriesEty existingSeries = new SeriesEty(SERIES_UID);
        InstanceEty existingInstance = new InstanceEty(INSTANCE_UID, testPath.toString());

        when(patientRepository.findByPatientId(PATIENT_ID)).thenReturn(existingPatient);
        when(studyRepository.findByStudyInstanceUID(STUDY_UID)).thenReturn(existingStudy);
        when(seriesRepository.findBySeriesInstanceUID(SERIES_UID)).thenReturn(existingSeries);
        when(instanceRepository.findByInstanceUID(INSTANCE_UID)).thenReturn(existingInstance);

        // When
        dicomImportService.dicomToDatabase(dcmAttributes, testPath);

        // Then
        verify(patientRepository, never()).save(any(PatientEty.class));
        verify(studyRepository, never()).save(any(StudyEty.class));
        verify(seriesRepository, never()).save(any(SeriesEty.class));
        verify(instanceRepository, never()).save(any(InstanceEty.class));
    }

    @DisplayName("Should establish bidirectional relationships between entities")
    @Test
    void testDicomToDatabase_VerifiesBidirectionalRelationships()
    {
        // Given
        dcmAttributes = setupValidDicomAttributes();

        PatientEty patient = new PatientEty(PATIENT_NAME, PATIENT_ID);
        StudyEty study = new StudyEty(STUDY_UID);
        SeriesEty series = new SeriesEty(SERIES_UID);
        InstanceEty instance = new InstanceEty(INSTANCE_UID, testPath.toString());

        when(patientRepository.findByPatientId(PATIENT_ID)).thenReturn(null);
        when(studyRepository.findByStudyInstanceUID(STUDY_UID)).thenReturn(null);
        when(seriesRepository.findBySeriesInstanceUID(SERIES_UID)).thenReturn(null);
        when(instanceRepository.findByInstanceUID(INSTANCE_UID)).thenReturn(null);

        when(patientRepository.save(any(PatientEty.class))).thenReturn(patient);
        when(studyRepository.save(any(StudyEty.class))).thenReturn(study);
        when(seriesRepository.save(any(SeriesEty.class))).thenReturn(series);
        when(instanceRepository.save(any(InstanceEty.class))).thenReturn(instance);

        // When
        dicomImportService.dicomToDatabase(dcmAttributes, testPath);

        // Then - verify relationships are set
        assertNotNull(study.getPatient());
        assertEquals(patient, study.getPatient());

        assertNotNull(series.getStudy());
        assertEquals(study, series.getStudy());

        assertNotNull(instance.getSeries());
        assertEquals(series, instance.getSeries());
        assertEquals(testPath.toAbsolutePath().toString(), instance.getPath());
    }


    @DisplayName("Should only save new instance when multiple instances added")
    @Test
    void testDicomToDatabase_MultipleInstances_OnlySavesNewInstance()
    {
        // Given - First import
        dcmAttributes = setupValidDicomAttributes();

        PatientEty patient = new PatientEty(PATIENT_NAME, PATIENT_ID);
        StudyEty study = new StudyEty(STUDY_UID);
        SeriesEty series = new SeriesEty(SERIES_UID);
        InstanceEty instance1 = new InstanceEty(INSTANCE_UID, testPath.toString());

        when(patientRepository.findByPatientId(PATIENT_ID)).thenReturn(null);
        when(studyRepository.findByStudyInstanceUID(STUDY_UID)).thenReturn(null);
        when(seriesRepository.findBySeriesInstanceUID(SERIES_UID)).thenReturn(null);
        when(instanceRepository.findByInstanceUID(INSTANCE_UID)).thenReturn(null);

        when(patientRepository.save(any(PatientEty.class))).thenReturn(patient);
        when(studyRepository.save(any(StudyEty.class))).thenReturn(study);
        when(seriesRepository.save(any(SeriesEty.class))).thenReturn(series);
        when(instanceRepository.save(any(InstanceEty.class))).thenReturn(instance1);

        // When - First import
        dicomImportService.dicomToDatabase(dcmAttributes, testPath);

        // Then
        verify(instanceRepository, times(1)).save(any(InstanceEty.class));

        // Given - Second import with same instance
        when(instanceRepository.findByInstanceUID(INSTANCE_UID)).thenReturn(instance1);
        when(patientRepository.findByPatientId(PATIENT_ID)).thenReturn(patient);
        when(studyRepository.findByStudyInstanceUID(STUDY_UID)).thenReturn(study);
        when(seriesRepository.findBySeriesInstanceUID(SERIES_UID)).thenReturn(series);

        // When - Second import (duplicate)
        dicomImportService.dicomToDatabase(dcmAttributes, testPath);

        // Then - should still be 1 save (not 2)
        verify(instanceRepository, times(1)).save(any(InstanceEty.class));
    }

    /**
     * Helper method to setup valid DICOM attributes for testing.
     */
    private Attributes setupValidDicomAttributes()
    {
        // Given
        dcmAttributes = new Attributes();
        dcmAttributes.setString(Tag.PatientName, VR.PN, PATIENT_NAME);
        dcmAttributes.setString(Tag.PatientID, VR.LO, PATIENT_ID);
        dcmAttributes.setString(Tag.StudyInstanceUID, VR.PN, STUDY_UID);
        dcmAttributes.setString(Tag.SeriesInstanceUID, VR.PN, SERIES_UID);
        dcmAttributes.setString(Tag.SOPInstanceUID, VR.PN, INSTANCE_UID);

        return dcmAttributes;
    }
}
