package com.upgrade.challenge.controllers;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.upgrade.challenge.exception.AvailabilityException;
import com.upgrade.challenge.exception.BookingException;
import com.upgrade.challenge.exception.BookingNotFoundException;
import com.upgrade.challenge.exception.InputFormatException;
import com.upgrade.challenge.model.Booking;
import com.upgrade.challenge.model.BookingRequest;
import com.upgrade.challenge.services.BookingService;

@Validated
@RestController
@RequestMapping(path="/booking")
public class BookingController {
	
	@Autowired
	private BookingService bookingService;
	
	@GetMapping(path= "/{bookingId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Booking> get(@PathVariable(name= "bookingId", required = false) String bookingId)
			throws BookingNotFoundException, InputFormatException {

		return new ResponseEntity<Booking>(bookingService.get(bookingId), HttpStatus.OK);
	}

	@PostMapping(path= "/", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
	public ResponseEntity<String> book(@Valid @RequestBody BookingRequest booking)
			throws BookingException, AvailabilityException, InputFormatException {
		
		return new ResponseEntity<String>(String.valueOf(bookingService.add(booking)), HttpStatus.OK);
	}

	@DeleteMapping(path= "/{bookingId}", produces = MediaType.TEXT_PLAIN_VALUE)
	public ResponseEntity<String> cancel(@PathVariable(name= "bookingId", required = false) String bookingId)
			throws InputFormatException, BookingNotFoundException, AvailabilityException, BookingException {
		
		bookingService.delete(bookingId);
		
		return new ResponseEntity<String>(String.format("Booking ID: %s succesfully cancelled.", bookingId), HttpStatus.OK);
	}

	@PutMapping(path= "/{bookingId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
	public ResponseEntity<String> edit(@PathVariable(name= "bookingId", required = false) String bookingId,
			@RequestBody BookingRequest booking)
					throws AvailabilityException, BookingException, InputFormatException, BookingNotFoundException {

		if (bookingService.edit(bookingId, booking)) {
			return new ResponseEntity<String>(String.format("Booking ID: %s succesfully modified.", bookingId), HttpStatus.OK);
		}

		return ResponseEntity.ok(
				String.format("Booking ID: %s not modified. There were no differences against the stored booking.", bookingId));
	}

}
