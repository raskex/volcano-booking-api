package com.upgrade.challenge.controllers;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.upgrade.challenge.model.DailyAvailability;
import com.upgrade.challenge.services.DailyAvailabilityService;


@Validated
@RestController
@RequestMapping(path="/availability")
public class DailyAvailabilityController {

	@Autowired
	private DailyAvailabilityService dailyAvailabilityService;
	
	@GetMapping(path="/")
	public List<DailyAvailability> get(
			@RequestParam(name="from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
			@RequestParam(name="to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

		return dailyAvailabilityService.getAvailability(from, to);
	}

}
