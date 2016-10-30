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
        name = "SERIES",
        uniqueConstraints={
                @UniqueConstraint(
                        name="AK_SERIESUID",
                        columnNames={"seriesInstanceUID"})
        })
public class SeriesEty
{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private String seriesInstanceUID;

    @OneToMany(fetch = FetchType.LAZY, mappedBy="series")
    private List<InstanceEty> instances;

    @ManyToOne
    private StudyEty study;

    public SeriesEty()
    {
        seriesInstanceUID = "";
        instances = new ArrayList<>();
    }

    public long getId()
    {
        return id;
    }

    public String getSeriesInstanceUID()
    {
        return seriesInstanceUID;
    }

    public void setSeriesInstanceUID(String seriesInstanceUID)
    {
        this.seriesInstanceUID = seriesInstanceUID;
    }

    public Collection<InstanceEty> getInstances()
    {
        return instances;
    }

    public synchronized void addInstance(InstanceEty instanceEty)
    {
        if (instances.contains(instanceEty))
        {
            return;
        }

        instances.add(instanceEty);
    }

    public void setStudy(StudyEty study)
    {
        this.study = study;
    }

    public StudyEty getStudy()
    {
        return study;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }

        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        SeriesEty seriesEty = (SeriesEty) o;

        if (id != seriesEty.id)
        {
            return false;
        }

        return seriesInstanceUID.equals(seriesEty.seriesInstanceUID);

    }

    @Override
    public int hashCode()
    {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + seriesInstanceUID.hashCode();
        return result;
    }
}
