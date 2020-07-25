package com.upgrade.challenge.services;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.upgrade.challenge.exception.AvailabilityException;
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

	public List<DailyAvailability> getAvailability(LocalDate from, LocalDate to) {
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
			validator.validateDatesInput(from, to, false);
		}
		List<DailyAvailability> availabilityResult = new LinkedList<DailyAvailability>();

		LocalDate consecutiveDate = from;
		LocalDate endDate = to.minusDays(1);
		List<DailyOccupation> occupabilities = dailyOccupationRepository.findAllByDateBetweenOrderByDateAsc(from, endDate);
		for (DailyOccupation dailyOccupation : occupabilities) {
			DailyAvailability dailyAvailability = new DailyAvailability(dailyOccupation.getDate(),
					validator.getMaxCapacity() - dailyOccupation.getGuests());
			LocalDate currentDate = dailyOccupation.getDate();
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

	public void validateAvailability(LocalDate from, LocalDate to, Integer guests, Boolean isBooking) {
		validator.validateDatesInput(from, to, isBooking);
		validator.validateGuestsInput(guests);
		if (dailyOccupationRepository.existsByDateBetweenAndGuestsGreaterThan(
				from, to.minusDays(1), validator.getMaxCapacity() - guests)) {
			throw new AvailabilityException(String.format(
					"There is no availability for the selected dates - From: %s, To: %s and %s guest(s)."
					+ " Please try again with differents dates.", from, to, guests));
		}
	}

	@Transactional
	public void blockAvailability(LocalDate from, LocalDate to, Integer guests) {
		LocalDate endDate = to.minusDays(1);
		Map<LocalDate, DailyOccupation> currentOccupation = dailyOccupationRepository
				.findAllByDateBetweenOrderByDateAsc(from, endDate).stream()
				.collect(Collectors.toMap(DailyOccupation::getDate, Function.identity()));
		List<DailyOccupation> futureOccupation = new LinkedList<DailyOccupation>();
		for (LocalDate current = from; !current.isAfter(endDate); current = current.plusDays(1)) {
			DailyOccupation dailyOccupation = currentOccupation.get(current);
			if (dailyOccupation == null) {
				futureOccupation.add(new DailyOccupation(current, guests));
			} else {
				dailyOccupation.setGuests(dailyOccupation.getGuests() + guests);
				futureOccupation.add(dailyOccupation);
			}
		}
		dailyOccupationRepository.saveAll(futureOccupation);
	}

	@Transactional
	public void releaseAvailability(LocalDate from, LocalDate to, Integer guests) {
		List<DailyOccupation> daysToRelease = new LinkedList<DailyOccupation>();
		List<DailyOccupation> occupability = dailyOccupationRepository.findAllByDateBetween(from, to.minusDays(1));
		for (DailyOccupation dailyOccupation : occupability) {
			dailyOccupation.setGuests(dailyOccupation.getGuests() - guests);
			daysToRelease.add(dailyOccupation);
		}
		dailyOccupationRepository.saveAll(daysToRelease);
	}

}
