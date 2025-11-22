package de.famst.controller;

import de.famst.data.PatientEty;
import de.famst.data.PatientRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jens on 10/10/2016.
 */
@Controller
public class PatientListController
{
  private static final int PAGE_SIZE = 10;

  PatientRepository patientRepository;

  private PatientListController(PatientRepository patientRepository)
  {
    this.patientRepository = patientRepository;
  }


  @RequestMapping("/patientlist")
  public String getListOfPatients(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      Model model)
  {
    // Ensure page size is reasonable
    if (size < 1 || size > 100) {
      size = PAGE_SIZE;
    }

    // Ensure page number is not negative
    if (page < 0) {
      page = 0;
    }

    // Create pageable object with sorting by patient name
    Pageable pageable = PageRequest.of(page, size, Sort.by("patientName").ascending());

    // Fetch paginated data with studies eagerly loaded
    Page<PatientEty> patientEtyPage = patientRepository.findAllWithStudies(pageable);
    List<PatientModel> patients = new ArrayList<>();

    patientEtyPage.forEach(patientEty ->
      patients.add(PatientModel.fromPatientEty(patientEty))
    );

    // Add pagination information to the model
    model.addAttribute("patients", patients);
    model.addAttribute("currentPage", page);
    model.addAttribute("totalPages", patientEtyPage.getTotalPages());
    model.addAttribute("totalItems", patientEtyPage.getTotalElements());
    model.addAttribute("pageSize", size);

    return "patientList";
  }


}
