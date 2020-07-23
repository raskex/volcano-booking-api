package com.upgrade.challenge.controllers;

import javax.validation.Valid;
import javax.validation.constraints.Min;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.upgrade.challenge.model.BookingRequest;
import com.upgrade.challenge.model.BookingResponse;
import com.upgrade.challenge.services.BookingService;

@Validated
@RestController
@RequestMapping(path="/booking")
public class BookingController {
	
	@Autowired
	private BookingService bookingService;
	
	@GetMapping(path = "/{bookingId}")
	public BookingResponse get(
			@PathVariable(name = "bookingId", required = true) @Min(value = 1, message = "bookingId should be a positive number") Integer bookingId) {

		return bookingService.get(bookingId);
	}

	@PostMapping(path= "/")
	public BookingResponse book(@Valid @RequestBody BookingRequest booking) {
		
		return bookingService.add(booking);
	}

	@PutMapping(path= "/{bookingId}")
	public BookingResponse edit(@PathVariable(name = "bookingId", required = true) @Min(value = 1, message = "bookingId should be a positive number") Integer bookingId,
			@RequestBody BookingRequest booking) {

		return bookingService.edit(bookingId, booking);
	}

	@DeleteMapping(path= "/{bookingId}")
	public void cancel(@PathVariable(name= "bookingId", required = true) @Min(value = 1, message = "bookingId should be a positive number") Integer bookingId) {
		
		bookingService.delete(bookingId);
	}

}
