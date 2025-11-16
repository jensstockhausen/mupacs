package de.famst.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

/**
 * Created by jens on 03/10/2016.
 */
@RepositoryRestResource(collectionResourceRel = "instances", path = "instances")
public interface InstanceRepository extends JpaRepository<InstanceEty, Long>
{
    InstanceEty findByInstanceUID(@Param("instanceUID") String instanceUID);

    List<InstanceEty> findBySeriesId(@Param("series_id") long seriesId);
}
