package com.upgrade.challenge.services;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;

import com.upgrade.challenge.exception.BookingException;
import com.upgrade.challenge.exception.InputFormatException;
import com.upgrade.challenge.model.DailyAvailability;
import com.upgrade.challenge.repository.DailyAvailabilityRepository;
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

        @Bean
        public BookingValidator bookingValidator() {
        	BookingValidator.DATE_FORMAT = "yyyy-MM-dd";
        	return new BookingValidator();
        }
    }
 
    @Autowired
    private DailyAvailabilityService dailyAvailabilityService;
 
    @MockBean
    private DailyAvailabilityRepository dailyAvailabilityRepository;
 
    @Test
    public void testGetAvailabilityWithFromTo() throws BookingException, InputFormatException {
		LocalDate now = LocalDate.now();
    	List<DailyAvailability> days = new LinkedList<DailyAvailability>();
    	days.add(new DailyAvailability(now.plusDays(1).toString(), 2));
    	days.add(new DailyAvailability(now.plusDays(2).toString(), 3));
    	days.add(new DailyAvailability(now.plusDays(5).toString(), 10));
    	when(dailyAvailabilityRepository.findAllByDateBetween(anyString(), anyString())).thenReturn(days);
    	
    	List<DailyAvailability> expectedAvailability = new LinkedList<DailyAvailability>();
    	expectedAvailability.add(new DailyAvailability(now.plusDays(1).toString(), 8));
    	expectedAvailability.add(new DailyAvailability(now.plusDays(2).toString(), 7));
    	expectedAvailability.add(new DailyAvailability(now.plusDays(3).toString(), 10));
    	expectedAvailability.add(new DailyAvailability(now.plusDays(4).toString(), 10));
    	expectedAvailability.add(new DailyAvailability(now.plusDays(5).toString(), 0));

		List<DailyAvailability> currentAvailability = dailyAvailabilityService
				.getAvailability(now.plusDays(1).toString(), now.plusDays(6).toString());

		assertNotNull(currentAvailability);
		assertFalse(currentAvailability.isEmpty());
		assertEquals(5, currentAvailability.size());
		for (int i = 0; i < 5; i++) {
			assertEquals(expectedAvailability.get(i).getDate(), currentAvailability.get(i).getDate());
			assertEquals(expectedAvailability.get(i).getGuests(), currentAvailability.get(i).getGuests());
		}
    }

    @Test
    public void testGetAvailabilityOnlyWithFrom() throws BookingException, InputFormatException {
		LocalDate now = LocalDate.now();
    	List<DailyAvailability> days = new LinkedList<DailyAvailability>();
    	days.add(new DailyAvailability(now.plusDays(20).toString(), 2));
    	days.add(new DailyAvailability(now.plusDays(21).toString(), 3));
    	days.add(new DailyAvailability(now.plusDays(25).toString(), 10));
    	when(dailyAvailabilityRepository.findAllByDateBetween(anyString(), anyString())).thenReturn(days);
    	
    	List<DailyAvailability> expectedAvailability = new LinkedList<DailyAvailability>();
    	expectedAvailability.add(new DailyAvailability(now.plusDays(20).toString(), 8));
    	expectedAvailability.add(new DailyAvailability(now.plusDays(21).toString(), 7));
    	expectedAvailability.add(new DailyAvailability(now.plusDays(22).toString(), 10));
    	expectedAvailability.add(new DailyAvailability(now.plusDays(23).toString(), 10));
    	expectedAvailability.add(new DailyAvailability(now.plusDays(24).toString(), 10));
    	expectedAvailability.add(new DailyAvailability(now.plusDays(25).toString(), 0));
    	LocalDate oneMonth = now.plusMonths(1);
    	for (LocalDate currentDate = now.plusDays(26); currentDate.isBefore(oneMonth);) {
        	expectedAvailability.add(new DailyAvailability(currentDate.toString(), 10));
        	currentDate = currentDate.plusDays(1);
    	}

		List<DailyAvailability> currentAvailability = dailyAvailabilityService
				.getAvailability(now.plusDays(20).toString(), null);

		assertNotNull(currentAvailability);
		assertFalse(currentAvailability.isEmpty());
		assertEquals(expectedAvailability.size(), currentAvailability.size());
		for (int i = 0; i < 10; i++) {
			assertEquals(expectedAvailability.get(i).getDate(), currentAvailability.get(i).getDate());
			assertEquals(expectedAvailability.get(i).getGuests(), currentAvailability.get(i).getGuests());
		}
    }

    @Test
    public void testGetAvailabilityOnlyWithTo() throws BookingException, InputFormatException {
		LocalDate now = LocalDate.now();
    	List<DailyAvailability> days = new LinkedList<DailyAvailability>();
    	days.add(new DailyAvailability(now.plusDays(1).toString(), 2));
    	days.add(new DailyAvailability(now.plusDays(2).toString(), 10));
    	days.add(new DailyAvailability(now.plusDays(10).toString(), 7));
    	when(dailyAvailabilityRepository.findAllByDateBetween(anyString(), anyString())).thenReturn(days);
    	
    	List<DailyAvailability> expectedAvailability = new LinkedList<DailyAvailability>();
    	expectedAvailability.add(new DailyAvailability(now.plusDays(1).toString(), 8));
    	expectedAvailability.add(new DailyAvailability(now.plusDays(2).toString(), 0));
    	for (LocalDate currentDate = now.plusDays(3); currentDate.isBefore(now.plusDays(10));) {
        	expectedAvailability.add(new DailyAvailability(currentDate.toString(), 10));
        	currentDate = currentDate.plusDays(1);
    	}
    	expectedAvailability.add(new DailyAvailability(now.plusDays(10).toString(), 3));

		List<DailyAvailability> currentAvailability = dailyAvailabilityService
				.getAvailability(null, now.plusDays(11).toString());

		assertNotNull(currentAvailability);
		assertFalse(currentAvailability.isEmpty());
		assertEquals(expectedAvailability.size(), currentAvailability.size());
		for (int i = 0; i < 10; i++) {
			assertEquals(expectedAvailability.get(i).getDate(), currentAvailability.get(i).getDate());
			assertEquals(expectedAvailability.get(i).getGuests(), currentAvailability.get(i).getGuests());
		}
    }

    @Test(expected= InputFormatException.class)
    public void testGetAvailabilityWrongDateFormat() throws BookingException, InputFormatException {
		dailyAvailabilityService.getAvailability(null, "20f20-09-03");
    }

    @Test(expected= BookingException.class)
    public void testGetAvailabilityAlreadyPastFromDate() throws BookingException, InputFormatException {
		LocalDate now = LocalDate.now();
		dailyAvailabilityService.getAvailability(now.minusDays(1).toString(), null);
    }

    @Test(expected= BookingException.class)
    public void testGetAvailabilityArrivalToday() throws BookingException, InputFormatException {
		LocalDate now = LocalDate.now();
		dailyAvailabilityService.getAvailability(now.toString(), null);
    }

    @Test(expected= BookingException.class)
    public void testGetAvailabilityArrivalMoreThanOneMonth() throws BookingException, InputFormatException {
		LocalDate now = LocalDate.now();
		dailyAvailabilityService.getAvailability(now.plusDays(33).toString(), null);
    }

    @Test(expected= BookingException.class)
    public void testGetAvailabilityDepartureBeforeArrival() throws BookingException, InputFormatException {
		LocalDate now = LocalDate.now();
		dailyAvailabilityService.getAvailability(now.plusDays(10).toString(), now.plusDays(5).toString());
    }

}
