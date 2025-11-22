package de.famst.controller;

import de.famst.service.FolderImportManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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

    /**
     * Triggers a new folder import operation.
     *
     * @param folderPath the path to the folder to import
     * @param redirectAttributes attributes for redirect to show messages
     * @return redirect to the import list page
     */
    @PostMapping("/importlist/add")
    public String addFolderImport(
            @RequestParam("folderPath") String folderPath,
            RedirectAttributes redirectAttributes)
    {
        LOG.info("Request to import folder: {}", folderPath);

        if (folderPath == null || folderPath.trim().isEmpty())
        {
            LOG.warn("Empty folder path provided");
            redirectAttributes.addFlashAttribute("errorMessage", "Folder path cannot be empty");
            return "redirect:/importlist";
        }

        try
        {
            Path path = Paths.get(folderPath.trim());

            if (!Files.exists(path))
            {
                LOG.warn("Folder does not exist: {}", folderPath);
                redirectAttributes.addFlashAttribute("errorMessage", "Folder does not exist: " + folderPath);
                return "redirect:/importlist";
            }

            if (!Files.isDirectory(path))
            {
                LOG.warn("Path is not a directory: {}", folderPath);
                redirectAttributes.addFlashAttribute("errorMessage", "Path is not a directory: " + folderPath);
                return "redirect:/importlist";
            }

            importManager.addImport(path);
            LOG.info("Import successfully added for folder: {}", folderPath);
            redirectAttributes.addFlashAttribute("successMessage", "Import started for folder: " + folderPath);
        }
        catch (InterruptedException e)
        {
            LOG.error("Import interrupted for folder: {}", folderPath, e);
            Thread.currentThread().interrupt();
            redirectAttributes.addFlashAttribute("errorMessage", "Import was interrupted: " + e.getMessage());
        }
        catch (Exception e)
        {
            LOG.error("Error adding import for folder: {}", folderPath, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error starting import: " + e.getMessage());
        }

        return "redirect:/importlist";
    }


}
