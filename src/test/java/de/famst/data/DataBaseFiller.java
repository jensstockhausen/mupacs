package de.famst.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.stereotype.Component;

/**
 * Utility component for populating the test database with DICOM test data.
 *
 * <p>This class creates a hierarchical structure of test data following the DICOM hierarchy:
 * Patient → Study → Series → Instance
 *
 * <p>Data Structure Created:
 * <ul>
 *   <li>10 Patients (Demo_000 to Demo_009)</li>
 *   <li>2 Studies per Patient</li>
 *   <li>2 Series per Study</li>
 *   <li>2 Instances per Series</li>
 *   <li>Total: 10 patients, 20 studies, 40 series, 80 instances</li>
 * </ul>
 *
 * <p>UID Structure:
 * <ul>
 *   <li>Patient ID: 1.2.48.{patientIndex}</li>
 *   <li>Study UID: {patientId}.{studyIndex}</li>
 *   <li>Series UID: {studyUid}.{seriesIndex}</li>
 *   <li>Instance UID: {seriesUid}.{instanceIndex}</li>
 * </ul>
 *
 * @author jens
 * @since 2016-11-06
 */
@Component
public class DataBaseFiller
{
  private static final Logger LOG = LoggerFactory.getLogger(DataBaseFiller.class);

  // Test data configuration constants
  private static final String UID_PREFIX = "1.2.48";
  private static final String PATIENT_NAME_PREFIX = "Demo_";
  private static final String INSTANCE_PATH_PREFIX = "PATH";

  private static final int PATIENT_COUNT = 10;
  private static final int STUDIES_PER_PATIENT = 2;
  private static final int SERIES_PER_STUDY = 2;
  private static final int INSTANCES_PER_SERIES = 2;

  private TestEntityManager entityManager;

  /**
   * Fills the database with hierarchical test DICOM data.
   *
   * <p>Creates a complete DICOM hierarchy with patients, studies, series, and instances.
   * All entities are persisted and flushed to ensure they're available for queries.
   *
   * @param entityManager the test entity manager to use for persistence
   * @throws IllegalArgumentException if entityManager is null
   */
  public void fillDB(TestEntityManager entityManager)
  {
    if (entityManager == null)
    {
      throw new IllegalArgumentException("EntityManager cannot be null");
    }

    LOG.info("Starting to fill database with test data");
    this.entityManager = entityManager;

    int totalPatients = 0;
    int totalStudies = 0;
    int totalSeries = 0;
    int totalInstances = 0;

    for (int patientIndex = 0; patientIndex < PATIENT_COUNT; patientIndex++)
    {
      PatientEty patient = createPatient(patientIndex);
      totalPatients++;

      for (int studyIndex = 0; studyIndex < STUDIES_PER_PATIENT; studyIndex++)
      {
        StudyEty study = createStudy(patient, studyIndex);
        totalStudies++;

        for (int seriesIndex = 0; seriesIndex < SERIES_PER_STUDY; seriesIndex++)
        {
          SeriesEty series = createSeries(study, seriesIndex);
          totalSeries++;

          for (int instanceIndex = 0; instanceIndex < INSTANCES_PER_SERIES; instanceIndex++)
          {
            createInstance(series, instanceIndex);
            totalInstances++;
          }
        }
      }
    }

    LOG.info("Database filled successfully: {} patients, {} studies, {} series, {} instances",
             totalPatients, totalStudies, totalSeries, totalInstances);
  }

  /**
   * Creates and persists a test patient entity.
   *
   * @param patientIndex the index of the patient (0-based)
   * @return the persisted patient entity
   */
  private PatientEty createPatient(int patientIndex)
  {
    String patientId = String.format("%s.%d", UID_PREFIX, patientIndex);
    String patientName = String.format("%s%03d", PATIENT_NAME_PREFIX, patientIndex);

    PatientEty patient = new PatientEty();
    patient.setPatientId(patientId);
    patient.setPatientName(patientName);

    PatientEty persistedPatient = entityManager.persistFlushFind(patient);
    LOG.debug("Created patient: {} (ID: {})", patientName, patientId);

    return persistedPatient;
  }

  /**
   * Creates and persists a test study entity.
   *
   * @param patient the parent patient entity
   * @param studyIndex the index of the study within the patient (0-based)
   * @return the persisted study entity
   */
  private StudyEty createStudy(PatientEty patient, int studyIndex)
  {
    String studyInstanceUID = String.format("%s.%d", patient.getPatientId(), studyIndex);
    String accessionNumber = String.format("%d%d%d", studyIndex, studyIndex, studyIndex);

    StudyEty study = new StudyEty();
    study.setStudyInstanceUID(studyInstanceUID);
    study.setAccessionNumber(accessionNumber);
    study.setPatient(patient);

    StudyEty persistedStudy = entityManager.persistFlushFind(study);
    LOG.debug("Created study: {} for patient: {}", studyInstanceUID, patient.getPatientName());

    return persistedStudy;
  }

  /**
   * Creates and persists a test series entity.
   *
   * @param study the parent study entity
   * @param seriesIndex the index of the series within the study (0-based)
   * @return the persisted series entity
   */
  private SeriesEty createSeries(StudyEty study, int seriesIndex)
  {
    String seriesInstanceUID = String.format("%s.%d", study.getStudyInstanceUID(), seriesIndex);

    SeriesEty series = new SeriesEty();
    series.setSeriesInstanceUID(seriesInstanceUID);
    series.setStudy(study);

    SeriesEty persistedSeries = entityManager.persistFlushFind(series);
    LOG.debug("Created series: {} for study: {}", seriesInstanceUID, study.getStudyInstanceUID());

    return persistedSeries;
  }

  /**
   * Creates and persists a test instance entity.
   *
   * @param series the parent series entity
   * @param instanceIndex the index of the instance within the series (0-based)
   * @return the persisted instance entity
   */
  private InstanceEty createInstance(SeriesEty series, int instanceIndex)
  {
    String instanceUID = String.format("%s.%d", series.getSeriesInstanceUID(), instanceIndex);
    String instancePath = String.format("%s%d", INSTANCE_PATH_PREFIX, instanceIndex);

    InstanceEty instance = new InstanceEty();
    instance.setInstanceUID(instanceUID);
    instance.setPath(instancePath);
    instance.setSeries(series);

    InstanceEty persistedInstance = entityManager.persistFlushFind(instance);
    LOG.debug("Created instance: {} at path: {}", instanceUID, instancePath);

    return persistedInstance;
  }

  /**
   * Returns the total number of patients that will be created.
   *
   * @return the patient count
   */
  public int getPatientCount()
  {
    return PATIENT_COUNT;
  }

  /**
   * Returns the total number of studies that will be created.
   *
   * @return the study count
   */
  public int getTotalStudyCount()
  {
    return PATIENT_COUNT * STUDIES_PER_PATIENT;
  }

  /**
   * Returns the total number of series that will be created.
   *
   * @return the series count
   */
  public int getTotalSeriesCount()
  {
    return PATIENT_COUNT * STUDIES_PER_PATIENT * SERIES_PER_STUDY;
  }

  /**
   * Returns the total number of instances that will be created.
   *
   * @return the instance count
   */
  public int getTotalInstanceCount()
  {
    return PATIENT_COUNT * STUDIES_PER_PATIENT * SERIES_PER_STUDY * INSTANCES_PER_SERIES;
  }

}

