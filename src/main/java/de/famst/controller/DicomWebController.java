package de.famst.controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
/**
 * Controller for DICOM-Web services page.
 * Provides a user interface for accessing WADO-RS and QIDO-RS services.
 *
 * @author jens
 * @since 2026-04-24
 */
@Controller
public class DicomWebController
{
    private static final Logger LOG = LoggerFactory.getLogger(DicomWebController.class);
    /**
     * Displays the DICOM-Web services page.
     *
     * @param model the Spring MVC model to add attributes to
     * @return the name of the view template to render ("dicomWeb")
     */
    @GetMapping("/dicomweb")
    public String getDicomWebPage(Model model)
    {
        LOG.info("Displaying DICOM-Web services page");
        return "dicomWeb";
    }
}
