package com.upgrade.challenge.services;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;

import com.upgrade.challenge.exception.AvailabilityException;
import com.upgrade.challenge.exception.BookingException;
import com.upgrade.challenge.exception.InputFormatException;
import com.upgrade.challenge.model.DailyAvailability;
import com.upgrade.challenge.model.dto.DailyOccupation;
import com.upgrade.challenge.repository.DailyOccupationRepository;
import com.upgrade.challenge.validator.BookingValidator;

@RunWith(SpringRunner.class)
@DataJpaTest
public class DailyAvailabilityServiceTest {

	@TestConfiguration
	static class DailyAvailabilityServiceTestContextConfiguration {
		@Bean
		public DailyAvailabilityService dailyAvailabilityService() {
			return new DailyAvailabilityService();
		}
	}

	@Autowired
	private DailyAvailabilityService dailyAvailabilityService;

	@MockBean
	private DailyOccupationRepository dailyAvailabilityRepository;

	@MockBean
    public BookingValidator validator;

	@Value("${volcano.min_days_ahead_of_arrival}")
	private int MINIMUM_DAYS_AHEAD_OF_ARRIVAL;

	@Value("${volcano.months_up_to_booking}")
	private int MONTHS_UP_TO_BOOKING;
	
	@Value("${volcano.max_guests_capacity}")
	private int MAX_CAPACITY;

	private LocalDate now = LocalDate.now();

	@Test
	public void testGetAvailabilityWithFromTo() {
		when(validator.getMaxCapacity()).thenReturn(MAX_CAPACITY);
		
		List<DailyOccupation> days = new LinkedList<DailyOccupation>();
		days.add(new DailyOccupation(now.plusDays(1), 2));
		days.add(new DailyOccupation(now.plusDays(2), 3));
		days.add(new DailyOccupation(now.plusDays(5), 10));
		when(dailyAvailabilityRepository.findAllByDateBetweenOrderByDateAsc(any(LocalDate.class), any(LocalDate.class)))
				.thenReturn(days);

		List<DailyAvailability> expectedAvailability = new LinkedList<DailyAvailability>();
		expectedAvailability.add(new DailyAvailability(now.plusDays(1), 8));
		expectedAvailability.add(new DailyAvailability(now.plusDays(2), 7));
		expectedAvailability.add(new DailyAvailability(now.plusDays(3), 10));
		expectedAvailability.add(new DailyAvailability(now.plusDays(4), 10));
		expectedAvailability.add(new DailyAvailability(now.plusDays(5), 0));

		List<DailyAvailability> currentAvailability = dailyAvailabilityService
				.getAvailability(now.plusDays(1), now.plusDays(6));

		assertNotNull(currentAvailability);
		assertFalse(currentAvailability.isEmpty());
		assertEquals(5, currentAvailability.size());
		for (int i = 0; i < 5; i++) {
			assertEquals(expectedAvailability.get(i).getDate(), currentAvailability.get(i).getDate());
			assertEquals(expectedAvailability.get(i).getAvailability(), currentAvailability.get(i).getAvailability());
		}
		verify(validator, times(1)).validateDatesInput(any(LocalDate.class), any(LocalDate.class), anyBoolean());
	}

	@Test
	public void testGetAvailabilityOnlyWithFrom() throws BookingException, InputFormatException {
		when(validator.getMonthsUpToBooking()).thenReturn(MONTHS_UP_TO_BOOKING);
		when(validator.getMaxCapacity()).thenReturn(MAX_CAPACITY);
		
		List<DailyOccupation> days = new LinkedList<DailyOccupation>();
		days.add(new DailyOccupation(now.plusDays(20), 2));
		days.add(new DailyOccupation(now.plusDays(21), 3));
		days.add(new DailyOccupation(now.plusDays(25), 10));
		when(dailyAvailabilityRepository.findAllByDateBetweenOrderByDateAsc(any(LocalDate.class), any(LocalDate.class)))
				.thenReturn(days);

		List<DailyAvailability> expectedAvailability = new LinkedList<DailyAvailability>();
		expectedAvailability.add(new DailyAvailability(now.plusDays(20), 8));
		expectedAvailability.add(new DailyAvailability(now.plusDays(21), 7));
		expectedAvailability.add(new DailyAvailability(now.plusDays(22), 10));
		expectedAvailability.add(new DailyAvailability(now.plusDays(23), 10));
		expectedAvailability.add(new DailyAvailability(now.plusDays(24), 10));
		expectedAvailability.add(new DailyAvailability(now.plusDays(25), 0));
		LocalDate oneMonth = now.plusMonths(1);
		for (LocalDate currentDate = now.plusDays(26); currentDate.isBefore(oneMonth);) {
			expectedAvailability.add(new DailyAvailability(currentDate, 10));
			currentDate = currentDate.plusDays(1);
		}

		List<DailyAvailability> currentAvailability = dailyAvailabilityService
				.getAvailability(now.plusDays(20), null);

		assertNotNull(currentAvailability);
		assertFalse(currentAvailability.isEmpty());
		assertEquals(expectedAvailability.size(), currentAvailability.size());
		for (int i = 0; i < 10; i++) {
			assertEquals(expectedAvailability.get(i).getDate(), currentAvailability.get(i).getDate());
			assertEquals(expectedAvailability.get(i).getAvailability(), currentAvailability.get(i).getAvailability());
		}
		verify(validator, times(1)).validateDatesInput(any(LocalDate.class), any(LocalDate.class), anyBoolean());
	}

	@Test
	public void testGetAvailabilityOnlyWithTo() throws BookingException, InputFormatException {
		when(validator.getMinimumDaysAheadOfArrival()).thenReturn(MINIMUM_DAYS_AHEAD_OF_ARRIVAL);
		when(validator.getMaxCapacity()).thenReturn(MAX_CAPACITY);
		
		List<DailyOccupation> days = new LinkedList<DailyOccupation>();
		days.add(new DailyOccupation(now.plusDays(1), 2));
		days.add(new DailyOccupation(now.plusDays(2), 10));
		days.add(new DailyOccupation(now.plusDays(10), 7));
		when(dailyAvailabilityRepository.findAllByDateBetweenOrderByDateAsc(any(LocalDate.class), any(LocalDate.class)))
				.thenReturn(days);

		List<DailyAvailability> expectedAvailability = new LinkedList<DailyAvailability>();
		expectedAvailability.add(new DailyAvailability(now.plusDays(1), 8));
		expectedAvailability.add(new DailyAvailability(now.plusDays(2), 0));
		for (LocalDate currentDate = now.plusDays(3); currentDate.isBefore(now.plusDays(10));) {
			expectedAvailability.add(new DailyAvailability(currentDate, 10));
			currentDate = currentDate.plusDays(1);
		}
		expectedAvailability.add(new DailyAvailability(now.plusDays(10), 3));

		List<DailyAvailability> currentAvailability = dailyAvailabilityService.getAvailability(null,
				now.plusDays(11));

		assertNotNull(currentAvailability);
		assertFalse(currentAvailability.isEmpty());
		assertEquals(expectedAvailability.size(), currentAvailability.size());
		for (int i = 0; i < 10; i++) {
			assertEquals(expectedAvailability.get(i).getDate(), currentAvailability.get(i).getDate());
			assertEquals(expectedAvailability.get(i).getAvailability(), currentAvailability.get(i).getAvailability());
		}
		verify(validator, times(1)).validateDatesInput(any(LocalDate.class), any(LocalDate.class), anyBoolean());
	}

	@Test
	public void testValidateAvailabilityOK() throws AvailabilityException, BookingException, InputFormatException {
		when(dailyAvailabilityRepository.existsByDateBetweenAndGuestsGreaterThan(any(LocalDate.class),
				any(LocalDate.class), anyInt())).thenReturn(false);

		dailyAvailabilityService.validateAvailability(now.plusDays(2), now.plusDays(3), 2, false);

		verify(validator, times(1)).validateDatesInput(any(LocalDate.class), any(LocalDate.class), anyBoolean());
		verify(validator, times(1)).validateGuestsInput(anyInt());
	}

	@Test(expected = AvailabilityException.class)
	public void testValidateAvailabilityNotAvailability() throws AvailabilityException, BookingException, InputFormatException {
		when(dailyAvailabilityRepository.existsByDateBetweenAndGuestsGreaterThan(any(LocalDate.class),
				any(LocalDate.class), anyInt())).thenReturn(true);

		dailyAvailabilityService.validateAvailability(now.plusDays(2), now.plusDays(3), 2,
				false);
	}

}
