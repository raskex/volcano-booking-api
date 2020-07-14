package com.upgrade.challenge.validator;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.upgrade.challenge.exception.BookingException;
import com.upgrade.challenge.exception.InputFormatException;
import com.upgrade.challenge.services.DailyAvailabilityService;

@Component
public class BookingValidator {
	
	public static int MINIMUM_DAYS_AHEAD_OF_ARRIVAL;
	
	public static int MONTHS_UP_TO_BOOKING;
	
	private static int MAX_BOOKING_DAYS;

	public static int MAX_CAPACITY;
	
	public static String DATE_FORMAT;

	public static DateTimeFormatter formatter;
	
	private static String ARRIVAL_DAYS_AHEAD_ERROR_MESSAGE;

	private static String ARRIVAL_UP_TO_DAYS_ERROR_MESSAGE;
	
	private static String STAY_LENGTH_ERROR_MESSAGE;

	private static String FROM_TO_ERROR_MESSAGE = "Checkin date should be prior to checkout date.";
	
	private static String DATES_FORMAT_EXCEPTION = "Dates format error. It should be like %s";

	private static String NUMBER_FORMAT_EXCEPTION = "%s should be a positive number.";

	private static String NOT_ENOUGH_CAPACITY_EXCEPTION;
	
	private static String EXPIRED_BOOKING_EXCEPTION = "It's too late to %s this booking.";

	private static String PAST_DAY_EXCEPTION = "Checkin date is a past day.";

	@Autowired
	public void setDateFormat(@Value("${volcano.date_format}") String format) {
		DATE_FORMAT = format;
		formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
		DATES_FORMAT_EXCEPTION = String.format(DATES_FORMAT_EXCEPTION, format);
	}

	@Autowired
	public void setMinimumDaysAheadOfArrival(@Value("${volcano.min_days_ahead_of_arrival}") int minimumDaysAhead) {
		MINIMUM_DAYS_AHEAD_OF_ARRIVAL = minimumDaysAhead;
		ARRIVAL_DAYS_AHEAD_ERROR_MESSAGE = String.format("The campsite can be reserved minimum %d day(s) ahead of arrival.", MINIMUM_DAYS_AHEAD_OF_ARRIVAL);
	}

	@Autowired
	public void setDaysUpToBooking(@Value("${volcano.months_up_to_booking}") int monthsUpToBooking) {
		MONTHS_UP_TO_BOOKING = monthsUpToBooking;
		ARRIVAL_UP_TO_DAYS_ERROR_MESSAGE = String.format("The campsite can be reserved up to %d month(s) in advance.", MONTHS_UP_TO_BOOKING);
	}

	@Autowired
	public void setMaxBookingDays(@Value("${volcano.max_booking_days}") int maxBookingDays) {
		MAX_BOOKING_DAYS = maxBookingDays;
		STAY_LENGTH_ERROR_MESSAGE = String.format("The campsite can be reserved for max %d days.", MAX_BOOKING_DAYS);
	}

	@Autowired
	public void setMaxCapacity(@Value("${volcano.max_guests_capacity}") int maxCapacity) {
		MAX_CAPACITY = maxCapacity;
		NOT_ENOUGH_CAPACITY_EXCEPTION = String.format("The maximum capacity for the campsite is %d. Please try again with fewer guests.", MAX_CAPACITY);
	}

	
	public static void validateGuestsInput(String guests) throws InputFormatException {
		try {
			int number = Integer.valueOf(guests);
			if (number > MAX_CAPACITY) {
				throw new InputFormatException(String.format(NOT_ENOUGH_CAPACITY_EXCEPTION));
			}
			if (number < 1) {
				throw new InputFormatException(String.format(NUMBER_FORMAT_EXCEPTION, DailyAvailabilityService.GUESTS));
			}
		} catch (NumberFormatException e) {
			throw new InputFormatException(String.format(NUMBER_FORMAT_EXCEPTION, DailyAvailabilityService.GUESTS));
		}
	}
	
	public static void validateNumberInput(String number, String fieldName) throws InputFormatException {
		try {
			Integer.valueOf(number);
		} catch (NumberFormatException e) {
			throw new InputFormatException(String.format(NUMBER_FORMAT_EXCEPTION, fieldName));
		}
	}
	
	public static void validateDateInput(String from, String to, Boolean isBooking) throws BookingException, InputFormatException {
		try {
			LocalDate fromDay = LocalDate.parse(from, formatter);
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

			LocalDate toDay = LocalDate.parse(to, formatter);
			if (!fromDay.isBefore(toDay)) {
				throw new BookingException(FROM_TO_ERROR_MESSAGE);
			}

			if (isBooking && fromDay.plusDays(MAX_BOOKING_DAYS).isBefore(toDay)) {
				throw new BookingException(STAY_LENGTH_ERROR_MESSAGE);
			}

		} catch (DateTimeParseException e) {
			throw new InputFormatException(DATES_FORMAT_EXCEPTION);
		}
		
	}
	
	public static void validateStayLenght(String from, String to) throws BookingException, InputFormatException {
		try {
			LocalDate fromDay = LocalDate.parse(from, formatter);
			LocalDate toDay = LocalDate.parse(to, formatter);

			if (fromDay.plusDays(MAX_BOOKING_DAYS).isBefore(toDay)) {
				throw new BookingException(STAY_LENGTH_ERROR_MESSAGE);
			}

		} catch (DateTimeParseException e) {
			throw new InputFormatException(DATES_FORMAT_EXCEPTION);
		}
		
	}
	
	public static void validatePastDate(String date, String operationType) throws BookingException, InputFormatException {
		try {
			LocalDate day = LocalDate.parse(date, formatter);

			if (day.isBefore(LocalDate.now())) {
				throw new BookingException(String.format(EXPIRED_BOOKING_EXCEPTION, operationType));
			}
		} catch (DateTimeParseException e) {
			throw new InputFormatException(DATES_FORMAT_EXCEPTION);
		}
		
	}
		
}
