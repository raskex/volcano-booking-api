package com.upgrade.challenge.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.upgrade.challenge.exception.AvailabilityException;
import com.upgrade.challenge.exception.BookingException;
import com.upgrade.challenge.exception.BookingNotFoundException;
import com.upgrade.challenge.model.BookingRequest;
import com.upgrade.challenge.model.BookingResponse;
import com.upgrade.challenge.services.BookingService;
import com.upgrade.challenge.services.BookingServiceTest;

@RunWith(SpringRunner.class)
@WebMvcTest(BookingController.class)
public class BookingControllerTest {

    @Autowired
    private MockMvc mvc;
 
    @MockBean
	private BookingService bookingService;
    
    private static ObjectMapper mapper;
    
    @BeforeClass
    public static void setup() {
    	mapper = new ObjectMapper();
    	mapper.registerModule(new JavaTimeModule());
    }

    @Test
    public void testGet() throws Exception {
    	BookingResponse booking = new BookingResponse(Long.valueOf(33), BookingServiceTest.createBookingRequest());
        when(bookingService.get(anyLong())).thenReturn(booking);

        mvc.perform(MockMvcRequestBuilders
        	      .get("/booking/{bookingId}", 33)
        	      .accept(MediaType.APPLICATION_JSON))
        	      .andDo(print())
        	      .andExpect(status().isOk())
        	      .andExpect(jsonPath("$.id").value(33))
        	      .andExpect(jsonPath("$.fromDay").value(LocalDate.now().plusDays(2).toString()))
        	      .andExpect(jsonPath("$.toDay").value(LocalDate.now().plusDays(3).toString()))
        	      .andExpect(jsonPath("$.firstName").value("name"))
        	      .andExpect(jsonPath("$.lastName").value("surname"))
        	      .andExpect(jsonPath("$.guests").value(3))
        		  .andExpect(jsonPath("$.email").value("some@email.com"));
    }  

    @Test
    public void testGetNoAvailability() throws Exception {
		doThrow(BookingNotFoundException.class).when(bookingService).get(anyLong());

        mvc.perform(MockMvcRequestBuilders
        	      .get("/booking/{bookingId}", 33)
        	      .accept(MediaType.APPLICATION_JSON))
        	      .andDo(print())
        	      .andExpect(status().isNotFound());
    }  

    @Test
    public void testBookOK() throws Exception {
    	BookingRequest bookingRequest = BookingServiceTest.createBookingRequest();
        when(bookingService.add(any(BookingRequest.class))).thenReturn(new BookingResponse(Long.valueOf(33), bookingRequest));

    	mvc.perform(MockMvcRequestBuilders
        		  .post("/booking/")
        	      .contentType(MediaType.APPLICATION_JSON)
        	      .content(mapper.writeValueAsString(bookingRequest)))
        	      .andDo(print())
        	      .andExpect(status().isOk());
    }  

    @Test
    public void testBookNotAvailability() throws Exception {
    	BookingRequest bookingRequest = BookingServiceTest.createBookingRequest();
        when(bookingService.add(any(BookingRequest.class))).thenThrow(AvailabilityException.class);

        mvc.perform(MockMvcRequestBuilders
        	      .post("/booking/")
        	      .contentType(MediaType.APPLICATION_JSON)
        	      .content(mapper.writeValueAsString(bookingRequest)))
        	      .andDo(print())
        	      .andExpect(status().isBadRequest());
    }  

    @Test
    public void testEditBookingOk() throws Exception {
    	BookingRequest bookingRequest = BookingServiceTest.createBookingRequest();
        when(bookingService.edit(anyLong(), any(BookingRequest.class))).thenReturn(BookingServiceTest.createBookingResponse(33));
        
        mvc.perform(MockMvcRequestBuilders
        	      .put("/booking/{bookingId}", 33)
        	      .contentType(MediaType.APPLICATION_JSON)
        	      .content(mapper.writeValueAsString(bookingRequest)))
        	      .andDo(print())
        	      .andExpect(status().isOk());
    }  

    @Test
    public void testEditBookingNoAvailability() throws Exception {
    	BookingRequest bookingRequest = BookingServiceTest.createBookingRequest();
        when(bookingService.edit(anyLong(), any(BookingRequest.class))).thenThrow(AvailabilityException.class);
        
        mvc.perform(MockMvcRequestBuilders
        	      .put("/booking/{bookingId}", 33)
        	      .contentType(MediaType.APPLICATION_JSON)
        	      .content(mapper.writeValueAsString(bookingRequest)))
        	      .andDo(print())
        	      .andExpect(status().isBadRequest());
    }  

    @Test
    public void testDeleteBookingOk() throws Exception {
        mvc.perform(MockMvcRequestBuilders
        	      .delete("/booking/{bookingId}", 33))
        	      .andDo(print())
        	      .andExpect(status().isOk());
    }  

    @Test
    public void testDeleteBookingNotFound() throws Exception {
		doThrow(BookingNotFoundException.class).when(bookingService).delete(anyLong());

		mvc.perform(MockMvcRequestBuilders
        	      .delete("/booking/{bookingId}", 33))
        	      .andDo(print())
        	      .andExpect(status().isNotFound());
    }  

    @Test
    public void testDeleteBookingAlreadyPassed() throws Exception {
		doThrow(BookingException.class).when(bookingService).delete(anyLong());

		mvc.perform(MockMvcRequestBuilders
        	      .delete("/booking/{bookingId}", 33))
        	      .andDo(print())
        	      .andExpect(status().isBadRequest());
    }
    
}
