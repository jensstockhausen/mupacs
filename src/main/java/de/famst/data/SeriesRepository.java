package de.famst.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

/**
 * Created by jens on 05/10/2016.
 */
@RepositoryRestResource(collectionResourceRel = "series", path = "series")
public interface SeriesRepository extends JpaRepository<SeriesEty, Long>
{
    SeriesEty findBySeriesInstanceUID(@Param("seriesinstanceuid") String seriesInstanceUID);

    List<SeriesEty> findByStudyId(@Param("study_id") long studyId);
}
