package com.upgrade.challenge.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.upgrade.challenge.model.dto.DailyOccupation;

@Repository
public interface DailyOccupationRepository extends CrudRepository<DailyOccupation, Long>{

	/**
	 * Returns true if there is any date with not enough availability for the amount of guests.
	 * @param from
	 * @param to
	 * @param guests
	 */
	Boolean existsByDateBetweenAndGuestsGreaterThan(LocalDate from, LocalDate to, Integer guests);

	/**
	 * Find the Daily Availability for a date interval.
	 * @param from
	 * @param to
	 */
	List<DailyOccupation> findAllByDateBetween(LocalDate from, LocalDate to);

	/**
	 * Find the Daily Availability for a date interval ordered by date asc.
	 * @param from
	 * @param to
	 * @return
	 */
	List<DailyOccupation> findAllByDateBetweenOrderByDateAsc(LocalDate from, LocalDate to);

}
