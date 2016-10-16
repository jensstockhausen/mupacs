package de.famst.service;

import de.famst.dcm.DcmFile;
import de.famst.controller.FolderImportRestService;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Future;

/**
 * Created by jens on 03/10/2016.
 */
@Service
public class FolderImportService
{
    private static Logger LOG = LoggerFactory.getLogger(FolderImportRestService.class);

    @Inject
    private DicomImportService dicomImportService;

    @Async
    public Future<FolderImportInformation> importInstances(FolderImportInformation fi) throws InterruptedException
    {
        Path rootPath = fi.getRootPath();

        LOG.info("importing from [{}]", rootPath.toAbsolutePath());

        try
        {
            Files.walk(rootPath)
                    .filter(path -> !Files.isDirectory(path))
                    .filter(path -> DcmFile.isDCMFile(path.toFile()))
                    .forEach(path ->
                    {
                        //files.add(path.toAbsolutePath().toString());
                        fi.addInfo(parse(path));
                    });
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return null;
        }

        return new AsyncResult<>(fi);
    }

    @Transactional
    private String parse(Path p)
    {
        Attributes dcm = DcmFile.readContent(p.toFile());

        dicomImportService.dicomToDatabase(dcm);

        return dcm.getString(Tag.SOPInstanceUID);
    }


}