package com.upgrade.challenge.controllers;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.LinkedList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.upgrade.challenge.exception.AvailabilityException;
import com.upgrade.challenge.model.DailyAvailability;
import com.upgrade.challenge.services.DailyAvailabilityService;

@RunWith(SpringRunner.class)
@WebMvcTest(DailyAvailabilityController.class)
public class DailyAvailabilityControllerTest {

    @Autowired
    private MockMvc mvc;
 
    @MockBean
    private DailyAvailabilityService dailyAvailabilityService;

    @Test
    public void testGetAllDatesNoInputDates() throws Exception {
        List<DailyAvailability> availabilities = new LinkedList<DailyAvailability>();
        availabilities.add(new DailyAvailability("2020-09-09", 3));
        
        when(dailyAvailabilityService.getAvailability(null, null)).thenReturn(availabilities);

        mvc.perform(MockMvcRequestBuilders
        	      .get("/availability/alldates")
        	      .accept(MediaType.APPLICATION_JSON))
        	      .andDo(print())
        	      .andExpect(status().isOk())
        	      .andExpect(jsonPath("$", hasSize(1)))
        		  .andExpect(jsonPath("$[?(@.date === '2020-09-09')]").exists());
    }  

    @Test
    public void testGetAllDatesAllDates() throws Exception {
        List<DailyAvailability> availabilities = new LinkedList<DailyAvailability>();
        availabilities.add(new DailyAvailability("2020-09-09", 3));
        
        when(dailyAvailabilityService.getAvailability(anyString(), anyString())).thenReturn(availabilities);

        mvc.perform(MockMvcRequestBuilders
        	      .get("/availability/alldates")
        	      .param("from", "2020-09-09")
        	      .param("to", "2020-09-10")
        	      .accept(MediaType.APPLICATION_JSON))
        	      .andDo(print())
        	      .andExpect(status().isOk())
        	      .andExpect(jsonPath("$", hasSize(1)))
        		  .andExpect(jsonPath("$[?(@.date === '2020-09-09')]").exists());
    }  

    @Test
    public void testAvailability() throws Exception {
        mvc.perform(MockMvcRequestBuilders
        	      .get("/availability/")
        	      .param("from", "2020-09-09")
        	      .param("to", "2020-09-10")
        	      .param("guests", "2")
        	      .accept(MediaType.TEXT_PLAIN))
        	      .andDo(print())
        	      .andExpect(status().isOk());
    }  

    @Test
    public void testNoAvailability() throws Exception {
		doThrow(AvailabilityException.class).when(dailyAvailabilityService).validateAvailability(anyString(),
				anyString(), anyString(), anyBoolean());

		mvc.perform(MockMvcRequestBuilders
        	      .get("/availability/")
        	      .param("from", "2020-09-09")
        	      .param("to", "2020-09-10")
        	      .param("guests", "2")
        	      .accept(MediaType.TEXT_PLAIN))
        	      .andDo(print())
        	      .andExpect(status().isBadRequest());
    }

}
