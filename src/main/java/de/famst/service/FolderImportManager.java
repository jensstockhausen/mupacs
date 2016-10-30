package de.famst.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * Created by jens on 03/10/2016.
 */
@Service
public class FolderImportManager
{
    private static Logger LOG = LoggerFactory.getLogger(FolderImportManager.class);

    private FolderImportService folderImportService;

    private Map<String, Future<FolderImportInformation>> imports;

    /**
     * @param folderImportService
     */
    @Inject
    public FolderImportManager(FolderImportService folderImportService)
    {
        this.folderImportService = folderImportService;

        imports = new HashMap<>();
    }


    /**
     * Add a folder to be parsed recursively
     * all DICOM files found will be imported
     * @param rootPath
     * @throws InterruptedException
     */
    @Async
    public synchronized void addImport(Path rootPath) throws InterruptedException
    {
        LOG.info("adding import folder [{}]", rootPath);

        String key = rootPath.toAbsolutePath().toString();

        if (imports.containsKey(key))
        {
            return;
        }

        FolderImportInformation fi = new FolderImportInformation(rootPath);

        Future<FolderImportInformation> ffi = folderImportService.importInstances(fi);

        imports.put(key, ffi);
    }

    /**
     * Retuns the list of all running and finished imports
     * @return
     */
    public synchronized List<String> runningImports()
    {
        List<String> running = new ArrayList<>();

        for(Map.Entry<String, Future<FolderImportInformation>> entry: imports.entrySet())
        {
            String path = entry.getKey();
            Future<FolderImportInformation> fi = entry.getValue();

            if (!fi.isDone())
            {
                running.add(path);
            }
            else
            {
                running.add("DONE: " + path);
            }

        }

        return running;
    }

}
