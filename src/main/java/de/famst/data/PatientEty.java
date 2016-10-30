package de.famst.data;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by jens on 05/10/2016.
 */
@Entity
@Table(
        name = "PATIENT",
        uniqueConstraints={
                @UniqueConstraint(
                        name="AK_PATIENTNAME",
                        columnNames={"patientName"})
        })
public class PatientEty
{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @OneToMany(fetch = FetchType.LAZY, mappedBy="patient")
    private List<StudyEty> studies;

    private String patientName;

    public PatientEty()
    {
        studies = new ArrayList<>();
    }

    public long getId()
    {
        return id;
    }

    public String getPatientName()
    {
        return patientName;
    }

    public void setPatientName(String patientName)
    {
        this.patientName = patientName;
    }

    public Collection<StudyEty> getStudies()
    {
        return studies;
    }

    public void addStudy(StudyEty study)
    {
        if (studies.contains(study))
        {
            return;
        }

        studies.add(study);
    }

}
