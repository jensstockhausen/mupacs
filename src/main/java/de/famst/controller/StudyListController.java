package de.famst.controller;

import de.famst.data.StudyEty;
import de.famst.data.StudyRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.inject.Inject;
import javax.ws.rs.GET;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jens on 09/10/2016.
 */
@Controller
public class StudyListController
{
    @Inject
    StudyRepository studyRepository;

    private StudyListController()
    {
    }

    @GET
    @RequestMapping("/studylist")
    public String getListOfStudiesForPatient(@RequestParam("patientId") long patientId, Model model)
    {
        List<StudyEty> studyEtyList;

        studyEtyList = studyRepository.findByPatientId(patientId);

        List<StudyModel> studies = new ArrayList<>();

        studyEtyList.forEach(studyEty ->
                studies.add(StudyModel.fromStudyEty(studyEty))
        );

        model.addAttribute("studies", studies);

        return "studyList";
    }

}
