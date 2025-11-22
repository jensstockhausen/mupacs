package de.famst;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Integration test for the MuPACS application.
 * Verifies that the Spring application context loads successfully with test configuration.
 */
@SpringBootTest
@ActiveProfiles("test")
public class MupacsApplicationTests {

	/**
	 * Tests that the Spring application context loads successfully.
	 * This verifies all beans can be created and autowired properly.
	 */
	@Test
	public void contextLoads()
	{
		// Context loads successfully if this test passes
	}

}
