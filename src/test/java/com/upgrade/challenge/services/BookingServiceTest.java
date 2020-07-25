package com.upgrade.challenge.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
import com.upgrade.challenge.model.BookingRequest;
import com.upgrade.challenge.model.BookingResponse;
import com.upgrade.challenge.model.dto.Booking;
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
    }

	private static LocalDate now = LocalDate.parse(LocalDate.now().toString(), DateTimeFormatter.ISO_DATE);

    @Autowired
    private BookingService bookingService;
 
	@MockBean
	private DailyAvailabilityService dailyAvailabilityService;

	@MockBean
	private BookingRepository bookingRepository;
 
	@MockBean
    public BookingValidator validator;

    @Test
    public void testGetOK() throws BookingNotFoundException, InputFormatException {
    	Booking booking = createBooking(33);
    	Optional<Booking> optionalBooking = Optional.of(booking);
    	when(bookingRepository.findById(anyLong())).thenReturn(optionalBooking);
    	
    	BookingResponse currentBooking = bookingService.get(Long.valueOf(33));

		assertEquals(booking.getId(), currentBooking.getId());
    }

    @Test(expected = BookingNotFoundException.class)
    public void testGetNotFound() throws BookingNotFoundException, InputFormatException {
    	Optional<Booking> optionalBooking = Optional.empty();
    	when(bookingRepository.findById(anyLong())).thenReturn(optionalBooking);
    	
    	bookingService.get(Long.valueOf(33));
    }

    @Test
    public void testAddOK() throws BookingException, AvailabilityException, InputFormatException {
    	Booking expectedBooking = new Booking();
    	expectedBooking.setId(Long.valueOf(33));
    	when(bookingRepository.save(any(Booking.class))).thenReturn(expectedBooking);
    	BookingRequest bookingRequest = createBookingRequest();
    	
    	BookingResponse createdBooking = bookingService.add(bookingRequest);

		assertEquals(expectedBooking.getId(), createdBooking.getId());
		verify(bookingRepository, times(1)).save(any(Booking.class));
		verify(dailyAvailabilityService, times(1)).validateAvailability(any(LocalDate.class), any(LocalDate.class), anyInt(), anyBoolean());
		verify(dailyAvailabilityService, times(1)).blockAvailability(any(LocalDate.class), any(LocalDate.class), anyInt());
    }
    
    @Test(expected = AvailabilityException.class)
    public void testAddNoAvailability() throws BookingException, AvailabilityException, InputFormatException {
		doThrow(AvailabilityException.class).when(dailyAvailabilityService).validateAvailability(any(LocalDate.class),
				any(LocalDate.class), anyInt(), anyBoolean());

    	bookingService.add(createBookingRequest());
    }
    
    @Test
    public void testDeleteOK() throws AvailabilityException, InputFormatException, BookingNotFoundException, BookingException {
    	Booking booking = createBooking(33);
    	Optional<Booking> optionalBooking = Optional.of(booking);
    	when(bookingRepository.findById(anyLong())).thenReturn(optionalBooking);
    	
    	bookingService.delete(Long.valueOf(33));

		verify(validator, times(1)).validatePastDate(any(LocalDate.class), anyString());
		verify(bookingRepository, times(1)).deleteById(anyLong());
		verify(dailyAvailabilityService, times(1)).releaseAvailability(any(LocalDate.class), any(LocalDate.class), anyInt());
		verify(bookingRepository, times(1)).findById(anyLong());
    }
    
    @Test(expected = BookingNotFoundException.class)
    public void testDeleteBookingNotFound() throws InputFormatException, BookingNotFoundException, AvailabilityException, BookingException {
    	Optional<Booking> optionalBooking = Optional.empty();
    	when(bookingRepository.findById(anyLong())).thenReturn(optionalBooking);
    	
    	bookingService.delete(Long.valueOf(33));
		verify(bookingRepository, times(1)).findById(anyLong());
    }

    @Test(expected = BookingException.class)
    public void testDeletePastBooking() throws InputFormatException, BookingNotFoundException, AvailabilityException, BookingException {
    	Optional<Booking> optionalBooking = Optional.of(createBooking(33));
    	when(bookingRepository.findById(anyLong())).thenReturn(optionalBooking);
    	doThrow(BookingException.class).when(validator).validatePastDate(any(LocalDate.class), anyString());
    	
    	bookingService.delete(Long.valueOf(33));

		verify(bookingRepository, times(1)).findById(anyLong());
    }

    @Test
    public void testEditBookingWithoutCheckingAvailabilityReleasingAvailabilityDaysInBetween() throws AvailabilityException, InputFormatException, BookingNotFoundException, BookingException {
    	Booking booking = createBooking(33);
    	Optional<Booking> optionalBooking = Optional.of(booking);
    	when(bookingRepository.findById(anyLong())).thenReturn(optionalBooking);
    	BookingRequest bookingRequest = createBookingRequest();
    	bookingRequest.setEmail("another@email.com");
    	BookingResponse expectedBooking = new BookingResponse(Long.valueOf(33), bookingRequest);

    	BookingResponse editedBooking = bookingService.edit(Long.valueOf(33), bookingRequest);

    	assertEquals(expectedBooking.getId(), editedBooking.getId());
		verify(bookingRepository, times(1)).findById(anyLong());
		verify(validator, times(1)).validatePastDate(any(LocalDate.class), anyString());
		verify(validator, times(1)).validateDatesInput(any(LocalDate.class), any(LocalDate.class), anyBoolean());
		verify(dailyAvailabilityService, times(1)).releaseAvailability(any(LocalDate.class), any(LocalDate.class), anyInt());
		verify(bookingRepository, times(1)).save(any(Booking.class));
    }
    
    @Test
    public void testEditBookingWithoutCheckingAvailabilityReleasingAvailabilityDaysBefore() throws AvailabilityException, InputFormatException, BookingNotFoundException, BookingException {
    	Booking booking = createBooking(33);
    	Optional<Booking> optionalBooking = Optional.of(booking);
    	when(bookingRepository.findById(anyLong())).thenReturn(optionalBooking);
    	BookingRequest bookingRequest = createBookingRequest();
    	bookingRequest.setFromDay(now.plusDays(2));
    	bookingRequest.setToDay(now.plusDays(3));
    	bookingRequest.setEmail("another@email.com");
    	BookingResponse expectedBooking = new BookingResponse(Long.valueOf(33), bookingRequest);

    	BookingResponse editedBooking = bookingService.edit(Long.valueOf(33), bookingRequest);

    	assertEquals(expectedBooking.getId(), editedBooking.getId());
		verify(bookingRepository, times(1)).findById(anyLong());
		verify(validator, times(1)).validatePastDate(any(LocalDate.class), anyString());
		verify(validator, times(1)).validateDatesInput(any(LocalDate.class), any(LocalDate.class), anyBoolean());
		verify(dailyAvailabilityService, times(1)).releaseAvailability(any(LocalDate.class), any(LocalDate.class), anyInt());
		verify(bookingRepository, times(1)).save(any(Booking.class));
    }
    
    @Test
    public void testEditBookingWithoutCheckingAvailabilityReleasingAvailabilityDaysAfter() throws AvailabilityException, InputFormatException, BookingNotFoundException, BookingException {
    	Booking booking = createBooking(33);
    	Optional<Booking> optionalBooking = Optional.of(booking);
    	when(bookingRepository.findById(anyLong())).thenReturn(optionalBooking);
    	BookingRequest bookingRequest = createBookingRequest();
    	bookingRequest.setFromDay(now.plusDays(3));
    	bookingRequest.setToDay(now.plusDays(4));
    	bookingRequest.setEmail("another@email.com");
    	BookingResponse expectedBooking = new BookingResponse(Long.valueOf(33), bookingRequest);

    	BookingResponse editedBooking = bookingService.edit(Long.valueOf(33), bookingRequest);

    	assertEquals(expectedBooking.getId(), editedBooking.getId());
		verify(bookingRepository, times(1)).findById(anyLong());
		verify(validator, times(1)).validatePastDate(any(LocalDate.class), anyString());
		verify(validator, times(1)).validateDatesInput(any(LocalDate.class), any(LocalDate.class), anyBoolean());
		verify(dailyAvailabilityService, times(1)).releaseAvailability(any(LocalDate.class), any(LocalDate.class), anyInt());
		verify(bookingRepository, times(1)).save(any(Booking.class));
    }
    
    @Test
    public void testEditBookingCheckingAvailability() throws AvailabilityException, InputFormatException, BookingNotFoundException, BookingException {
    	Booking booking = createBooking(33);
    	booking.setToDay(now.plusDays(3));
    	Optional<Booking> optionalBooking = Optional.of(booking);
    	when(bookingRepository.findById(anyLong())).thenReturn(optionalBooking);
    	when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
    	BookingRequest bookingRequest = createBookingRequest();
    	bookingRequest.setFromDay(now.plusDays(1));
    	bookingRequest.setToDay(now.plusDays(4));
    	BookingResponse expectedBooking = new BookingResponse(Long.valueOf(33), bookingRequest);

    	BookingResponse editedBooking = bookingService.edit(Long.valueOf(33), bookingRequest);

    	assertEquals(expectedBooking.getId(), editedBooking.getId());
		verify(bookingRepository, times(1)).findById(anyLong());
		verify(validator, times(1)).validatePastDate(any(LocalDate.class), anyString());
		verify(validator, times(1)).validateDatesInput(any(LocalDate.class), any(LocalDate.class), anyBoolean());
		verify(dailyAvailabilityService, times(1)).releaseAvailability(any(LocalDate.class), any(LocalDate.class), anyInt());
		verify(bookingRepository, times(1)).save(any(Booking.class));
    }
    
    @Test
    public void testEditBookingWithoutCheckingAvailabilityOnlyChangesEmail() throws AvailabilityException, InputFormatException, BookingNotFoundException, BookingException {
    	Booking booking = createBooking(33);
    	Optional<Booking> optionalBooking = Optional.of(booking);
    	when(bookingRepository.findById(anyLong())).thenReturn(optionalBooking);
    	BookingRequest bookingRequest = createBookingRequest();
    	bookingRequest.setToDay(now.plusDays(4));
    	bookingRequest.setEmail("another@email.com");
    	BookingResponse expectedBooking = new BookingResponse(Long.valueOf(33), bookingRequest);

    	BookingResponse editedBooking = bookingService.edit(Long.valueOf(33), bookingRequest);

    	assertEquals(expectedBooking.getId(), editedBooking.getId());
		verify(bookingRepository, times(1)).findById(Long.valueOf(33));
		verify(validator, times(1)).validatePastDate(any(LocalDate.class), anyString());
		verify(validator, times(1)).validateDatesInput(any(LocalDate.class), any(LocalDate.class), anyBoolean());
		verify(dailyAvailabilityService, times(0)).releaseAvailability(any(LocalDate.class), any(LocalDate.class), anyInt());
		verify(bookingRepository, times(1)).save(any(Booking.class));
    }
    
    public static Booking createBooking(int id) {
    	Booking booking = new Booking(createBookingResponse(id));
    	booking.setToDay(now.plusDays(4));
    	return booking;
    }

    public static BookingRequest createBookingRequest() {
    	BookingRequest bookingRequest = new BookingRequest();
    	bookingRequest.setEmail("some@email.com");
    	bookingRequest.setFirstName("name");
    	bookingRequest.setLastName("surname");
    	bookingRequest.setFromDay(now.plusDays(2));
    	bookingRequest.setToDay(now.plusDays(3));
    	bookingRequest.setGuests(3);
    	return bookingRequest;
    }

    public static BookingResponse createBookingResponse(int id) {
    	return new BookingResponse(Long.valueOf(id), createBookingRequest());
    }

}
