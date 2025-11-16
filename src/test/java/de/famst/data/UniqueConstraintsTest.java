package de.famst.data;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataAccessException;

import static de.famst.AssertException.ThrowableAssertion.assertThrown;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Comprehensive tests for unique constraints on all DICOM entities.
 * Tests verify that duplicate UIDs/names are properly rejected by the database.
 */
@DataJpaTest
public class UniqueConstraintsTest
{
    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private StudyRepository studyRepository;

    @Autowired
    private SeriesRepository seriesRepository;

    @Autowired
    private InstanceRepository instanceRepository;

    // ========== Patient Unique Constraint Tests ==========

    @Test
    public void cannotAddPatientWithSameNameTwice()
    {
        PatientEty patientA = new PatientEty();
        patientA.setPatientName("Doe^John");
        patientA.setPatientId("PAT001");

        PatientEty patientB = new PatientEty();
        patientB.setPatientName("Doe^John");
        patientB.setPatientId("PAT002");

        patientRepository.saveAndFlush(patientA);

        assertThrown(() -> patientRepository.saveAndFlush(patientB))
            .isInstanceOf(DataAccessException.class);
    }

    @Test
    public void canAddPatientWithDifferentName()
    {
        PatientEty patientA = new PatientEty();
        patientA.setPatientName("Doe^John");
        patientA.setPatientId("PAT001");

        PatientEty patientB = new PatientEty();
        patientB.setPatientName("Doe^Jane");
        patientB.setPatientId("PAT002");

        assertDoesNotThrow(() -> {
            patientRepository.saveAndFlush(patientA);
            patientRepository.saveAndFlush(patientB);
        });

        assertNotNull(patientRepository.findByPatientName("Doe^John"));
        assertNotNull(patientRepository.findByPatientName("Doe^Jane"));
    }

    @Test
    public void cannotAddPatientWithNullName()
    {
        PatientEty patient = new PatientEty();
        patient.setPatientId("PAT001");
        // patientName is null

        assertThrown(() -> patientRepository.saveAndFlush(patient))
            .isInstanceOf(DataAccessException.class);
    }

    // ========== Study Unique Constraint Tests ==========

    @Test
    public void cannotAddStudyWithSameInstanceUIDTwice()
    {
        // Create a patient first
        PatientEty patient = new PatientEty();
        patient.setPatientName("Test^Patient");
        patient.setPatientId("PAT001");
        patient = patientRepository.saveAndFlush(patient);

        StudyEty studyA = new StudyEty();
        studyA.setStudyInstanceUID("1.2.840.113619.2.1.100");
        studyA.setStudyId("STU001");
        studyA.setPatient(patient);

        StudyEty studyB = new StudyEty();
        studyB.setStudyInstanceUID("1.2.840.113619.2.1.100");
        studyB.setStudyId("STU002");
        studyB.setPatient(patient);

        studyRepository.saveAndFlush(studyA);

        assertThrown(() -> studyRepository.saveAndFlush(studyB))
            .isInstanceOf(DataAccessException.class);
    }

    @Test
    public void canAddStudyWithDifferentInstanceUID()
    {
        PatientEty patient = new PatientEty();
        patient.setPatientName("Test^Patient2");
        patient.setPatientId("PAT002");
        patient = patientRepository.saveAndFlush(patient);

        StudyEty studyA = new StudyEty();
        studyA.setStudyInstanceUID("1.2.840.113619.2.1.100");
        studyA.setStudyId("STU001");
        studyA.setPatient(patient);

        StudyEty studyB = new StudyEty();
        studyB.setStudyInstanceUID("1.2.840.113619.2.1.200");
        studyB.setStudyId("STU002");
        studyB.setPatient(patient);

        assertDoesNotThrow(() -> {
            studyRepository.saveAndFlush(studyA);
            studyRepository.saveAndFlush(studyB);
        });

        assertNotNull(studyRepository.findByStudyInstanceUID("1.2.840.113619.2.1.100"));
        assertNotNull(studyRepository.findByStudyInstanceUID("1.2.840.113619.2.1.200"));
    }

    @Test
    public void cannotAddStudyWithNullInstanceUID()
    {
        PatientEty patient = new PatientEty();
        patient.setPatientName("Test^Patient3");
        patient.setPatientId("PAT003");
        patient = patientRepository.saveAndFlush(patient);

        StudyEty study = new StudyEty();
        study.setStudyId("STU001");
        study.setPatient(patient);
        // studyInstanceUID is null

        assertThrown(() -> studyRepository.saveAndFlush(study))
            .isInstanceOf(DataAccessException.class);
    }

    // ========== Series Unique Constraint Tests ==========

    @Test
    public void cannotAddSeriesWithSameInstanceUIDTwice()
    {
        // Create patient and study first
        PatientEty patient = new PatientEty();
        patient.setPatientName("Test^Patient4");
        patient.setPatientId("PAT004");
        patient = patientRepository.saveAndFlush(patient);

        StudyEty study = new StudyEty();
        study.setStudyInstanceUID("1.2.840.113619.2.1.300");
        study.setStudyId("STU003");
        study.setPatient(patient);
        study = studyRepository.saveAndFlush(study);

        SeriesEty seriesA = new SeriesEty();
        seriesA.setSeriesInstanceUID("1.2.840.113619.2.1.300.1");
        seriesA.setStudy(study);

        SeriesEty seriesB = new SeriesEty();
        seriesB.setSeriesInstanceUID("1.2.840.113619.2.1.300.1");
        seriesB.setStudy(study);

        seriesRepository.saveAndFlush(seriesA);

        assertThrown(() -> seriesRepository.saveAndFlush(seriesB))
            .isInstanceOf(DataAccessException.class);
    }

    @Test
    public void canAddSeriesWithDifferentInstanceUID()
    {
        PatientEty patient = new PatientEty();
        patient.setPatientName("Test^Patient5");
        patient.setPatientId("PAT005");
        patient = patientRepository.saveAndFlush(patient);

        StudyEty study = new StudyEty();
        study.setStudyInstanceUID("1.2.840.113619.2.1.400");
        study.setStudyId("STU004");
        study.setPatient(patient);
        study = studyRepository.saveAndFlush(study);

        SeriesEty seriesA = new SeriesEty();
        seriesA.setSeriesInstanceUID("1.2.840.113619.2.1.400.1");
        seriesA.setStudy(study);

        SeriesEty seriesB = new SeriesEty();
        seriesB.setSeriesInstanceUID("1.2.840.113619.2.1.400.2");
        seriesB.setStudy(study);

        assertDoesNotThrow(() -> {
            seriesRepository.saveAndFlush(seriesA);
            seriesRepository.saveAndFlush(seriesB);
        });

        assertNotNull(seriesRepository.findBySeriesInstanceUID("1.2.840.113619.2.1.400.1"));
        assertNotNull(seriesRepository.findBySeriesInstanceUID("1.2.840.113619.2.1.400.2"));
    }

    @Test
    public void cannotAddSeriesWithNullInstanceUID()
    {
        PatientEty patient = new PatientEty();
        patient.setPatientName("Test^Patient6");
        patient.setPatientId("PAT006");
        patient = patientRepository.saveAndFlush(patient);

        StudyEty study = new StudyEty();
        study.setStudyInstanceUID("1.2.840.113619.2.1.500");
        study.setStudyId("STU005");
        study.setPatient(patient);
        study = studyRepository.saveAndFlush(study);

        SeriesEty series = new SeriesEty();
        series.setStudy(study);
        // seriesInstanceUID is null

        assertThrown(() -> seriesRepository.saveAndFlush(series))
            .isInstanceOf(DataAccessException.class);
    }

    // ========== Instance Unique Constraint Tests ==========

    @Test
    public void cannotInsertSameInstanceTwice()
    {
        InstanceEty instanceA = new InstanceEty();
        instanceA.setInstanceUID("1.2.3.4");
        instanceA.setPath("/some/path");

        InstanceEty instanceB = new InstanceEty();
        instanceB.setInstanceUID("1.2.3.4");
        instanceB.setPath("/some/other/path");

        instanceRepository.saveAndFlush(instanceA);

        assertThrown(() -> instanceRepository.saveAndFlush(instanceB))
            .isInstanceOf(DataAccessException.class);
    }

    @Test
    public void canAddInstanceWithDifferentUID()
    {
        InstanceEty instanceA = new InstanceEty();
        instanceA.setInstanceUID("1.2.3.4");
        instanceA.setPath("/some/path");

        InstanceEty instanceB = new InstanceEty();
        instanceB.setInstanceUID("1.2.3.5");
        instanceB.setPath("/some/other/path");

        assertDoesNotThrow(() -> {
            instanceRepository.saveAndFlush(instanceA);
            instanceRepository.saveAndFlush(instanceB);
        });

        assertNotNull(instanceRepository.findByInstanceUID("1.2.3.4"));
        assertNotNull(instanceRepository.findByInstanceUID("1.2.3.5"));
    }

    @Test
    public void cannotAddInstanceWithNullUID()
    {
        InstanceEty instance = new InstanceEty();
        instance.setPath("/some/path");
        // instanceUID is null

        assertThrown(() -> instanceRepository.saveAndFlush(instance))
            .isInstanceOf(DataAccessException.class);
    }

    @Test
    public void cannotAddInstanceWithNullPath()
    {
        InstanceEty instance = new InstanceEty();
        instance.setInstanceUID("1.2.3.6");
        // path is null

        assertThrown(() -> instanceRepository.saveAndFlush(instance))
            .isInstanceOf(DataAccessException.class);
    }

    // ========== Cross-Entity Tests ==========

    @Test
    public void canAddSamePatientNameInDifferentPatients_ButNotAllowed()
    {
        // This test verifies the constraint works as designed
        // Patient names must be unique per the constraint
        PatientEty patientA = new PatientEty();
        patientA.setPatientName("Smith^John");
        patientA.setPatientId("PAT100");

        PatientEty patientB = new PatientEty();
        patientB.setPatientName("Smith^John");
        patientB.setPatientId("PAT101");

        patientRepository.saveAndFlush(patientA);

        // Should fail due to unique constraint on patient name
        assertThrown(() -> patientRepository.saveAndFlush(patientB))
            .isInstanceOf(DataAccessException.class);
    }

    @Test
    public void multipleStudiesCanBelongToSamePatient()
    {
        PatientEty patient = new PatientEty();
        patient.setPatientName("MultiStudy^Patient");
        patient.setPatientId("PAT200");
        patient = patientRepository.saveAndFlush(patient);

        StudyEty study1 = new StudyEty();
        study1.setStudyInstanceUID("1.2.840.113619.2.1.1000");
        study1.setStudyId("STU100");
        study1.setPatient(patient);

        StudyEty study2 = new StudyEty();
        study2.setStudyInstanceUID("1.2.840.113619.2.1.2000");
        study2.setStudyId("STU101");
        study2.setPatient(patient);

        assertDoesNotThrow(() -> {
            studyRepository.saveAndFlush(study1);
            studyRepository.saveAndFlush(study2);
        });
    }

    @Test
    public void multipleSeriesCanBelongToSameStudy()
    {
        PatientEty patient = new PatientEty();
        patient.setPatientName("MultiSeries^Patient");
        patient.setPatientId("PAT300");
        patient = patientRepository.saveAndFlush(patient);

        StudyEty study = new StudyEty();
        study.setStudyInstanceUID("1.2.840.113619.2.1.3000");
        study.setStudyId("STU200");
        study.setPatient(patient);
        study = studyRepository.saveAndFlush(study);

        SeriesEty series1 = new SeriesEty();
        series1.setSeriesInstanceUID("1.2.840.113619.2.1.3000.1");
        series1.setStudy(study);

        SeriesEty series2 = new SeriesEty();
        series2.setSeriesInstanceUID("1.2.840.113619.2.1.3000.2");
        series2.setStudy(study);

        assertDoesNotThrow(() -> {
            seriesRepository.saveAndFlush(series1);
            seriesRepository.saveAndFlush(series2);
        });
    }

    @Test
    public void multipleInstancesCanBelongToSameSeries()
    {
        PatientEty patient = new PatientEty();
        patient.setPatientName("MultiInstance^Patient");
        patient.setPatientId("PAT400");
        patient = patientRepository.saveAndFlush(patient);

        StudyEty study = new StudyEty();
        study.setStudyInstanceUID("1.2.840.113619.2.1.4000");
        study.setStudyId("STU300");
        study.setPatient(patient);
        study = studyRepository.saveAndFlush(study);

        SeriesEty series = new SeriesEty();
        series.setSeriesInstanceUID("1.2.840.113619.2.1.4000.1");
        series.setStudy(study);
        series = seriesRepository.saveAndFlush(series);

        InstanceEty instance1 = new InstanceEty();
        instance1.setInstanceUID("1.2.840.113619.2.1.4000.1.1");
        instance1.setPath("/path/to/instance1.dcm");
        instance1.setSeries(series);

        InstanceEty instance2 = new InstanceEty();
        instance2.setInstanceUID("1.2.840.113619.2.1.4000.1.2");
        instance2.setPath("/path/to/instance2.dcm");
        instance2.setSeries(series);

        assertDoesNotThrow(() -> {
            instanceRepository.saveAndFlush(instance1);
            instanceRepository.saveAndFlush(instance2);
        });
    }

}
