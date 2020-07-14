package com.upgrade.challenge.services;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.upgrade.challenge.exception.AvailabilityException;
import com.upgrade.challenge.exception.BookingException;
import com.upgrade.challenge.exception.BookingNotFoundException;
import com.upgrade.challenge.exception.InputFormatException;
import com.upgrade.challenge.model.Booking;
import com.upgrade.challenge.model.BookingRequest;
import com.upgrade.challenge.repository.BookingRepository;
import com.upgrade.challenge.validator.BookingValidator;

@Service
public class BookingService {

	@Autowired
	private BookingRepository bookingRepository;

	@Autowired
	private DailyAvailabilityService dailyAvailabilityService;
	
	private String BOOKING_ID = "Booking ID";
	private String CANCEL = "cancel";
	private String EDIT = "edit";

	public Booking get(String bookingId) throws BookingNotFoundException, InputFormatException {
		BookingValidator.validateNumberInput(bookingId, BOOKING_ID);;
		Booking booking = bookingRepository.findById(Integer.valueOf(bookingId))
				.orElseThrow(() -> new BookingNotFoundException(bookingId));
		return booking;
	}

	@Transactional
//	@Retryable(value = {BatchUpdateException.class, DataIntegrityViolationException.class, ObjectOptimisticLockingFailureException.class}, maxAttempts = 5)
	public Integer add(BookingRequest bookingRequest) throws BookingException, AvailabilityException, InputFormatException {
		dailyAvailabilityService.validateAvailability(bookingRequest.getFromDay(), bookingRequest.getToDay(), bookingRequest.getGuests(), true);
		Booking booking = new Booking(bookingRequest);
		dailyAvailabilityService.blockAvailability(booking.getFromDay(), booking.getToDay(), booking.getGuests());
		return bookingRepository.save(booking).getId();
	}
	
	@Transactional
	public void delete(String bookingId) throws InputFormatException, BookingNotFoundException, AvailabilityException, BookingException {
		Booking booking = get(bookingId);
		BookingValidator.validatePastDate(booking.getFromDay(), CANCEL);
		dailyAvailabilityService.releaseAvailability(booking);
	    bookingRepository.deleteById(Integer.valueOf(bookingId));
	}

	@Transactional
	public Boolean edit(String bookingId, BookingRequest bookingRequest) throws AvailabilityException, BookingException, InputFormatException, BookingNotFoundException {
		Booking storedBooking = get(bookingId);
		BookingValidator.validatePastDate(storedBooking.getFromDay(), EDIT);
		BookingValidator.validateDateInput(bookingRequest.getFromDay(), bookingRequest.getToDay(), true);
		Booking editedBooking = new Booking(bookingRequest);
		editedBooking.setId(Integer.valueOf(bookingId));
		
		Integer storedGuests =  storedBooking.getGuests();
		Integer editedGuests = editedBooking.getGuests();
		boolean hasChanges = false;
		if (editedBooking.getFromDay().compareTo(storedBooking.getFromDay()) < 0 
				|| editedBooking.getToDay().compareTo(storedBooking.getToDay()) > 0 || editedGuests > storedGuests) {
			dailyAvailabilityService.releaseAvailability(storedBooking);
			this.add(bookingRequest);
			return true;
		} else {
			// Release availability in case of a later arrival.
			if (storedBooking.getFromDay().compareTo(editedBooking.getFromDay()) < 0) {
				dailyAvailabilityService.releaseAvailability(storedBooking.getFromDay(), editedBooking.getFromDay(), storedGuests);
				hasChanges = true;
			}
			// Release availability in case of an early departure.
			if (editedBooking.getToDay().compareTo(storedBooking.getToDay()) < 0) {
				dailyAvailabilityService.releaseAvailability(editedBooking.getToDay(), storedBooking.getToDay(), storedGuests);
				hasChanges = true;
			}
			// Release availability in case of fewer guests.
			if (storedGuests > editedGuests) {
				dailyAvailabilityService.releaseAvailability(editedBooking.getFromDay(), editedBooking.getToDay(),
						storedGuests - editedGuests);
				hasChanges = true;
			}
		}
		
		if (hasChanges || !(editedBooking.getFirstName().equalsIgnoreCase(storedBooking.getFirstName()) 
				&& editedBooking.getFirstName().equalsIgnoreCase(storedBooking.getFirstName())
				&& editedBooking.getEmail().equalsIgnoreCase(storedBooking.getEmail()))) {
			bookingRepository.save(editedBooking);
			return true;
		}
		return false;
	}
	
}
