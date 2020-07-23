package com.upgrade.challenge.services;

import java.sql.BatchUpdateException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import com.upgrade.challenge.exception.AvailabilityException;
import com.upgrade.challenge.exception.BookingException;
import com.upgrade.challenge.exception.BookingNotFoundException;
import com.upgrade.challenge.exception.InputFormatException;
import com.upgrade.challenge.model.BookingRequest;
import com.upgrade.challenge.model.BookingResponse;
import com.upgrade.challenge.model.dto.Booking;
import com.upgrade.challenge.repository.BookingRepository;
import com.upgrade.challenge.validator.BookingValidator;

@Service
@EnableRetry
public class BookingService {

	@Autowired
	private BookingRepository bookingRepository;

	@Autowired
	private DailyAvailabilityService dailyAvailabilityService;

	@Autowired
	private BookingValidator validator;

	private static final int MAX_ATTEMPTS = 10;
	
	private String CANCEL = "cancel";
	private String EDIT = "edit";

	public BookingResponse get(Integer bookingId) throws BookingNotFoundException, InputFormatException {
		Booking booking = bookingRepository.findById(Integer.valueOf(bookingId))
				.orElseThrow(() -> new BookingNotFoundException(bookingId));
		return new BookingResponse(booking.getId(), LocalDate.parse(booking.getFromDay(), DateTimeFormatter.ISO_DATE),
				LocalDate.parse(booking.getToDay(), DateTimeFormatter.ISO_DATE), booking.getGuests(), booking.getFirstName(),
				booking.getLastName(), booking.getEmail());
	}

	@Transactional
	@Retryable(value = {BatchUpdateException.class, DataIntegrityViolationException.class, ObjectOptimisticLockingFailureException.class}, maxAttempts = MAX_ATTEMPTS)
	public BookingResponse add(BookingRequest bookingRequest) throws BookingException, AvailabilityException, InputFormatException {
		dailyAvailabilityService.validateAvailability(bookingRequest.getFromDay(), bookingRequest.getToDay(), bookingRequest.getGuests(), true);
		dailyAvailabilityService.blockAvailability(bookingRequest.getFromDay(), bookingRequest.getToDay(), Integer.valueOf(bookingRequest.getGuests()));
		return new BookingResponse(bookingRepository.save(new Booking(bookingRequest)).getId(), bookingRequest);
	}

	@Transactional
	@Retryable(value = {BatchUpdateException.class, DataIntegrityViolationException.class, ObjectOptimisticLockingFailureException.class}, maxAttempts = MAX_ATTEMPTS)
	public BookingResponse edit(Integer bookingId, BookingRequest bookingRequest) throws AvailabilityException, BookingException, InputFormatException, BookingNotFoundException {
		BookingResponse storedBooking = get(bookingId);
		validator.validatePastDate(storedBooking.getFromDay(), EDIT);
		validator.validateDateInput(bookingRequest.getFromDay(), bookingRequest.getToDay(), true);
		BookingResponse editedBooking = new BookingResponse(bookingId, bookingRequest);
		
		Integer storedGuests =  storedBooking.getGuests();
		Integer editedGuests = editedBooking.getGuests();
		if (editedBooking.getFromDay().compareTo(storedBooking.getFromDay()) < 0 
				|| editedBooking.getToDay().compareTo(storedBooking.getToDay()) > 0 || editedGuests > storedGuests) {
			dailyAvailabilityService.releaseAvailability(storedBooking.getFromDay(), storedBooking.getToDay(), storedBooking.getGuests());
			return update(editedBooking);
		}
		// Release availability in case of a later arrival.
		if (storedBooking.getFromDay().compareTo(editedBooking.getFromDay()) < 0) {
			dailyAvailabilityService.releaseAvailability(storedBooking.getFromDay(), editedBooking.getFromDay(), storedGuests);
		}
		// Release availability in case of an early departure.
		if (editedBooking.getToDay().compareTo(storedBooking.getToDay()) < 0) {
			dailyAvailabilityService.releaseAvailability(editedBooking.getToDay(), storedBooking.getToDay(), storedGuests);
		}
		// Release availability in case of fewer guests.
		if (storedGuests > editedGuests) {
			dailyAvailabilityService.releaseAvailability(editedBooking.getFromDay(), editedBooking.getToDay(), storedGuests - editedGuests);
		}

		bookingRepository.save(new Booking(editedBooking));
		return editedBooking;
	}
	
	@Transactional
	@Retryable(value = {BatchUpdateException.class, DataIntegrityViolationException.class, ObjectOptimisticLockingFailureException.class}, maxAttempts = MAX_ATTEMPTS)
	public void delete(Integer bookingId) {
		BookingResponse booking = get(bookingId);
		validator.validatePastDate(booking.getFromDay(), CANCEL);
		dailyAvailabilityService.releaseAvailability(booking.getFromDay(), booking.getToDay(), booking.getGuests());
	    bookingRepository.deleteById(bookingId);
	}

//	@Transactional
//	@Retryable(value = {BatchUpdateException.class, DataIntegrityViolationException.class, ObjectOptimisticLockingFailureException.class}, maxAttempts = MAX_ATTEMPTS)
	private BookingResponse update(BookingResponse booking) throws BookingException, AvailabilityException, InputFormatException {
		dailyAvailabilityService.validateAvailability(booking.getFromDay(), booking.getToDay(), booking.getGuests(), true);
		dailyAvailabilityService.blockAvailability(booking.getFromDay(), booking.getToDay(), booking.getGuests());
		bookingRepository.save(new Booking(booking));
		return booking;
	}
	
//	@Transactional
//	@Retryable(value = {BatchUpdateException.class, DataIntegrityViolationException.class, ObjectOptimisticLockingFailureException.class}, maxAttempts = MAX_ATTEMPTS)
//	private BookingResponse createOrUpdate(BookingRequest bookingRequest, Integer bookingId) throws BookingException, AvailabilityException, InputFormatException {
//		dailyAvailabilityService.validateAvailability(bookingRequest.getFromDay(), bookingRequest.getToDay(), bookingRequest.getGuests(), true);
//		dailyAvailabilityService.blockAvailability(bookingRequest.getFromDay(), bookingRequest.getToDay(), bookingRequest.getGuests());
//		BookingResponse bookingResponse = new BookingResponse(bookingId, bookingRequest);
//		bookingRepository.save(new Booking(bookingResponse));
//		return bookingResponse;
//	}
	
}
