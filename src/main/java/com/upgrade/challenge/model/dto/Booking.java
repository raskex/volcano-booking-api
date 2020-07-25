package com.upgrade.challenge.model.dto;

import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.upgrade.challenge.model.BookingRequest;
import com.upgrade.challenge.model.BookingResponse;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Booking {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	@Column
	private LocalDate fromDay;
	
	@Column
	private LocalDate toDay;
	
	@Column
	private Integer guests;

	@Column
	private String firstName;
	
	@Column
	private String lastName;
	
	@Column
	private String email;
	
	public Booking() {}

	public Booking(BookingRequest bookingRequest) {
		this.fromDay = bookingRequest.getFromDay();
		this.toDay = bookingRequest.getToDay();
		this.guests = bookingRequest.getGuests();
		this.firstName = bookingRequest.getFirstName();
		this.lastName = bookingRequest.getLastName();
		this.email = bookingRequest.getEmail();
	}

	public Booking(BookingResponse bookingResponse) {
		this.id = bookingResponse.getId();
		this.fromDay = bookingResponse.getFromDay();
		this.toDay = bookingResponse.getToDay();
		this.guests = bookingResponse.getGuests();
		this.firstName = bookingResponse.getFirstName();
		this.lastName = bookingResponse.getLastName();
		this.email = bookingResponse.getEmail();
	}

}
