package de.famst.controller;

import de.famst.service.FolderImportManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Created by jens on 03/10/2016.
 */
@Controller
public class FolderImportRestService
{
    private static Logger LOG = LoggerFactory.getLogger(FolderImportRestService.class);

    @Inject
    private FolderImportManager importManager;

    @RequestMapping(value = "/importfolder", method = RequestMethod.GET)
    public @ResponseBody String importStudiesFromFolder(
            @RequestParam(value = "folder", required = true, defaultValue = "./") String folderName,
            HttpServletResponse response) throws InterruptedException
    {
        Path rootPath = Paths.get(folderName);

        importManager.addImport(rootPath);

        return "Importing " + rootPath.toAbsolutePath().toString();
    }

    @RequestMapping("/importlist")
    public  String getListOfRunningImports(Model model)
    {
        model.addAttribute("imports", importManager.runningImports());
        return "importList";
    }


}
