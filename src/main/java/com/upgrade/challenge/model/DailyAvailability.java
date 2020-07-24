package com.upgrade.challenge.model;

import java.io.Serializable;
import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DailyAvailability implements Serializable {

	private static final long serialVersionUID = 7435393245143608638L;

	private LocalDate date;
	
	private Integer availability;
	
	public DailyAvailability() {
	}

	public DailyAvailability(LocalDate date, Integer availability) {
		this.date = date;
		this.availability = availability;
	}
	
}
