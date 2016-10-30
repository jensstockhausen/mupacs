package de.famst.controller;

import de.famst.data.PatientEty;

/**
 * Created by jens on 10/10/2016.
 */
public class PatientModel
{
    private long id;
    private String patientsName;
    private long numberOfStudies;

    public PatientModel(long id, String patientsName, long numberOfStudies)
    {
        this.id = id;
        this.patientsName = patientsName;
        this.numberOfStudies = numberOfStudies;
    }

    public static PatientModel fromPatientEty(PatientEty patientEty)
    {
        return  new PatientModel(
                patientEty.getId(),
                patientEty.getPatientName(),
                patientEty.getStudies().size()
        );
    }


    public long getId()
    {
        return id;
    }

    public String getPatientsName()
    {
        return patientsName;
    }

    public long getNumberOfStudies()
    {
        return numberOfStudies;
    }


}
