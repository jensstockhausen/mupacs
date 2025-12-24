package de.famst.dcm;

import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.data.VR;
import org.dcm4che3.net.Association;
import org.dcm4che3.net.Dimse;
import org.dcm4che3.net.PDVInputStream;
import org.dcm4che3.net.pdu.PresentationContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

/**
 * Unit tests for DcmEchoSCP class
 */
@DisplayName("DcmEchoSCP Tests")
class DcmEchoSCPTest
{
    @Mock
    private Association mockAssociation;

    @Mock
    private PresentationContext mockPresentationContext;

    @Mock
    private PDVInputStream mockPDVInputStream;

    private DcmEchoSCP dcmEchoSCP;
    private AutoCloseable closeable;

    @BeforeEach
    void setUp()
    {
        closeable = MockitoAnnotations.openMocks(this);
        dcmEchoSCP = new DcmEchoSCP();
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() throws Exception
    {
        if (closeable != null)
        {
            closeable.close();
        }
    }

    @DisplayName("Should create DcmEchoSCP instance successfully")
    @Test
    void testConstructor()
    {
        assertNotNull(dcmEchoSCP);
    }

    @DisplayName("Should handle C-ECHO request without throwing exception")
    @Test
    void testCEchoRequest() throws Exception
    {
        // Create C-ECHO request attributes
        Attributes cmd = new Attributes();
        cmd.setInt(Tag.CommandField, VR.US, Dimse.C_ECHO_RQ.commandField());
        cmd.setInt(Tag.MessageID, VR.US, 1);
        cmd.setString(Tag.AffectedSOPClassUID, VR.UI, "1.2.840.10008.1.1"); // Verification SOP Class

        // Mock association
        when(mockAssociation.getRemoteAET()).thenReturn("TEST_AET");

        // Test that C-ECHO request is handled without throwing exception
        assertDoesNotThrow(() ->
            dcmEchoSCP.onDimseRQ(mockAssociation, mockPresentationContext, Dimse.C_ECHO_RQ, cmd, mockPDVInputStream)
        );
    }

    @DisplayName("Should handle multiple consecutive C-ECHO requests")
    @Test
    void testMultipleCEchoRequests() throws Exception
    {
        // Create C-ECHO request attributes
        Attributes cmd = new Attributes();
        cmd.setInt(Tag.CommandField, VR.US, Dimse.C_ECHO_RQ.commandField());
        cmd.setInt(Tag.MessageID, VR.US, 1);
        cmd.setString(Tag.AffectedSOPClassUID, VR.UI, "1.2.840.10008.1.1");

        when(mockAssociation.getRemoteAET()).thenReturn("TEST_AET");

        // Test multiple C-ECHO requests
        for (int i = 0; i < 5; i++)
        {
            cmd.setInt(Tag.MessageID, VR.US, i + 1);
            final int iteration = i;
            assertDoesNotThrow(() ->
                dcmEchoSCP.onDimseRQ(mockAssociation, mockPresentationContext, Dimse.C_ECHO_RQ, cmd, mockPDVInputStream),
                "C-ECHO request " + (iteration + 1) + " should not throw exception"
            );
        }
    }
}
