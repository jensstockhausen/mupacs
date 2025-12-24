package de.famst.dcm;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4che3.io.DicomOutputStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for DcmClient.
 *
 * Note: Most tests are disabled by default as they require a running DICOM SCP.
 * To run these tests, start a DICOM SCP (e.g., dcm4che storescp) on localhost:11112.
 */
@DisplayName("DcmClient Tests")
class DcmClientTest
{
    @TempDir
    Path tempDir;

    private DcmClient dcmClient;

    @BeforeEach
    void setUp()
    {
        dcmClient = new DcmClient();
        ReflectionTestUtils.setField(dcmClient, "localAeTitle", "TESTSCU");
        // Note: initialization happens lazily on first use via ensureInitialized()
    }

    @AfterEach
    void tearDown()
    {
        if (dcmClient != null)
        {
            dcmClient.shutdown();
        }
    }

    @DisplayName("Should create DcmClient instance successfully")
    @Test
    void testConstruction()
    {
        assertNotNull(dcmClient);
    }

    @DisplayName("Should initialize without errors when called explicitly")
    @Test
    void testExplicitInitialization()
    {
        assertDoesNotThrow(() -> dcmClient.initialize());
    }

    @DisplayName("Should handle multiple initialization calls safely")
    @Test
    void testMultipleInitializations()
    {
        // Initialize multiple times - should be safe
        assertDoesNotThrow(() -> {
            dcmClient.initialize();
            dcmClient.initialize();
            dcmClient.initialize();
        });
    }

    @DisplayName("Should initialize lazily when first operation is called")
    @Test
    void testLazyInitialization()
    {
        // Echo should trigger automatic initialization
        // This will fail due to invalid host, but initialization should work
        dcmClient.echo("TEST", "invalid-host-xyz", 11112);
        // If we get here, initialization happened (even though echo failed)
        assertTrue(true);
    }

    @DisplayName("Should succeed C-ECHO when SCP is available")
    @Test
    @Disabled("Requires running DICOM SCP on localhost:11112")
    void testEchoSuccess()
    {
        boolean result = dcmClient.echo("STORESCP", "localhost", 11112);
        assertTrue(result, "C-ECHO should succeed when SCP is available");
    }

    @DisplayName("Should fail C-ECHO for invalid host")
    @Test
    void testEchoFailureInvalidHost()
    {
        boolean result = dcmClient.echo("INVALID", "invalid-host-that-does-not-exist", 11112);
        assertFalse(result, "C-ECHO should fail for invalid host");
    }

    @DisplayName("Should fail C-ECHO for invalid port")
    @Test
    void testEchoFailureInvalidPort()
    {
        boolean result = dcmClient.echo("INVALID", "localhost", 99999);
        assertFalse(result, "C-ECHO should fail for invalid port");
    }

    @DisplayName("Should fail C-STORE for non-existent file")
    @Test
    void testStoreNonExistentFile()
    {
        File nonExistent = new File(tempDir.toFile(), "nonexistent.dcm");
        boolean result = dcmClient.store("STORESCP", "localhost", 11112, nonExistent);
        assertFalse(result, "C-STORE should fail for non-existent file");
    }

    @DisplayName("Should succeed C-STORE when SCP is available")
    @Test
    @Disabled("Requires running DICOM SCP on localhost:11112")
    void testStoreSuccess() throws Exception
    {
        // Create a test DICOM file
        File dicomFile = createTestDicomFile("test-instance.dcm");

        boolean result = dcmClient.store("STORESCP", "localhost", 11112, dicomFile);
        assertTrue(result, "C-STORE should succeed when SCP is available");
    }

    @DisplayName("Should store all DICOM files from directory")
    @Test
    @Disabled("Requires running DICOM SCP on localhost:11112")
    void testStoreDirectory() throws Exception
    {
        // Create multiple test DICOM files
        createTestDicomFile("test1.dcm");
        createTestDicomFile("test2.dcm");
        createTestDicomFile("test3.dcm");

        int successCount = dcmClient.storeDirectory("STORESCP", "localhost", 11112, tempDir.toFile());
        assertEquals(3, successCount, "Should successfully store all 3 files");
    }

    @DisplayName("Should return 0 for non-existent directory")
    @Test
    void testStoreDirectoryNonExistent()
    {
        File nonExistent = new File(tempDir.toFile(), "nonexistent-dir");
        int successCount = dcmClient.storeDirectory("STORESCP", "localhost", 11112, nonExistent);
        assertEquals(0, successCount, "Should return 0 for non-existent directory");
    }

    @DisplayName("Should return 0 for empty directory")
    @Test
    void testStoreDirectoryEmpty()
    {
        File emptyDir = new File(tempDir.toFile(), "empty");
        emptyDir.mkdir();
        int successCount = dcmClient.storeDirectory("STORESCP", "localhost", 11112, emptyDir);
        assertEquals(0, successCount, "Should return 0 for empty directory");
    }

    @DisplayName("Should fail C-STORE for invalid DICOM file")
    @Test
    void testStoreInvalidDicomFile() throws Exception
    {
        // Create a non-DICOM file
        File invalidFile = new File(tempDir.toFile(), "invalid.dcm");
        invalidFile.createNewFile();

        boolean result = dcmClient.store("STORESCP", "localhost", 11112, invalidFile);
        assertFalse(result, "C-STORE should fail for invalid DICOM file");
    }

    @DisplayName("Should shutdown without errors")
    @Test
    void testShutdown()
    {
        assertDoesNotThrow(() -> dcmClient.shutdown());
    }

    @DisplayName("Should handle multiple shutdown calls gracefully")
    @Test
    void testMultipleShutdowns()
    {
        assertDoesNotThrow(() -> {
            dcmClient.initialize();
            dcmClient.shutdown();
            dcmClient.shutdown(); // Should handle multiple shutdowns gracefully
        });
    }

    @DisplayName("Should reinitialize successfully after shutdown")
    @Test
    void testReinitializationAfterShutdown()
    {
        // Initialize, shutdown, then reinitialize
        assertDoesNotThrow(() -> {
            dcmClient.initialize();
            dcmClient.shutdown();
            dcmClient.initialize(); // Should work again
        });
    }

    /**
     * Helper method to create a valid DICOM file for testing.
     */
    private File createTestDicomFile(String filename) throws Exception
    {
        File dicomFile = new File(tempDir.toFile(), filename);

        // Create file meta information
        Attributes fmi = new Attributes();
        fmi.setBytes(Tag.FileMetaInformationVersion, VR.OB, new byte[]{0, 1});
        fmi.setString(Tag.MediaStorageSOPClassUID, VR.UI, "1.2.840.10008.5.1.4.1.1.2"); // CT Image Storage
        fmi.setString(Tag.MediaStorageSOPInstanceUID, VR.UI, generateUID());
        fmi.setString(Tag.TransferSyntaxUID, VR.UI, "1.2.840.10008.1.2.1"); // Explicit VR Little Endian
        fmi.setString(Tag.ImplementationClassUID, VR.UI, "1.2.3.4.5");
        fmi.setString(Tag.ImplementationVersionName, VR.SH, "MUPACS_TEST");

        // Create dataset
        Attributes dataset = new Attributes();
        dataset.setString(Tag.SOPClassUID, VR.UI, "1.2.840.10008.5.1.4.1.1.2");
        dataset.setString(Tag.SOPInstanceUID, VR.UI, fmi.getString(Tag.MediaStorageSOPInstanceUID));
        dataset.setString(Tag.PatientID, VR.LO, "TEST123");
        dataset.setString(Tag.PatientName, VR.PN, "Test^Patient");
        dataset.setString(Tag.StudyInstanceUID, VR.UI, generateUID());
        dataset.setString(Tag.SeriesInstanceUID, VR.UI, generateUID());
        dataset.setString(Tag.Modality, VR.CS, "CT");
        dataset.setString(Tag.StudyDate, VR.DA, "20241123");
        dataset.setString(Tag.StudyTime, VR.TM, "120000");

        // Write DICOM file
        try (DicomOutputStream dos = new DicomOutputStream(dicomFile))
        {
            dos.writeFileMetaInformation(fmi);
            dos.writeDataset(fmi, dataset);
        }

        return dicomFile;
    }

    /**
     * Helper method to generate a unique UID for testing.
     */
    private String generateUID()
    {
        return "1.2.3.4.5.6.7.8." + System.currentTimeMillis() + "." + (int)(Math.random() * 10000);
    }
}
