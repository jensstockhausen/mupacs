package de.famst.data;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataAccessException;

import static de.famst.AssertException.ThrowableAssertion.assertThrown;


@DataJpaTest
public class PatientRepositoryTest
{
    @Autowired
    private PatientRepository patientRepository;

    @Test
    public void cannotAddPatientWithSameNameTwice() throws Exception
    {
        PatientEty patientEtyA = new PatientEty();
        patientEtyA.setPatientName("Doe^John");

        PatientEty patientEtyB = new PatientEty();
        patientEtyB.setPatientName("Doe^John");

        patientRepository.saveAndFlush(patientEtyA);

        assertThrown(() -> patientRepository.saveAndFlush(patientEtyB))
            .isInstanceOf(DataAccessException.class);
    }

}
