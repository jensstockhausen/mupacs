package de.famst.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Service for handling DICOM bulk data operations.
 *
 * <p>This service provides methods for reading DICOM files with support for:
 * <ul>
 *   <li>Full file retrieval</li>
 *   <li>Range-based partial file retrieval (for HTTP Range requests)</li>
 *   <li>File size queries</li>
 * </ul>
 *
 * @author jens
 * @since 2026-04-24
 */
@Service
public class DicomBulkDataService
{
    private static final Logger LOG = LoggerFactory.getLogger(DicomBulkDataService.class);

    /**
     * Gets the size of a file.
     *
     * @param filePath the path to the file
     * @return the file size in bytes
     * @throws IOException if the file cannot be accessed or does not exist
     */
    public long getFileSize(String filePath) throws IOException
    {
        Path path = Paths.get(filePath);

        if (!Files.exists(path))
        {
            LOG.error("File not found: {}", filePath);
            throw new IOException("File not found: " + filePath);
        }

        if (!Files.isRegularFile(path))
        {
            LOG.error("Path is not a regular file: {}", filePath);
            throw new IOException("Path is not a regular file: " + filePath);
        }

        long size = Files.size(path);
        LOG.debug("File size for {}: {} bytes", filePath, size);

        return size;
    }

    /**
     * Retrieves the full file as a Resource.
     *
     * @param filePath the path to the file
     * @return a Resource representing the full file
     * @throws IOException if the file cannot be read
     */
    public Resource getFullFile(String filePath) throws IOException
    {
        Path path = Paths.get(filePath);

        if (!Files.exists(path))
        {
            LOG.error("File not found: {}", filePath);
            throw new IOException("File not found: " + filePath);
        }

        LOG.debug("Reading full file: {}", filePath);

        File file = path.toFile();
        InputStream inputStream = new FileInputStream(file);

        return new InputStreamResource(inputStream);
    }

    /**
     * Retrieves a range of bytes from a file as a Resource.
     * This supports HTTP Range requests.
     *
     * @param filePath the path to the file
     * @param start the starting byte position (inclusive)
     * @param end the ending byte position (inclusive)
     * @return a Resource representing the requested byte range
     * @throws IOException if the file cannot be read or range is invalid
     */
    public Resource getFileRange(String filePath, long start, long end) throws IOException
    {
        Path path = Paths.get(filePath);

        if (!Files.exists(path))
        {
            LOG.error("File not found: {}", filePath);
            throw new IOException("File not found: " + filePath);
        }

        long fileSize = Files.size(path);

        // Validate range
        if (start < 0 || end < start || end >= fileSize)
        {
            LOG.error("Invalid range: start={}, end={}, fileSize={}", start, end, fileSize);
            throw new IllegalArgumentException(
                    String.format("Invalid range: start=%d, end=%d, fileSize=%d", start, end, fileSize));
        }

        LOG.debug("Reading file range: {} bytes {}-{}", filePath, start, end);

        InputStream inputStream = new RangeInputStream(path.toFile(), start, end);

        return new InputStreamResource(inputStream);
    }

    /**
     * InputStream implementation that reads a specific byte range from a file.
     * This class ensures that only the specified range is read and properly closes resources.
     */
    private static class RangeInputStream extends InputStream
    {
        private final FileInputStream fileInputStream;
        private long remainingBytes;

        /**
         * Creates a new RangeInputStream.
         *
         * @param file the file to read from
         * @param start the starting byte position (inclusive)
         * @param end the ending byte position (inclusive)
         * @throws IOException if the file cannot be opened or positioned
         */
        public RangeInputStream(File file, long start, long end) throws IOException
        {
            this.fileInputStream = new FileInputStream(file);
            this.remainingBytes = end - start + 1;

            // Skip to start position
            long skipped = fileInputStream.skip(start);
            if (skipped != start)
            {
                fileInputStream.close();
                throw new IOException("Failed to skip to start position: " + start);
            }
        }

        @Override
        public int read() throws IOException
        {
            if (remainingBytes <= 0)
            {
                return -1; // End of range
            }

            int data = fileInputStream.read();
            if (data != -1)
            {
                remainingBytes--;
            }

            return data;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException
        {
            if (remainingBytes <= 0)
            {
                return -1; // End of range
            }

            // Don't read more than the remaining bytes in the range
            int toRead = (int) Math.min(len, remainingBytes);

            int bytesRead = fileInputStream.read(b, off, toRead);
            if (bytesRead > 0)
            {
                remainingBytes -= bytesRead;
            }

            return bytesRead;
        }

        @Override
        public void close() throws IOException
        {
            fileInputStream.close();
        }

        @Override
        public int available()
        {
            return (int) Math.min(remainingBytes, Integer.MAX_VALUE);
        }
    }
}

