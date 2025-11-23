package de.famst.controller;

import de.famst.dcm.DcmServiceRegisty;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller for displaying DICOM service configuration
 */
@Controller
public class DicomConfigController
{
    private final DcmServiceRegisty dcmServiceRegistry;

    public DicomConfigController(DcmServiceRegisty dcmServiceRegistry)
    {
        this.dcmServiceRegistry = dcmServiceRegistry;
    }

    @GetMapping("/dicomconfig")
    public String getDicomConfiguration(Model model)
    {
        DicomConfigModel config = new DicomConfigModel(
            dcmServiceRegistry.getAeTitle(),
            dcmServiceRegistry.getHost(),
            dcmServiceRegistry.getPort(),
            dcmServiceRegistry.isRunning(),
            dcmServiceRegistry.getDeviceName(),
            dcmServiceRegistry.getRegisteredServicesCount()
        );

        model.addAttribute("config", config);
        return "dicomConfig";
    }
}

