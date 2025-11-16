package de.famst.data;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

/**
 * Created by jens on 05/10/2016.
 */
@RepositoryRestResource(collectionResourceRel = "patients", path = "patients")
public interface PatientRepository
    extends ListCrudRepository<PatientEty, Long>, PagingAndSortingRepository<PatientEty, Long>
{
    PatientEty findByPatientName(@Param("patientname") String patientName);

    List<PatientEty> findByPatientId(String patientId);

    List<PatientEty> findByPatientNameLike(String patientName);

}
