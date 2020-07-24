package com.upgrade.challenge.validator;

import java.time.LocalDate;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.upgrade.challenge.exception.BookingException;
import com.upgrade.challenge.exception.InputFormatException;

@Component
public class BookingValidator {
	
	@Value("${volcano.min_days_ahead_of_arrival}")
	private int MINIMUM_DAYS_AHEAD_OF_ARRIVAL;
	
	@Value("${volcano.months_up_to_booking}")
	private int MONTHS_UP_TO_BOOKING;
	
	@Value("${volcano.max_booking_days}")
	private int MAX_BOOKING_DAYS;

	@Value("${volcano.max_guests_capacity}")
	private int MAX_CAPACITY;

	
	
	// ver de sacar este formatter de aca y del properties, o usar el custom pattern para crear el local date.
	@Value("${volcano.date_format}")
	private String DATE_FORMAT;

	private String ARRIVAL_DAYS_AHEAD_ERROR_MESSAGE;

	private String ARRIVAL_UP_TO_DAYS_ERROR_MESSAGE;
	
	private String STAY_LENGTH_ERROR_MESSAGE;

	private final String FROM_TO_ERROR_MESSAGE = "Checkin date should be prior to checkout date.";
	
	private String DATES_FORMAT_EXCEPTION = "Dates format error. It should be like %s";

	private String NUMBER_FORMAT_EXCEPTION = "%s should be a positive number.";

	private String NOT_ENOUGH_CAPACITY_EXCEPTION;
	
	private String EXPIRED_BOOKING_EXCEPTION = "It's too late to %s this booking.";

	private final String PAST_DAY_EXCEPTION = "Checkin date is a past day.";

	private final String GUESTS = "Guests";
	
	@PostConstruct
	public void initialize() {
		DATES_FORMAT_EXCEPTION = String.format(DATES_FORMAT_EXCEPTION, DATE_FORMAT);
		ARRIVAL_DAYS_AHEAD_ERROR_MESSAGE = String.format("The campsite can be reserved minimum %d day(s) ahead of arrival.", MINIMUM_DAYS_AHEAD_OF_ARRIVAL);
		ARRIVAL_UP_TO_DAYS_ERROR_MESSAGE = String.format("The campsite can be reserved up to %d month(s) in advance. Please try again with closer dates.", MONTHS_UP_TO_BOOKING);
		STAY_LENGTH_ERROR_MESSAGE = String.format("The campsite can be reserved for max %d days.", MAX_BOOKING_DAYS);
		NOT_ENOUGH_CAPACITY_EXCEPTION = String.format("The maximum capacity for the campsite is %d. Please try again with fewer guests.", MAX_CAPACITY);
	}

	public void validateGuestsInput(Integer guests) {
		if (guests > MAX_CAPACITY) {
			throw new InputFormatException(String.format(NOT_ENOUGH_CAPACITY_EXCEPTION));
		}
		if (guests < 1) {
			throw new InputFormatException(String.format(NUMBER_FORMAT_EXCEPTION, GUESTS));
		}
	}
	
	public void validateDateInput(LocalDate fromDay, LocalDate toDay, Boolean isBooking) {
		LocalDate now = LocalDate.now();

		if (fromDay.isBefore(now)) {
			throw new BookingException(PAST_DAY_EXCEPTION);
		}

		if (!(now.isBefore(fromDay))) {
			throw new BookingException(ARRIVAL_DAYS_AHEAD_ERROR_MESSAGE);
		}

		if (now.plusMonths(MONTHS_UP_TO_BOOKING).isBefore(fromDay)) {
			throw new BookingException(ARRIVAL_UP_TO_DAYS_ERROR_MESSAGE);
		}

		if (!fromDay.isBefore(toDay)) {
			throw new BookingException(FROM_TO_ERROR_MESSAGE);
		}

		if (isBooking && fromDay.plusDays(MAX_BOOKING_DAYS).isBefore(toDay)) {
			throw new BookingException(STAY_LENGTH_ERROR_MESSAGE);
		}
	}
	
	public void validatePastDate(LocalDate day, String operationType) {
		if (day.isBefore(LocalDate.now())) {
			throw new BookingException(String.format(EXPIRED_BOOKING_EXCEPTION, operationType));
		}
	}

	public int getMinimumDaysAheadOfArrival() {
		return MINIMUM_DAYS_AHEAD_OF_ARRIVAL;
	}

	public int getMonthsUpToBooking() {
		return MONTHS_UP_TO_BOOKING;
	}

	public int getMaxCapacity() {
		return MAX_CAPACITY;
	}
	
}
