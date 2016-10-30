package de.famst.data;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

/**
 * Created by jens on 03/10/2016.
 */
@RepositoryRestResource(collectionResourceRel = "instances", path = "instances")
public interface InstanceRepository extends PagingAndSortingRepository<InstanceEty, Long>
{
    InstanceEty findByInstanceUID(@Param("instanceUID") String instanceUID);
}
