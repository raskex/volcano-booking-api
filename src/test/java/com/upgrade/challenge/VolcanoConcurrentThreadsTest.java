package com.upgrade.challenge;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.time.LocalDate;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.upgrade.challenge.model.BookingRequest;
import com.upgrade.challenge.services.BookingServiceTest;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class VolcanoConcurrentThreadsTest {

	private static final Logger logger = LoggerFactory.getLogger(VolcanoConcurrentThreadsTest.class);

	private final int TOTAL_THREADS = 100;
	private final int OPERATIONS_PER_THREAD = 20;

	private TestRestTemplate rest = new TestRestTemplate();
	
	private final String BASE_URL = "http://localhost:";
	private final String BOOKING_ENDPOINT = "/booking/";
	private final String AVAILABILITY_ENDPOINT = "/availability/?from=%s&to=%s";
	private String urlBooking;
	private String urlAvailability;
	
	private final LocalDate now = LocalDate.now();

	@LocalServerPort
	private int port;
	
	private ConcurrentLinkedQueue<Integer> bookingsQueue = new ConcurrentLinkedQueue<Integer>();
	
	/**
	 * Test the normal behavior of the app.
	 */
	@Test
	public void testVolcanoMultiThreadingAnyDatesWithin35Days() throws InterruptedException {
		urlBooking = BASE_URL + port + BOOKING_ENDPOINT;
		urlAvailability = BASE_URL + port + AVAILABILITY_ENDPOINT;

		AtomicInteger createFailures = new AtomicInteger();
		AtomicInteger editFailures = new AtomicInteger();
		AtomicInteger cancelFailures = new AtomicInteger();
		runMultithreaded(new Runnable() {
			public void run() {
				for (int i = 0; i < OPERATIONS_PER_THREAD; i++) {
					try {
						int number = generateRandomNumber();

						// Create Booking
						if (number < 6) {
							logger.info("-- CREATE --");
							if (!createBookingAnyDates()) {
								createFailures.incrementAndGet();
							}
							logger.info("-- END CREATE --");
						
						// Edit Booking	
						} else if (number < 8) {
							logger.info("-- EDIT --");
							if (!editBooking()) {
								editFailures.incrementAndGet();
							}
							logger.info("-- END EDIT --");
							
						// Cancel Booking	
						} else {
							logger.info("-- CANCEL --");
							if (!cancelBooking()) {
								cancelFailures.incrementAndGet();
							}
							logger.info("-- END CANCEL --");
						}
						printBookingsStack();
					} catch (Exception e) {
						e.printStackTrace();
					}				
				}

			}

		}, TOTAL_THREADS);
		
		// Show Availability
		logger.info("-- GET --");
		checkAvailability(now.plusDays(1), now.plusDays(37));
		logger.info("-- END GET --");

		assertEquals(0, createFailures.get());
		assertEquals(0, editFailures.get());
		assertEquals(0, cancelFailures.get());
	}
	
	/**
	 * Test the normal behavior of the app within 3 days and less percentage of bookings.
	 */
	@Test
	public void testVolcanoMultiThreadingFixedDates() throws InterruptedException {
		urlBooking = BASE_URL + port + BOOKING_ENDPOINT;
		urlAvailability = BASE_URL + port + AVAILABILITY_ENDPOINT;

		AtomicInteger createFailures = new AtomicInteger();
		AtomicInteger editFailures = new AtomicInteger();
		AtomicInteger cancelFailures = new AtomicInteger();
		runMultithreaded(new Runnable() {
			public void run() {
				for (int i = 0; i < OPERATIONS_PER_THREAD; i++) {
					try {
						int number = generateRandomNumber();
						
						// Create Booking
						if (number < 6) {
							logger.info("-- CREATE --");
							if (!createBookingFixedDates()) {
								createFailures.incrementAndGet();
							}
							logger.info("-- END CREATE --");
						
						// Edit Booking	
						} else if (number < 8) {
							logger.info("-- EDIT --");
							if (!editBooking()) {
								editFailures.incrementAndGet();
							}
							logger.info("-- END EDIT --");
							
						// Cancel Booking	
						} else {
							logger.info("-- CANCEL --");
							if (!cancelBooking()) {
								cancelFailures.incrementAndGet();
							}
							logger.info("-- END CANCEL --");
						}
						printBookingsStack();
					} catch (Exception e) {
						e.printStackTrace();
					}				
				}

			}

		}, TOTAL_THREADS);
		
		// Show Availability
		logger.info("-- GET --");
		checkAvailability(now.plusDays(1), now.plusDays(6));
		logger.info("-- END GET --");

		assertEquals(0, createFailures.get());
		assertEquals(0, editFailures.get());
		assertEquals(0, cancelFailures.get());
	}
	
	private void runMultithreaded(Runnable runnable, int threadCount) throws InterruptedException {
		Thread[] threads = new Thread[threadCount];
		for (int i = 0; i < threadCount; i++) {
			threads[i] = new Thread(runnable);
			threads[i].start();
		}
		for (int i = 0; i < threadCount; i++) {
			threads[i].join();
		}
	}

	private boolean createBookingAnyDates() throws JSONException {
		return createBooking(createBookingRequestRandomInfo());
	}
	
	private boolean createBookingFixedDates() throws JSONException {
		return createBooking(BookingServiceTest.createBookingRequest());
	}
	
	private boolean createBooking(BookingRequest bookingRequest) throws JSONException {
		ResponseEntity<String> response = rest.exchange(
				RequestEntity.post(URI.create(urlBooking)).body(bookingRequest), String.class);
		if (response.getStatusCode().is2xxSuccessful()) {
			JSONObject responseBody = new JSONObject(response.getBody());
			bookingsQueue.add((Integer) responseBody.get("id"));
			logger.info("Booking ID: " + responseBody.get("id") + " created!");
			return true;
		} else if (response.getStatusCode().is4xxClientError()) {
			logger.info("Booking could not be created: " + response.getBody());
			return true;
		} else {
			logger.info("Booking not created: " + response.getBody());
			return false;
		}
	}
	
	private boolean editBooking() {
		Integer bookingId = bookingsQueue.poll();
		if (bookingId == null) {
			logger.info("There are no bookings to edit in the stack.");
			return true;
		}
		BookingRequest bookingRequest = createBookingRequestRandomInfo();
		bookingRequest.setFromDay(now.plusDays(2));
		bookingRequest.setToDay(now.plusDays(4));
		ResponseEntity<String> response = rest.exchange(
				RequestEntity.put(URI.create(urlBooking + bookingId)).body(bookingRequest), String.class);
		bookingsQueue.add(bookingId);
		if (response.getStatusCode().is2xxSuccessful()) {
			logger.info("Booking edited.");
			return true;
		} else if (response.getStatusCode().is4xxClientError()) {
			logger.info("Booking could not be edited: " + response.getBody());
			return true;
		} else {
			logger.info("Booking could not be edited: " + response.getBody());
			return false;
		}
	}

	private boolean cancelBooking() {
		Integer bookingId = bookingsQueue.poll();
		if (bookingId == null) {
			logger.info("There are no bookings to cancel in the stack.");
			return true;
		}
		ResponseEntity<String> response = rest.exchange(
				RequestEntity.delete(URI.create(urlBooking + bookingId)).build(), String.class);
		if (response.getStatusCode().is2xxSuccessful()) {
			logger.info("Booking canceled.");
			return true;
		} else if (response.getStatusCode().is4xxClientError()) {
			logger.info("Booking could not be canceled: " + response.getBody());
			return true;
		}
		logger.info("Booking could not be canceled: " + response.getBody());
		return false;
	}

	private void checkAvailability(LocalDate fromDay, LocalDate toDay) {
		ResponseEntity<String> response = rest.getForEntity(String.format(urlAvailability, fromDay, toDay), String.class);
		assertTrue(response.getStatusCode().is2xxSuccessful());
		logger.info(response.getBody());
	}
	private BookingRequest createBookingRequestRandomInfo() {
		BookingRequest bookingRequest = new BookingRequest();
		LocalDate[] dates = generateRandomDates();
		bookingRequest.setFromDay(dates[0]);
		bookingRequest.setToDay(dates[1]);
		bookingRequest.setGuests(generateRandomGuests());
		bookingRequest.setFirstName(generateRandomString());
		bookingRequest.setLastName(generateRandomString());
		bookingRequest.setEmail(generateRandomEmail());
		return bookingRequest;
	}
	
	private String generateRandomEmail() {
		return RandomStringUtils.random(15, true, true).concat("@mail.com");
	}

	private Integer generateRandomGuests() {
		return RandomUtils.nextInt(1, 5);
	}

	private String generateRandomString() {
		return RandomStringUtils.random(8, true, false);
	}

	private LocalDate[] generateRandomDates() {
		int dayFrom = RandomUtils.nextInt(1, 35);
		int dayTo = RandomUtils.nextInt(1, 4) + dayFrom;
		return new LocalDate[] {now.plusDays(dayFrom), now.plusDays(dayTo)};
	}
	
	private int generateRandomNumber() {
		return RandomUtils.nextInt(0, 11);
	}
	
	private void printBookingsStack() {
		logger.info("Booking: " + bookingsQueue.toString());
	}
	
}
