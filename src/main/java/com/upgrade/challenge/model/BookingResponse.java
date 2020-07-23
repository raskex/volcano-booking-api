package com.upgrade.challenge.model;

import java.io.Serializable;
import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookingResponse implements Serializable {

	private static final long serialVersionUID = -6984345757381817804L;

	private Integer id;
	
	private LocalDate fromDay;
	
	private LocalDate toDay;
	
	private Integer guests;

	private String firstName;
	
	private String lastName;
	
	private String email;

	public BookingResponse() {
	}

	public BookingResponse(Integer id, LocalDate fromDay, LocalDate toDay, Integer guests, String firstName, String lastName, String email) {
		this.id = id;
		this.fromDay = fromDay;
		this.toDay = toDay;
		this.guests = guests;
		this.firstName = firstName;
		this.lastName = lastName;
		this.email = email;
	}
	
	public BookingResponse(Integer id, BookingRequest bookingRequest) {
		this.id = id;
		this.fromDay = bookingRequest.getFromDay();
		this.toDay = bookingRequest.getToDay();
		this.guests = Integer.valueOf(bookingRequest.getGuests());
		this.firstName = bookingRequest.getFirstName();
		this.lastName = bookingRequest.getLastName();
		this.email = bookingRequest.getEmail();
	}

}
