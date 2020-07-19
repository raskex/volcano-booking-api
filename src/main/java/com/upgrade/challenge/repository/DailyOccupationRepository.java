package com.upgrade.challenge.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.upgrade.challenge.model.DailyOccupation;

@Repository
public interface DailyOccupationRepository extends CrudRepository<DailyOccupation, Integer>{

	/**
	 * Returns true if there is any date with not enough availability for the amount of guests.
	 * @param from
	 * @param to
	 * @param guests
	 */
	Boolean existsByDateBetweenAndGuestsGreaterThan(String from, String to, Integer guests);

	/**
	 * Find the Daily Availability for a date interval.
	 * @param from
	 * @param to
	 */
	List<DailyOccupation> findAllByDateBetween(String from, String to);

	/**
	 * Find the Daily Availability for a date interval ordered by date asc.
	 * @param from
	 * @param to
	 * @return
	 */
	List<DailyOccupation> findAllByDateBetweenOrderByDateAsc(String from, String to);

}
