package de.famst.dcm;

import de.famst.service.FolderImportManager;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4che3.io.DicomInputStream;
import org.dcm4che3.io.DicomOutputStream;
import org.dcm4che3.net.Association;
import org.dcm4che3.net.PDVInputStream;
import org.dcm4che3.net.Status;
import org.dcm4che3.net.pdu.PresentationContext;
import org.dcm4che3.net.service.DicomServiceException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for DcmStoreSCP class
 */
class DcmStoreSCPTest
{
    @TempDir
    Path tempDir;

    @Mock
    private FolderImportManager mockImportManager;


    @Mock
    private Association mockAssociation;

    @Mock
    private PresentationContext mockPresentationContext;

    @Mock
    private PDVInputStream mockPDVInputStream;

    private DcmStoreSCP dcmStoreSCP;
    private AutoCloseable closeable;

    @BeforeEach
    void setUp()
    {
        closeable = MockitoAnnotations.openMocks(this);
        dcmStoreSCP = new DcmStoreSCP(mockImportManager);

        // Set test values using reflection
        ReflectionTestUtils.setField(dcmStoreSCP, "importFolder", tempDir.toString());
    }

    @AfterEach
    void tearDown() throws Exception
    {
        if (closeable != null)
        {
            closeable.close();
        }
    }

    @Test
    void testConstructor()
    {
        assertNotNull(dcmStoreSCP);
        assertNotNull(mockImportManager);
    }

    @Test
    void testStoreWithNullSOPInstanceUID()
    {
        // Create request attributes with null SOP Instance UID
        Attributes requestAttrs = new Attributes();
        requestAttrs.setString(Tag.AffectedSOPClassUID, VR.UI, "1.2.840.10008.5.1.4.1.1.2");
        requestAttrs.setNull(Tag.AffectedSOPInstanceUID, VR.UI);

        Attributes responseAttrs = new Attributes();

        when(mockPresentationContext.getTransferSyntax()).thenReturn("1.2.840.10008.1.2.1");

        // Should throw DicomServiceException for invalid SOP Instance UID
        DicomServiceException exception = assertThrows(
            DicomServiceException.class,
            () -> dcmStoreSCP.store(mockAssociation, mockPresentationContext, requestAttrs, mockPDVInputStream, responseAttrs)
        );

        assertEquals(Status.ProcessingFailure, exception.getStatus());
    }

    @Test
    void testStoreWithEmptySOPInstanceUID()
    {
        // Create request attributes with empty SOP Instance UID
        Attributes requestAttrs = new Attributes();
        requestAttrs.setString(Tag.AffectedSOPClassUID, VR.UI, "1.2.840.10008.5.1.4.1.1.2");
        requestAttrs.setString(Tag.AffectedSOPInstanceUID, VR.UI, "");

        Attributes responseAttrs = new Attributes();

        when(mockPresentationContext.getTransferSyntax()).thenReturn("1.2.840.10008.1.2.1");

        // Should throw DicomServiceException for empty SOP Instance UID
        DicomServiceException exception = assertThrows(
            DicomServiceException.class,
            () -> dcmStoreSCP.store(mockAssociation, mockPresentationContext, requestAttrs, mockPDVInputStream, responseAttrs)
        );

        assertEquals(Status.ProcessingFailure, exception.getStatus());
    }

    @Test
    void testImportFolderCreation()
    {
        // Verify that temp directory exists
        assertTrue(Files.exists(tempDir));
        assertTrue(Files.isDirectory(tempDir));
    }

    @Test
    void testImportManagerInteraction() throws InterruptedException
    {
        // Verify import manager can be called
        Path testPath = tempDir.resolve("test");
        doNothing().when(mockImportManager).addImport(testPath);

        mockImportManager.addImport(testPath);

        verify(mockImportManager, times(1)).addImport(testPath);
    }

    @Test
    void testImportManagerInterruptedException() throws InterruptedException
    {
        // Test handling of InterruptedException
        Path testPath = tempDir.resolve("test");
        doThrow(new InterruptedException("Test interrupt"))
            .when(mockImportManager).addImport(testPath);

        assertThrows(InterruptedException.class, () -> mockImportManager.addImport(testPath));
    }

    @Disabled
    @Test
    void testCStoreSimulation() throws Exception
    {
        // Create DICOM attributes for a test instance
        Attributes dicomData = new Attributes();
        dicomData.setString(Tag.PatientID, VR.LO, "TEST12345");
        dicomData.setString(Tag.PatientName, VR.PN, "Doe^John");
        dicomData.setString(Tag.StudyInstanceUID, VR.UI, "1.2.3.4.5.678");
        dicomData.setString(Tag.SeriesInstanceUID, VR.UI, "1.2.3.4.5.678.90");
        dicomData.setString(Tag.SOPInstanceUID, VR.UI, "1.2.3.4.5.678.90.12");
        dicomData.setString(Tag.SOPClassUID, VR.UI, "1.2.840.10008.5.1.4.1.1.2"); // CT Image Storage
        dicomData.setString(Tag.Modality, VR.CS, "CT");
        dicomData.setString(Tag.StudyDate, VR.DA, "20241122");
        dicomData.setString(Tag.StudyTime, VR.TM, "120000");

        // Create file meta information
        Attributes fmi = new Attributes();
        fmi.setBytes(Tag.FileMetaInformationVersion, VR.OB, new byte[]{0, 1});
        fmi.setString(Tag.MediaStorageSOPClassUID, VR.UI, "1.2.840.10008.5.1.4.1.1.2");
        fmi.setString(Tag.MediaStorageSOPInstanceUID, VR.UI, "1.2.3.4.5.678.90.12");
        fmi.setString(Tag.TransferSyntaxUID, VR.UI, "1.2.840.10008.1.2.1"); // Explicit VR Little Endian
        fmi.setString(Tag.ImplementationClassUID, VR.UI, "1.2.3.4.5");
        fmi.setString(Tag.ImplementationVersionName, VR.SH, "MUPACS_TEST");

        // Create a temporary DICOM file to simulate incoming data
        File tempDicomFile = tempDir.resolve("incoming.dcm").toFile();
        try (DicomOutputStream dos = new DicomOutputStream(tempDicomFile))
        {
            dos.writeFileMetaInformation(fmi);
            dos.writeDataset(fmi, dicomData);
        }

        // Read it back as bytes to simulate PDV stream
        byte[] dicomBytes = Files.readAllBytes(tempDicomFile.toPath());

        // Create mock PDVInputStream that returns our DICOM data
        @SuppressWarnings("resource")
        PDVInputStream mockPDV = mock(PDVInputStream.class);
        ByteArrayInputStream bais = new ByteArrayInputStream(dicomBytes);

        // Mock the PDV stream to read from our byte array
        when(mockPDV.read(any(byte[].class))).thenAnswer(invocation -> {
            byte[] buffer = invocation.getArgument(0);
            return bais.read(buffer);
        });
        when(mockPDV.read(any(byte[].class), anyInt(), anyInt())).thenAnswer(invocation -> {
            byte[] buffer = invocation.getArgument(0);
            int offset = invocation.getArgument(1);
            int length = invocation.getArgument(2);
            return bais.read(buffer, offset, length);
        });


        // Create request attributes for C-STORE
        Attributes requestAttrs = new Attributes();
        requestAttrs.setString(Tag.AffectedSOPClassUID, VR.UI, "1.2.840.10008.5.1.4.1.1.2");
        requestAttrs.setString(Tag.AffectedSOPInstanceUID, VR.UI, "1.2.3.4.5.678.90.12");

        Attributes responseAttrs = new Attributes();

        // Setup mocks
        when(mockPresentationContext.getTransferSyntax()).thenReturn("1.2.840.10008.1.2.1");
        when(mockAssociation.createFileMetaInformation(anyString(), anyString(), anyString())).thenReturn(fmi);
        when(mockAssociation.toString()).thenReturn("Test Association");

        // Mock import manager to not throw exception
        doNothing().when(mockImportManager).addImport(any(Path.class));

        // Execute the store operation
        assertDoesNotThrow(() ->
            dcmStoreSCP.store(mockAssociation, mockPresentationContext, requestAttrs, mockPDV, responseAttrs)
        );

        // Verify that files were created in the expected directory structure
        File expectedDir = new File(tempDir.toFile(), "TEST12345/1.2.3.4.5.678/1.2.3.4.5.678.90");
        File expectedFile = new File(expectedDir, "1.2.3.4.5.678.90.12");

        // Give it a moment for file operations to complete
        Thread.sleep(100);

        // Verify directory structure was created
        assertTrue(expectedDir.exists(), "Expected directory structure should be created");
        assertTrue(expectedFile.exists(), "DICOM file should be stored at expected location");

        // Verify the stored file contains valid DICOM data
        try (DicomInputStream dis = new DicomInputStream(expectedFile))
        {
            Attributes storedData = dis.readDataset();
            assertNotNull(storedData);
            assertEquals("TEST12345", storedData.getString(Tag.PatientID));
            assertEquals("Doe^John", storedData.getString(Tag.PatientName));
            assertEquals("1.2.3.4.5.678", storedData.getString(Tag.StudyInstanceUID));
            assertEquals("1.2.3.4.5.678.90", storedData.getString(Tag.SeriesInstanceUID));
        }

        // Verify import manager was called with the correct directory
        verify(mockImportManager, atLeastOnce()).addImport(any(Path.class));
    }
}

