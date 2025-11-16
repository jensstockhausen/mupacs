package de.famst.controller;

import de.famst.service.FolderImportManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * REST controller for managing DICOM folder imports.
 * Provides endpoints to view and manage folder import operations.
 *
 * @author jens
 * @since 2016-10-03
 */
@Controller
public class FolderImportRestService
{
    private static final Logger LOG = LoggerFactory.getLogger(FolderImportRestService.class);

    private final FolderImportManager importManager;

    /**
     * Constructs a new FolderImportRestService.
     *
     * @param importManager the manager for folder import operations
     */
    public FolderImportRestService(FolderImportManager importManager)
    {
        this.importManager = importManager;
    }

    /**
     * Displays a list of all running and completed import operations.
     *
     * @param model the Spring MVC model to add attributes to
     * @return the name of the view template to render ("importList")
     */
    @GetMapping("/importlist")
    public String getListOfRunningImports(Model model)
    {
        LOG.info("Retrieving list of import operations");

        model.addAttribute("imports", importManager.getRunningImports());
        return "importList";
    }


}
