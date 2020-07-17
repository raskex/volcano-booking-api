package com.upgrade.challenge.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

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
import com.upgrade.challenge.exception.AvailabilityException;
import com.upgrade.challenge.exception.BookingException;
import com.upgrade.challenge.exception.BookingNotFoundException;
import com.upgrade.challenge.model.Booking;
import com.upgrade.challenge.model.BookingRequest;
import com.upgrade.challenge.services.BookingService;
import com.upgrade.challenge.services.BookingServiceTest;

@RunWith(SpringRunner.class)
@WebMvcTest(BookingController.class)
public class BookingControllerTest {

    @Autowired
    private MockMvc mvc;
 
    @MockBean
	private BookingService bookingService;

    @Test
    public void testGet() throws Exception {
    	Booking booking = BookingServiceTest.createBooking(33);
        when(bookingService.get(anyString())).thenReturn(booking);

        mvc.perform(MockMvcRequestBuilders
        	      .get("/booking/{bookingId}", 33)
        	      .accept(MediaType.APPLICATION_JSON))
        	      .andDo(print())
        	      .andExpect(status().isOk())
        	      .andExpect(jsonPath("$.id").value(33))
        	      .andExpect(jsonPath("$.fromDay").value(LocalDate.now().plusDays(2).toString()))
        	      .andExpect(jsonPath("$.toDay").value(LocalDate.now().plusDays(4).toString()))
        	      .andExpect(jsonPath("$.firstName").value("name"))
        	      .andExpect(jsonPath("$.lastName").value("surname"))
        	      .andExpect(jsonPath("$.guests").value(3))
        		  .andExpect(jsonPath("$.email").value("some@email.com"));
    }  

    @Test
    public void testGetNoAvailability() throws Exception {
		doThrow(BookingNotFoundException.class).when(bookingService).get(anyString());

        mvc.perform(MockMvcRequestBuilders
        	      .get("/booking/{bookingId}", 33)
        	      .accept(MediaType.APPLICATION_JSON))
        	      .andDo(print())
        	      .andExpect(status().isNotFound());
    }  

    @Test
    public void testBookOK() throws Exception {
    	BookingRequest bookingRequest = BookingServiceTest.createBookingRequest();
        when(bookingService.add(any(BookingRequest.class))).thenReturn(33);

        mvc.perform(MockMvcRequestBuilders
        		  .post("/booking/")
        	      .contentType(MediaType.APPLICATION_JSON)
        	      .content(new ObjectMapper().writeValueAsString(bookingRequest))
        	      .accept(MediaType.TEXT_PLAIN))
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
        	      .content(new ObjectMapper().writeValueAsString(bookingRequest))
        	      .accept(MediaType.TEXT_PLAIN))
        	      .andDo(print())
        	      .andExpect(status().isBadRequest());
    }  

    @Test
    public void testEditBookingOk() throws Exception {
    	BookingRequest bookingRequest = BookingServiceTest.createBookingRequest();
        when(bookingService.edit(anyString(), any(BookingRequest.class))).thenReturn(true);
        
        mvc.perform(MockMvcRequestBuilders
        	      .put("/booking/{bookingId}", 33)
        	      .contentType(MediaType.APPLICATION_JSON)
        	      .content(new ObjectMapper().writeValueAsString(bookingRequest))
        	      .accept(MediaType.TEXT_PLAIN))
        	      .andDo(print())
        	      .andExpect(status().isOk());
    }  

    @Test
    public void testEditBookingNoAvailability() throws Exception {
    	BookingRequest bookingRequest = BookingServiceTest.createBookingRequest();
        when(bookingService.edit(anyString(), any(BookingRequest.class))).thenThrow(AvailabilityException.class);
        
        mvc.perform(MockMvcRequestBuilders
        	      .put("/booking/{bookingId}", 33)
        	      .contentType(MediaType.APPLICATION_JSON)
        	      .content(new ObjectMapper().writeValueAsString(bookingRequest))
        	      .accept(MediaType.TEXT_PLAIN))
        	      .andDo(print())
        	      .andExpect(status().isBadRequest());
    }  

    @Test
    public void testDeleteBookingOk() throws Exception {
        mvc.perform(MockMvcRequestBuilders
        	      .delete("/booking/{bookingId}", 33)
        	      .accept(MediaType.TEXT_PLAIN))
        	      .andDo(print())
        	      .andExpect(status().isOk());
    }  

    @Test
    public void testDeleteBookingNotFound() throws Exception {
		doThrow(BookingNotFoundException.class).when(bookingService).delete(anyString());

		mvc.perform(MockMvcRequestBuilders
        	      .delete("/booking/{bookingId}", 33)
        	      .accept(MediaType.TEXT_PLAIN))
        	      .andDo(print())
        	      .andExpect(status().isNotFound());
    }  

    @Test
    public void testDeleteBookingAlreadyPassed() throws Exception {
		doThrow(BookingException.class).when(bookingService).delete(anyString());

		mvc.perform(MockMvcRequestBuilders
        	      .delete("/booking/{bookingId}", 33)
        	      .accept(MediaType.TEXT_PLAIN))
        	      .andDo(print())
        	      .andExpect(status().isBadRequest());
    }

}
