package com.upgrade.challenge.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
//import org.springframework.stereotype.Repository;

import com.upgrade.challenge.model.DailyAvailability;

//@Repository
public interface DailyAvailabilityRepository extends CrudRepository<DailyAvailability, Integer>{

	/**
	 * Returns true if there is any date with not enough availability for the amount of guests.
	 * @param from
	 * @param to
	 * @param guests
	 */
	Boolean existsByDateBetweenAndGuestsGreaterThan(@Param("from") String from, @Param("to") String to, @Param("guests") Integer guests);

	/**
	 * Find the Daily Availability for a date interval.
	 * @param from
	 * @param to
	 */
	List<DailyAvailability> findAllByDateBetween(@Param("from") String from, @Param("to") String to);

}
