package de.famst;

import de.famst.data.*;
import de.famst.util.DBFiller;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Created by jens on 31/10/2016.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class RepositoryTest
{
    @Inject
    DBFiller dbFiller;

    @Inject
    PatientRepository patientRepository;

    @Inject
    StudyRepository studyRepository;

    @Inject
    SeriesRepository seriesRepository;

    @Before
    public void addData()
    {
        dbFiller.fillDB();
    }

    @Test
    public void can_find_patient_by_exact_name() throws Exception
    {
        PatientEty patientEty = patientRepository.findByPatientName("Demo_005");

        assertThat(patientEty, is(notNullValue()));
        assertThat(patientEty.getPatientName(), is(equalTo("Demo_005")));
    }

    @Test
    public void can_find_patient_by_partial_name()
    {
        List<PatientEty> patients = patientRepository.findByPatientNameLike("Demo%5");

        assertThat(patients, hasSize(1));
        assertThat(patients.get(0), is(notNullValue()));
        assertThat(patients.get(0).getPatientName(), is(equalTo("Demo_005")));
    }

    @Test
    public void can_find_series_by_study_inst_uid()
    {
        StudyEty studyEty = studyRepository.findByStudyInstanceUID("1.2.48.1.1");
        List<SeriesEty> series = seriesRepository.findByStudyId(studyEty.getId());

        assertThat(series, hasSize(2));
    }


}
