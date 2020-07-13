package com.upgrade.challenge.model;

import javax.validation.constraints.Email;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookingRequest {
	
	@NotNull(message = "fromDay is required")
	private String fromDay;
	
	@NotNull(message = "toDay is required")
	private String toDay;
	
    @NotNull(message = "guests is required")
    @Min(value=1, message= "guests should be a positive number")
	private String guests;

    @NotNull(message = "firstName is required")
	private String firstName;
	
    @NotNull(message = "lastName is required")
	private String lastName;
	
    @Email(message = "invalid email address")
    @NotNull(message = "email is required")
	private String email;
	
	public BookingRequest() {
	}

}
