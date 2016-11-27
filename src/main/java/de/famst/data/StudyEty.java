package de.famst.data;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by jens on 03/10/2016.
 */
@Entity
@Table(
        name = "STUDY",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "AK_STUDYUID",
                        columnNames = {"studyInstanceUID"})
        })
public class StudyEty
{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "study")
    private List<SeriesEty> series;

    @ManyToOne
    private PatientEty patient;

    private String studyInstanceUID;
    private String studyId;
    private String studyDescription;
    private LocalDate studyDate;
    private LocalTime studyTime;
    private String accessionNumber;
    private String modalitiesInStudy;
    private String referringPhysicianName;

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

    public PatientEty getPatient()
    {
        return patient;
    }

    public void setPatient(PatientEty patient)
    {
        this.patient = patient;
    }

    public LocalDate getStudyDate()
    {
        return studyDate;
    }

    public void setStudyDate(LocalDate studyDate)
    {
        this.studyDate = studyDate;
    }

    public LocalTime getStudyTime()
    {
        return studyTime;
    }

    public void setStudyTime(LocalTime studyTime)
    {
        this.studyTime = studyTime;
    }

    public String getAccessionNumber()
    {
        return accessionNumber;
    }

    public void setAccessionNumber(String accessionNumber)
    {
        this.accessionNumber = accessionNumber;
    }

    public String getModalitiesInStudy()
    {
        return modalitiesInStudy;
    }

    public void setModalitiesInStudy(String modalitiesInStudy)
    {
        this.modalitiesInStudy = modalitiesInStudy;
    }

    public String getReferringPhysicianName()
    {
        return referringPhysicianName;
    }

    public void setReferringPhysicianName(String referringPhysicianName)
    {
        this.referringPhysicianName = referringPhysicianName;
    }

    public String getStudyId()
    {
        return studyId;
    }

    public void setStudyId(String studyId)
    {
        this.studyId = studyId;
    }

    public void setStudyDescription(String studyDescription)
    {
        this.studyDescription = studyDescription;
    }

    public String getStudyDescription()
    {
        return studyDescription;
    }
}
