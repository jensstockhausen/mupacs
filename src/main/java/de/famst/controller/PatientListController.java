package de.famst.controller;

import de.famst.data.PatientEty;
import de.famst.data.PatientRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.inject.Inject;
import javax.ws.rs.GET;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jens on 10/10/2016.
 */
@Controller
public class PatientListController
{
    @Inject
    PatientRepository patientRepository;

    @GET
    @RequestMapping("/patientlist")
    public String getListOfPatients(Model model)
    {
        Iterable<PatientEty> patientEtyList = patientRepository.findAll();
        List<PatientModel> patients = new ArrayList<>();

        patientEtyList.forEach(patientEty ->
                patients.add(PatientModel.fromPatientEty(patientEty))
        );

        model.addAttribute("patients", patients);

        return "patientList";
    }



}
