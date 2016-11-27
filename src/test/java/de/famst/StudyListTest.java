package de.famst;

import de.famst.controller.StudyListController;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.inject.Inject;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;



/**
 * Created by jens on 09/10/2016.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class StudyListTest
{
    private MockMvc mockMvc;

    @Inject
    private StudyListController studyListController;

    @Inject
    WebApplicationContext wac;

    @Before
    public void setup()
    {
        mockMvc = MockMvcBuilders.standaloneSetup(studyListController).build();
    }

    @Test
    public void test_study_list() throws Exception
    {
        MvcResult mvcResult = mockMvc.perform(get("/studylist?patientId=1"))
            .andExpect(status().isOk())
            .andExpect(model().size(2))
            .andExpect(view().name("studyList"))
            .andDo(MockMvcResultHandlers.print())
            .andReturn();

    }
}
