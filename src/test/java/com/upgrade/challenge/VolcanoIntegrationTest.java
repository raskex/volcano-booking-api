package com.upgrade.challenge;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.upgrade.challenge.model.BookingRequest;
import com.upgrade.challenge.services.BookingServiceTest;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource("classpath:application-integrationtest.properties")
public class VolcanoIntegrationTest {

		private TestRestTemplate rest = new TestRestTemplate();
		private static final String BASE_URL = "http://localhost:";
		private static final String BOOKING_ENDPOINT = "/booking/";
		private static final String GET_AVAILABILITY_ENDPOINT = "/availability/alldates?from=%s&to=%s";

		@LocalServerPort
		private int port;
		
		@Test
		public void testBookAndGetAndEditAndDeleteBooking() throws Exception {
			String url_booking = BASE_URL.concat(String.valueOf(port)).concat(BOOKING_ENDPOINT);
			String url_availability = BASE_URL.concat(String.valueOf(port)).concat(GET_AVAILABILITY_ENDPOINT);
			LocalDate now = LocalDate.now();

			String from = now.plusDays(1).toString();
			String to = now.plusDays(6).toString();
			ResponseEntity<String> response = rest.getForEntity(String.format(url_availability, from, to), String.class);
			assertTrue(response.getStatusCode().is2xxSuccessful());
			System.out.println("----------- INIT -----------");
			System.out.println(response.getBody());
			System.out.println("-----------");

			BookingRequest bookingRequest = BookingServiceTest.createBookingRequest();
			HttpEntity<BookingRequest> bookingEntity = new HttpEntity<BookingRequest>(bookingRequest);
			response = rest.postForEntity(url_booking, bookingEntity, String.class);
			assertTrue(response.getStatusCode().is2xxSuccessful());
			String bookingId = response.getBody();
			System.out.println("CREATED BOOKING ID: " + response.getBody());
			System.out.println("-----------");
			
			response = rest.getForEntity(url_booking.concat(bookingId), String.class);
			assertEquals(bookingId, String.valueOf(new JSONObject(response.getBody()).get("id")));
			System.out.println("GET BOOKING ID " + bookingId + ": " + response.getBody());
			System.out.println("-----------");

			response = rest.getForEntity(String.format(url_availability, from, to), String.class);
			assertTrue(response.getStatusCode().is2xxSuccessful());
			System.out.println(response.getBody());
			System.out.println("-----------");

			bookingRequest.setFromDay(LocalDate.parse(bookingRequest.getFromDay()).plusDays(2).toString());
			bookingRequest.setToDay(LocalDate.parse(bookingRequest.getToDay()).plusDays(2).toString());
			HttpEntity<BookingRequest> newEntity = new HttpEntity<BookingRequest>(bookingRequest);
			
			rest.put(url_booking.concat(bookingId), newEntity);
			System.out.println("PUT BOOKING ID: " + bookingId);
			System.out.println("-----------");
			
			response = rest.getForEntity(url_booking.concat(bookingId), String.class);
			assertEquals(bookingId, String.valueOf(new JSONObject(response.getBody()).get("id")));
			System.out.println("GET BOOKING ID " + bookingId + ": " + response.getBody());
			System.out.println("-----------");

			response = rest.getForEntity(String.format(url_availability, from, to), String.class);
			assertTrue(response.getStatusCode().is2xxSuccessful());
			System.out.println(response.getBody());
			System.out.println("-----------");

			rest.delete(url_booking.concat(bookingId));
			System.out.println("DELETE BOOKING ID: " + bookingId);
			System.out.println("-----------");

			response = rest.getForEntity(url_booking.concat(bookingId), String.class);
			System.out.println("GET BOOKING ID " + bookingId + ": " + response.getBody());
			System.out.println("-----------");

			response = rest.getForEntity(String.format(url_availability, from, to), String.class);
			assertTrue(response.getStatusCode().is2xxSuccessful());
			System.out.println(response.getBody());
			System.out.println("----------- END -----------");
		}

}
