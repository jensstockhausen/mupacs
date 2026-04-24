package de.famst.controller;

import de.famst.data.InstanceEty;
import de.famst.data.PatientEty;
import de.famst.data.SeriesEty;
import de.famst.data.StudyEty;
import de.famst.dcm.PatientStudyFinder;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DICOM QIDO-RS (Query based on ID for DICOM Objects) Controller.
 *
 * <p>This controller implements the DICOM QIDO-RS (Query based on ID for DICOM Objects - RESTful Services)
 * endpoints for querying DICOM studies, series, and instances.
 *
 * <p>QIDO-RS is defined in DICOM PS3.18 Section 10.6, which specifies RESTful web services for
 * searching for DICOM objects.
 *
 * <p>Supported endpoints:
 * <ul>
 *   <li>GET /qido-rs/studies - Search for studies</li>
 *   <li>GET /qido-rs/series - Search for series</li>
 *   <li>GET /qido-rs/instances - Search for instances</li>
 * </ul>
 *
 * <p>Supported query parameters (matching C-FIND SCP fields):
 * <ul>
 *   <li>Studies: PatientID, PatientName, PatientBirthDate, PatientSex, StudyInstanceUID, StudyID,
 *       StudyDate, StudyDescription, AccessionNumber, ModalitiesInStudy, ReferringPhysicianName</li>
 *   <li>Series: StudyInstanceUID, SeriesInstanceUID, Modality, SeriesNumber, SeriesDescription,
 *       SeriesDate, PerformingPhysicianName, BodyPartExamined</li>
 *   <li>Instances: SeriesInstanceUID, SOPInstanceUID, InstanceNumber, ContentDate, AcquisitionNumber,
 *       AcquisitionDate, ImageType, Rows, Columns</li>
 * </ul>
 *
 * <p>Returns: JSON array of matching DICOM objects
 *
 * @author jens
 * @since 2026-04-24
 */
@RestController
@RequestMapping("/qido-rs")
public class DicomWebQidoController
{
    private static final Logger LOG = LoggerFactory.getLogger(DicomWebQidoController.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HHmmss");

    private final PatientStudyFinder patientStudyFinder;

    /**
     * Constructs a new DicomWebQidoController.
     *
     * @param patientStudyFinder the finder service for querying DICOM data
     */
    public DicomWebQidoController(PatientStudyFinder patientStudyFinder)
    {
        this.patientStudyFinder = patientStudyFinder;
    }

    /**
     * Search for DICOM studies.
     *
     * @param patientID Patient ID
     * @param patientName Patient Name (supports wildcards * and ?)
     * @param patientBirthDate Patient Birth Date (YYYYMMDD format)
     * @param patientSex Patient Sex (M, F, O)
     * @param studyInstanceUID Study Instance UID
     * @param studyID Study ID
     * @param studyDate Study Date (YYYYMMDD format)
     * @param studyDescription Study Description (supports wildcards)
     * @param accessionNumber Accession Number
     * @param modalitiesInStudy Modalities in Study
     * @param referringPhysicianName Referring Physician Name (supports wildcards)
     * @return JSON array of matching studies
     */
    @GetMapping(value = "/studies", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Map<String, Object>>> searchStudies(
            @RequestParam(value = "PatientID", required = false) String patientID,
            @RequestParam(value = "PatientName", required = false) String patientName,
            @RequestParam(value = "PatientBirthDate", required = false) String patientBirthDate,
            @RequestParam(value = "PatientSex", required = false) String patientSex,
            @RequestParam(value = "StudyInstanceUID", required = false) String studyInstanceUID,
            @RequestParam(value = "StudyID", required = false) String studyID,
            @RequestParam(value = "StudyDate", required = false) String studyDate,
            @RequestParam(value = "StudyDescription", required = false) String studyDescription,
            @RequestParam(value = "AccessionNumber", required = false) String accessionNumber,
            @RequestParam(value = "ModalitiesInStudy", required = false) String modalitiesInStudy,
            @RequestParam(value = "ReferringPhysicianName", required = false) String referringPhysicianName)
    {
        LOG.info("QIDO-RS Studies query: PatientID={}, PatientName={}, StudyInstanceUID={}, StudyDate={}",
                patientID, patientName, studyInstanceUID, studyDate);

        try
        {
            // Build DICOM attributes from query parameters
            Attributes keys = new Attributes();
            addStringAttribute(keys, Tag.PatientID, patientID);
            addStringAttribute(keys, Tag.PatientName, patientName);
            addDateAttribute(keys, Tag.PatientBirthDate, patientBirthDate);
            addStringAttribute(keys, Tag.PatientSex, patientSex);
            addStringAttribute(keys, Tag.StudyInstanceUID, studyInstanceUID);
            addStringAttribute(keys, Tag.StudyID, studyID);
            addDateAttribute(keys, Tag.StudyDate, studyDate);
            addStringAttribute(keys, Tag.StudyDescription, studyDescription);
            addStringAttribute(keys, Tag.AccessionNumber, accessionNumber);
            addStringAttribute(keys, Tag.ModalitiesInStudy, modalitiesInStudy);
            addStringAttribute(keys, Tag.ReferringPhysicianName, referringPhysicianName);

            // Query for studies
            List<StudyEty> studies = patientStudyFinder.findStudies(keys);

            // Convert to JSON-compatible maps
            List<Map<String, Object>> result = new ArrayList<>();
            for (StudyEty study : studies)
            {
                result.add(studyToJson(study));
            }

            LOG.info("QIDO-RS Studies query returned {} results", studies.size());
            return ResponseEntity.ok(result);
        }
        catch (Exception e)
        {
            LOG.error("Error processing QIDO-RS Studies query", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Search for DICOM series.
     *
     * @param studyInstanceUID Study Instance UID (recommended)
     * @param seriesInstanceUID Series Instance UID
     * @param modality Modality (e.g., CT, MR, US)
     * @param seriesNumber Series Number
     * @param seriesDescription Series Description (supports wildcards)
     * @param seriesDate Series Date (YYYYMMDD format)
     * @param performingPhysicianName Performing Physician Name (supports wildcards)
     * @param bodyPartExamined Body Part Examined (supports wildcards)
     * @return JSON array of matching series
     */
    @GetMapping(value = "/series", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Map<String, Object>>> searchSeries(
            @RequestParam(value = "StudyInstanceUID", required = false) String studyInstanceUID,
            @RequestParam(value = "SeriesInstanceUID", required = false) String seriesInstanceUID,
            @RequestParam(value = "Modality", required = false) String modality,
            @RequestParam(value = "SeriesNumber", required = false) Integer seriesNumber,
            @RequestParam(value = "SeriesDescription", required = false) String seriesDescription,
            @RequestParam(value = "SeriesDate", required = false) String seriesDate,
            @RequestParam(value = "PerformingPhysicianName", required = false) String performingPhysicianName,
            @RequestParam(value = "BodyPartExamined", required = false) String bodyPartExamined)
    {
        LOG.info("QIDO-RS Series query: StudyInstanceUID={}, SeriesInstanceUID={}, Modality={}",
                studyInstanceUID, seriesInstanceUID, modality);

        try
        {
            // Build DICOM attributes from query parameters
            Attributes keys = new Attributes();
            addStringAttribute(keys, Tag.StudyInstanceUID, studyInstanceUID);
            addStringAttribute(keys, Tag.SeriesInstanceUID, seriesInstanceUID);
            addStringAttribute(keys, Tag.Modality, modality);
            if (seriesNumber != null)
            {
                keys.setInt(Tag.SeriesNumber, org.dcm4che3.data.VR.IS, seriesNumber);
            }
            addStringAttribute(keys, Tag.SeriesDescription, seriesDescription);
            addDateAttribute(keys, Tag.SeriesDate, seriesDate);
            addStringAttribute(keys, Tag.PerformingPhysicianName, performingPhysicianName);
            addStringAttribute(keys, Tag.BodyPartExamined, bodyPartExamined);

            // Query for series
            List<SeriesEty> series = patientStudyFinder.findSeries(keys);

            // Convert to JSON-compatible maps
            List<Map<String, Object>> result = new ArrayList<>();
            for (SeriesEty s : series)
            {
                result.add(seriesToJson(s));
            }

            LOG.info("QIDO-RS Series query returned {} results", series.size());
            return ResponseEntity.ok(result);
        }
        catch (Exception e)
        {
            LOG.error("Error processing QIDO-RS Series query", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Search for DICOM instances.
     *
     * @param seriesInstanceUID Series Instance UID (recommended)
     * @param sopInstanceUID SOP Instance UID
     * @param instanceNumber Instance Number
     * @param contentDate Content Date (YYYYMMDD format)
     * @param acquisitionNumber Acquisition Number
     * @param acquisitionDate Acquisition Date (YYYYMMDD format)
     * @param imageType Image Type
     * @param rows Image Rows
     * @param columns Image Columns
     * @return JSON array of matching instances
     */
    @GetMapping(value = "/instances", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Map<String, Object>>> searchInstances(
            @RequestParam(value = "SeriesInstanceUID", required = false) String seriesInstanceUID,
            @RequestParam(value = "SOPInstanceUID", required = false) String sopInstanceUID,
            @RequestParam(value = "InstanceNumber", required = false) Integer instanceNumber,
            @RequestParam(value = "ContentDate", required = false) String contentDate,
            @RequestParam(value = "AcquisitionNumber", required = false) Integer acquisitionNumber,
            @RequestParam(value = "AcquisitionDate", required = false) String acquisitionDate,
            @RequestParam(value = "ImageType", required = false) String imageType,
            @RequestParam(value = "Rows", required = false) Integer rows,
            @RequestParam(value = "Columns", required = false) Integer columns)
    {
        LOG.info("QIDO-RS Instances query: SeriesInstanceUID={}, SOPInstanceUID={}, InstanceNumber={}",
                seriesInstanceUID, sopInstanceUID, instanceNumber);

        try
        {
            // Build DICOM attributes from query parameters
            Attributes keys = new Attributes();
            addStringAttribute(keys, Tag.SeriesInstanceUID, seriesInstanceUID);
            addStringAttribute(keys, Tag.SOPInstanceUID, sopInstanceUID);
            if (instanceNumber != null)
            {
                keys.setInt(Tag.InstanceNumber, org.dcm4che3.data.VR.IS, instanceNumber);
            }
            addDateAttribute(keys, Tag.ContentDate, contentDate);
            if (acquisitionNumber != null)
            {
                keys.setInt(Tag.AcquisitionNumber, org.dcm4che3.data.VR.IS, acquisitionNumber);
            }
            addDateAttribute(keys, Tag.AcquisitionDate, acquisitionDate);
            addStringAttribute(keys, Tag.ImageType, imageType);
            if (rows != null)
            {
                keys.setInt(Tag.Rows, org.dcm4che3.data.VR.US, rows);
            }
            if (columns != null)
            {
                keys.setInt(Tag.Columns, org.dcm4che3.data.VR.US, columns);
            }

            // Query for instances
            List<InstanceEty> instances = patientStudyFinder.findInstances(keys);

            // Convert to JSON-compatible maps
            List<Map<String, Object>> result = new ArrayList<>();
            for (InstanceEty instance : instances)
            {
                result.add(instanceToJson(instance));
            }

            LOG.info("QIDO-RS Instances query returned {} results", instances.size());
            return ResponseEntity.ok(result);
        }
        catch (Exception e)
        {
            LOG.error("Error processing QIDO-RS Instances query", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Adds a string attribute to DICOM keys if the value is not null.
     */
    private void addStringAttribute(Attributes keys, int tag, String value)
    {
        if (value != null && !value.isEmpty())
        {
            keys.setString(tag, org.dcm4che3.data.VR.LO, value);
        }
    }

    /**
     * Adds a date attribute to DICOM keys if the value is not null.
     * Expects date in YYYYMMDD format.
     */
    private void addDateAttribute(Attributes keys, int tag, String dateString)
    {
        if (dateString != null && !dateString.isEmpty())
        {
            try
            {
                java.time.LocalDate localDate = java.time.LocalDate.parse(dateString, DATE_FORMATTER);
                keys.setDate(tag, org.dcm4che3.data.VR.DA, java.sql.Date.valueOf(localDate));
            }
            catch (Exception e)
            {
                LOG.warn("Invalid date format for tag {}: {}", tag, dateString);
            }
        }
    }

    /**
     * Converts a StudyEty to JSON-compatible map.
     */
    private Map<String, Object> studyToJson(StudyEty study)
    {
        Map<String, Object> json = new HashMap<>();

        // Patient-level attributes
        PatientEty patient = study.getPatient();
        if (patient != null)
        {
            json.put("00100020", createValueMap(patient.getPatientId())); // PatientID
            json.put("00100010", createValueMap(patient.getPatientName())); // PatientName
            if (patient.getPatientBirthDate() != null)
            {
                json.put("00100030", createValueMap(patient.getPatientBirthDate().format(DATE_FORMATTER))); // PatientBirthDate
            }
            json.put("00100040", createValueMap(patient.getPatientSex())); // PatientSex
        }

        // Study-level attributes
        json.put("0020000D", createValueMap(study.getStudyInstanceUID())); // StudyInstanceUID
        json.put("00200010", createValueMap(study.getStudyId())); // StudyID
        if (study.getStudyDate() != null)
        {
            json.put("00080020", createValueMap(study.getStudyDate().format(DATE_FORMATTER))); // StudyDate
        }
        if (study.getStudyTime() != null)
        {
            json.put("00080030", createValueMap(study.getStudyTime().format(TIME_FORMATTER))); // StudyTime
        }
        json.put("00081030", createValueMap(study.getStudyDescription())); // StudyDescription
        json.put("00080050", createValueMap(study.getAccessionNumber())); // AccessionNumber
        json.put("00080061", createValueMap(study.getModalitiesInStudy())); // ModalitiesInStudy
        json.put("00080090", createValueMap(study.getReferringPhysicianName())); // ReferringPhysicianName

        return json;
    }

    /**
     * Converts a SeriesEty to JSON-compatible map.
     */
    private Map<String, Object> seriesToJson(SeriesEty series)
    {
        Map<String, Object> json = new HashMap<>();

        // Series-level attributes
        json.put("0020000E", createValueMap(series.getSeriesInstanceUID())); // SeriesInstanceUID
        json.put("00080060", createValueMap(series.getModality())); // Modality
        if (series.getSeriesNumber() != null)
        {
            json.put("00200011", createValueMap(series.getSeriesNumber().toString())); // SeriesNumber
        }
        json.put("0008103E", createValueMap(series.getSeriesDescription())); // SeriesDescription
        if (series.getSeriesDate() != null)
        {
            json.put("00080021", createValueMap(series.getSeriesDate().format(DATE_FORMATTER))); // SeriesDate
        }
        if (series.getSeriesTime() != null)
        {
            json.put("00080031", createValueMap(series.getSeriesTime().format(TIME_FORMATTER))); // SeriesTime
        }
        json.put("00081050", createValueMap(series.getPerformingPhysicianName())); // PerformingPhysicianName
        json.put("00180015", createValueMap(series.getBodyPartExamined())); // BodyPartExamined
        json.put("00181030", createValueMap(series.getProtocolName())); // ProtocolName
        json.put("00185100", createValueMap(series.getPatientPosition())); // PatientPosition
        json.put("00200060", createValueMap(series.getLaterality())); // Laterality
        json.put("00081070", createValueMap(series.getOperatorsName())); // OperatorsName

        // Include study UID for reference
        if (series.getStudy() != null)
        {
            json.put("0020000D", createValueMap(series.getStudy().getStudyInstanceUID())); // StudyInstanceUID
        }

        return json;
    }

    /**
     * Converts an InstanceEty to JSON-compatible map.
     */
    private Map<String, Object> instanceToJson(InstanceEty instance)
    {
        Map<String, Object> json = new HashMap<>();

        // Instance-level attributes
        json.put("00080018", createValueMap(instance.getInstanceUID())); // SOPInstanceUID
        if (instance.getInstanceNumber() != null)
        {
            json.put("00200013", createValueMap(instance.getInstanceNumber().toString())); // InstanceNumber
        }
        if (instance.getContentDate() != null)
        {
            json.put("00080023", createValueMap(instance.getContentDate().format(DATE_FORMATTER))); // ContentDate
        }
        if (instance.getContentTime() != null)
        {
            json.put("00080033", createValueMap(instance.getContentTime().format(TIME_FORMATTER))); // ContentTime
        }
        json.put("00080008", createValueMap(instance.getImageType())); // ImageType
        if (instance.getAcquisitionNumber() != null)
        {
            json.put("00200012", createValueMap(instance.getAcquisitionNumber().toString())); // AcquisitionNumber
        }
        if (instance.getAcquisitionDate() != null)
        {
            json.put("00080022", createValueMap(instance.getAcquisitionDate().format(DATE_FORMATTER))); // AcquisitionDate
        }
        if (instance.getAcquisitionTime() != null)
        {
            json.put("00080032", createValueMap(instance.getAcquisitionTime().format(TIME_FORMATTER))); // AcquisitionTime
        }
        if (instance.getRows() != null)
        {
            json.put("00280010", createValueMap(instance.getRows().toString())); // Rows
        }
        if (instance.getColumns() != null)
        {
            json.put("00280011", createValueMap(instance.getColumns().toString())); // Columns
        }
        if (instance.getBitsAllocated() != null)
        {
            json.put("00280100", createValueMap(instance.getBitsAllocated().toString())); // BitsAllocated
        }
        if (instance.getBitsStored() != null)
        {
            json.put("00280101", createValueMap(instance.getBitsStored().toString())); // BitsStored
        }

        // Include series UID for reference
        if (instance.getSeries() != null)
        {
            json.put("0020000E", createValueMap(instance.getSeries().getSeriesInstanceUID())); // SeriesInstanceUID
        }

        return json;
    }

    /**
     * Creates a DICOM JSON value map with a single string value.
     */
    private Map<String, Object> createValueMap(String value)
    {
        Map<String, Object> map = new HashMap<>();
        if (value != null && !value.isEmpty())
        {
            List<String> valueArray = List.of(value);
            map.put("Value", valueArray);
        }
        return map;
    }
}










