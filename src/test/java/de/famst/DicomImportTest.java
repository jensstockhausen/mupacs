package de.famst;

import de.famst.data.*;
import de.famst.service.DicomImportService;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.hibernate.SessionFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;

import static de.famst.AssertException.ThrowableAssertion.assertThrown;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Created by jens on 08/10/2016.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class DicomImportTest
{
    @Inject
    DicomImportService dicomImportService;

    @Inject
    private InstanceRepository instanceRepository;

    @Inject
    private SeriesRepository seriesRepository;

    @Inject
    private StudyRepository studyRepository;

    @Inject
    private PatientRepository patientRepository;


    private Attributes createDcm(String name, String prefix, String s, String i)
    {
        Attributes dcm = new Attributes();

        dcm.setString(Tag.PatientName, VR.PN, name);
        dcm.setString(Tag.StudyInstanceUID, VR.UI, prefix);
        dcm.setString(Tag.SeriesInstanceUID, VR.UI, prefix + "." + s);
        dcm.setString(Tag.SOPInstanceUID, VR.UI, prefix + "."+ s + "." + i);
        return dcm;
    }


    private void importDcm()
    {
        Attributes dcm;
        dcm = createDcm("Doe^John", "1.2.3.4", "1", "1");
        dicomImportService.dicomToDatabase(dcm);

        dcm = createDcm("Doe^John", "1.2.3.4", "1", "2");
        dicomImportService.dicomToDatabase(dcm);

        dcm = createDcm("Doe^John", "1.2.3.4", "2", "1");
        dicomImportService.dicomToDatabase(dcm);

        dcm = createDcm("Doe^John", "1.2.3.4", "2", "2");
        dicomImportService.dicomToDatabase(dcm);
    }

    @Test
    @Transactional
    public void instances_are_children_of_series() throws Exception
    {
        importDcm();

        InstanceEty instanceEty = instanceRepository.findByInstanceUID("1.2.3.4.1.1");
        assertThat(instanceEty.getSeries(), is(notNullValue()));
        assertThat(instanceEty.getSeries().getSeriesInstanceUID(), is(equalTo("1.2.3.4.1")));

        SeriesEty seriesEty = seriesRepository.findBySeriesInstanceUID("1.2.3.4.1");
        assertThat(seriesEty, is(notNullValue()));
        assertThat(seriesEty.getInstances(), hasSize(2));
    }

    @Test
    @Transactional
    public void series_are_children_of_studies() throws Exception
    {
        importDcm();

        SeriesEty seriesEty = seriesRepository.findBySeriesInstanceUID("1.2.3.4.1");
        assertThat(seriesEty, is(notNullValue()));
        assertThat(seriesEty.getStudy().getStudyInstanceUID(), is(equalTo("1.2.3.4")));

        StudyEty studyEty = studyRepository.findByStudyInstanceUID("1.2.3.4");
        assertThat(studyEty, is(notNullValue()));
        assertThat(studyEty.getSeries(), hasSize(2));
    }

    @Test
    @Transactional
    public void studies_are_children_of_patients() throws Exception
    {
        importDcm();

        StudyEty studyEty = studyRepository.findByStudyInstanceUID("1.2.3.4");
        assertThat(studyEty, is(notNullValue()));
        assertThat(studyEty.getPatient().getPatientName(), is(equalTo("Doe^John")));

        PatientEty patientEty = patientRepository.findByPatientName("Doe^John");
        assertThat(patientEty, is(notNullValue()));
        assertThat(patientEty.getStudies(), hasSize(1));
    }

    @Test
    @Transactional
    public void hierarchy_is_created() throws Exception
    {
        importDcm();

        assertThat(patientRepository.findByPatientName("Doe^John").getStudies(), hasSize(1));
        assertThat(studyRepository.findByStudyInstanceUID("1.2.3.4").getSeries(), hasSize(2));
        assertThat(seriesRepository.findBySeriesInstanceUID("1.2.3.4.1").getInstances(), hasSize(2));
    }

    @Test
    @Transactional
    public void cannot_add_patient_with_same_name_twice() throws Exception
    {
        PatientEty patientEtyA = new PatientEty();
        patientEtyA.setPatientName("Doe^John");

        PatientEty patientEtyB = new PatientEty();
        patientEtyB.setPatientName("Doe^John");

        patientRepository.save(patientEtyA);

        assertThrown(() -> patientRepository.save(patientEtyB))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @Transactional
    public void can_get_studies_for_patient() throws Exception
    {
        importDcm();

        long patientId = patientRepository.findByPatientName("Doe^John").getId();
        assertThat(studyRepository.findByPatientId(patientId), hasSize(1));
    }



}
