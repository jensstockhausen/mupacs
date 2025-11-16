package de.famst.data;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

/**
 * Created by jens on 03/10/2016.
 */

@RepositoryRestResource(collectionResourceRel = "studies", path = "studies")
public interface StudyRepository extends ListCrudRepository<StudyEty, Long>, PagingAndSortingRepository<StudyEty, Long>
{
    StudyEty findByStudyInstanceUID(@Param("studyinstanceuid") String studyInstanceUID);

    List<StudyEty> findByPatientId(@Param("patient_id") long patientId);
}
