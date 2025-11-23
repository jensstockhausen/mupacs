package de.famst.controller;

import de.famst.data.AetEty;
import de.famst.data.AetRepository;
import de.famst.dcm.DcmServiceRegisty;
import de.famst.service.DcmClientService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

/**
 * Controller for displaying DICOM service configuration and managing AETs
 */
@Controller
public class DicomConfigController
{
    private final DcmServiceRegisty dcmServiceRegistry;
    private final AetRepository aetRepository;
    private final DcmClientService dcmClientService;

    public DicomConfigController(DcmServiceRegisty dcmServiceRegistry,
                                 AetRepository aetRepository,
                                 DcmClientService dcmClientService)
    {
        this.dcmServiceRegistry = dcmServiceRegistry;
        this.aetRepository = aetRepository;
        this.dcmClientService = dcmClientService;
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

        List<AetEty> aets = aetRepository.findAll();

        model.addAttribute("config", config);
        model.addAttribute("aets", aets);
        model.addAttribute("newAet", new AetEty());
        return "dicomConfig";
    }

    @PostMapping("/dicomconfig/aet/add")
    public String addAet(@RequestParam("aet") String aet,
                        @RequestParam("host") String host,
                        @RequestParam("port") int port,
                        RedirectAttributes redirectAttributes)
    {
        try
        {
            if (aetRepository.existsByAet(aet))
            {
                redirectAttributes.addFlashAttribute("errorMessage",
                    "AET '" + aet + "' already exists");
                return "redirect:/dicomconfig";
            }

            AetEty newAet = new AetEty(aet, host, port);
            aetRepository.save(newAet);
            redirectAttributes.addFlashAttribute("successMessage",
                "AET '" + aet + "' added successfully");
        }
        catch (Exception e)
        {
            redirectAttributes.addFlashAttribute("errorMessage",
                "Failed to add AET: " + e.getMessage());
        }

        return "redirect:/dicomconfig";
    }

    @PostMapping("/dicomconfig/aet/edit/{id}")
    public String editAet(@PathVariable("id") Long id,
                         @RequestParam("aet") String aet,
                         @RequestParam("host") String host,
                         @RequestParam("port") int port,
                         RedirectAttributes redirectAttributes)
    {
        try
        {
            Optional<AetEty> existingAet = aetRepository.findById(id);
            if (existingAet.isEmpty())
            {
                redirectAttributes.addFlashAttribute("errorMessage", "AET not found");
                return "redirect:/dicomconfig";
            }

            // Check if new AET name conflicts with another entry
            Optional<AetEty> conflict = aetRepository.findByAet(aet);
            if (conflict.isPresent() && conflict.get().getId() != id)
            {
                redirectAttributes.addFlashAttribute("errorMessage",
                    "AET '" + aet + "' already exists");
                return "redirect:/dicomconfig";
            }

            AetEty aetToUpdate = existingAet.get();
            aetToUpdate.setAet(aet);
            aetToUpdate.setHost(host);
            aetToUpdate.setPort(port);
            aetRepository.save(aetToUpdate);

            redirectAttributes.addFlashAttribute("successMessage",
                "AET updated successfully");
        }
        catch (Exception e)
        {
            redirectAttributes.addFlashAttribute("errorMessage",
                "Failed to update AET: " + e.getMessage());
        }

        return "redirect:/dicomconfig";
    }

    @PostMapping("/dicomconfig/aet/delete/{id}")
    public String deleteAet(@PathVariable("id") Long id, RedirectAttributes redirectAttributes)
    {
        try
        {
            Optional<AetEty> aet = aetRepository.findById(id);
            if (aet.isEmpty())
            {
                redirectAttributes.addFlashAttribute("errorMessage", "AET not found");
                return "redirect:/dicomconfig";
            }

            aetRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage",
                "AET '" + aet.get().getAet() + "' deleted successfully");
        }
        catch (Exception e)
        {
            redirectAttributes.addFlashAttribute("errorMessage",
                "Failed to delete AET: " + e.getMessage());
        }

        return "redirect:/dicomconfig";
    }

    @PostMapping("/dicomconfig/aet/echo/{id}")
    public String echoAet(@PathVariable("id") Long id, RedirectAttributes redirectAttributes)
    {
        try
        {
            Optional<AetEty> aet = aetRepository.findById(id);
            if (aet.isEmpty())
            {
                redirectAttributes.addFlashAttribute("errorMessage", "AET not found");
                return "redirect:/dicomconfig";
            }

            AetEty remote = aet.get();
            boolean success = dcmClientService.echoById(id);

            if (success)
            {
                redirectAttributes.addFlashAttribute("successMessage",
                    "C-ECHO successful to " + remote.getAet() + " (" + remote.getConnectionString() + ") - DICOM connection is working correctly.");
            }
            else
            {
                redirectAttributes.addFlashAttribute("errorMessage",
                    "C-ECHO failed to " + remote.getAet() + " (" + remote.getConnectionString() + ") - " + dcmClientService.getLastMessage());
            }
        }
        catch (Exception e)
        {
            redirectAttributes.addFlashAttribute("errorMessage",
                "C-ECHO error: " + e.getMessage());
        }

        return "redirect:/dicomconfig";
    }
}

