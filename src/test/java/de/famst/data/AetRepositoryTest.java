package de.famst.data;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests for AetEty entity and AetRepository.
 */
@DataJpaTest
class AetRepositoryTest
{
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private AetRepository aetRepository;

    @Test
    void testCreateAndFindAet()
    {
        // Create and persist an AET
        AetEty aet = new AetEty("PACS_REMOTE", "192.168.1.100", 104);
        AetEty savedAet = aetRepository.save(aet);

        // Verify it was saved
        assertTrue(savedAet.getId() > 0);

        // Find by AET
        Optional<AetEty> found = aetRepository.findByAet("PACS_REMOTE");
        assertTrue(found.isPresent());
        assertEquals("PACS_REMOTE", found.get().getAet());
        assertEquals("192.168.1.100", found.get().getHost());
        assertEquals(104, found.get().getPort());
    }

    @Test
    void testAetUniquenessConstraint()
    {
        // Create first AET
        AetEty aet1 = new AetEty("DUPLICATE_AET", "host1.example.com", 104);
        aetRepository.save(aet1);
        entityManager.flush();

        // Try to create second AET with same AET name
        AetEty aet2 = new AetEty("DUPLICATE_AET", "host2.example.com", 105);

        // Should throw exception due to unique constraint
        assertThrows(Exception.class, () -> {
            aetRepository.save(aet2);
            entityManager.flush();
        });
    }

    @Test
    void testFindByHost()
    {
        // Create multiple AETs with same host
        aetRepository.save(new AetEty("AET1", "shared-host.com", 104));
        aetRepository.save(new AetEty("AET2", "shared-host.com", 105));
        aetRepository.save(new AetEty("AET3", "other-host.com", 104));

        // Find by host
        List<AetEty> results = aetRepository.findByHost("shared-host.com");
        assertEquals(2, results.size());
    }

    @Test
    void testFindByPort()
    {
        // Create multiple AETs with same port
        aetRepository.save(new AetEty("AET1", "host1.com", 11112));
        aetRepository.save(new AetEty("AET2", "host2.com", 11112));
        aetRepository.save(new AetEty("AET3", "host3.com", 104));

        // Find by port
        List<AetEty> results = aetRepository.findByPort(11112);
        assertEquals(2, results.size());
    }

    @Test
    void testFindByHostAndPort()
    {
        // Create AETs
        aetRepository.save(new AetEty("AET1", "specific-host.com", 104));
        aetRepository.save(new AetEty("AET2", "specific-host.com", 105));

        // Find by host and port combination
        Optional<AetEty> found = aetRepository.findByHostAndPort("specific-host.com", 104);
        assertTrue(found.isPresent());
        assertEquals("AET1", found.get().getAet());

        Optional<AetEty> notFound = aetRepository.findByHostAndPort("specific-host.com", 999);
        assertFalse(notFound.isPresent());
    }

    @Test
    void testExistsByAet()
    {
        // Create an AET
        aetRepository.save(new AetEty("EXISTS_AET", "host.com", 104));

        // Test exists
        assertTrue(aetRepository.existsByAet("EXISTS_AET"));
        assertFalse(aetRepository.existsByAet("NONEXISTENT_AET"));
    }

    @Test
    void testAetValidation()
    {
        // Test null AET
        assertThrows(IllegalArgumentException.class, () -> new AetEty(null, "host.com", 104));

        // Test empty AET
        assertThrows(IllegalArgumentException.class, () -> new AetEty("", "host.com", 104));

        // Test null host
        assertThrows(IllegalArgumentException.class, () -> new AetEty("AET", null, 104));

        // Test empty host
        assertThrows(IllegalArgumentException.class, () -> new AetEty("AET", "", 104));

        // Test invalid port (too low)
        assertThrows(IllegalArgumentException.class, () -> new AetEty("AET", "host.com", 0));

        // Test invalid port (too high)
        assertThrows(IllegalArgumentException.class, () -> new AetEty("AET", "host.com", 65536));
    }

    @Test
    void testConnectionString()
    {
        AetEty aet = new AetEty("TEST_AET", "pacs.hospital.org", 11112);
        assertEquals("TEST_AET@pacs.hospital.org:11112", aet.getConnectionString());
    }

    @Test
    void testAetTrimming()
    {
        // AET and host should be trimmed
        AetEty aet = new AetEty("  TRIMMED_AET  ", "  trimmed-host.com  ", 104);
        assertEquals("TRIMMED_AET", aet.getAet());
        assertEquals("trimmed-host.com", aet.getHost());
    }

    @Test
    void testEqualsAndHashCode()
    {
        AetEty aet1 = new AetEty("SAME_AET", "host1.com", 104);
        AetEty aet2 = new AetEty("SAME_AET", "host2.com", 105);
        AetEty aet3 = new AetEty("DIFFERENT_AET", "host1.com", 104);

        // Same AET name should be equal
        assertEquals(aet1, aet2);
        assertEquals(aet1.hashCode(), aet2.hashCode());

        // Different AET name should not be equal
        assertNotEquals(aet1, aet3);
    }
}

