package de.famst.service;

import de.famst.data.AetEty;
import de.famst.data.AetRepository;
import de.famst.dcm.DcmClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for DcmClientService
 */
class DcmClientServiceTest
{
    @Mock
    private DcmClient mockDcmClient;

    @Mock
    private AetRepository mockAetRepository;

    private DcmClientService dcmClientService;
    private AutoCloseable closeable;

    @BeforeEach
    void setUp()
    {
        closeable = MockitoAnnotations.openMocks(this);
        dcmClientService = new DcmClientService(mockDcmClient, mockAetRepository);
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
    void testEchoByName_Success()
    {
        AetEty aet = new AetEty("REMOTE_PACS", "localhost", 104);
        when(mockAetRepository.findByAet("REMOTE_PACS")).thenReturn(Optional.of(aet));
        when(mockDcmClient.echo("REMOTE_PACS", "localhost", 104)).thenReturn(true);

        boolean result = dcmClientService.echo("REMOTE_PACS");

        assertTrue(result);
        verify(mockDcmClient, times(1)).echo("REMOTE_PACS", "localhost", 104);
    }

    @Test
    void testEchoByName_AetNotFound()
    {
        when(mockAetRepository.findByAet("NONEXISTENT")).thenReturn(Optional.empty());

        boolean result = dcmClientService.echo("NONEXISTENT");

        assertFalse(result);
        verify(mockDcmClient, never()).echo(anyString(), anyString(), anyInt());
    }

    @Test
    void testEchoById_Success()
    {
        AetEty aet = new AetEty("REMOTE_PACS", "192.168.1.100", 11112);
        when(mockAetRepository.findById(1L)).thenReturn(Optional.of(aet));
        when(mockDcmClient.echo("REMOTE_PACS", "192.168.1.100", 11112)).thenReturn(true);

        boolean result = dcmClientService.echoById(1L);

        assertTrue(result);
        verify(mockDcmClient, times(1)).echo("REMOTE_PACS", "192.168.1.100", 11112);
    }

    @Test
    void testEchoById_AetNotFound()
    {
        when(mockAetRepository.findById(999L)).thenReturn(Optional.empty());

        boolean result = dcmClientService.echoById(999L);

        assertFalse(result);
        verify(mockDcmClient, never()).echo(anyString(), anyString(), anyInt());
    }

    @Test
    void testStoreByName_Success()
    {
        AetEty aet = new AetEty("REMOTE_PACS", "localhost", 104);
        File dicomFile = new File("test.dcm");

        when(mockAetRepository.findByAet("REMOTE_PACS")).thenReturn(Optional.of(aet));
        when(mockDcmClient.store("REMOTE_PACS", "localhost", 104, dicomFile)).thenReturn(true);

        boolean result = dcmClientService.store("REMOTE_PACS", dicomFile);

        assertTrue(result);
        verify(mockDcmClient, times(1)).store("REMOTE_PACS", "localhost", 104, dicomFile);
    }

    @Test
    void testStoreByName_AetNotFound()
    {
        File dicomFile = new File("test.dcm");
        when(mockAetRepository.findByAet("NONEXISTENT")).thenReturn(Optional.empty());

        boolean result = dcmClientService.store("NONEXISTENT", dicomFile);

        assertFalse(result);
        verify(mockDcmClient, never()).store(anyString(), anyString(), anyInt(), any(File.class));
    }

    @Test
    void testStoreById_Success()
    {
        AetEty aet = new AetEty("REMOTE_PACS", "localhost", 104);
        File dicomFile = new File("test.dcm");

        when(mockAetRepository.findById(1L)).thenReturn(Optional.of(aet));
        when(mockDcmClient.store("REMOTE_PACS", "localhost", 104, dicomFile)).thenReturn(true);

        boolean result = dcmClientService.storeById(1L, dicomFile);

        assertTrue(result);
        verify(mockDcmClient, times(1)).store("REMOTE_PACS", "localhost", 104, dicomFile);
    }

    @Test
    void testStoreDirectory_Success()
    {
        AetEty aet = new AetEty("REMOTE_PACS", "localhost", 104);
        File directory = new File("/tmp/dicom");

        when(mockAetRepository.findByAet("REMOTE_PACS")).thenReturn(Optional.of(aet));
        when(mockDcmClient.storeDirectory("REMOTE_PACS", "localhost", 104, directory)).thenReturn(5);

        int result = dcmClientService.storeDirectory("REMOTE_PACS", directory);

        assertEquals(5, result);
        verify(mockDcmClient, times(1)).storeDirectory("REMOTE_PACS", "localhost", 104, directory);
    }

    @Test
    void testStoreDirectory_AetNotFound()
    {
        File directory = new File("/tmp/dicom");
        when(mockAetRepository.findByAet("NONEXISTENT")).thenReturn(Optional.empty());

        int result = dcmClientService.storeDirectory("NONEXISTENT", directory);

        assertEquals(0, result);
        verify(mockDcmClient, never()).storeDirectory(anyString(), anyString(), anyInt(), any(File.class));
    }

    @Test
    void testStoreDirectoryById_Success()
    {
        AetEty aet = new AetEty("REMOTE_PACS", "localhost", 104);
        File directory = new File("/tmp/dicom");

        when(mockAetRepository.findById(1L)).thenReturn(Optional.of(aet));
        when(mockDcmClient.storeDirectory("REMOTE_PACS", "localhost", 104, directory)).thenReturn(3);

        int result = dcmClientService.storeDirectoryById(1L, directory);

        assertEquals(3, result);
        verify(mockDcmClient, times(1)).storeDirectory("REMOTE_PACS", "localhost", 104, directory);
    }

    @Test
    void testEchoDirect()
    {
        when(mockDcmClient.echo("DIRECT_AET", "direct.host.com", 11112)).thenReturn(true);

        boolean result = dcmClientService.echoDirect("DIRECT_AET", "direct.host.com", 11112);

        assertTrue(result);
        verify(mockDcmClient, times(1)).echo("DIRECT_AET", "direct.host.com", 11112);
        verify(mockAetRepository, never()).findByAet(anyString());
    }

    @Test
    void testStoreDirect()
    {
        File dicomFile = new File("test.dcm");
        when(mockDcmClient.store("DIRECT_AET", "direct.host.com", 11112, dicomFile)).thenReturn(true);

        boolean result = dcmClientService.storeDirect("DIRECT_AET", "direct.host.com", 11112, dicomFile);

        assertTrue(result);
        verify(mockDcmClient, times(1)).store("DIRECT_AET", "direct.host.com", 11112, dicomFile);
        verify(mockAetRepository, never()).findByAet(anyString());
    }
}

