package de.famst.controller;

import de.famst.service.FolderImportManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.isA;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(FolderImportRestService.class)
public class FolderImportRestServiceTest
{
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FolderImportManager importManager;

    @Test
    public void canGetImportList() throws Exception
    {
        List<String> imports = new ArrayList<>();
        imports.add("/test/path");
        imports.add("DONE: /completed/path");

        given(importManager.getRunningImports()).willReturn(imports);

        mockMvc.perform(MockMvcRequestBuilders.get("/importlist"))
            .andExpect(status().isOk())
            .andExpect(model().attributeExists("imports"))
            .andExpect(model().attribute("imports", isA(List.class)))
            .andExpect(view().name("importList"));
    }
}

