package de.famst.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * Unit tests for LogsController
 */
@WebMvcTest(LogsController.class)
class LogsControllerTest
{
    @Autowired
    private MockMvc mockMvc;

    @Test
    void testGetLogsPage() throws Exception
    {
        mockMvc.perform(get("/logs"))
            .andExpect(status().isOk())
            .andExpect(view().name("logs"))
            .andExpect(model().attributeExists("lines"));
    }

    @Test
    void testGetLogContent_Default() throws Exception
    {
        mockMvc.perform(get("/logs/content"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("text/plain;charset=UTF-8"));
    }

    @Test
    void testGetLogContent_CustomLines() throws Exception
    {
        mockMvc.perform(get("/logs/content")
                .param("lines", "100"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("text/plain;charset=UTF-8"));
    }

    @Test
    void testGetLogContent_LargeNumber() throws Exception
    {
        mockMvc.perform(get("/logs/content")
                .param("lines", "2000"))
            .andExpect(status().isOk())
            .andExpect(content().contentType("text/plain;charset=UTF-8"));
    }
}

