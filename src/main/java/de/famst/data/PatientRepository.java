package de.famst.data;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

/**
 * Created by jens on 05/10/2016.
 */
@RepositoryRestResource(collectionResourceRel = "patients", path = "patients")
public interface PatientRepository extends JpaRepository<PatientEty, Long>
{
    PatientEty findByPatientName(@Param("patientname") String patientName);

    PatientEty findByPatientId(String patientId);

    List<PatientEty> findByPatientNameLike(String patientName);

    @Query(value = "SELECT DISTINCT p FROM PatientEty p LEFT JOIN FETCH p.studies",
           countQuery = "SELECT COUNT(DISTINCT p) FROM PatientEty p")
    Page<PatientEty> findAllWithStudies(Pageable pageable);

}
