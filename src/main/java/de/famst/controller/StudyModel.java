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

    public static StudyModel fromStudyEty(StudyEty studyEty)
    {
        StudyModel studyModel = new StudyModel(
                studyEty.getId(),
                studyEty.getStudyInstanceUID(),
                studyEty.getSeries().size()

        );

        return studyModel;
    }


    public StudyModel(long id, String studyInstanceUID, long numberOfSeries)
    {
        this.id = id;
        this.studyInstanceUID = studyInstanceUID;
        this.numberOfSeries = numberOfSeries;
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
