package com.upgrade.challenge.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Booking implements Serializable {
	
	private static final long serialVersionUID = 1516769826790180416L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;
	
	@Column
	private String fromDay;
	
	@Column
	private String toDay;
	
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
		this.guests = Integer.valueOf(bookingRequest.getGuests());
		this.firstName = bookingRequest.getFirstName();
		this.lastName = bookingRequest.getLastName();
		this.email = bookingRequest.getEmail();
	}

}
