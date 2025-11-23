package de.famst.controller;

import de.famst.dcm.DcmServiceRegisty;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(DicomConfigController.class)
class DicomConfigControllerTest
{
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DcmServiceRegisty mockDcmServiceRegistry;

    @Test
    void testGetDicomConfiguration() throws Exception
    {
        // Setup mock data
        given(mockDcmServiceRegistry.getAeTitle()).willReturn("MUPACS");
        given(mockDcmServiceRegistry.getHost()).willReturn("localhost");
        given(mockDcmServiceRegistry.getPort()).willReturn(11112);
        given(mockDcmServiceRegistry.isRunning()).willReturn(true);
        given(mockDcmServiceRegistry.getDeviceName()).willReturn("MUPACS-DEVICE");
        given(mockDcmServiceRegistry.getRegisteredServicesCount()).willReturn(3);

        mockMvc.perform(get("/dicomconfig"))
            .andExpect(status().isOk())
            .andExpect(view().name("dicomConfig"))
            .andExpect(model().attributeExists("config"))
            .andExpect(model().attribute("config",
                org.hamcrest.Matchers.hasProperty("aeTitle", is("MUPACS"))))
            .andExpect(model().attribute("config",
                org.hamcrest.Matchers.hasProperty("host", is("localhost"))))
            .andExpect(model().attribute("config",
                org.hamcrest.Matchers.hasProperty("port", is(11112))))
            .andExpect(model().attribute("config",
                org.hamcrest.Matchers.hasProperty("running", is(true))))
            .andExpect(model().attribute("config",
                org.hamcrest.Matchers.hasProperty("deviceName", is("MUPACS-DEVICE"))))
            .andExpect(model().attribute("config",
                org.hamcrest.Matchers.hasProperty("registeredServices", is(3))));
    }

    @Test
    void testDicomConfigModelHelperMethods() throws Exception
    {
        // Setup mock data
        given(mockDcmServiceRegistry.getAeTitle()).willReturn("TEST_AE");
        given(mockDcmServiceRegistry.getHost()).willReturn("192.168.1.100");
        given(mockDcmServiceRegistry.getPort()).willReturn(104);
        given(mockDcmServiceRegistry.isRunning()).willReturn(false);
        given(mockDcmServiceRegistry.getDeviceName()).willReturn("TEST-DEVICE");
        given(mockDcmServiceRegistry.getRegisteredServicesCount()).willReturn(3);

        mockMvc.perform(get("/dicomconfig"))
            .andExpect(status().isOk())
            .andExpect(model().attribute("config",
                org.hamcrest.Matchers.hasProperty("connectionString", is("192.168.1.100:104"))))
            .andExpect(model().attribute("config",
                org.hamcrest.Matchers.hasProperty("statusLabel", is("Stopped"))))
            .andExpect(model().attribute("config",
                org.hamcrest.Matchers.hasProperty("statusClass", is("label-default"))));
    }
}

