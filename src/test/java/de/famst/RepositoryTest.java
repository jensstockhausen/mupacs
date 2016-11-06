package de.famst;

import de.famst.data.PatientEty;
import de.famst.data.PatientRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Created by jens on 31/10/2016.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
public class RepositoryTest
{
    @Inject
    PatientRepository patientRepository;

    @Before
    public void addData()
    {
        PatientEty patientEty = new PatientEty();

        patientEty.setPatientId("PAT_ID");
        patientEty.setPatientName("PAT_NAME");

        patientRepository.save(patientEty);
    }

    @Test
    public void can_find_patient_by_exact_name() throws Exception
    {
        PatientEty patientEty = patientRepository.findByPatientName("PAT_NAME");

        assertThat(patientEty, is(notNullValue()));
        assertThat(patientEty.getPatientName(), is(equalTo("PAT_NAME")));
    }

    @Test
    public  void can_find_patient_by_partial_name()
    {
        List<PatientEty> patients = patientRepository.findByPatientNameLike("PAT%");

        assertThat(patients, hasSize(1));
        assertThat(patients.get(0), is(notNullValue()));
        assertThat(patients.get(0).getPatientName(), is(equalTo("PAT_NAME")));
    }

    


}
