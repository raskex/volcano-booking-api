package com.upgrade.challenge.repository;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

import com.upgrade.challenge.model.DailyAvailability;

@RunWith(SpringRunner.class)
@DataJpaTest
public class DailyAvailabilityRepositoryTest {

	@Autowired
    private TestEntityManager entityManager;
	
	@Autowired
	private DailyAvailabilityRepository dailyAvailabilityRepository;
	
	@Before
	public void setup() {
		DailyAvailability dailyAvailability1 = new DailyAvailability("2020-09-01", 1);
		DailyAvailability dailyAvailability2 = new DailyAvailability("2020-09-02", 2);
		DailyAvailability dailyAvailability3 = new DailyAvailability("2020-09-03", 3);
		DailyAvailability dailyAvailability4 = new DailyAvailability("2020-09-04", 4);
		entityManager.persist(dailyAvailability1);
		entityManager.persist(dailyAvailability2);
		entityManager.persist(dailyAvailability3);
		entityManager.persist(dailyAvailability4);
		entityManager.flush();
	}
	@Test
	public void testWhenFindAllByDateBetween() {
		List<DailyAvailability> dayAvailabilitiesFound = dailyAvailabilityRepository.findAllByDateBetween("2020-09-02", "2020-09-03"); 

		assertNotNull(dayAvailabilitiesFound);
		assertEquals(2, dayAvailabilitiesFound.size());
		assertEquals(dayAvailabilitiesFound.get(0).getDate(), "2020-09-02");
		assertEquals(dayAvailabilitiesFound.get(0).getGuests(), 2);
		assertEquals(dayAvailabilitiesFound.get(1).getDate(), "2020-09-03");
		assertEquals(dayAvailabilitiesFound.get(1).getGuests(), 3);
	}
	
	@Test
	public void testExistsByDateBetweenAndGuestsGreaterThanFound() {
		boolean dayAvailabilitiesFound = dailyAvailabilityRepository
				.existsByDateBetweenAndGuestsGreaterThan("2020-09-01", "2020-09-04", 3);

		assertTrue(dayAvailabilitiesFound);
	}

	@Test
	public void testExistsByDateBetweenAndGuestsGreaterThanNotFound() {
		boolean dayAvailabilitiesFound = dailyAvailabilityRepository
				.existsByDateBetweenAndGuestsGreaterThan("2020-09-01", "2020-09-04", 5);

		assertFalse(dayAvailabilitiesFound);
	}

}
