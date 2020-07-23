package com.upgrade.challenge.services;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
//import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.upgrade.challenge.exception.AvailabilityException;
import com.upgrade.challenge.exception.BookingException;
import com.upgrade.challenge.exception.InputFormatException;
import com.upgrade.challenge.model.DailyAvailability;
import com.upgrade.challenge.model.dto.DailyOccupation;
import com.upgrade.challenge.repository.DailyOccupationRepository;
import com.upgrade.challenge.validator.BookingValidator;

@Service
public class DailyAvailabilityService {

	@Autowired
	private DailyOccupationRepository dailyOccupationRepository;

	@Autowired
	private BookingValidator validator;

	@Value("${volcano.date_format}")
	public String DATE_FORMAT;

	public List<DailyAvailability> getAvailability(LocalDate from, LocalDate to) throws BookingException, InputFormatException {
		LocalDate now = LocalDate.parse(LocalDate.now().toString(), DateTimeFormatter.ISO_DATE);
		if (Objects.isNull(from) && Objects.isNull(to)) {
			from = now.plusDays(validator.getMinimumDaysAheadOfArrival());
			to = now.plusMonths(validator.getMonthsUpToBooking());
		} else {
			if (StringUtils.isEmpty(from)) {
				from = now.plusDays(validator.getMinimumDaysAheadOfArrival());
			}
			if (StringUtils.isEmpty(to)) {
				to = now.plusMonths(validator.getMonthsUpToBooking());
			}
			validator.validateDateInput(from, to, false);
		}
		List<DailyAvailability> availabilityResult = new LinkedList<DailyAvailability>();

		LocalDate consecutiveDate = from;
		LocalDate endDate = to.minusDays(1);
		List<DailyOccupation> occupabilities = dailyOccupationRepository.findAllByDateBetweenOrderByDateAsc(from.toString(), endDate.toString());
		for (DailyOccupation dailyOccupation : occupabilities) {
			DailyAvailability dailyAvailability = new DailyAvailability(LocalDate.parse(dailyOccupation.getDate(), DateTimeFormatter.ISO_DATE), validator.getMaxCapacity() - dailyOccupation.getGuests());
			LocalDate currentDate = LocalDate.parse(dailyOccupation.getDate(), DateTimeFormatter.ISO_DATE);
			while (consecutiveDate.isBefore(currentDate)) {
				availabilityResult.add(new DailyAvailability(consecutiveDate, validator.getMaxCapacity()));
				consecutiveDate = consecutiveDate.plusDays(1);
			}
			availabilityResult.add(dailyAvailability);
			consecutiveDate = consecutiveDate.plusDays(1);
		}
		while (!consecutiveDate.isAfter(endDate)) {
			availabilityResult.add(new DailyAvailability(consecutiveDate, validator.getMaxCapacity()));
			consecutiveDate = consecutiveDate.plusDays(1);
		}

		return availabilityResult;
	}

	public void validateAvailability(LocalDate from, LocalDate to, Integer guests, Boolean isBooking)
			throws AvailabilityException, BookingException, InputFormatException {
		validator.validateDateInput(from, to, isBooking);
		validator.validateGuestsInput(guests);
		String endDate = to.minusDays(1).toString();
		if (dailyOccupationRepository.existsByDateBetweenAndGuestsGreaterThan(
				from.toString(), endDate.toString(), validator.getMaxCapacity() - Integer.valueOf(guests))) {
			throw new AvailabilityException(String.format(
					"There is no availability for the selected dates - From: %s, To: %s and %s guest(s)."
					+ " Please try again with differents dates.", from, to, guests));
		}
	}

	@Transactional
	public void blockAvailability(LocalDate from, LocalDate to, Integer guests) throws AvailabilityException {
		List<DailyOccupation> daysToBlock = new LinkedList<DailyOccupation>();
		LocalDate consecutiveDate = from;
		LocalDate endDate = to.minusDays(1);

		List<DailyOccupation> occupabilities = dailyOccupationRepository.findAllByDateBetweenOrderByDateAsc(from.toString(), endDate.toString());
		for (DailyOccupation dailyOccupation : occupabilities) {
			dailyOccupation.setGuests(dailyOccupation.getGuests() + guests);
			LocalDate currentDate = LocalDate.parse(dailyOccupation.getDate(), DateTimeFormatter.ISO_DATE);
			while (consecutiveDate.isBefore(currentDate)) {
				daysToBlock.add(new DailyOccupation(consecutiveDate.toString(), guests));
				consecutiveDate = consecutiveDate.plusDays(1);
			}
			daysToBlock.add(dailyOccupation);
			consecutiveDate = consecutiveDate.plusDays(1);
		}
		while (!consecutiveDate.isAfter(endDate)) {
			daysToBlock.add(new DailyOccupation(consecutiveDate.toString(), guests));
			consecutiveDate = consecutiveDate.plusDays(1);
		}
		dailyOccupationRepository.saveAll(daysToBlock);
	}

	@Transactional
	public void releaseAvailability(LocalDate from, LocalDate to, Integer guests) {
		String endBookingDate = to.minusDays(1).toString();
		List<DailyOccupation> daysToRelease = new LinkedList<DailyOccupation>();
		List<DailyOccupation> occupability = dailyOccupationRepository.findAllByDateBetween(from.toString(), endBookingDate);
		for (DailyOccupation dailyOccupation : occupability) {
			dailyOccupation.setGuests(dailyOccupation.getGuests() - guests);
			daysToRelease.add(dailyOccupation);
		}
		dailyOccupationRepository.saveAll(daysToRelease);
	}

}
