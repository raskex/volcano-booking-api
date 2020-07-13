package com.upgrade.challenge.services;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.upgrade.challenge.exception.AvailabilityException;
import com.upgrade.challenge.exception.BookingException;
import com.upgrade.challenge.exception.InputFormatException;
import com.upgrade.challenge.model.Booking;
import com.upgrade.challenge.model.DailyAvailability;
import com.upgrade.challenge.repository.DailyAvailabilityRepository;
import com.upgrade.challenge.validator.BookingValidator;

@Service
public class DailyAvailabilityService {
	
	@Autowired
	private DailyAvailabilityRepository dailyAvailabilityRepository;
	
	public static String GUESTS = "Guests";

	public List<DailyAvailability> getAvailability(String from, String to) throws BookingException, InputFormatException {
		if (StringUtils.isEmpty(from) && StringUtils.isEmpty(to)) {
			LocalDate now = LocalDate.now();
			from = now.plusDays(BookingValidator.MINIMUM_DAYS_AHEAD_OF_ARRIVAL).toString();
			to = now.plusMonths(BookingValidator.MONTHS_UP_TO_BOOKING).toString();
		} else {
			if (StringUtils.isEmpty(from)) {
				LocalDate now = LocalDate.now();
				from = now.plusDays(BookingValidator.MINIMUM_DAYS_AHEAD_OF_ARRIVAL).toString();
			}
			if (StringUtils.isEmpty(to)) {
				LocalDate now = LocalDate.now();
				to = now.plusMonths(BookingValidator.MONTHS_UP_TO_BOOKING).toString();
			}
			BookingValidator.validateDateInput(from, to, false);
		}
		List<DailyAvailability> availabilityResult = new LinkedList<DailyAvailability>();
		
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(BookingValidator.DATE_FORMAT);
		LocalDate consecutiveDate = LocalDate.parse(from, formatter);
		LocalDate endDate = LocalDate.parse(to, formatter);
		List<DailyAvailability> availabilities = dailyAvailabilityRepository.findAllByDateBetween(from, to);
		for (DailyAvailability dailyAvailability : availabilities) {
			dailyAvailability.setGuests(BookingValidator.MAX_CAPACITY - dailyAvailability.getGuests());
			LocalDate currentDate = LocalDate.parse(dailyAvailability.getDate(), formatter);
			while (consecutiveDate.isBefore(currentDate)) {
				availabilityResult.add(new DailyAvailability(consecutiveDate.toString(), BookingValidator.MAX_CAPACITY));
				consecutiveDate = consecutiveDate.plusDays(1);
			}
			availabilityResult.add(dailyAvailability);
			consecutiveDate = consecutiveDate.plusDays(1);
		}
		while (consecutiveDate.isBefore(endDate)) {
			availabilityResult.add(new DailyAvailability(consecutiveDate.toString(), BookingValidator.MAX_CAPACITY));
			consecutiveDate = consecutiveDate.plusDays(1);
		}
		
		return availabilityResult;
	}
	
	public void validateAvailability(String from, String to, String guests, Boolean isBooking)
			throws AvailabilityException, BookingException, InputFormatException {
		
		BookingValidator.validateDateInput(from, to, isBooking);
		BookingValidator.validateGuestsInput(guests);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(BookingValidator.DATE_FORMAT);
		String endDate = LocalDate.parse(to, formatter).minusDays(1).toString();
		if (dailyAvailabilityRepository.existsByDateBetweenAndGuestsGreaterThan(from, endDate, BookingValidator.MAX_CAPACITY - Integer.valueOf(guests))) {
			throw new AvailabilityException(String.format(
					"There is no availability for the selected dates - From: %s, To: %s and %s guest(s)."
					+ " Please try again with differents dates.", from, to, guests));
		}
	}

	public void blockAvailability(String from, String to, Integer guests) throws AvailabilityException {
		List<DailyAvailability> availabilities = new LinkedList<DailyAvailability>();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(BookingValidator.DATE_FORMAT);
		LocalDate consecutiveDate = LocalDate.parse(from, formatter);
		LocalDate endDate = LocalDate.parse(to, formatter).minusDays(1);
		for (DailyAvailability dailyAvailability : dailyAvailabilityRepository.findAllByDateBetween(from, endDate.toString())) {
			dailyAvailability.addBooking(guests);
			LocalDate currentDate = LocalDate.parse(dailyAvailability.getDate(), formatter);
			while (consecutiveDate.isBefore(currentDate)) {
				availabilities.add(new DailyAvailability(consecutiveDate.toString(), guests));
				consecutiveDate = consecutiveDate.plusDays(1);
			}
			availabilities.add(dailyAvailability);
			consecutiveDate = consecutiveDate.plusDays(1);
		}
		while (!consecutiveDate.isAfter(endDate)) {
			availabilities.add(new DailyAvailability(consecutiveDate.toString(), guests));
			consecutiveDate = consecutiveDate.plusDays(1);
		}
		dailyAvailabilityRepository.saveAll(availabilities);
	}
	
	public void releaseAvailability(Booking booking) throws AvailabilityException {
		releaseAvailability(booking.getFromDay(), booking.getToDay(), booking.getGuests());
	}
	
	public void releaseAvailability(String from, String to, Integer guests) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(BookingValidator.DATE_FORMAT);
		String endBookingDate = LocalDate.parse(to, formatter).minusDays(1).toString();
		List<DailyAvailability> availabilities = new LinkedList<DailyAvailability>();
		for (DailyAvailability dailyAvailability : dailyAvailabilityRepository.findAllByDateBetween(from, endBookingDate)) {
			dailyAvailability.cancelBooking(guests);
			availabilities.add(dailyAvailability);
		}
		dailyAvailabilityRepository.saveAll(availabilities);
	}
	
}