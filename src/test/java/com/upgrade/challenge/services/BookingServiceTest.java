package com.upgrade.challenge.services;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;

import com.upgrade.challenge.exception.AvailabilityException;
import com.upgrade.challenge.exception.BookingException;
import com.upgrade.challenge.exception.BookingNotFoundException;
import com.upgrade.challenge.exception.InputFormatException;
import com.upgrade.challenge.model.Booking;
import com.upgrade.challenge.model.BookingRequest;
import com.upgrade.challenge.repository.BookingRepository;
import com.upgrade.challenge.validator.BookingValidator;

@RunWith(SpringRunner.class)
@DataJpaTest
public class BookingServiceTest {

	@TestConfiguration
    static class BookingServiceTestContextConfiguration {
 
        @Bean
        public BookingService bookingService() {
            return new BookingService();
        }

        @Bean
        public BookingValidator bookingValidator() {
        	return new BookingValidator();
        }
    }
 
	private LocalDate now = LocalDate.now();
	
    @Autowired
    private BookingService bookingService;
 
	@MockBean
	private DailyAvailabilityService dailyAvailabilityService;

	@MockBean
	private BookingRepository bookingRepository;
 
    @Test
    public void testGetOK() throws BookingNotFoundException, InputFormatException {
    	Booking booking = new Booking();
    	Optional<Booking> optionalBooking = Optional.of(booking);
    	when(bookingRepository.findById(anyInt())).thenReturn(optionalBooking);
    	
    	Booking currentBooking = bookingService.get("45");

		assertEquals(booking, currentBooking);
    }

    @Test(expected = BookingNotFoundException.class)
    public void testGetNotOK() throws BookingNotFoundException, InputFormatException {
    	Optional<Booking> optionalBooking = Optional.empty();
    	when(bookingRepository.findById(anyInt())).thenReturn(optionalBooking);
    	
    	bookingService.get("45");
    }

    @Test(expected = InputFormatException.class)
    public void testGetNumberException() throws BookingNotFoundException, InputFormatException {
    	bookingService.get("4f5");
    }

    @Test
    public void testAddOK() throws BookingException, AvailabilityException, InputFormatException {
    	Booking booking = new Booking();
    	booking.setId(33);
    	when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
    	BookingRequest bookingRequest = createBookingRequest();
    	
    	Integer bookingId = bookingService.add(bookingRequest);

		assertEquals(booking.getId(), bookingId);
		verify(bookingRepository, times(1)).save(any(Booking.class));
		verify(dailyAvailabilityService, times(1)).validateAvailability(anyString(), anyString(), anyString(), anyBoolean());
		verify(dailyAvailabilityService, times(1)).blockAvailability(anyString(), anyString(), anyInt());
    }
    
    @Test(expected = AvailabilityException.class)
    public void testAddNoAvailability() throws BookingException, AvailabilityException, InputFormatException {
		doThrow(AvailabilityException.class).when(dailyAvailabilityService).validateAvailability(anyString(),
				anyString(), anyString(), anyBoolean());
    	        
    	bookingService.add(createBookingRequest());
    }
    
    @Test
    public void testDeleteOK() throws AvailabilityException, InputFormatException, BookingNotFoundException, BookingException {
    	Booking booking = createBooking(33);
    	Optional<Booking> optionalBooking = Optional.of(booking);
    	when(bookingRepository.findById(anyInt())).thenReturn(optionalBooking);
    	
    	bookingService.delete("33");

		verify(bookingRepository, times(1)).deleteById(33);
		verify(dailyAvailabilityService, times(1)).releaseAvailability(booking);
		verify(bookingRepository, times(1)).findById(anyInt());
    }
    
    @Test(expected = BookingNotFoundException.class)
    public void testDeleteBookingNotFound() throws InputFormatException, BookingNotFoundException, AvailabilityException, BookingException {
    	Optional<Booking> optionalBooking = Optional.empty();
    	when(bookingRepository.findById(anyInt())).thenReturn(optionalBooking);
    	
    	bookingService.delete("33");
		verify(bookingRepository, times(1)).findById(33);
    }

    @Test(expected = BookingException.class)
    public void testDeletePastBooking() throws InputFormatException, BookingNotFoundException, AvailabilityException, BookingException {
    	Booking booking = createBooking(33);
    	booking.setFromDay(now.minusDays(2).toString());
    	Optional<Booking> optionalBooking = Optional.of(booking);
    	when(bookingRepository.findById(anyInt())).thenReturn(optionalBooking);
    	
    	bookingService.delete("33");
		verify(bookingRepository, times(1)).findById(33);
    }

    @Test
    public void testEditBookingWithoutCheckingAvailabilityReleasingAvailabilityDaysInBetween() throws AvailabilityException, InputFormatException, BookingNotFoundException, BookingException {
    	Booking booking = createBooking(33);
    	Optional<Booking> optionalBooking = Optional.of(booking);
    	when(bookingRepository.findById(anyInt())).thenReturn(optionalBooking);
    	BookingRequest bookingRequest = createBookingRequest();
    	bookingRequest.setEmail("another@email.com");

    	boolean edited = bookingService.edit("33", bookingRequest);

    	assertTrue(edited);
		verify(bookingRepository, times(1)).findById(33);
		verify(dailyAvailabilityService, times(0)).releaseAvailability(any(Booking.class));
		verify(dailyAvailabilityService, times(1)).releaseAvailability(anyString(), anyString(), anyInt());
		verify(bookingRepository, times(1)).save(any(Booking.class));
    }
    
    @Test
    public void testEditBookingWithoutCheckingAvailabilityReleasingAvailabilityDaysBefore() throws AvailabilityException, InputFormatException, BookingNotFoundException, BookingException {
    	Booking booking = createBooking(33);
    	Optional<Booking> optionalBooking = Optional.of(booking);
    	when(bookingRepository.findById(anyInt())).thenReturn(optionalBooking);
    	BookingRequest bookingRequest = createBookingRequest();
    	bookingRequest.setFromDay(now.plusDays(2).toString());
    	bookingRequest.setToDay(now.plusDays(3).toString());
    	bookingRequest.setEmail("another@email.com");

    	boolean edited = bookingService.edit("33", bookingRequest);

    	assertTrue(edited);
		verify(bookingRepository, times(1)).findById(33);
		verify(dailyAvailabilityService, times(0)).releaseAvailability(any(Booking.class));
		verify(dailyAvailabilityService, times(1)).releaseAvailability(anyString(), anyString(), anyInt());
		verify(bookingRepository, times(1)).save(any(Booking.class));
    }
    
    @Test
    public void testEditBookingWithoutCheckingAvailabilityReleasingAvailabilityDaysAfter() throws AvailabilityException, InputFormatException, BookingNotFoundException, BookingException {
    	Booking booking = createBooking(33);
    	Optional<Booking> optionalBooking = Optional.of(booking);
    	when(bookingRepository.findById(anyInt())).thenReturn(optionalBooking);
    	BookingRequest bookingRequest = createBookingRequest();
    	bookingRequest.setFromDay(now.plusDays(3).toString());
    	bookingRequest.setToDay(now.plusDays(4).toString());
    	bookingRequest.setEmail("another@email.com");

    	boolean edited = bookingService.edit("33", bookingRequest);

    	assertTrue(edited);
		verify(bookingRepository, times(1)).findById(33);
		verify(dailyAvailabilityService, times(0)).releaseAvailability(any(Booking.class));
		verify(dailyAvailabilityService, times(1)).releaseAvailability(anyString(), anyString(), anyInt());
		verify(bookingRepository, times(1)).save(any(Booking.class));
    }
    
    @Test
    public void testEditBookingCheckingAvailability() throws AvailabilityException, InputFormatException, BookingNotFoundException, BookingException {
    	Booking booking = createBooking(33);
    	booking.setToDay(now.plusDays(3).toString());
    	Optional<Booking> optionalBooking = Optional.of(booking);
    	when(bookingRepository.findById(anyInt())).thenReturn(optionalBooking);
    	when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
    	BookingRequest bookingRequest = createBookingRequest();
    	bookingRequest.setFromDay(now.plusDays(1).toString());
    	bookingRequest.setToDay(now.plusDays(4).toString());

    	boolean edited = bookingService.edit("33", bookingRequest);

    	assertTrue(edited);
		verify(bookingRepository, times(1)).findById(33);
		verify(dailyAvailabilityService, times(1)).releaseAvailability(any(Booking.class));
		verify(dailyAvailabilityService, times(0)).releaseAvailability(anyString(), anyString(), anyInt());
		verify(bookingRepository, times(1)).save(any(Booking.class));
    }
    
    @Test
    public void testEditBookingWithoutCheckingAvailabilityOnlyChangesEmail() throws AvailabilityException, InputFormatException, BookingNotFoundException, BookingException {
    	Booking booking = createBooking(33);
    	Optional<Booking> optionalBooking = Optional.of(booking);
    	when(bookingRepository.findById(anyInt())).thenReturn(optionalBooking);
    	BookingRequest bookingRequest = createBookingRequest();
    	bookingRequest.setToDay(now.plusDays(4).toString());
    	bookingRequest.setEmail("another@email.com");

    	boolean edited = bookingService.edit("33", bookingRequest);

    	assertTrue(edited);
		verify(bookingRepository, times(1)).findById(33);
		verify(dailyAvailabilityService, times(0)).releaseAvailability(any(Booking.class));
		verify(dailyAvailabilityService, times(0)).releaseAvailability(anyString(), anyString(), anyInt());
		verify(bookingRepository, times(1)).save(any(Booking.class));
    }
    
    @Test
    public void testEditBookingNoChangesWithoutCheckingAvailability() throws AvailabilityException, InputFormatException, BookingNotFoundException, BookingException {
    	Booking booking = createBooking(33);
    	Optional<Booking> optionalBooking = Optional.of(booking);
    	when(bookingRepository.findById(anyInt())).thenReturn(optionalBooking);
    	BookingRequest bookingRequest = createBookingRequest();
    	bookingRequest.setToDay(now.plusDays(4).toString());

    	boolean edited = bookingService.edit("33", bookingRequest);

    	assertFalse(edited);
		verify(bookingRepository, times(1)).findById(33);
		verify(dailyAvailabilityService, times(0)).releaseAvailability(any(Booking.class));
		verify(dailyAvailabilityService, times(0)).releaseAvailability(anyString(), anyString(), anyInt());
		verify(bookingRepository, times(0)).save(any(Booking.class));
    }
    
    private Booking createBooking(int id) {
    	Booking booking = new Booking();
    	booking.setId(id);
    	booking.setEmail("some@email.com");
    	booking.setFirstName("name");
    	booking.setLastName("surname");
    	booking.setFromDay(now.plusDays(2).toString());
    	booking.setToDay(now.plusDays(4).toString());
    	booking.setGuests(3);
    	return booking;
    }

    private BookingRequest createBookingRequest() {
    	BookingRequest bookingRequest = new BookingRequest();
    	bookingRequest.setEmail("some@email.com");
    	bookingRequest.setFirstName("name");
    	bookingRequest.setLastName("surname");
    	bookingRequest.setFromDay(now.plusDays(2).toString());
    	bookingRequest.setToDay(now.plusDays(3).toString());
    	bookingRequest.setGuests("3");
    	return bookingRequest;
    }

}
