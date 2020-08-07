package com.upgrade.challenge.services;

import java.sql.BatchUpdateException;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import com.upgrade.challenge.exception.BookingNotFoundException;
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

	public BookingResponse get(Long bookingId) {
		Booking booking = bookingRepository.findById(bookingId)
				.orElseThrow(() -> new BookingNotFoundException(bookingId));
		return new BookingResponse(booking.getId(), booking.getFromDay(), booking.getToDay(),
				booking.getGuests(), booking.getFirstName(), booking.getLastName(), booking.getEmail());
	}

	@Transactional
	@Retryable(value = { BatchUpdateException.class, DataIntegrityViolationException.class,
			ObjectOptimisticLockingFailureException.class }, maxAttempts = MAX_ATTEMPTS)
	public BookingResponse add(BookingRequest bookingRequest) {
		dailyAvailabilityService.validateAvailability(bookingRequest.getFromDay(), bookingRequest.getToDay(),
				bookingRequest.getGuests());
		dailyAvailabilityService.blockAvailability(bookingRequest.getFromDay(), bookingRequest.getToDay(),
				bookingRequest.getGuests());
		return new BookingResponse(bookingRepository.save(new Booking(bookingRequest)).getId(), bookingRequest);
	}

	@Transactional
	@Retryable(value = { BatchUpdateException.class, DataIntegrityViolationException.class,
			ObjectOptimisticLockingFailureException.class }, maxAttempts = MAX_ATTEMPTS)
	public BookingResponse edit(Long bookingId, BookingRequest bookingRequest) {
		BookingResponse storedBooking = get(bookingId);
		validator.validatePastDate(storedBooking.getFromDay(), EDIT);
		validator.validateDatesInput(bookingRequest.getFromDay(), bookingRequest.getToDay(), true);
		BookingResponse editedBooking = new BookingResponse(bookingId, bookingRequest);

		Integer storedGuests = storedBooking.getGuests();
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
	@Retryable(value = { BatchUpdateException.class, DataIntegrityViolationException.class,
			ObjectOptimisticLockingFailureException.class }, maxAttempts = MAX_ATTEMPTS)
	public void delete(Long bookingId) {
		BookingResponse booking = get(bookingId);
		validator.validatePastDate(booking.getFromDay(), CANCEL);
		dailyAvailabilityService.releaseAvailability(booking.getFromDay(), booking.getToDay(), booking.getGuests());
		bookingRepository.deleteById(bookingId);
	}

	private BookingResponse update(BookingResponse booking) {
		dailyAvailabilityService.validateAvailability(booking.getFromDay(), booking.getToDay(), booking.getGuests());
		dailyAvailabilityService.blockAvailability(booking.getFromDay(), booking.getToDay(), booking.getGuests());
		bookingRepository.save(new Booking(booking));
		return booking;
	}
	
}
