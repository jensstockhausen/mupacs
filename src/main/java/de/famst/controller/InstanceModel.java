package de.famst.controller;

import de.famst.data.InstanceEty;
import de.famst.data.SeriesEty;

/**
 * Created by jens on 30/10/2016.
 */
public class InstanceModel
{
    private long id;
    private String instanceUID;

    public InstanceModel(long id, String instanceUID)
    {
        this.id = id;
        this.instanceUID = instanceUID;
    }

    public static InstanceModel fromInstanceEty(InstanceEty instanceEty)
    {
        return new InstanceModel(
                instanceEty.getId(),
                instanceEty.getInstanceUID()
        );
    }


    public long getId()
    {
        return id;
    }

    public String getInstanceUID()
    {
        return instanceUID;
    }
}
