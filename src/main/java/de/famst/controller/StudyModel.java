package de.famst.controller;

import de.famst.data.StudyEty;

/**
 * Created by jens on 10/10/2016.
 */
public class StudyModel
{
    private final StudyEty studyEty;
    private final long id;
    private final long numberOfSeries;

    public StudyModel()
    {
        studyEty = null;
        id = -1;
        numberOfSeries = 0;
    }

    public StudyModel(long id, long numberOfSeries, StudyEty studyEty)
    {
        this.id = id;
        this.numberOfSeries = numberOfSeries;
        this.studyEty = studyEty;
    }

    public static StudyModel fromStudyEty(StudyEty studyEty)
    {
        if (null == studyEty)
        {
            return new StudyModel();
        }

        return new StudyModel(
            studyEty.getId(),
            studyEty.getSeries().size(),
            studyEty
        );
    }

    public long getId()
    {
        return id;
    }

    public long getNumberOfSeries()
    {
        return numberOfSeries;
    }

    public StudyEty ety()
    {
        return studyEty;
    }
}
