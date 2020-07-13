package com.upgrade.challenge.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

import org.springframework.format.annotation.NumberFormat;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Booking {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;
	
	@NotNull(message = "fromDay is required")
	@Column
	private String fromDay;
	
	@NotNull(message = "toDay is required")
	@Column
	private String toDay;
	
    @NotNull(message = "guests is required")
    @NumberFormat
	@Column
	private Integer guests;

	@Column
    @NotNull(message = "firstName is required")
	private String firstName;
	
	@Column
    @NotNull(message = "lastName is required")
	private String lastName;
	
	@Column
    @Email(message = "invalid email address")
    @NotNull(message = "email is required")
	private String email;
	
	public Booking() {
	}

	public Booking(BookingRequest bookingRequest) {
		this.fromDay = bookingRequest.getFromDay();
		this.toDay = bookingRequest.getToDay();
		this.guests = Integer.valueOf(bookingRequest.getGuests());
		this.firstName = bookingRequest.getFirstName();
		this.lastName = bookingRequest.getLastName();
		this.email = bookingRequest.getEmail();
	}

}
