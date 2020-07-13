package com.upgrade.challenge.controllers;

import java.util.List;

import javax.validation.constraints.NotEmpty;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.upgrade.challenge.exception.AvailabilityException;
import com.upgrade.challenge.exception.BookingException;
import com.upgrade.challenge.exception.InputFormatException;
import com.upgrade.challenge.model.DailyAvailability;
import com.upgrade.challenge.services.DailyAvailabilityService;


@Validated
@RestController
@RequestMapping(path="/availability")
public class DailyAvailabilityController {

	@Autowired
	private DailyAvailabilityService dailyAvailabilityService;
	
	@GetMapping(path="/alldates", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<DailyAvailability>> alldates(
			@RequestParam(name="from", required = false) String from,
			@RequestParam(name="to", required = false) String to)
					throws BookingException, InputFormatException {

		return new ResponseEntity<List<DailyAvailability>>(dailyAvailabilityService.getAvailability(from, to), HttpStatus.OK);
	}

	@GetMapping(produces = MediaType.TEXT_PLAIN_VALUE)
	public ResponseEntity<String> dates(
			@NotEmpty(message= "is required") @RequestParam(name="from", required = false) String from,
			@NotEmpty(message= "is required") @RequestParam(name="to", required = false) String to,
			@NotEmpty(message= "is required") @RequestParam(name="guests", required = false) String guests)
					throws AvailabilityException, BookingException, InputFormatException {

		dailyAvailabilityService.validateAvailability(from, to, guests, false);
		String response = String.format(
				"There is availability for the selected dates - From: %s, To: %s and %s guest.", from, to, guests);

		return new ResponseEntity<String>(response, HttpStatus.OK);
	}

}
