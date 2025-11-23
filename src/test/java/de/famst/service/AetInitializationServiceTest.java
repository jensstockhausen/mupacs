package de.famst.service;

import de.famst.data.AetEty;
import de.famst.data.AetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for AetInitializationService
 */
class AetInitializationServiceTest
{
    @Mock
    private AetRepository mockAetRepository;

    private AetInitializationService service;
    private AutoCloseable closeable;

    @BeforeEach
    void setUp()
    {
        closeable = MockitoAnnotations.openMocks(this);
        service = new AetInitializationService(mockAetRepository);
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() throws Exception
    {
        if (closeable != null)
        {
            closeable.close();
        }
    }

    @Test
    void testParseAetConfiguration_SingleEntry()
    {
        String config = "PACS1@192.168.1.100:104";
        List<AetEty> aets = service.parseAetConfiguration(config);

        assertEquals(1, aets.size());
        assertEquals("PACS1", aets.get(0).getAet());
        assertEquals("192.168.1.100", aets.get(0).getHost());
        assertEquals(104, aets.get(0).getPort());
    }

    @Test
    void testParseAetConfiguration_MultipleEntries()
    {
        String config = "PACS1@192.168.1.100:104;PACS2@pacs.hospital.org:11112;REMOTE@10.0.0.50:8104";
        List<AetEty> aets = service.parseAetConfiguration(config);

        assertEquals(3, aets.size());

        assertEquals("PACS1", aets.get(0).getAet());
        assertEquals("192.168.1.100", aets.get(0).getHost());
        assertEquals(104, aets.get(0).getPort());

        assertEquals("PACS2", aets.get(1).getAet());
        assertEquals("pacs.hospital.org", aets.get(1).getHost());
        assertEquals(11112, aets.get(1).getPort());

        assertEquals("REMOTE", aets.get(2).getAet());
        assertEquals("10.0.0.50", aets.get(2).getHost());
        assertEquals(8104, aets.get(2).getPort());
    }

    @Test
    void testParseAetConfiguration_WithWhitespace()
    {
        String config = "  PACS1 @ 192.168.1.100 : 104  ;  PACS2@pacs.hospital.org:11112  ";
        List<AetEty> aets = service.parseAetConfiguration(config);

        assertEquals(2, aets.size());
        assertEquals("PACS1", aets.get(0).getAet());
        assertEquals("192.168.1.100", aets.get(0).getHost());
        assertEquals(104, aets.get(0).getPort());
    }

    @Test
    void testParseAetConfiguration_EmptyString()
    {
        List<AetEty> aets = service.parseAetConfiguration("");
        assertTrue(aets.isEmpty());

        aets = service.parseAetConfiguration("   ");
        assertTrue(aets.isEmpty());

        aets = service.parseAetConfiguration(null);
        assertTrue(aets.isEmpty());
    }

    @Test
    void testParseAetConfiguration_InvalidEntries()
    {
        // Invalid entries should be skipped
        String config = "VALID@host:104;invalid;ANOTHER@host2:105";
        List<AetEty> aets = service.parseAetConfiguration(config);

        // Only valid entries should be parsed
        assertEquals(2, aets.size());
        assertEquals("VALID", aets.get(0).getAet());
        assertEquals("ANOTHER", aets.get(1).getAet());
    }

    @Test
    void testParseAetConfiguration_MissingAtSymbol()
    {
        String config = "PACS1:192.168.1.100:104";
        List<AetEty> aets = service.parseAetConfiguration(config);
        assertTrue(aets.isEmpty());
    }

    @Test
    void testParseAetConfiguration_MissingColon()
    {
        String config = "PACS1@192.168.1.100";
        List<AetEty> aets = service.parseAetConfiguration(config);
        assertTrue(aets.isEmpty());
    }

    @Test
    void testParseAetConfiguration_InvalidPort()
    {
        String config = "PACS1@192.168.1.100:invalid";
        List<AetEty> aets = service.parseAetConfiguration(config);
        assertTrue(aets.isEmpty());
    }

    @Test
    void testParseAetConfiguration_EmptyAet()
    {
        String config = "@192.168.1.100:104";
        List<AetEty> aets = service.parseAetConfiguration(config);
        assertTrue(aets.isEmpty());
    }

    @Test
    void testParseAetConfiguration_EmptyHost()
    {
        String config = "PACS1@:104";
        List<AetEty> aets = service.parseAetConfiguration(config);
        assertTrue(aets.isEmpty());
    }

    @Test
    void testInitializeAets_EmptyConfiguration()
    {
        ReflectionTestUtils.setField(service, "aetConfigString", "");

        service.initializeAets();

        verify(mockAetRepository, never()).save(any(AetEty.class));
    }

    @Test
    void testInitializeAets_NullConfiguration()
    {
        ReflectionTestUtils.setField(service, "aetConfigString", null);

        service.initializeAets();

        verify(mockAetRepository, never()).save(any(AetEty.class));
    }

    @Test
    void testInitializeAets_CreatesNewAets()
    {
        ReflectionTestUtils.setField(service, "aetConfigString", "PACS1@host1:104;PACS2@host2:105");

        when(mockAetRepository.existsByAet(anyString())).thenReturn(false);

        service.initializeAets();

        ArgumentCaptor<AetEty> captor = ArgumentCaptor.forClass(AetEty.class);
        verify(mockAetRepository, times(2)).save(captor.capture());

        List<AetEty> savedAets = captor.getAllValues();
        assertEquals("PACS1", savedAets.get(0).getAet());
        assertEquals("PACS2", savedAets.get(1).getAet());
    }

    @Test
    void testInitializeAets_SkipsExistingAets()
    {
        ReflectionTestUtils.setField(service, "aetConfigString", "EXISTING@host1:104;NEW@host2:105");

        when(mockAetRepository.existsByAet("EXISTING")).thenReturn(true);
        when(mockAetRepository.existsByAet("NEW")).thenReturn(false);

        service.initializeAets();

        // Should only save the NEW AET
        ArgumentCaptor<AetEty> captor = ArgumentCaptor.forClass(AetEty.class);
        verify(mockAetRepository, times(1)).save(captor.capture());
        assertEquals("NEW", captor.getValue().getAet());
    }

    @Test
    void testInitializeAets_HandlesException()
    {
        ReflectionTestUtils.setField(service, "aetConfigString", "PACS1@host1:104");

        when(mockAetRepository.existsByAet("PACS1")).thenReturn(false);
        when(mockAetRepository.save(any(AetEty.class))).thenThrow(new RuntimeException("Database error"));

        // Should not throw exception, just log error
        assertDoesNotThrow(() -> service.initializeAets());
    }

    @Test
    void testParseAetConfiguration_IPv6Address()
    {
        String config = "PACS1@[2001:db8::1]:104";
        List<AetEty> aets = service.parseAetConfiguration(config);

        assertEquals(1, aets.size());
        assertEquals("PACS1", aets.get(0).getAet());
        assertEquals("[2001:db8::1]", aets.get(0).getHost());
        assertEquals(104, aets.get(0).getPort());
    }

    @Test
    void testParseAetConfiguration_DomainWithMultipleColons()
    {
        // Should use lastIndexOf to find port separator
        String config = "PACS1@sub.domain.com:11112";
        List<AetEty> aets = service.parseAetConfiguration(config);

        assertEquals(1, aets.size());
        assertEquals("sub.domain.com", aets.get(0).getHost());
        assertEquals(11112, aets.get(0).getPort());
    }
}

