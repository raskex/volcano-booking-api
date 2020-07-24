package com.upgrade.challenge;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.time.LocalDate;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.upgrade.challenge.model.BookingRequest;
import com.upgrade.challenge.services.BookingServiceTest;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class VolcanoIntegrationTest {

	private static final Logger logger = LoggerFactory.getLogger(VolcanoIntegrationTest.class);
	private TestRestTemplate rest = new TestRestTemplate();
	private static final String BASE_URL = "http://localhost:";
	private static final String BOOKING_ENDPOINT = "/booking/";
	private static final String GET_AVAILABILITY_ENDPOINT = "/availability/?from=%s&to=%s";

	@LocalServerPort
	private int port;

	@Test
	public void testBookAndGetAndEditAndDeleteBooking() throws Exception {
		String urlBooking = BASE_URL.concat(String.valueOf(port)).concat(BOOKING_ENDPOINT);
		String urlAvailability = BASE_URL.concat(String.valueOf(port)).concat(GET_AVAILABILITY_ENDPOINT);
		LocalDate now = LocalDate.now();
		String from = now.plusDays(1).toString();
		String to = now.plusDays(6).toString();

		logger.info("----------- INIT -----------");
		getAvailability(urlAvailability, from, to);
		logger.info("-----------");

		BookingRequest bookingRequest = BookingServiceTest.createBookingRequest();
		ResponseEntity<String> bookingResponse = rest
				.exchange(RequestEntity.post(URI.create(urlBooking)).body(bookingRequest), String.class);
		assertTrue(bookingResponse.getStatusCode().is2xxSuccessful());
		Integer bookingId = (Integer) new JSONObject(bookingResponse.getBody()).get("id");
		logger.info("CREATED BOOKING ID: " + bookingId);
		logger.info("-----------");

		bookingResponse = rest.exchange(RequestEntity.get(URI.create(urlBooking + bookingId)).build(), String.class);
		assertTrue(bookingResponse.getStatusCode().is2xxSuccessful());
		assertEquals(bookingId, (Integer) new JSONObject(bookingResponse.getBody()).get("id"));
		logger.info("GET BOOKING ID " + bookingId + ": " + bookingResponse.getBody().toString());
		logger.info("-----------");

		getAvailability(urlAvailability, from, to);
		logger.info("-----------");

		bookingRequest.setFromDay(bookingRequest.getFromDay().plusDays(2));
		bookingRequest.setToDay(bookingRequest.getToDay().plusDays(2));
		HttpEntity<BookingRequest> newEntity = new HttpEntity<BookingRequest>(bookingRequest);
		rest.put(urlBooking + bookingId, newEntity);
		logger.info("PUT BOOKING ID: " + bookingId);
		logger.info("-----------");

		bookingResponse = rest.exchange(RequestEntity.get(URI.create(urlBooking + bookingId)).build(), String.class);
		assertEquals(bookingId, (Integer) new JSONObject(bookingResponse.getBody()).get("id"));
		logger.info("GET BOOKING ID " + bookingId + ": " + bookingResponse.getBody());
		logger.info("-----------");

		getAvailability(urlAvailability, from, to);
		logger.info("-----------");

		bookingResponse = rest.exchange(RequestEntity.delete(URI.create(urlBooking + bookingId)).build(), String.class);
		logger.info("DELETE BOOKING ID: " + bookingId);
		logger.info("-----------");

		bookingResponse = rest.exchange(RequestEntity.get(URI.create(urlBooking + bookingId)).build(), String.class);
		assertTrue(bookingResponse.getStatusCode().is4xxClientError());
		logger.info("GET BOOKING ID " + bookingId + ": " + bookingResponse.getBody());
		logger.info("-----------");

		getAvailability(urlAvailability, from, to);
		logger.info("----------- END -----------");
	}

	private void getAvailability(String url, String from, String to) {
		ResponseEntity<String> responseAvailability = rest
				.exchange(RequestEntity.get(URI.create(String.format(url, from, to))).build(), String.class);
		assertTrue(responseAvailability.getStatusCode().is2xxSuccessful());
		logger.info("AVAILABILITY: " + responseAvailability.getBody());
	}

}
