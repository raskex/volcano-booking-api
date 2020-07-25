package com.upgrade.challenge.repository;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

import com.upgrade.challenge.model.dto.DailyOccupation;

@RunWith(SpringRunner.class)
@DataJpaTest
public class DailyOccupationRepositoryTest {

	@Autowired
    private TestEntityManager entityManager;
	
	@Autowired
	private DailyOccupationRepository dailyAvailabilityRepository;
	
	@Before
	public void setup() {
		DailyOccupation dailyAvailability1 = new DailyOccupation(LocalDate.parse("2020-09-01"), 1);
		DailyOccupation dailyAvailability2 = new DailyOccupation(LocalDate.parse("2020-09-02"), 2);
		DailyOccupation dailyAvailability3 = new DailyOccupation(LocalDate.parse("2020-09-03"), 3);
		DailyOccupation dailyAvailability4 = new DailyOccupation(LocalDate.parse("2020-09-04"), 4);
		entityManager.persist(dailyAvailability1);
		entityManager.persist(dailyAvailability2);
		entityManager.persist(dailyAvailability3);
		entityManager.persist(dailyAvailability4);
		entityManager.flush();
	}
	@Test
	public void testWhenFindAllByDateBetween() {
		List<DailyOccupation> dayAvailabilitiesFound = dailyAvailabilityRepository
				.findAllByDateBetween(LocalDate.parse("2020-09-02"), LocalDate.parse("2020-09-03"));

		assertNotNull(dayAvailabilitiesFound);
		assertEquals(2, dayAvailabilitiesFound.size());
		assertEquals(dayAvailabilitiesFound.get(0).getDate(), LocalDate.parse("2020-09-02"));
		assertEquals(dayAvailabilitiesFound.get(0).getGuests(), 2);
		assertEquals(dayAvailabilitiesFound.get(1).getDate(), LocalDate.parse("2020-09-03"));
		assertEquals(dayAvailabilitiesFound.get(1).getGuests(), 3);
	}
	
	@Test
	public void testExistsByDateBetweenAndGuestsGreaterThanFound() {
		boolean dayAvailabilitiesFound = dailyAvailabilityRepository
				.existsByDateBetweenAndGuestsGreaterThan(LocalDate.parse("2020-09-01"), LocalDate.parse("2020-09-04"), 3);

		assertTrue(dayAvailabilitiesFound);
	}

	@Test
	public void testExistsByDateBetweenAndGuestsGreaterThanNotFound() {
		boolean dayAvailabilitiesFound = dailyAvailabilityRepository
				.existsByDateBetweenAndGuestsGreaterThan(LocalDate.parse("2020-09-01"), LocalDate.parse("2020-09-04"), 5);

		assertFalse(dayAvailabilitiesFound);
	}

}
