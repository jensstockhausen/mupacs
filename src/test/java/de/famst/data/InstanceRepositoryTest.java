package de.famst.data;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataAccessException;

import static de.famst.AssertException.ThrowableAssertion.assertThrown;

/**
 * Test for InstanceRepository to verify unique constraint enforcement.
 * Tests that duplicate DICOM instances cannot be inserted into the database.
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
        instanceEtyA.setPath("/some/path");

        InstanceEty instanceEtyB = new InstanceEty();
        instanceEtyB.setInstanceUID("1.2.3.4");
        instanceEtyB.setPath("/some/path");

        // Save and flush first instance to complete the transaction
        instanceRepository.saveAndFlush(instanceEtyA);

        // Attempt to save duplicate - should throw constraint violation
        assertThrown(() -> instanceRepository.saveAndFlush(instanceEtyB))
            .isInstanceOf(DataAccessException.class);
    }

}
