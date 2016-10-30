package de.famst.controller;

import de.famst.data.InstanceEty;

/**
 * Created by jens on 30/10/2016.
 */
public class InstanceModel
{
    private long id;
    private String instanceUID;
    private String path;

    public InstanceModel(long id, String instanceUID)
    {
        this.id = id;
        this.instanceUID = instanceUID;

        path = "asdf";
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

    public String getPath()
    {
        return path;
    }
}
