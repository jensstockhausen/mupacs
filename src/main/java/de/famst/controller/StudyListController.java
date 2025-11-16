package de.famst.controller;

import de.famst.data.PatientEty;
import de.famst.data.PatientRepository;
import de.famst.data.StudyEty;
import de.famst.data.StudyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jens on 09/10/2016.
 */
@Controller
public class StudyListController
{
  private static Logger LOG = LoggerFactory.getLogger(StudyListController.class);

  StudyRepository studyRepository;
  PatientRepository patientRepository;

  private StudyListController(StudyRepository studyRepository, PatientRepository patientRepository)
  {
    this.studyRepository = studyRepository;
    this.patientRepository = patientRepository;
  }

  @RequestMapping("/studylist")
  public String getListOfStudiesForPatient(@RequestParam("patientId") long patientId, Model model)
  {
    LOG.info("list of studies for patientId [{}]", patientId);

    List<StudyEty> studyEtyList;

    studyEtyList = studyRepository.findByPatientId(patientId);

    List<StudyModel> studies = new ArrayList<>();

    studyEtyList.forEach(studyEty ->
      studies.add(StudyModel.fromStudyEty(studyEty))
    );

    PatientEty patientEty = patientRepository.findById(patientId).orElse(null);
    PatientModel patientModel = PatientModel.fromPatientEty(patientEty);

    model.addAttribute("studies", studies);
    model.addAttribute("patient", patientModel);

    return "studyList";
  }


}
