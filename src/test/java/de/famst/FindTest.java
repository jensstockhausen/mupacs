package de.famst;

import de.famst.data.PatientEty;
import de.famst.data.SeriesEty;
import de.famst.data.StudyEty;
import de.famst.dcm.PatientStudyFinder;
import de.famst.util.DBFiller;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

/**
 * Created by jens on 01/11/2016.
 */

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class FindTest
{
    @Inject
    DBFiller dbFiller;

    @Inject
    PatientStudyFinder finder;

    @Before
    public void addData()
    {
        dbFiller.fillDB();
    }

    public Attributes keysStudyByPatientName()
    {
        Attributes keys = new Attributes();

        keys.setSpecificCharacterSet("ISO_IR 100");
        keys.setString(Tag.QueryRetrieveLevel, VR.CS, "STUDY");
        keys.setString(Tag.PatientName, VR.PN, "Demo*");
        keys.setString(Tag.StudyInstanceUID, VR.UI, "");

        return keys;
    }

    public Attributes keysSeriesByStudyInstance()
    {
        Attributes keys = new Attributes();

        keys.setSpecificCharacterSet("ISO_IR 100");
        keys.setString(Tag.QueryRetrieveLevel, VR.CS, "SERIES");
        keys.setString(Tag.StudyInstanceUID, VR.UI, "1.2.48.1.1");
        keys.setString(Tag.SeriesInstanceUID, VR.UI, "");

        return keys;
    }



    /*
    (0008,0005) CS [ISO_IR 100] SpecificCharacterSet
    (0008,0021) DA [] SeriesDate
    (0008,0031) TM [] SeriesTime
    (0008,0052) CS [SERIES] QueryRetrieveLevel
    (0008,0060) CS [] Modality
    (0008,0080) LO [] InstitutionName
    (0008,0090) PN [] ReferringPhysicianName
    (0008,103E) LO [] SeriesDescription
    (0008,1050) PN [] PerformingPhysicianName
    (0020,000D) UI [] StudyInstanceUID
    (0020,000E) UI [] SeriesInstanceUID
    (0020,0011) IS [] SeriesNumber
    (0020,1209) IS [] NumberOfSeriesRelatedInstances
    (0032,4000) LT [] StudyComments
    (4008,0212) CS [] InterpretationStatusID
     */


    /*
    public Attributes keysStudyByPatientName()
    {
        Attributes keys = new Attributes();

        keys.setSpecificCharacterSet("ISO_IR 100");
        keys.setString(Tag.QueryRetrieveLevel, VR.CS, "STUDY");
        keys.setString(Tag.PatientName, VR.PN, "Demo*");
        keys.setString(Tag.StudyInstanceUID, VR.UI, "");

        return keys;
    }
    */


    @Test
    @Transactional
    public void can_find_patient() throws Exception
    {
        List<PatientEty> patients = finder.findPatients(keysStudyByPatientName());

        assertThat(patients, hasSize(10));
    }

    @Test
    @Transactional
    public void can_find_studies_for_patient() throws Exception
    {
        List<PatientEty> patients = finder.findPatients(keysStudyByPatientName());
        List<StudyEty> studies = finder.getStudiesForPatient(patients);

        assertThat(studies, hasSize(20));
    }

    @Test
    public void can_get_series() throws Exception
    {
        List<SeriesEty> series = finder.findSeries(keysSeriesByStudyInstance());

        assertThat(series, hasSize(2));
    }
}
