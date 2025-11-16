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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.nio.file.Paths;

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

    @Mock
    private DicomReader dicomReader;

    @Mock
    private Attributes dcmAttributes;

    @InjectMocks
    private DicomImportService dicomImportService;

    private Path testPath;
    private static final String PATIENT_NAME = "Doe^John";
    private static final String PATIENT_ID = "12345";
    private static final String STUDY_UID = "1.2.840.113619.2.1.1";
    private static final String SERIES_UID = "1.2.840.113619.2.1.2";
    private static final String INSTANCE_UID = "1.2.840.113619.2.1.3";

    @BeforeEach
    void setUp()
    {
        testPath = Paths.get("/test/path/image.dcm");
    }

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

    @Test
    void testDicomToDatabase_WithNullPath_ThrowsException()
    {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> dicomImportService.dicomToDatabase(dcmAttributes, null)
        );

        assertEquals("File path cannot be null", exception.getMessage());
    }

    @Test
    void testDicomToDatabase_WithMissingPatientName_ThrowsException()
    {
        // Given
        when(dcmAttributes.getString(Tag.PatientName)).thenReturn(null);

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> dicomImportService.dicomToDatabase(dcmAttributes, testPath)
        );

        assertEquals("Patient Name is required but missing or empty", exception.getMessage());
    }

    @Test
    void testDicomToDatabase_WithEmptyPatientName_ThrowsException()
    {
        // Given
        when(dcmAttributes.getString(Tag.PatientName)).thenReturn("  ");

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> dicomImportService.dicomToDatabase(dcmAttributes, testPath)
        );

        assertEquals("Patient Name is required but missing or empty", exception.getMessage());
    }

    @Test
    void testDicomToDatabase_WithMissingStudyInstanceUID_ThrowsException()
    {
        // Given
        when(dcmAttributes.getString(Tag.PatientName)).thenReturn(PATIENT_NAME);
        when(dcmAttributes.getString(Tag.StudyInstanceUID)).thenReturn(null);

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> dicomImportService.dicomToDatabase(dcmAttributes, testPath)
        );

        assertEquals("Study Instance UID is required but missing or empty", exception.getMessage());
    }

    @Test
    void testDicomToDatabase_WithMissingSeriesInstanceUID_ThrowsException()
    {
        // Given
        when(dcmAttributes.getString(Tag.PatientName)).thenReturn(PATIENT_NAME);
        when(dcmAttributes.getString(Tag.StudyInstanceUID)).thenReturn(STUDY_UID);
        when(dcmAttributes.getString(Tag.SeriesInstanceUID)).thenReturn(null);

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> dicomImportService.dicomToDatabase(dcmAttributes, testPath)
        );

        assertEquals("Series Instance UID is required but missing or empty", exception.getMessage());
    }

    @Test
    void testDicomToDatabase_WithMissingSOPInstanceUID_ThrowsException()
    {
        // Given
        when(dcmAttributes.getString(Tag.PatientName)).thenReturn(PATIENT_NAME);
        when(dcmAttributes.getString(Tag.StudyInstanceUID)).thenReturn(STUDY_UID);
        when(dcmAttributes.getString(Tag.SeriesInstanceUID)).thenReturn(SERIES_UID);
        when(dcmAttributes.getString(Tag.SOPInstanceUID)).thenReturn(null);

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> dicomImportService.dicomToDatabase(dcmAttributes, testPath)
        );

        assertEquals("SOP Instance UID is required but missing or empty", exception.getMessage());
    }

    @Test
    void testDicomToDatabase_WithCompletelyNewData_CreatesAllEntities()
    {
        // Given
        setupValidDicomAttributes();

        PatientEty patient = new PatientEty(PATIENT_NAME, PATIENT_ID);
        StudyEty study = new StudyEty(STUDY_UID);
        SeriesEty series = new SeriesEty(SERIES_UID);
        InstanceEty instance = new InstanceEty(INSTANCE_UID, testPath.toString());

        when(patientRepository.findByPatientName(PATIENT_NAME)).thenReturn(null);
        when(studyRepository.findByStudyInstanceUID(STUDY_UID)).thenReturn(null);
        when(seriesRepository.findBySeriesInstanceUID(SERIES_UID)).thenReturn(null);
        when(instanceRepository.findByInstanceUID(INSTANCE_UID)).thenReturn(null);

        when(dicomReader.readPatient(dcmAttributes)).thenReturn(patient);
        when(dicomReader.readStudy(dcmAttributes)).thenReturn(study);
        when(dicomReader.readSeries(dcmAttributes)).thenReturn(series);
        when(dicomReader.readInstance(dcmAttributes)).thenReturn(instance);

        when(patientRepository.save(any(PatientEty.class))).thenReturn(patient);
        when(studyRepository.save(any(StudyEty.class))).thenReturn(study);
        when(seriesRepository.save(any(SeriesEty.class))).thenReturn(series);
        when(instanceRepository.save(any(InstanceEty.class))).thenReturn(instance);

        // When
        dicomImportService.dicomToDatabase(dcmAttributes, testPath);

        // Then
        verify(patientRepository).findByPatientName(PATIENT_NAME);
        verify(patientRepository).save(any(PatientEty.class));

        verify(studyRepository).findByStudyInstanceUID(STUDY_UID);
        verify(studyRepository).save(any(StudyEty.class));

        verify(seriesRepository).findBySeriesInstanceUID(SERIES_UID);
        verify(seriesRepository).save(any(SeriesEty.class));

        verify(instanceRepository).findByInstanceUID(INSTANCE_UID);
        verify(instanceRepository).save(any(InstanceEty.class));

        verify(dicomReader).readPatient(dcmAttributes);
        verify(dicomReader).readStudy(dcmAttributes);
        verify(dicomReader).readSeries(dcmAttributes);
        verify(dicomReader).readInstance(dcmAttributes);
    }

    @Test
    void testDicomToDatabase_WithExistingPatient_ReusesPatient()
    {
        // Given
        setupValidDicomAttributes();

        PatientEty existingPatient = new PatientEty(PATIENT_NAME, PATIENT_ID);
        StudyEty study = new StudyEty(STUDY_UID);
        SeriesEty series = new SeriesEty(SERIES_UID);
        InstanceEty instance = new InstanceEty(INSTANCE_UID, testPath.toString());

        when(patientRepository.findByPatientName(PATIENT_NAME)).thenReturn(existingPatient);
        when(studyRepository.findByStudyInstanceUID(STUDY_UID)).thenReturn(null);
        when(seriesRepository.findBySeriesInstanceUID(SERIES_UID)).thenReturn(null);
        when(instanceRepository.findByInstanceUID(INSTANCE_UID)).thenReturn(null);

        when(dicomReader.readStudy(dcmAttributes)).thenReturn(study);
        when(dicomReader.readSeries(dcmAttributes)).thenReturn(series);
        when(dicomReader.readInstance(dcmAttributes)).thenReturn(instance);

        when(studyRepository.save(any(StudyEty.class))).thenReturn(study);
        when(seriesRepository.save(any(SeriesEty.class))).thenReturn(series);
        when(instanceRepository.save(any(InstanceEty.class))).thenReturn(instance);

        // When
        dicomImportService.dicomToDatabase(dcmAttributes, testPath);

        // Then
        verify(patientRepository).findByPatientName(PATIENT_NAME);
        verify(patientRepository, never()).save(any(PatientEty.class));
        verify(dicomReader, never()).readPatient(any());

        verify(studyRepository).save(any(StudyEty.class));
        verify(seriesRepository).save(any(SeriesEty.class));
        verify(instanceRepository).save(any(InstanceEty.class));
    }

    @Test
    void testDicomToDatabase_WithExistingStudy_ReusesStudy()
    {
        // Given
        setupValidDicomAttributes();

        PatientEty patient = new PatientEty(PATIENT_NAME, PATIENT_ID);
        StudyEty existingStudy = new StudyEty(STUDY_UID);
        SeriesEty series = new SeriesEty(SERIES_UID);
        InstanceEty instance = new InstanceEty(INSTANCE_UID, testPath.toString());

        when(patientRepository.findByPatientName(PATIENT_NAME)).thenReturn(patient);
        when(studyRepository.findByStudyInstanceUID(STUDY_UID)).thenReturn(existingStudy);
        when(seriesRepository.findBySeriesInstanceUID(SERIES_UID)).thenReturn(null);
        when(instanceRepository.findByInstanceUID(INSTANCE_UID)).thenReturn(null);

        when(dicomReader.readSeries(dcmAttributes)).thenReturn(series);
        when(dicomReader.readInstance(dcmAttributes)).thenReturn(instance);

        when(seriesRepository.save(any(SeriesEty.class))).thenReturn(series);
        when(instanceRepository.save(any(InstanceEty.class))).thenReturn(instance);

        // When
        dicomImportService.dicomToDatabase(dcmAttributes, testPath);

        // Then
        verify(studyRepository).findByStudyInstanceUID(STUDY_UID);
        verify(studyRepository, never()).save(any(StudyEty.class));
        verify(dicomReader, never()).readStudy(any());

        verify(seriesRepository).save(any(SeriesEty.class));
        verify(instanceRepository).save(any(InstanceEty.class));
    }

    @Test
    void testDicomToDatabase_WithExistingSeries_ReusesSeries()
    {
        // Given
        setupValidDicomAttributes();

        PatientEty patient = new PatientEty(PATIENT_NAME, PATIENT_ID);
        StudyEty study = new StudyEty(STUDY_UID);
        SeriesEty existingSeries = new SeriesEty(SERIES_UID);
        InstanceEty instance = new InstanceEty(INSTANCE_UID, testPath.toString());

        when(patientRepository.findByPatientName(PATIENT_NAME)).thenReturn(patient);
        when(studyRepository.findByStudyInstanceUID(STUDY_UID)).thenReturn(study);
        when(seriesRepository.findBySeriesInstanceUID(SERIES_UID)).thenReturn(existingSeries);
        when(instanceRepository.findByInstanceUID(INSTANCE_UID)).thenReturn(null);

        when(dicomReader.readInstance(dcmAttributes)).thenReturn(instance);
        when(instanceRepository.save(any(InstanceEty.class))).thenReturn(instance);

        // When
        dicomImportService.dicomToDatabase(dcmAttributes, testPath);

        // Then
        verify(seriesRepository).findBySeriesInstanceUID(SERIES_UID);
        verify(seriesRepository, never()).save(any(SeriesEty.class));
        verify(dicomReader, never()).readSeries(any());

        verify(instanceRepository).save(any(InstanceEty.class));
    }

    @Test
    void testDicomToDatabase_WithExistingInstance_SkipsInstanceCreation()
    {
        // Given
        setupValidDicomAttributes();

        PatientEty patient = new PatientEty(PATIENT_NAME, PATIENT_ID);
        StudyEty study = new StudyEty(STUDY_UID);
        SeriesEty series = new SeriesEty(SERIES_UID);
        InstanceEty existingInstance = new InstanceEty(INSTANCE_UID, testPath.toString());

        when(patientRepository.findByPatientName(PATIENT_NAME)).thenReturn(patient);
        when(studyRepository.findByStudyInstanceUID(STUDY_UID)).thenReturn(study);
        when(seriesRepository.findBySeriesInstanceUID(SERIES_UID)).thenReturn(series);
        when(instanceRepository.findByInstanceUID(INSTANCE_UID)).thenReturn(existingInstance);

        // When
        dicomImportService.dicomToDatabase(dcmAttributes, testPath);

        // Then
        verify(instanceRepository).findByInstanceUID(INSTANCE_UID);
        verify(instanceRepository, never()).save(any(InstanceEty.class));
        verify(dicomReader, never()).readInstance(any());
    }

    @Test
    void testDicomToDatabase_WithAllExistingEntities_NoEntitiesCreated()
    {
        // Given
        setupValidDicomAttributes();

        PatientEty existingPatient = new PatientEty(PATIENT_NAME, PATIENT_ID);
        StudyEty existingStudy = new StudyEty(STUDY_UID);
        SeriesEty existingSeries = new SeriesEty(SERIES_UID);
        InstanceEty existingInstance = new InstanceEty(INSTANCE_UID, testPath.toString());

        when(patientRepository.findByPatientName(PATIENT_NAME)).thenReturn(existingPatient);
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

        verify(dicomReader, never()).readPatient(any());
        verify(dicomReader, never()).readStudy(any());
        verify(dicomReader, never()).readSeries(any());
        verify(dicomReader, never()).readInstance(any());
    }

    @Test
    void testDicomToDatabase_VerifiesBidirectionalRelationships()
    {
        // Given
        setupValidDicomAttributes();

        PatientEty patient = new PatientEty(PATIENT_NAME, PATIENT_ID);
        StudyEty study = new StudyEty(STUDY_UID);
        SeriesEty series = new SeriesEty(SERIES_UID);
        InstanceEty instance = new InstanceEty(INSTANCE_UID, testPath.toString());

        when(patientRepository.findByPatientName(PATIENT_NAME)).thenReturn(null);
        when(studyRepository.findByStudyInstanceUID(STUDY_UID)).thenReturn(null);
        when(seriesRepository.findBySeriesInstanceUID(SERIES_UID)).thenReturn(null);
        when(instanceRepository.findByInstanceUID(INSTANCE_UID)).thenReturn(null);

        when(dicomReader.readPatient(dcmAttributes)).thenReturn(patient);
        when(dicomReader.readStudy(dcmAttributes)).thenReturn(study);
        when(dicomReader.readSeries(dcmAttributes)).thenReturn(series);
        when(dicomReader.readInstance(dcmAttributes)).thenReturn(instance);

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

    @Test
    void testDicomToDatabase_MultipleInstances_OnlySavesNewInstance()
    {
        // Given - First import
        setupValidDicomAttributes();

        PatientEty patient = new PatientEty(PATIENT_NAME, PATIENT_ID);
        StudyEty study = new StudyEty(STUDY_UID);
        SeriesEty series = new SeriesEty(SERIES_UID);
        InstanceEty instance1 = new InstanceEty(INSTANCE_UID, testPath.toString());

        when(patientRepository.findByPatientName(PATIENT_NAME)).thenReturn(null);
        when(studyRepository.findByStudyInstanceUID(STUDY_UID)).thenReturn(null);
        when(seriesRepository.findBySeriesInstanceUID(SERIES_UID)).thenReturn(null);
        when(instanceRepository.findByInstanceUID(INSTANCE_UID)).thenReturn(null);

        when(dicomReader.readPatient(dcmAttributes)).thenReturn(patient);
        when(dicomReader.readStudy(dcmAttributes)).thenReturn(study);
        when(dicomReader.readSeries(dcmAttributes)).thenReturn(series);
        when(dicomReader.readInstance(dcmAttributes)).thenReturn(instance1);

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
        when(patientRepository.findByPatientName(PATIENT_NAME)).thenReturn(patient);
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
    private void setupValidDicomAttributes()
    {
        when(dcmAttributes.getString(Tag.PatientName)).thenReturn(PATIENT_NAME);
        when(dcmAttributes.getString(Tag.StudyInstanceUID)).thenReturn(STUDY_UID);
        when(dcmAttributes.getString(Tag.SeriesInstanceUID)).thenReturn(SERIES_UID);
        when(dcmAttributes.getString(Tag.SOPInstanceUID)).thenReturn(INSTANCE_UID);
    }
}
