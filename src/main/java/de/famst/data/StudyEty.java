package de.famst.data;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by jens on 03/10/2016.
 */
@Entity
@Table(
        name = "STUDY",
        uniqueConstraints={
                @UniqueConstraint(
                        name="AK_STUDYUID",
                        columnNames={"studyInstanceUID"})
        })
public class StudyEty
{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @OneToMany(fetch = FetchType.LAZY, mappedBy="study")
    private List<SeriesEty> series;

    @ManyToOne
    private PatientEty patient;

    private String studyInstanceUID;

    public StudyEty()
    {
        series = new ArrayList<>();
    }

    public long getId()
    {
        return id;
    }

    public String getStudyInstanceUID()
    {
        return studyInstanceUID;
    }

    public void setStudyInstanceUID(String studyInstanceUID)
    {
        this.studyInstanceUID = studyInstanceUID;
    }

    public Collection<SeriesEty> getSeries()
    {
        return series;
    }

    public void addSeries(SeriesEty seriesEty)
    {
        if (series.contains(seriesEty))
        {
            return;
        }

        series.add(seriesEty);
    }

    public void setPatient(PatientEty patient)
    {
        this.patient = patient;
    }


    public PatientEty getPatient()
    {
        return patient;
    }
}
