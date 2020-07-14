package com.upgrade.challenge.model;

import lombok.Getter;

@Getter
public class DailyAvailability {

	private String date;
	
	private Integer availability;

	public DailyAvailability(String date, Integer availability) {
		this.date = date;
		this.availability = availability;
	}
	
}
