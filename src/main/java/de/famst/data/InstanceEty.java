package de.famst.data;

import javax.persistence.*;

/**
 * Created by jens on 03/10/2016.
 */
@Entity
@Table(
        name = "INSTANCE",
        uniqueConstraints={
                @UniqueConstraint(
                        name="AK_INSTANCEUID",
                        columnNames={"instanceUID"})
        })
public class InstanceEty
{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private String instanceUID;

    @ManyToOne
    private SeriesEty series;

    public InstanceEty()
    {
        instanceUID = "";
    }

    public long getId()
    {
        return id;
    }

    public String getInstanceUID()
    {
        return instanceUID;
    }

    public void setInstanceUID(String instanceUID)
    {
        this.instanceUID = instanceUID;
    }

    public SeriesEty getSeries()
    {
        return series;
    }

    public void setSeries(SeriesEty series)
    {
        this.series = series;
    }
}
