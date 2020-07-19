package com.upgrade.challenge;

import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.util.EmptyStackException;
import java.util.Stack;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.upgrade.challenge.model.BookingRequest;
import com.upgrade.challenge.services.BookingServiceTest;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class VolcanoConcurrentThreadsTest {

	private final int TOTAL_THREADS = 1000;

	private TestRestTemplate rest = new TestRestTemplate();
	
	private final String BASE_URL = "http://localhost:";
	private final String BOOKING_ENDPOINT = "/booking/";
	private final String AVAILABILITY_ENDPOINT = "/availability/alldates?from=%s&to=%s";
	private String urlBooking;
	private String urlAvailability;
	
	private final LocalDate now = LocalDate.now();

	@LocalServerPort
	private int port;
	
	private Stack<String> bookings = new Stack<String>();
	
	/**
	 * Test the normal behavior of the app.
	 */
	@Test
	public void testVolcanoMultiThreadingAnyDatesWithin35Days() throws InterruptedException {
		urlBooking = BASE_URL.concat(String.valueOf(port)).concat(BOOKING_ENDPOINT);
		urlAvailability = BASE_URL.concat(String.valueOf(port)).concat(AVAILABILITY_ENDPOINT);

		runMultithreaded(new Runnable() {
			public void run() {
				try {
					int number = generateRandomNumber();

					// Create Booking
					if (number < 7) {
						System.out.println("-- CREATE --");
						createBookingAnyDates();
						System.out.println("-- END CREATE --");
					
					// Edit Booking	
					} else if (number < 9) {
						System.out.println("-- EDIT --");
						editBooking();
						System.out.println("-- END EDIT --");
						
					// Cancel Booking	
					} else {
						System.out.println("-- CANCEL --");
						cancelBooking();
						System.out.println("-- END CANCEL --");
					}
					printBookingsStack();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		}, TOTAL_THREADS);
		
		// Show Availability
		System.out.println("-- GET --");
		checkAvailability(1, 37);
		System.out.println("-- END GET --");
	}
	
	/**
	 * Test the normal behavior of the app within 3 days and less percentage of bookings.
	 */
	@Test
	public void testVolcanoMultiThreadingFixedDates() throws InterruptedException {
		urlBooking = BASE_URL.concat(String.valueOf(port)).concat(BOOKING_ENDPOINT);
		urlAvailability = BASE_URL.concat(String.valueOf(port)).concat(AVAILABILITY_ENDPOINT);

		runMultithreaded(new Runnable() {
			public void run() {
				try {
					int number = generateRandomNumber();

					// Create Booking
					if (number < 4) {
						System.out.println("-- CREATE --");
						createBookingFixedDates();
						System.out.println("-- END CREATE --");
					
					// Edit Booking	
					} else if (number < 7) {
						System.out.println("-- EDIT --");
						editBooking();
						System.out.println("-- END EDIT --");
						
					// Cancel Booking	
					} else {
						System.out.println("-- CANCEL --");
						cancelBooking();
						System.out.println("-- END CANCEL --");
					}
					printBookingsStack();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		}, TOTAL_THREADS);
		
		// Show Availability
		System.out.println("-- GET --");
		checkAvailability(1, 6);
		System.out.println("-- END GET --");

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

	private void createBookingAnyDates() {
		createBooking(createBookingRequestRandomInfo());
	}
	
	private void createBookingFixedDates() {
		createBooking(BookingServiceTest.createBookingRequest());
	}
	
	private void createBooking(BookingRequest bookingRequest) {
		HttpEntity<BookingRequest> bookingEntity = new HttpEntity<BookingRequest>(bookingRequest);
		ResponseEntity<String> response = rest.postForEntity(urlBooking, bookingEntity, String.class);
		if (response.getStatusCode().is2xxSuccessful()) {
			bookings.push(response.getBody());
			System.out.println("Booking ID: " + response.getBody() + " created!");
		} else {
			System.out.println("Booking not created: " + response.getBody());
		}
	}
	
	private void editBooking() {
		try {
			String bookingId = bookings.pop();
			BookingRequest bookingRequest = createBookingRequestRandomInfo();
			bookingRequest.setFromDay(now.plusDays(3).toString());
			bookingRequest.setToDay(now.plusDays(4).toString());
			rest.put(urlBooking.concat(bookingId), bookingRequest, String.class);
			System.out.println("Booking edited.");
			bookings.push(bookingId);
		} catch (EmptyStackException e) {
			System.out.println("There are no bookings to edit in the stack.");
		}
	}

	private void cancelBooking() {
		try {
			String bookingId = bookings.pop();
			rest.delete(urlBooking.concat(bookingId));
			System.out.println("Booking canceled.");
		} catch (EmptyStackException e) {
			System.out.println("There are no bookings to cancel in the stack.");
		}
	}

	private void checkAvailability(int fromDay, int toDay) {
		String from = now.plusDays(fromDay).toString();
		String to = now.plusDays(toDay).toString();
		ResponseEntity<String> response = rest.getForEntity(String.format(urlAvailability, from, to), String.class);
		assertTrue(response.getStatusCode().is2xxSuccessful());
		System.out.println(response.getBody());
	}
	private BookingRequest createBookingRequestRandomInfo() {
		BookingRequest bookingRequest = new BookingRequest();
		String[] dates = generateRandomDates();
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

	private String generateRandomGuests() {
		return String.valueOf(RandomUtils.nextInt(1, 5));
	}

	private String generateRandomString() {
		return RandomStringUtils.random(8, true, false);
	}

	private String[] generateRandomDates() {
		int dayFrom = RandomUtils.nextInt(1, 35);
		int dayTo = RandomUtils.nextInt(1, 4) + dayFrom;
		return new String[] {now.plusDays(dayFrom).toString(), now.plusDays(dayTo).toString()};
	}
	
	private int generateRandomNumber() {
		return RandomUtils.nextInt(0, 11);
	}
	
	private void printBookingsStack() {
		System.out.println("Bookings: " + bookings.toString());
	}

}
