package de.famst;

import de.famst.data.PatientEty;
import de.famst.data.PatientRepository;
import de.famst.data.StudyEty;
import de.famst.data.StudyRepository;
import de.famst.dcm.PatientStudyFinder;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
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
public class FindTest
{
    @Inject
    PatientRepository patientRepository;

    @Inject
    StudyRepository studyRepository;

    @Inject
    PatientStudyFinder finder;

    @Before
    public void addData()
    {
        for (int patIdx = 0; patIdx<10; patIdx++)
        {
            PatientEty patientEty = new PatientEty();

            patientEty.setPatientId(String.format("PID_%03d", patIdx));
            patientEty.setPatientName(String.format("Demo_%03d", patIdx));

            patientRepository.save(patientEty);

            for (int stuIdx= 0; stuIdx < 3; stuIdx ++)
            {
                StudyEty studyEty = new StudyEty();

                studyEty.setStudyInstanceUID(String.format("1.2.48.2.3.4.%02d.%02d", patIdx, stuIdx));
                studyEty.setPatient(patientEty);

                studyRepository.save(studyEty);

                patientEty.addStudy(studyEty);
                patientRepository.save(patientEty);
            }

        }
    }


    /*
        (0008,0005) CS [ISO_IR 100] SpecificCharacterSet
        (0008,0020) DA [] StudyDate
        (0008,0030) TM [] StudyTime
        (0008,0050) SH [] AccessionNumber
        (0008,0052) CS [STUDY] QueryRetrieveLevel
        (0008,0061) CS [] ModalitiesInStudy
        (0008,0080) LO [] InstitutionName
        (0008,0090) PN [] ReferringPhysicianName
        (0008,1030) LO [] StudyDescription
        (0008,1050) PN [] PerformingPhysicianName
        (0010,0010) PN [DEMO*] PatientName
        (0010,0020) LO [] PatientID
        (0010,0030) DA [] PatientBirthDate
        (0020,000D) UI [] StudyInstanceUID
        (0020,0010) SH [] StudyID
        (0020,1208) IS [] NumberOfStudyRelatedInstances
        (0032,4000) LT [] StudyComments
        (4008,0212) CS [] InterpretationStatusID
    */

    public Attributes createKeys()
    {
        Attributes keys = new Attributes();

        keys.setSpecificCharacterSet("ISO_IR 100");
        keys.setString(Tag.QueryRetrieveLevel, VR.CS, "STUDY");
        keys.setString(Tag.PatientName, VR.PN, "Demo*");
        keys.setString(Tag.StudyInstanceUID, VR.UI, "");

        return keys;
    }


    @Test
    @Transactional
    public void can_find_patient() throws Exception
    {
        List<PatientEty> patients = finder.findPatients(createKeys());

        assertThat(patients, hasSize(10));
    }

    @Test
    @Transactional
    public void can_find_studies_for_patient() throws Exception
    {
        List<PatientEty> patients = finder.findPatients(createKeys());
        List<StudyEty> studies = finder.findStudies(patients);

        assertThat(studies, hasSize(30));

    }
}
