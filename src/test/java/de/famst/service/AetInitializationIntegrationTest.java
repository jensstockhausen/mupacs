package de.famst.service;

import de.famst.data.AetEty;
import de.famst.data.AetRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration test for AET initialization from application-test.properties
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("AET Initialization Integration Tests")
class AetInitializationIntegrationTest
{
    @Autowired
    private AetRepository aetRepository;

    @Autowired
    private AetInitializationService aetInitializationService;

    @DisplayName("Should initialize AETs from application configuration on startup")
    @Test
    void testAetsAreInitializedFromConfiguration()
    {
        // The @PostConstruct method should have already initialized AETs

        // Verify both test AETs were created
        List<AetEty> allAets = aetRepository.findAll();
        assertTrue(allAets.size() >= 2, "At least 1 AETs should be initialized from config");

        // Verify TEST_PACS1
        Optional<AetEty> pacs1 = aetRepository.findByAet("TEST_PACS1");
        assertTrue(pacs1.isPresent(), "TEST_PACS1 should exist");
        assertEquals("localhost", pacs1.get().getHost());
        assertEquals(104, pacs1.get().getPort());
        assertEquals("TEST_PACS1@localhost:104", pacs1.get().getConnectionString());

        // Verify TEST_PACS2
        Optional<AetEty> pacs2 = aetRepository.findByAet("TEST_PACS2");
        assertTrue(pacs2.isPresent(), "TEST_PACS2 should exist");
        assertEquals("192.168.1.100", pacs2.get().getHost());
        assertEquals(11112, pacs2.get().getPort());
        assertEquals("TEST_PACS2@192.168.1.100:11112", pacs2.get().getConnectionString());
    }

    @DisplayName("Should not create duplicate AETs on re-initialization")
    @Test
    void testReInitializationDoesNotCreateDuplicates()
    {
        // Get initial count
        long initialCount = aetRepository.count();

        // Call initialization again (should skip existing AETs)
        aetInitializationService.initializeAets();

        // Count should remain the same
        assertEquals(initialCount, aetRepository.count(),
            "Re-initialization should not create duplicate AETs");
    }
}
