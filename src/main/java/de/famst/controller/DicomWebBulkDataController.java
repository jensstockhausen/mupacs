package de.famst.controller;

import de.famst.data.InstanceEty;
import de.famst.data.InstanceRepository;
import de.famst.service.DicomBulkDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRange;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

/**
 * DICOM WADO-RS BulkData Controller.
 *
 * <p>This controller implements the DICOM WADO-RS (Web Access to DICOM Objects - RESTful Services)
 * BulkData retrieval endpoint with support for HTTP 1.1 Range requests.
 *
 * <p>WADO-RS is defined in DICOM PS3.18 Section 10, which specifies RESTful web services for
 * accessing DICOM objects and their components.
 *
 * <p>Supported endpoints:
 * <ul>
 *   <li>GET /wado-rs/studies/{studyUID}/series/{seriesUID}/instances/{instanceUID}/bulkdata - Retrieve pixel data</li>
 * </ul>
 *
 * <p>Features:
 * <ul>
 *   <li>HTTP 1.1 Range request support (RFC 7233) for partial content retrieval</li>
 *   <li>Proper content-type headers (application/octet-stream)</li>
 *   <li>Content-Length and Accept-Ranges headers</li>
 *   <li>206 Partial Content responses for range requests</li>
 *   <li>416 Range Not Satisfiable for invalid ranges</li>
 * </ul>
 *
 * @author jens
 * @since 2026-04-24
 */
@RestController
@RequestMapping("/wado-rs")
public class DicomWebBulkDataController
{
    private static final Logger LOG = LoggerFactory.getLogger(DicomWebBulkDataController.class);

    private final InstanceRepository instanceRepository;
    private final DicomBulkDataService bulkDataService;

    /**
     * Constructs a new DicomWebBulkDataController.
     *
     * @param instanceRepository the repository for accessing DICOM instances
     * @param bulkDataService the service for handling bulk data operations
     */
    public DicomWebBulkDataController(
            InstanceRepository instanceRepository,
            DicomBulkDataService bulkDataService)
    {
        this.instanceRepository = instanceRepository;
        this.bulkDataService = bulkDataService;
    }

    /**
     * Retrieves the pixel data (bulk data) for a specific DICOM instance.
     * Supports HTTP 1.1 Range requests for partial content retrieval.
     *
     * @param studyUID the Study Instance UID
     * @param seriesUID the Series Instance UID
     * @param instanceUID the SOP Instance UID
     * @param rangeHeader the HTTP Range header value (optional)
     * @return ResponseEntity containing the bulk data or appropriate error response
     */
    @GetMapping("/studies/{studyUID}/series/{seriesUID}/instances/{instanceUID}/bulkdata")
    public ResponseEntity<Resource> getBulkData(
            @PathVariable String studyUID,
            @PathVariable String seriesUID,
            @PathVariable String instanceUID,
            @RequestHeader(value = HttpHeaders.RANGE, required = false) String rangeHeader)
    {
        LOG.info("WADO-RS BulkData request: Study={}, Series={}, Instance={}, Range={}",
                studyUID, seriesUID, instanceUID, rangeHeader);

        // Find the instance by UID
        InstanceEty instance = instanceRepository.findByInstanceUID(instanceUID);
        if (instance == null)
        {
            LOG.warn("Instance not found: {}", instanceUID);
            return ResponseEntity.notFound().build();
        }

        // Verify the instance belongs to the specified study and series
        if (instance.getSeries() == null ||
            !seriesUID.equals(instance.getSeries().getSeriesInstanceUID()))
        {
            LOG.warn("Instance {} does not belong to series {}", instanceUID, seriesUID);
            return ResponseEntity.notFound().build();
        }

        if (instance.getSeries().getStudy() == null ||
            !studyUID.equals(instance.getSeries().getStudy().getStudyInstanceUID()))
        {
            LOG.warn("Instance {} does not belong to study {}", instanceUID, studyUID);
            return ResponseEntity.notFound().build();
        }

        try
        {
            // Get file size for range request handling
            long fileSize = bulkDataService.getFileSize(instance.getPath());

            // Handle range request
            if (rangeHeader != null && !rangeHeader.isEmpty())
            {
                return handleRangeRequest(instance, rangeHeader, fileSize);
            }

            // Return full file
            return handleFullRequest(instance, fileSize);
        }
        catch (IOException e)
        {
            LOG.error("Error reading bulk data for instance {}: {}", instanceUID, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Handles a full file request (no range header).
     *
     * @param instance the DICOM instance
     * @param fileSize the size of the file
     * @return ResponseEntity with the full file content
     * @throws IOException if file cannot be read
     */
    private ResponseEntity<Resource> handleFullRequest(InstanceEty instance, long fileSize)
            throws IOException
    {
        LOG.debug("Serving full file for instance {}", instance.getInstanceUID());

        Resource resource = bulkDataService.getFullFile(instance.getPath());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE)
                .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(fileSize))
                .body(resource);
    }

    /**
     * Handles a range request (partial content).
     *
     * @param instance the DICOM instance
     * @param rangeHeader the Range header value
     * @param fileSize the size of the file
     * @return ResponseEntity with the requested range content
     * @throws IOException if file cannot be read
     */
    private ResponseEntity<Resource> handleRangeRequest(
            InstanceEty instance,
            String rangeHeader,
            long fileSize)
            throws IOException
    {
        LOG.debug("Processing range request for instance {}: {}", instance.getInstanceUID(), rangeHeader);

        try
        {
            // Parse range header
            List<HttpRange> ranges = HttpRange.parseRanges(rangeHeader);

            if (ranges.isEmpty())
            {
                LOG.warn("Invalid range header: {}", rangeHeader);
                return ResponseEntity.status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE)
                        .header(HttpHeaders.CONTENT_RANGE, "bytes */" + fileSize)
                        .build();
            }

            // Only support single range requests (as per DICOM WADO-RS spec)
            if (ranges.size() > 1)
            {
                LOG.warn("Multiple ranges not supported. Requested: {}", ranges.size());
                return ResponseEntity.status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE)
                        .header(HttpHeaders.CONTENT_RANGE, "bytes */" + fileSize)
                        .build();
            }

            HttpRange range = ranges.get(0);
            long start = range.getRangeStart(fileSize);
            long end = range.getRangeEnd(fileSize);

            // Validate range
            if (start > end || end >= fileSize)
            {
                LOG.warn("Invalid range: start={}, end={}, fileSize={}", start, end, fileSize);
                return ResponseEntity.status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE)
                        .header(HttpHeaders.CONTENT_RANGE, "bytes */" + fileSize)
                        .build();
            }

            long contentLength = end - start + 1;

            LOG.debug("Serving range for instance {}: bytes {}-{}/{}",
                    instance.getInstanceUID(), start, end, fileSize);

            Resource resource = bulkDataService.getFileRange(instance.getPath(), start, end);

            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE)
                    .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                    .header(HttpHeaders.CONTENT_RANGE, "bytes " + start + "-" + end + "/" + fileSize)
                    .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(contentLength))
                    .body(resource);
        }
        catch (IllegalArgumentException e)
        {
            LOG.warn("Invalid range header: {}", rangeHeader, e);
            return ResponseEntity.status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE)
                    .header(HttpHeaders.CONTENT_RANGE, "bytes */" + fileSize)
                    .build();
        }
    }
}

