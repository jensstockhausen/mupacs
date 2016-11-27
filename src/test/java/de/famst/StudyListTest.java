package de.famst;

import de.famst.controller.StudyListController;
import de.famst.dcm.LoadDICOMFromJSON;
import de.famst.service.DicomImportService;
import org.dcm4che3.data.Attributes;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.inject.Inject;
import java.nio.file.Paths;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


/**
 * Created by jens on 09/10/2016.
 */
//@RunWith(SpringRunner.class)
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = MuPACSApplication.class)
@TestPropertySource("/application-test.properties")
public class StudyListTest
{
    private MockMvc mockMvc;

    @Inject
    private DicomImportService dicomImportService;

    @Inject
    StudyListController studyListController;

    @Inject
    WebApplicationContext wac;

    @Before
    public void setup() throws InterruptedException
    {
        //mockMvc = MockMvcBuilders.standaloneSetup(studyListController).build();
        mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
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

        String contentAsString = mvcResult.getResponse().getContentAsString();

        assertThat(contentAsString,
            containsString("List of Studies"));

    }

    @Test
    public void can_display_studies() throws Exception
    {
        Attributes dcm = new LoadDICOMFromJSON().fromResource("dcm.json");
        dicomImportService.dicomToDatabase(dcm, Paths.get("./"));

        MvcResult mvcResult = mockMvc.perform(get("/studylist?patientId=1"))
            .andExpect(status().isOk())
            .andExpect(model().size(2))
            .andExpect(view().name("studyList"))
            .andDo(MockMvcResultHandlers.print())
            .andReturn();

        String contentAsString = mvcResult.getResponse().getContentAsString();

        assertThat(contentAsString,
            containsString("2.16.840.1.113669.632.20.1211.10000329900"));

    }
}
