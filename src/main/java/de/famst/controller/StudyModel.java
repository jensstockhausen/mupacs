package de.famst.controller;

import de.famst.data.StudyEty;

/**
 * Created by jens on 10/10/2016.
 */
public class StudyModel
{
    private long id;
    private String studyInstanceUID;
    private long numberOfSeries;

    public StudyModel(long id, String studyInstanceUID, long numberOfSeries)
    {
        this.id = id;
        this.studyInstanceUID = studyInstanceUID;
        this.numberOfSeries = numberOfSeries;
    }

    public static StudyModel fromStudyEty(StudyEty studyEty)
    {
        return new StudyModel(
                studyEty.getId(),
                studyEty.getStudyInstanceUID(),
                studyEty.getSeries().size()

        );
    }



    public long getId()
    {
        return id;
    }

    public String getStudyInstanceUID()
    {
        return studyInstanceUID;
    }

    public long getNumberOfSeries()
    {
        return numberOfSeries;
    }
}
