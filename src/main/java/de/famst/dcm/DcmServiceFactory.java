package de.famst.dcm;

import de.famst.service.FolderImportManager;
import org.dcm4che3.net.service.DicomServiceRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * Created by jens on 23/10/2016.
 */
@Component
public class DcmServiceFactory
{

    @Value("${mupacs.import}")
    private String importFolder;

    @Inject
    private FolderImportManager importManager;

    public DcmStoreSCP createDcmStoreSCPDevice(DicomServiceRegistry dicomServiceRegistry)
    {
        DcmStoreSCP dcmStoreSCP = new DcmStoreSCP(dicomServiceRegistry, importManager);

        dcmStoreSCP.setImportFolder(importFolder);

        return dcmStoreSCP;
    }

}
