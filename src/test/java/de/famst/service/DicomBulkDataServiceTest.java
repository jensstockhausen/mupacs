package de.famst.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.Resource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for DicomBulkDataService.
 *
 * <p>Tests file reading operations including:
 * <ul>
 *   <li>File size retrieval</li>
 *   <li>Full file reading</li>
 *   <li>Range-based partial file reading</li>
 *   <li>Error handling</li>
 * </ul>
 *
 * @author jens
 * @since 2026-04-24
 */
class DicomBulkDataServiceTest
{
    private DicomBulkDataService service;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp()
    {
        service = new DicomBulkDataService();
    }

    @Nested
    @DisplayName("File size retrieval tests")
    class FileSizeTests
    {
        @Test
        @DisplayName("Should return correct file size for existing file")
        void shouldReturnCorrectFileSizeForExistingFile() throws IOException
        {
            // Given
            byte[] data = "Test data for file size".getBytes();
            Path testFile = tempDir.resolve("test.dat");
            Files.write(testFile, data);

            // When
            long fileSize = service.getFileSize(testFile.toString());

            // Then
            assertThat(fileSize).isEqualTo(data.length);
        }

        @Test
        @DisplayName("Should throw IOException for non-existent file")
        void shouldThrowIOExceptionForNonExistentFile()
        {
            // Given
            String nonExistentPath = tempDir.resolve("nonexistent.dat").toString();

            // When/Then
            assertThatThrownBy(() -> service.getFileSize(nonExistentPath))
                    .isInstanceOf(IOException.class)
                    .hasMessageContaining("File not found");
        }

        @Test
        @DisplayName("Should throw IOException for directory instead of file")
        void shouldThrowIOExceptionForDirectory()
        {
            // Given
            String directoryPath = tempDir.toString();

            // When/Then
            assertThatThrownBy(() -> service.getFileSize(directoryPath))
                    .isInstanceOf(IOException.class)
                    .hasMessageContaining("not a regular file");
        }

        @Test
        @DisplayName("Should return zero for empty file")
        void shouldReturnZeroForEmptyFile() throws IOException
        {
            // Given
            Path emptyFile = tempDir.resolve("empty.dat");
            Files.write(emptyFile, new byte[0]);

            // When
            long fileSize = service.getFileSize(emptyFile.toString());

            // Then
            assertThat(fileSize).isZero();
        }
    }

    @Nested
    @DisplayName("Full file retrieval tests")
    class FullFileRetrievalTests
    {
        @Test
        @DisplayName("Should read entire file content correctly")
        void shouldReadEntireFileContentCorrectly() throws IOException
        {
            // Given
            byte[] expectedData = "Complete file content for testing".getBytes();
            Path testFile = tempDir.resolve("full.dat");
            Files.write(testFile, expectedData);

            // When
            Resource resource = service.getFullFile(testFile.toString());
            byte[] actualData = readResourceBytes(resource);

            // Then
            assertThat(actualData).isEqualTo(expectedData);
        }

        @Test
        @DisplayName("Should throw IOException for non-existent file")
        void shouldThrowIOExceptionForNonExistentFile()
        {
            // Given
            String nonExistentPath = tempDir.resolve("nonexistent.dat").toString();

            // When/Then
            assertThatThrownBy(() -> service.getFullFile(nonExistentPath))
                    .isInstanceOf(IOException.class)
                    .hasMessageContaining("File not found");
        }

        @Test
        @DisplayName("Should read empty file without error")
        void shouldReadEmptyFileWithoutError() throws IOException
        {
            // Given
            Path emptyFile = tempDir.resolve("empty.dat");
            Files.write(emptyFile, new byte[0]);

            // When
            Resource resource = service.getFullFile(emptyFile.toString());
            byte[] actualData = readResourceBytes(resource);

            // Then
            assertThat(actualData).isEmpty();
        }

        @Test
        @DisplayName("Should read large file correctly")
        void shouldReadLargeFileCorrectly() throws IOException
        {
            // Given
            byte[] largeData = new byte[10000];
            for (int i = 0; i < largeData.length; i++)
            {
                largeData[i] = (byte) (i % 256);
            }
            Path largeFile = tempDir.resolve("large.dat");
            Files.write(largeFile, largeData);

            // When
            Resource resource = service.getFullFile(largeFile.toString());
            byte[] actualData = readResourceBytes(resource);

            // Then
            assertThat(actualData).isEqualTo(largeData);
        }
    }

    @Nested
    @DisplayName("Range-based file retrieval tests")
    class RangeRetrievalTests
    {
        @Test
        @DisplayName("Should read specified byte range correctly")
        void shouldReadSpecifiedByteRangeCorrectly() throws IOException
        {
            // Given
            byte[] fileData = "0123456789ABCDEFGHIJ".getBytes();
            Path testFile = tempDir.resolve("range.dat");
            Files.write(testFile, fileData);

            // When
            Resource resource = service.getFileRange(testFile.toString(), 5, 9);
            byte[] actualData = readResourceBytes(resource);

            // Then
            assertThat(actualData).isEqualTo("56789".getBytes());
        }

        @Test
        @DisplayName("Should read single byte range")
        void shouldReadSingleByteRange() throws IOException
        {
            // Given
            byte[] fileData = "ABCDEFGH".getBytes();
            Path testFile = tempDir.resolve("single.dat");
            Files.write(testFile, fileData);

            // When
            Resource resource = service.getFileRange(testFile.toString(), 3, 3);
            byte[] actualData = readResourceBytes(resource);

            // Then
            assertThat(actualData).isEqualTo("D".getBytes());
        }

        @Test
        @DisplayName("Should read range from start of file")
        void shouldReadRangeFromStartOfFile() throws IOException
        {
            // Given
            byte[] fileData = "StartMiddleEnd".getBytes();
            Path testFile = tempDir.resolve("start.dat");
            Files.write(testFile, fileData);

            // When
            Resource resource = service.getFileRange(testFile.toString(), 0, 4);
            byte[] actualData = readResourceBytes(resource);

            // Then
            assertThat(actualData).isEqualTo("Start".getBytes());
        }

        @Test
        @DisplayName("Should read range to end of file")
        void shouldReadRangeToEndOfFile() throws IOException
        {
            // Given
            byte[] fileData = "StartMiddleEnd".getBytes();
            Path testFile = tempDir.resolve("end.dat");
            Files.write(testFile, fileData);

            // When
            Resource resource = service.getFileRange(testFile.toString(), 11, 13);
            byte[] actualData = readResourceBytes(resource);

            // Then
            assertThat(actualData).isEqualTo("End".getBytes());
        }

        @Test
        @DisplayName("Should read entire file as range")
        void shouldReadEntireFileAsRange() throws IOException
        {
            // Given
            byte[] fileData = "CompleteFile".getBytes();
            Path testFile = tempDir.resolve("complete.dat");
            Files.write(testFile, fileData);

            // When
            Resource resource = service.getFileRange(testFile.toString(), 0, fileData.length - 1);
            byte[] actualData = readResourceBytes(resource);

            // Then
            assertThat(actualData).isEqualTo(fileData);
        }

        @Test
        @DisplayName("Should throw IOException for non-existent file")
        void shouldThrowIOExceptionForNonExistentFile()
        {
            // Given
            String nonExistentPath = tempDir.resolve("nonexistent.dat").toString();

            // When/Then
            assertThatThrownBy(() -> service.getFileRange(nonExistentPath, 0, 10))
                    .isInstanceOf(IOException.class)
                    .hasMessageContaining("File not found");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException for negative start position")
        void shouldThrowIllegalArgumentExceptionForNegativeStart() throws IOException
        {
            // Given
            byte[] fileData = "TestData".getBytes();
            Path testFile = tempDir.resolve("negative.dat");
            Files.write(testFile, fileData);

            // When/Then
            assertThatThrownBy(() -> service.getFileRange(testFile.toString(), -1, 5))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid range");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException for end before start")
        void shouldThrowIllegalArgumentExceptionForEndBeforeStart() throws IOException
        {
            // Given
            byte[] fileData = "TestData".getBytes();
            Path testFile = tempDir.resolve("reversed.dat");
            Files.write(testFile, fileData);

            // When/Then
            assertThatThrownBy(() -> service.getFileRange(testFile.toString(), 5, 2))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid range");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException for end beyond file size")
        void shouldThrowIllegalArgumentExceptionForEndBeyondFileSize() throws IOException
        {
            // Given
            byte[] fileData = "ShortFile".getBytes();
            Path testFile = tempDir.resolve("short.dat");
            Files.write(testFile, fileData);

            // When/Then
            assertThatThrownBy(() -> service.getFileRange(testFile.toString(), 0, 100))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid range");
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException for start at file size")
        void shouldThrowIllegalArgumentExceptionForStartAtFileSize() throws IOException
        {
            // Given
            byte[] fileData = "TestData".getBytes();
            Path testFile = tempDir.resolve("atsize.dat");
            Files.write(testFile, fileData);

            // When/Then
            assertThatThrownBy(() -> service.getFileRange(testFile.toString(), fileData.length, fileData.length))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid range");
        }

        @Test
        @DisplayName("Should handle large range correctly")
        void shouldHandleLargeRangeCorrectly() throws IOException
        {
            // Given
            byte[] largeData = new byte[100000];
            for (int i = 0; i < largeData.length; i++)
            {
                largeData[i] = (byte) (i % 256);
            }
            Path largeFile = tempDir.resolve("largerange.dat");
            Files.write(largeFile, largeData);

            // When
            Resource resource = service.getFileRange(largeFile.toString(), 10000, 19999);
            byte[] actualData = readResourceBytes(resource);

            // Then
            assertThat(actualData).hasSize(10000);
            for (int i = 0; i < actualData.length; i++)
            {
                assertThat(actualData[i]).isEqualTo((byte) ((10000 + i) % 256));
            }
        }
    }

    /**
     * Helper method to read all bytes from a Resource's InputStream.
     *
     * @param resource the Resource to read from
     * @return byte array containing all data from the resource
     * @throws IOException if reading fails
     */
    private byte[] readResourceBytes(Resource resource) throws IOException
    {
        try (InputStream inputStream = resource.getInputStream();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream())
        {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1)
            {
                outputStream.write(buffer, 0, bytesRead);
            }
            return outputStream.toByteArray();
        }
    }
}

