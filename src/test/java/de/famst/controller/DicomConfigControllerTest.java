package de.famst.controller;

import de.famst.data.AetEty;
import de.famst.data.AetRepository;
import de.famst.dcm.DcmServiceRegisty;
import de.famst.service.DcmClientService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(DicomConfigController.class)
class DicomConfigControllerTest
{
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DcmServiceRegisty mockDcmServiceRegistry;

    @MockitoBean
    private AetRepository mockAetRepository;

    @MockitoBean
    private DcmClientService mockDcmClientService;

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

        AetEty aet1 = new AetEty("PACS1", "host1.com", 104);
        AetEty aet2 = new AetEty("PACS2", "host2.com", 105);
        List<AetEty> aets = Arrays.asList(aet1, aet2);
        given(mockAetRepository.findAll()).willReturn(aets);

        mockMvc.perform(get("/dicomconfig"))
            .andExpect(status().isOk())
            .andExpect(view().name("dicomConfig"))
            .andExpect(model().attributeExists("config"))
            .andExpect(model().attributeExists("aets"))
            .andExpect(model().attributeExists("newAet"))
            .andExpect(model().attribute("aets", hasSize(2)))
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
    void testGetDicomConfiguration_EmptyAetList() throws Exception
    {
        // Setup mock data
        given(mockDcmServiceRegistry.getAeTitle()).willReturn("MUPACS");
        given(mockDcmServiceRegistry.getHost()).willReturn("localhost");
        given(mockDcmServiceRegistry.getPort()).willReturn(11112);
        given(mockDcmServiceRegistry.isRunning()).willReturn(true);
        given(mockDcmServiceRegistry.getDeviceName()).willReturn("MUPACS-DEVICE");
        given(mockDcmServiceRegistry.getRegisteredServicesCount()).willReturn(3);
        given(mockAetRepository.findAll()).willReturn(Collections.emptyList());

        mockMvc.perform(get("/dicomconfig"))
            .andExpect(status().isOk())
            .andExpect(view().name("dicomConfig"))
            .andExpect(model().attribute("aets", hasSize(0)));
    }

    @Test
    void testAddAet() throws Exception
    {
        given(mockAetRepository.existsByAet("NEW_AET")).willReturn(false);

        mockMvc.perform(post("/dicomconfig/aet/add")
                .param("aet", "NEW_AET")
                .param("host", "newhost.com")
                .param("port", "104"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/dicomconfig"))
            .andExpect(flash().attributeExists("successMessage"));

        verify(mockAetRepository, times(1)).save(any(AetEty.class));
    }

    @Test
    void testAddAet_AlreadyExists() throws Exception
    {
        given(mockAetRepository.existsByAet("EXISTING_AET")).willReturn(true);

        mockMvc.perform(post("/dicomconfig/aet/add")
                .param("aet", "EXISTING_AET")
                .param("host", "host.com")
                .param("port", "104"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/dicomconfig"))
            .andExpect(flash().attributeExists("errorMessage"));

        verify(mockAetRepository, never()).save(any(AetEty.class));
    }

    @Test
    void testAddAet_InvalidPort() throws Exception
    {
        mockMvc.perform(post("/dicomconfig/aet/add")
                .param("aet", "NEW_AET")
                .param("host", "host.com")
                .param("port", "70000"))
            .andExpect(status().is3xxRedirection())
            .andExpect(flash().attributeExists("errorMessage"));
    }

    @Test
    void testEditAet() throws Exception
    {
        AetEty existingAet = new AetEty("OLD_AET", "oldhost.com", 104);
        given(mockAetRepository.findById(1L)).willReturn(Optional.of(existingAet));
        given(mockAetRepository.findByAet("UPDATED_AET")).willReturn(Optional.empty());

        mockMvc.perform(post("/dicomconfig/aet/edit/1")
                .param("aet", "UPDATED_AET")
                .param("host", "newhost.com")
                .param("port", "105"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/dicomconfig"))
            .andExpect(flash().attributeExists("successMessage"));

        verify(mockAetRepository, times(1)).save(any(AetEty.class));
    }

    @Test
    void testEditAet_NotFound() throws Exception
    {
        given(mockAetRepository.findById(999L)).willReturn(Optional.empty());

        mockMvc.perform(post("/dicomconfig/aet/edit/999")
                .param("aet", "SOME_AET")
                .param("host", "host.com")
                .param("port", "104"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/dicomconfig"))
            .andExpect(flash().attributeExists("errorMessage"));

        verify(mockAetRepository, never()).save(any(AetEty.class));
    }

    @Test
    void testEditAet_ConflictWithExisting() throws Exception
    {
        AetEty existingAet = new AetEty("OLD_AET", "oldhost.com", 104);
        AetEty conflictAet = new AetEty("CONFLICT_AET", "conflict.com", 105);

        given(mockAetRepository.findById(1L)).willReturn(Optional.of(existingAet));
        given(mockAetRepository.findByAet("CONFLICT_AET")).willReturn(Optional.of(conflictAet));

        mockMvc.perform(post("/dicomconfig/aet/edit/1")
                .param("aet", "CONFLICT_AET")
                .param("host", "newhost.com")
                .param("port", "106"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/dicomconfig"))
            .andExpect(flash().attributeExists("errorMessage"));

        verify(mockAetRepository, never()).save(any(AetEty.class));
    }

    @Test
    void testDeleteAet() throws Exception
    {
        AetEty aet = new AetEty("DELETE_ME", "host.com", 104);
        given(mockAetRepository.findById(1L)).willReturn(Optional.of(aet));

        mockMvc.perform(post("/dicomconfig/aet/delete/1"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/dicomconfig"))
            .andExpect(flash().attributeExists("successMessage"));

        verify(mockAetRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteAet_NotFound() throws Exception
    {
        given(mockAetRepository.findById(999L)).willReturn(Optional.empty());

        mockMvc.perform(post("/dicomconfig/aet/delete/999"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/dicomconfig"))
            .andExpect(flash().attributeExists("errorMessage"));

        verify(mockAetRepository, never()).deleteById(anyLong());
    }

    @Test
    void testEchoAet_Success() throws Exception
    {
        AetEty aet = new AetEty("REMOTE_PACS", "localhost", 104);
        given(mockAetRepository.findById(1L)).willReturn(Optional.of(aet));
        given(mockDcmClientService.echoById(1L)).willReturn(true);

        mockMvc.perform(post("/dicomconfig/aet/echo/1"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/dicomconfig"))
            .andExpect(flash().attributeExists("successMessage"));

        verify(mockDcmClientService, times(1)).echoById(1L);
    }

    @Test
    void testEchoAet_Failure() throws Exception
    {
        AetEty aet = new AetEty("REMOTE_PACS", "localhost", 104);
        given(mockAetRepository.findById(1L)).willReturn(Optional.of(aet));
        given(mockDcmClientService.echoById(1L)).willReturn(false);

        mockMvc.perform(post("/dicomconfig/aet/echo/1"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/dicomconfig"))
            .andExpect(flash().attributeExists("errorMessage"));

        verify(mockDcmClientService, times(1)).echoById(1L);
    }

    @Test
    void testEchoAet_NotFound() throws Exception
    {
        given(mockAetRepository.findById(999L)).willReturn(Optional.empty());

        mockMvc.perform(post("/dicomconfig/aet/echo/999"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/dicomconfig"))
            .andExpect(flash().attributeExists("errorMessage"));

        verify(mockDcmClientService, never()).echoById(anyLong());
    }
}

