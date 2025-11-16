package de.famst.data;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import static de.famst.AssertException.ThrowableAssertion.assertThrown;

/**
 * Created by jens on 08/10/2016.
 */
@DataJpaTest
public class InstanceRepositoryTest
{
  @Autowired
  InstanceRepository instanceRepository;

  @Test
  public void cannotInsertSameInstanceTwice() throws Exception
  {
    InstanceEty instanceEtyA = new InstanceEty();
    instanceEtyA.setInstanceUID("1.2.3.4");

    InstanceEty instanceEtyB = new InstanceEty();
    instanceEtyB.setInstanceUID("1.2.3.4");

    instanceRepository.save(instanceEtyA);

    assertThrown(() -> instanceRepository.save(instanceEtyB))
      .isInstanceOf(DataIntegrityViolationException.class);
  }

}
