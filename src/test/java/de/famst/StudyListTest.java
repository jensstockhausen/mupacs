package de.famst;

import de.famst.controller.StudyListController;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

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

    @Before
    public void setup()
    {
        mockMvc = MockMvcBuilders.standaloneSetup(studyListController).build();
    }

    @Ignore
    @Test
    public void test_study_list() throws Exception
    {
        MvcResult result = mockMvc.perform(get("/studylist"))
                .andExpect(status().isOk())
                .andExpect(model().size(1))
                .andExpect(view().name("studyList"))
                .andDo(MockMvcResultHandlers.print())
                //.andExpect(xpath("//tr[@id = 'studies.table.header']").nodeCount(1))
                .andReturn();
                //.andExpect(content().xml())

        String content = result.getResponse().getContentAsString();

    }
}
