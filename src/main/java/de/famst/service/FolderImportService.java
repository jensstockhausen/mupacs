package de.famst.service;

import de.famst.dcm.DcmFile;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service responsible for importing DICOM files from folders.
 * Recursively scans folders, identifies DICOM files, and imports them into the database.
 *
 * <p>This service performs asynchronous imports to avoid blocking the main application thread.
 * Each import operation tracks the number of files processed and provides status information.
 *
 * @author jens
 * @since 2016-10-03
 */
@Service
public class FolderImportService
{
    private static final Logger LOG = LoggerFactory.getLogger(FolderImportService.class);

    private final DicomImportService dicomImportService;

    /**
     * Constructs a new FolderImportService.
     *
     * @param dicomImportService the service responsible for importing DICOM data to the database
     */
    public FolderImportService(DicomImportService dicomImportService)
    {
        this.dicomImportService = dicomImportService;
    }

    /**
     * Asynchronously imports DICOM instances from a folder or single file.
     *
     * <p>If the path is a file, it will be imported if it's a valid DICOM file.
     * If the path is a directory, it will be recursively scanned for DICOM files.
     *
     * @param importInfo the import information containing the root path to import
     * @return a CompletableFuture containing the updated import information with results
     * @throws IllegalArgumentException if importInfo or its root path is null
     */
    @Async
    public CompletableFuture<FolderImportInformation> importInstances(FolderImportInformation importInfo)
    {
        if (importInfo == null)
        {
            throw new IllegalArgumentException("Import information cannot be null");
        }

        Path rootPath = importInfo.getRootPath();
        if (rootPath == null)
        {
            throw new IllegalArgumentException("Root path cannot be null");
        }

        LOG.info("Starting DICOM import from [{}]", rootPath.toAbsolutePath());

        AtomicInteger processedCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);

        try
        {
            if (Files.isRegularFile(rootPath))
            {
                importSingleFile(rootPath, importInfo, processedCount, errorCount);
            }
            else if (Files.isDirectory(rootPath))
            {
                importDirectory(rootPath, importInfo, processedCount, errorCount);
            }
            else
            {
                LOG.error("Path is neither a file nor a directory: [{}]", rootPath.toAbsolutePath());
                return CompletableFuture.completedFuture(importInfo);
            }

            LOG.info("DICOM import completed from [{}]. Processed: {}, Errors: {}",
                rootPath.toAbsolutePath(), processedCount.get(), errorCount.get());
        }
        catch (IOException e)
        {
            LOG.error("IOException while accessing path [{}]: {}", rootPath.toAbsolutePath(), e.getMessage(), e);
        }
        catch (Exception e)
        {
            LOG.error("Unexpected error during import from [{}]: {}", rootPath.toAbsolutePath(), e.getMessage(), e);
        }

        return CompletableFuture.completedFuture(importInfo);
    }

    /**
     * Imports a single DICOM file.
     */
    private void importSingleFile(Path filePath, FolderImportInformation importInfo,
                                  AtomicInteger processedCount, AtomicInteger errorCount)
    {
        if (DcmFile.isDCMFile(filePath.toFile()))
        {
            try
            {
                String sopInstanceUID = insertToDatabase(filePath);
                if (sopInstanceUID != null)
                {
                    importInfo.addInfo(sopInstanceUID);
                    processedCount.incrementAndGet();
                    LOG.debug("Imported DICOM file: [{}]", filePath.getFileName());
                }
                else
                {
                    errorCount.incrementAndGet();
                    LOG.warn("Failed to import DICOM file (no SOP Instance UID): [{}]", filePath);
                }
            }
            catch (Exception e)
            {
                errorCount.incrementAndGet();
                LOG.error("Error importing DICOM file [{}]: {}", filePath, e.getMessage(), e);
            }
        }
        else
        {
            LOG.debug("Skipping non-DICOM file: [{}]", filePath);
        }
    }

    /**
     * Recursively imports all DICOM files from a directory.
     */
    private void importDirectory(Path rootPath, FolderImportInformation importInfo,
                                 AtomicInteger processedCount, AtomicInteger errorCount) throws IOException
    {
        try (var stream = Files.walk(rootPath))
        {
            stream
                .filter(Files::isRegularFile)
                .filter(path -> DcmFile.isDCMFile(path.toFile()))
                .forEach(path ->
                {
                    try
                    {
                        String sopInstanceUID = insertToDatabase(path);
                        if (sopInstanceUID != null)
                        {
                            importInfo.addInfo(sopInstanceUID);
                            int count = processedCount.incrementAndGet();

                            if (count % 100 == 0)
                            {
                                LOG.info("Progress: {} DICOM files imported from [{}]", count, rootPath.getFileName());
                            }
                            else
                            {
                                LOG.debug("Imported DICOM file: [{}]", path.getFileName());
                            }
                        }
                        else
                        {
                            errorCount.incrementAndGet();
                            LOG.warn("Failed to import DICOM file (no SOP Instance UID): [{}]", path);
                        }
                    }
                    catch (Exception e)
                    {
                        errorCount.incrementAndGet();
                        LOG.error("Error importing DICOM file [{}]: {}", path, e.getMessage());
                    }
                });
        }
    }

    /**
     * Inserts a DICOM file into the database.
     *
     * <p>This method reads the DICOM content, extracts the necessary information,
     * and persists it to the database in a transactional manner.
     *
     * @param path the path to the DICOM file
     * @return the SOP Instance UID of the imported file, or null if import failed
     */
    @Transactional
    public String insertToDatabase(Path path)
    {
        if (path == null)
        {
            LOG.warn("Attempted to insert null path to database");
            return null;
        }

        try
        {
            Attributes dcmAttributes = DcmFile.readContent(path.toFile());

            if (dcmAttributes == null)
            {
                LOG.error("Failed to read DICOM content from file: [{}]", path);
                return null;
            }

            dicomImportService.dicomToDatabase(dcmAttributes, path);

            String sopInstanceUID = dcmAttributes.getString(Tag.SOPInstanceUID);

            if (sopInstanceUID == null || sopInstanceUID.isEmpty())
            {
                LOG.warn("DICOM file has no valid SOP Instance UID: [{}]", path);
            }

            return sopInstanceUID;
        }
        catch (Exception e)
        {
            LOG.error("Failed to insert DICOM file to database [{}]: {}", path, e.getMessage(), e);
            return null;
        }
    }

}
