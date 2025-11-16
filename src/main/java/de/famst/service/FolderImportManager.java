package de.famst.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

/**
 * Manages concurrent DICOM folder import operations.
 * Tracks running and completed imports, preventing duplicate imports of the same folder.
 *
 * <p>This service is thread-safe and uses a concurrent map to track import tasks.
 * Each folder path is used as a unique key to prevent duplicate imports.
 *
 * @author jens
 * @since 2016-10-03
 */
@Service
public class FolderImportManager
{
    private static final Logger LOG = LoggerFactory.getLogger(FolderImportManager.class);

    private final FolderImportService folderImportService;
    private final Map<String, Future<FolderImportInformation>> imports;

    /**
     * Constructs a new FolderImportManager.
     *
     * @param folderImportService the service responsible for performing folder imports
     */
    public FolderImportManager(FolderImportService folderImportService)
    {
        this.folderImportService = folderImportService;
        this.imports = new ConcurrentHashMap<>();
    }

    /**
     * Adds a folder to be parsed recursively for DICOM files.
     * All DICOM files found will be imported asynchronously.
     *
     * <p>If the folder is already being imported or has been imported,
     * this method returns immediately without creating a duplicate import task.
     *
     * @param rootPath the root path of the folder to import
     * @throws IllegalArgumentException if rootPath is null
     * @throws InterruptedException     if the import process is interrupted
     */
    public void addImport(Path rootPath) throws InterruptedException
    {
        if (rootPath == null)
        {
            throw new IllegalArgumentException("Root path cannot be null");
        }

        String key = rootPath.toAbsolutePath().toString();

        if (imports.containsKey(key))
        {
            LOG.info("Import already exists for folder [{}], skipping", key);
            return;
        }

        LOG.info("Adding import for folder [{}]", key);

        FolderImportInformation importInfo = new FolderImportInformation(rootPath);
        Future<FolderImportInformation> importTask = folderImportService.importInstances(importInfo);

        imports.put(key, importTask);

        LOG.debug("Import task created for folder [{}]", key);
    }

    /**
     * Returns the list of all running and finished imports.
     *
     * <p>Running imports are prefixed with their absolute path.
     * Completed imports are prefixed with "DONE: " followed by their absolute path.
     *
     * @return a list of import status strings, never null
     */
    public List<String> getRunningImports()
    {
        List<String> running = new ArrayList<>();

        imports.forEach((path, future) ->
        {
            if (future.isDone())
            {
                running.add("DONE: " + path);
            }
            else
            {
                running.add(path);
            }
        });

        return running;
    }

    /**
     * Returns the total number of imports (both running and completed).
     *
     * @return the number of imports
     */
    public int getImportCount()
    {
        return imports.size();
    }

    /**
     * Returns the number of currently running (not completed) imports.
     *
     * @return the number of active imports
     */
    public long getActiveImportCount()
    {
        return imports.values().stream()
            .filter(future -> !future.isDone())
            .count();
    }

    /**
     * Checks if a specific folder path is currently being imported or has been imported.
     *
     * @param path the folder path to check
     * @return true if the path is being tracked, false otherwise
     */
    public boolean hasImport(Path path)
    {
        if (path == null)
        {
            return false;
        }
        return imports.containsKey(path.toAbsolutePath().toString());
    }

    /**
     * Removes completed import tasks from the tracking map.
     * This helps prevent memory leaks from accumulating completed tasks.
     *
     * @return the number of completed imports that were removed
     */
    public int cleanupCompletedImports()
    {
        int removed = 0;

        var iterator = imports.entrySet().iterator();
        while (iterator.hasNext())
        {
            var entry = iterator.next();
            if (entry.getValue().isDone())
            {
                iterator.remove();
                removed++;
                LOG.debug("Cleaned up completed import for [{}]", entry.getKey());
            }
        }

        if (removed > 0)
        {
            LOG.info("Cleaned up {} completed import(s)", removed);
        }

        return removed;
    }

}
