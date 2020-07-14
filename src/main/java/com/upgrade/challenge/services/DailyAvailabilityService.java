package com.upgrade.challenge.services;

import java.time.LocalDate;
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
import com.upgrade.challenge.model.DailyOccupation;
import com.upgrade.challenge.repository.DailyOccupationRepository;
import com.upgrade.challenge.validator.BookingValidator;

@Service
public class DailyAvailabilityService {

	@Autowired
	private DailyOccupationRepository dailyOccupationRepository;

	public static String GUESTS = "Guests";

	public List<DailyAvailability> getAvailability(String from, String to) throws BookingException, InputFormatException {
		LocalDate now = LocalDate.now();
		if (StringUtils.isEmpty(from) && StringUtils.isEmpty(to)) {
			from = now.plusDays(BookingValidator.MINIMUM_DAYS_AHEAD_OF_ARRIVAL).toString();
			to = now.plusMonths(BookingValidator.MONTHS_UP_TO_BOOKING).toString();
		} else {
			if (StringUtils.isEmpty(from)) {
				from = now.plusDays(BookingValidator.MINIMUM_DAYS_AHEAD_OF_ARRIVAL).toString();
			}
			if (StringUtils.isEmpty(to)) {
				to = now.plusMonths(BookingValidator.MONTHS_UP_TO_BOOKING).toString();
			}
			BookingValidator.validateDateInput(from, to, false);
		}
		List<DailyAvailability> availabilityResult = new LinkedList<DailyAvailability>();

		LocalDate consecutiveDate = LocalDate.parse(from, BookingValidator.formatter);
		LocalDate endDate = LocalDate.parse(to, BookingValidator.formatter).minusDays(1);
		List<DailyOccupation> occupabilities = dailyOccupationRepository.findAllByDateBetween(from, endDate.toString());
		for (DailyOccupation dailyOccupation : occupabilities) {
			DailyAvailability dailyAvailability = new DailyAvailability(dailyOccupation.getDate(), BookingValidator.MAX_CAPACITY - dailyOccupation.getGuests());
			LocalDate currentDate = LocalDate.parse(dailyOccupation.getDate(), BookingValidator.formatter);
			while (consecutiveDate.isBefore(currentDate)) {
				availabilityResult.add(new DailyAvailability(consecutiveDate.toString(), BookingValidator.MAX_CAPACITY));
				consecutiveDate = consecutiveDate.plusDays(1);
			}
			availabilityResult.add(dailyAvailability);
			consecutiveDate = consecutiveDate.plusDays(1);
		}
		while (!consecutiveDate.isAfter(endDate)) {
			availabilityResult.add(new DailyAvailability(consecutiveDate.toString(), BookingValidator.MAX_CAPACITY));
			consecutiveDate = consecutiveDate.plusDays(1);
		}

		return availabilityResult;
	}

	public void validateAvailability(String from, String to, String guests, Boolean isBooking)
			throws AvailabilityException, BookingException, InputFormatException {
		
		BookingValidator.validateDateInput(from, to, isBooking);
		BookingValidator.validateGuestsInput(guests);
		String endDate = LocalDate.parse(to, BookingValidator.formatter).minusDays(1).toString();
		if (dailyOccupationRepository.existsByDateBetweenAndGuestsGreaterThan(from, endDate, BookingValidator.MAX_CAPACITY - Integer.valueOf(guests))) {
			throw new AvailabilityException(String.format(
					"There is no availability for the selected dates - From: %s, To: %s and %s guest(s)."
					+ " Please try again with differents dates.", from, to, guests));
		}
	}

	public void blockAvailability(String from, String to, Integer guests) throws AvailabilityException {
		List<DailyOccupation> daysToBlock = new LinkedList<DailyOccupation>();
		LocalDate consecutiveDate = LocalDate.parse(from, BookingValidator.formatter);
		LocalDate endDate = LocalDate.parse(to, BookingValidator.formatter).minusDays(1);
		for (DailyOccupation dailyAvailability : dailyOccupationRepository.findAllByDateBetween(from, endDate.toString())) {
			dailyAvailability.addBooking(guests);
			LocalDate currentDate = LocalDate.parse(dailyAvailability.getDate(), BookingValidator.formatter);
			while (consecutiveDate.isBefore(currentDate)) {
				daysToBlock.add(new DailyOccupation(consecutiveDate.toString(), guests));
				consecutiveDate = consecutiveDate.plusDays(1);
			}
			daysToBlock.add(dailyAvailability);
			consecutiveDate = consecutiveDate.plusDays(1);
		}
		while (!consecutiveDate.isAfter(endDate)) {
			daysToBlock.add(new DailyOccupation(consecutiveDate.toString(), guests));
			consecutiveDate = consecutiveDate.plusDays(1);
		}
		dailyOccupationRepository.saveAll(daysToBlock);
	}

	public void releaseAvailability(Booking booking) throws AvailabilityException {
		releaseAvailability(booking.getFromDay(), booking.getToDay(), booking.getGuests());
	}

	public void releaseAvailability(String from, String to, Integer guests) {
		String endBookingDate = LocalDate.parse(to, BookingValidator.formatter).minusDays(1).toString();
		List<DailyOccupation> daysToRelease = new LinkedList<DailyOccupation>();
		for (DailyOccupation dailyAvailability : dailyOccupationRepository.findAllByDateBetween(from, endBookingDate)) {
			dailyAvailability.cancelBooking(guests);
			daysToRelease.add(dailyAvailability);
		}
		dailyOccupationRepository.saveAll(daysToRelease);
	}

}
