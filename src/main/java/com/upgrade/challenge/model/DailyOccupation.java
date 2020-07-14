package com.upgrade.challenge.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class DailyOccupation {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@JsonIgnore
	private Integer id;
	
	@Column(unique = true)
	private String date;
	
	@Column
	@JsonProperty("availability")
	private Integer guests;
	
	public DailyOccupation() {
	}
	
	public DailyOccupation(String date, Integer guests) {
		this.date = date;
		this.guests = guests;
	}
	
	public void addBooking(Integer guests) {
		this.guests += guests;
	}

	public void cancelBooking(Integer canceledGuests) {
		this.guests -= canceledGuests;
	}
	
}
