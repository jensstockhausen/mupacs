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

    @Inject
    private FolderImportService importService;

    private Map<String, Future<FolderImportInformation>> imports;

    public FolderImportManager()
    {
        imports = new HashMap<>();
    }


    @Async
    public synchronized void addImport(Path rootPath) throws InterruptedException
    {
        String key = rootPath.toAbsolutePath().toString();

        if (imports.containsKey(key))
        {
            return;
        }

        FolderImportInformation fi = new FolderImportInformation(rootPath);

        Future<FolderImportInformation> ffi = importService.importInstances(fi);

        imports.put(key, ffi);
    }

    public synchronized List<String> runningImports()
    {
        List<String> running = new ArrayList<>();

        for(String path:imports.keySet())
        {
            Future<FolderImportInformation> fi = imports.get(path);

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
