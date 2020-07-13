package com.upgrade.challenge.services;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.LinkedList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;

import com.upgrade.challenge.exception.BookingException;
import com.upgrade.challenge.exception.InputFormatException;
import com.upgrade.challenge.model.DailyAvailability;
import com.upgrade.challenge.repository.DailyAvailabilityRepository;

@RunWith(SpringRunner.class)
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
    private DailyAvailabilityRepository dailyAvailabilityRepository;
 
    @Test
    public void testGetAvailability() throws BookingException, InputFormatException {
    	List<DailyAvailability> days = new LinkedList<DailyAvailability>();
    	days.add(new DailyAvailability("2020-09-01", 2));
    	days.add(new DailyAvailability("2020-09-02", 3));
    	days.add(new DailyAvailability("2020-09-04", 5));
    	days.add(new DailyAvailability("2020-09-05", 2));
    	when(dailyAvailabilityRepository.findAllByDateBetween(anyString(), anyString())).thenReturn(days);
    	
    	List<DailyAvailability> expectedAvailability = new LinkedList<DailyAvailability>();
    	expectedAvailability.add(new DailyAvailability("2020-09-01", 8));
    	expectedAvailability.add(new DailyAvailability("2020-09-02", 7));
    	expectedAvailability.add(new DailyAvailability("2020-09-03", 10));
    	expectedAvailability.add(new DailyAvailability("2020-09-04", 5));
    	expectedAvailability.add(new DailyAvailability("2020-09-05", 8));

    	dailyAvailabilityService.getAvailability("2020-09-01", "2020-09-05");
    }
}
