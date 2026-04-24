package de.famst.controller;

import de.famst.data.InstanceEty;
import de.famst.data.InstanceRepository;
import de.famst.data.PatientEty;
import de.famst.data.SeriesEty;
import de.famst.data.StudyEty;
import de.famst.service.DicomBulkDataService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.head;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit tests for DicomWebBulkDataController.
 *
 * <p>Tests WADO-RS BulkData endpoint functionality including:
 * <ul>
 *   <li>Full file retrieval</li>
 *   <li>HTTP Range request handling</li>
 *   <li>HEAD request for metadata/file size</li>
 *   <li>Error cases (not found, invalid ranges, etc.)</li>
 * </ul>
 *
 * @author jens
 * @since 2026-04-24
 */
@WebMvcTest(DicomWebBulkDataController.class)
class DicomWebBulkDataControllerTest
{
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private InstanceRepository instanceRepository;

    @MockitoBean
    private DicomBulkDataService bulkDataService;

    @TempDir
    Path tempDir;

    private PatientEty patient;
    private StudyEty study;
    private SeriesEty series;
    private InstanceEty instance;
    private String testFilePath;
    private byte[] testData;

    @BeforeEach
    void setUp() throws IOException
    {
        // Create test data
        testData = "DICOM_TEST_DATA_1234567890".getBytes();
        Path testFile = tempDir.resolve("test.dcm");
        Files.write(testFile, testData);
        testFilePath = testFile.toString();

        // Create test entities
        patient = new PatientEty("12345", "Test^Patient");
        study = new StudyEty("1.2.3.4.5");
        series = new SeriesEty("1.2.3.4.5.6");
        instance = new InstanceEty("1.2.3.4.5.6.7", testFilePath);

        // Setup relationships
        patient.addStudy(study);
        study.addSeries(series);
        series.addInstance(instance);
    }

    @Nested
    @DisplayName("Full file retrieval tests")
    class FullFileRetrievalTests
    {
        @Test
        @DisplayName("Should retrieve full file when no range header is provided")
        void shouldRetrieveFullFile() throws Exception
        {
            // Given
            when(instanceRepository.findByInstanceUID("1.2.3.4.5.6.7")).thenReturn(instance);
            when(bulkDataService.getFileSize(testFilePath)).thenReturn((long) testData.length);
            when(bulkDataService.getFullFile(testFilePath)).thenReturn(new ByteArrayResource(testData));

            // When/Then
            mockMvc.perform(get("/wado-rs/studies/1.2.3.4.5/series/1.2.3.4.5.6/instances/1.2.3.4.5.6.7/bulkdata"))
                    .andExpect(status().isOk())
                    .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "application/octet-stream"))
                    .andExpect(header().string(HttpHeaders.ACCEPT_RANGES, "bytes"))
                    .andExpect(header().string(HttpHeaders.CONTENT_LENGTH, String.valueOf(testData.length)))
                    .andExpect(content().bytes(testData));
        }

        @Test
        @DisplayName("Should return 404 when instance not found")
        void shouldReturn404WhenInstanceNotFound() throws Exception
        {
            // Given
            when(instanceRepository.findByInstanceUID("1.2.3.4.5.6.7")).thenReturn(null);

            // When/Then
            mockMvc.perform(get("/wado-rs/studies/1.2.3.4.5/series/1.2.3.4.5.6/instances/1.2.3.4.5.6.7/bulkdata"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 404 when instance belongs to different series")
        void shouldReturn404WhenInstanceBelongsToDifferentSeries() throws Exception
        {
            // Given
            when(instanceRepository.findByInstanceUID("1.2.3.4.5.6.7")).thenReturn(instance);

            // When/Then
            mockMvc.perform(get("/wado-rs/studies/1.2.3.4.5/series/9.9.9.9.9.9/instances/1.2.3.4.5.6.7/bulkdata"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 404 when instance belongs to different study")
        void shouldReturn404WhenInstanceBelongsToDifferentStudy() throws Exception
        {
            // Given
            when(instanceRepository.findByInstanceUID("1.2.3.4.5.6.7")).thenReturn(instance);

            // When/Then
            mockMvc.perform(get("/wado-rs/studies/9.9.9.9.9/series/1.2.3.4.5.6/instances/1.2.3.4.5.6.7/bulkdata"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 500 when file cannot be read")
        void shouldReturn500WhenFileCannotBeRead() throws Exception
        {
            // Given
            when(instanceRepository.findByInstanceUID("1.2.3.4.5.6.7")).thenReturn(instance);
            when(bulkDataService.getFileSize(testFilePath)).thenThrow(new IOException("File not found"));

            // When/Then
            mockMvc.perform(get("/wado-rs/studies/1.2.3.4.5/series/1.2.3.4.5.6/instances/1.2.3.4.5.6.7/bulkdata"))
                    .andExpect(status().isInternalServerError());
        }
    }

    @Nested
    @DisplayName("HTTP Range request tests")
    class RangeRequestTests
    {
        @Test
        @DisplayName("Should return partial content for valid range request")
        void shouldReturnPartialContentForValidRange() throws Exception
        {
            // Given
            byte[] rangeData = "DICOM".getBytes();
            when(instanceRepository.findByInstanceUID("1.2.3.4.5.6.7")).thenReturn(instance);
            when(bulkDataService.getFileSize(testFilePath)).thenReturn((long) testData.length);
            when(bulkDataService.getFileRange(eq(testFilePath), eq(0L), eq(4L)))
                    .thenReturn(new ByteArrayResource(rangeData));

            // When/Then
            mockMvc.perform(get("/wado-rs/studies/1.2.3.4.5/series/1.2.3.4.5.6/instances/1.2.3.4.5.6.7/bulkdata")
                            .header(HttpHeaders.RANGE, "bytes=0-4"))
                    .andExpect(status().isPartialContent())
                    .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "application/octet-stream"))
                    .andExpect(header().string(HttpHeaders.ACCEPT_RANGES, "bytes"))
                    .andExpect(header().string(HttpHeaders.CONTENT_RANGE, "bytes 0-4/" + testData.length))
                    .andExpect(header().string(HttpHeaders.CONTENT_LENGTH, "5"))
                    .andExpect(content().bytes(rangeData));
        }

        @Test
        @DisplayName("Should return partial content for range with start only")
        void shouldReturnPartialContentForRangeWithStartOnly() throws Exception
        {
            // Given
            byte[] rangeData = "1234567890".getBytes();
            long start = 17L;
            long end = testData.length - 1; // 26 - end is inclusive, so 17-25 = 9 bytes (not 10)

            when(instanceRepository.findByInstanceUID("1.2.3.4.5.6.7")).thenReturn(instance);
            when(bulkDataService.getFileSize(testFilePath)).thenReturn((long) testData.length);
            when(bulkDataService.getFileRange(eq(testFilePath), eq(start), eq(end)))
                    .thenReturn(new ByteArrayResource(rangeData));

            // When/Then
            mockMvc.perform(get("/wado-rs/studies/1.2.3.4.5/series/1.2.3.4.5.6/instances/1.2.3.4.5.6.7/bulkdata")
                            .header(HttpHeaders.RANGE, "bytes=17-"))
                    .andExpect(status().isPartialContent())
                    .andExpect(header().string(HttpHeaders.CONTENT_RANGE, "bytes 17-25/" + testData.length))
                    .andExpect(header().string(HttpHeaders.CONTENT_LENGTH, "9"));
        }

        @Test
        @DisplayName("Should return partial content for valid range exceeding file size but within bounds")
        void shouldReturnPartialContentForRangeExceedingFileSize() throws Exception
        {
            // Given - asking for bytes 0-1000 when file is only 27 bytes (indices 0-26)
            // HttpRange will adjust this to 0-25 (last valid index is length - 1 = 26, but range end is 25)
            byte[] rangeData = new byte[26]; // bytes 0-25 = 26 bytes
            System.arraycopy(testData, 0, rangeData, 0, 26);
            when(instanceRepository.findByInstanceUID("1.2.3.4.5.6.7")).thenReturn(instance);
            when(bulkDataService.getFileSize(testFilePath)).thenReturn((long) testData.length);
            when(bulkDataService.getFileRange(eq(testFilePath), eq(0L), eq(25L)))
                    .thenReturn(new ByteArrayResource(rangeData));

            // When/Then - HttpRange automatically caps the end to file size - 2 when out of bounds
            mockMvc.perform(get("/wado-rs/studies/1.2.3.4.5/series/1.2.3.4.5.6/instances/1.2.3.4.5.6.7/bulkdata")
                            .header(HttpHeaders.RANGE, "bytes=0-1000"))
                    .andExpect(status().isPartialContent())
                    .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "application/octet-stream"))
                    .andExpect(header().string(HttpHeaders.ACCEPT_RANGES, "bytes"))
                    .andExpect(header().string(HttpHeaders.CONTENT_RANGE, "bytes 0-25/" + testData.length))
                    .andExpect(header().string(HttpHeaders.CONTENT_LENGTH, "26"))
                    .andExpect(content().bytes(rangeData));
        }

        @Test
        @DisplayName("Should return 416 for invalid range with start after end")
        void shouldReturn416ForInvalidRangeStartAfterEnd() throws Exception
        {
            // Given
            when(instanceRepository.findByInstanceUID("1.2.3.4.5.6.7")).thenReturn(instance);
            when(bulkDataService.getFileSize(testFilePath)).thenReturn((long) testData.length);

            // When/Then
            mockMvc.perform(get("/wado-rs/studies/1.2.3.4.5/series/1.2.3.4.5.6/instances/1.2.3.4.5.6.7/bulkdata")
                            .header(HttpHeaders.RANGE, "bytes=10-5"))
                    .andExpect(status().isRequestedRangeNotSatisfiable())
                    .andExpect(header().string(HttpHeaders.CONTENT_RANGE, "bytes */" + testData.length));
        }

        @Test
        @DisplayName("Should return 416 for invalid range header format")
        void shouldReturn416ForInvalidRangeHeaderFormat() throws Exception
        {
            // Given
            when(instanceRepository.findByInstanceUID("1.2.3.4.5.6.7")).thenReturn(instance);
            when(bulkDataService.getFileSize(testFilePath)).thenReturn((long) testData.length);

            // When/Then
            mockMvc.perform(get("/wado-rs/studies/1.2.3.4.5/series/1.2.3.4.5.6/instances/1.2.3.4.5.6.7/bulkdata")
                            .header(HttpHeaders.RANGE, "invalid"))
                    .andExpect(status().isRequestedRangeNotSatisfiable())
                    .andExpect(header().string(HttpHeaders.CONTENT_RANGE, "bytes */" + testData.length));
        }

        @Test
        @DisplayName("Should return 416 for multiple ranges (not supported)")
        void shouldReturn416ForMultipleRanges() throws Exception
        {
            // Given
            when(instanceRepository.findByInstanceUID("1.2.3.4.5.6.7")).thenReturn(instance);
            when(bulkDataService.getFileSize(testFilePath)).thenReturn((long) testData.length);

            // When/Then
            mockMvc.perform(get("/wado-rs/studies/1.2.3.4.5/series/1.2.3.4.5.6/instances/1.2.3.4.5.6.7/bulkdata")
                            .header(HttpHeaders.RANGE, "bytes=0-5,10-15"))
                    .andExpect(status().isRequestedRangeNotSatisfiable())
                    .andExpect(header().string(HttpHeaders.CONTENT_RANGE, "bytes */" + testData.length));
        }

        @Test
        @DisplayName("Should return 416 for range starting beyond file size")
        void shouldReturn416ForRangeStartingBeyondFileSize() throws Exception
        {
            // Given
            when(instanceRepository.findByInstanceUID("1.2.3.4.5.6.7")).thenReturn(instance);
            when(bulkDataService.getFileSize(testFilePath)).thenReturn((long) testData.length);

            // When/Then - start at position 100 which is beyond the 27 byte file
            mockMvc.perform(get("/wado-rs/studies/1.2.3.4.5/series/1.2.3.4.5.6/instances/1.2.3.4.5.6.7/bulkdata")
                            .header(HttpHeaders.RANGE, "bytes=100-200"))
                    .andExpect(status().isRequestedRangeNotSatisfiable())
                    .andExpect(header().string(HttpHeaders.CONTENT_RANGE, "bytes */" + testData.length));
        }

        @Test
        @DisplayName("Should handle range request at end of file")
        void shouldHandleRangeRequestAtEndOfFile() throws Exception
        {
            // Given
            byte[] rangeData = "0".getBytes();
            long lastByte = testData.length - 1; // 26 (0-indexed)

            when(instanceRepository.findByInstanceUID("1.2.3.4.5.6.7")).thenReturn(instance);
            when(bulkDataService.getFileSize(testFilePath)).thenReturn((long) testData.length);
            when(bulkDataService.getFileRange(eq(testFilePath), eq(lastByte), eq(lastByte)))
                    .thenReturn(new ByteArrayResource(rangeData));

            // When/Then - get the last byte (position 25, not 26)
            mockMvc.perform(get("/wado-rs/studies/1.2.3.4.5/series/1.2.3.4.5.6/instances/1.2.3.4.5.6.7/bulkdata")
                            .header(HttpHeaders.RANGE, "bytes=25-25"))
                    .andExpect(status().isPartialContent())
                    .andExpect(header().string(HttpHeaders.CONTENT_RANGE, "bytes 25-25/" + testData.length))
                    .andExpect(header().string(HttpHeaders.CONTENT_LENGTH, "1"));
        }
    }

    @Nested
    @DisplayName("HEAD request tests")
    class HeadRequestTests
    {
        @Test
        @DisplayName("Should return file size in Content-Length header for HEAD request")
        void shouldReturnFileSizeForHeadRequest() throws Exception
        {
            // Given
            when(instanceRepository.findByInstanceUID("1.2.3.4.5.6.7")).thenReturn(instance);
            when(bulkDataService.getFileSize(testFilePath)).thenReturn((long) testData.length);

            // When/Then
            mockMvc.perform(head("/wado-rs/studies/1.2.3.4.5/series/1.2.3.4.5.6/instances/1.2.3.4.5.6.7/bulkdata"))
                    .andExpect(status().isOk())
                    .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "application/octet-stream"))
                    .andExpect(header().string(HttpHeaders.ACCEPT_RANGES, "bytes"))
                    .andExpect(header().string(HttpHeaders.CONTENT_LENGTH, String.valueOf(testData.length)));
        }

        @Test
        @DisplayName("Should return 404 for HEAD request when instance not found")
        void shouldReturn404ForHeadRequestWhenInstanceNotFound() throws Exception
        {
            // Given
            when(instanceRepository.findByInstanceUID("1.2.3.4.5.6.7")).thenReturn(null);

            // When/Then
            mockMvc.perform(head("/wado-rs/studies/1.2.3.4.5/series/1.2.3.4.5.6/instances/1.2.3.4.5.6.7/bulkdata"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 404 for HEAD request when instance belongs to different series")
        void shouldReturn404ForHeadRequestWhenInstanceBelongsToDifferentSeries() throws Exception
        {
            // Given
            when(instanceRepository.findByInstanceUID("1.2.3.4.5.6.7")).thenReturn(instance);

            // When/Then
            mockMvc.perform(head("/wado-rs/studies/1.2.3.4.5/series/9.9.9.9.9.9/instances/1.2.3.4.5.6.7/bulkdata"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 404 for HEAD request when instance belongs to different study")
        void shouldReturn404ForHeadRequestWhenInstanceBelongsToDifferentStudy() throws Exception
        {
            // Given
            when(instanceRepository.findByInstanceUID("1.2.3.4.5.6.7")).thenReturn(instance);

            // When/Then
            mockMvc.perform(head("/wado-rs/studies/9.9.9.9.9/series/1.2.3.4.5.6/instances/1.2.3.4.5.6.7/bulkdata"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 500 for HEAD request when file size cannot be retrieved")
        void shouldReturn500ForHeadRequestWhenFileSizeCannotBeRetrieved() throws Exception
        {
            // Given
            when(instanceRepository.findByInstanceUID("1.2.3.4.5.6.7")).thenReturn(instance);
            when(bulkDataService.getFileSize(testFilePath)).thenThrow(new IOException("File not accessible"));

            // When/Then
            mockMvc.perform(head("/wado-rs/studies/1.2.3.4.5/series/1.2.3.4.5.6/instances/1.2.3.4.5.6.7/bulkdata"))
                    .andExpect(status().isInternalServerError());
        }
    }
}

