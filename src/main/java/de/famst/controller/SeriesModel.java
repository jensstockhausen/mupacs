package de.famst.controller;

import de.famst.data.SeriesEty;

/**
 * Created by jens on 10/10/2016.
 */
public class SeriesModel
{
    private long id;
    private String seriesInstanceUID;
    private long numberOfInstances;

    public SeriesModel(long id, String seriesInstanceUID, long numberOfInstances)
    {
        this.id = id;
        this.seriesInstanceUID = seriesInstanceUID;
        this.numberOfInstances = numberOfInstances;
    }

    public static SeriesModel fromSeriesEty(SeriesEty seriesEty)
    {
        return new SeriesModel(
                seriesEty.getId(),
                seriesEty.getSeriesInstanceUID(),
                seriesEty.getInstances().size()
        );
    }

    public long getId()
    {
        return id;
    }

    public String getSeriesInstanceUID()
    {
        return seriesInstanceUID;
    }

    public long getNumberOfInstances()
    {
        return numberOfInstances;
    }
}
