package de.famst.data;

import static de.famst.AssertException.ThrowableAssertion.assertThrown;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
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

    patientRepository.save(patientEtyA);

    assertThrown(() -> patientRepository.save(patientEtyB))
      .isInstanceOf(DataIntegrityViolationException.class);
  }

}
