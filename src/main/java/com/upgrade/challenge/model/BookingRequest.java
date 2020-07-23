package com.upgrade.challenge.model;

import java.io.Serializable;
import java.time.LocalDate;

import javax.validation.constraints.Email;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookingRequest implements Serializable {
	
	private static final long serialVersionUID = -1373136681909845573L;

	@NotNull(message = "fromDay is required")
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	private LocalDate fromDay;
	
	@NotNull(message = "toDay is required")
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	private LocalDate toDay;
	
    @NotNull(message = "guests is required")
    @Min(value=1, message= "guests should be a positive number")
	private Integer guests;

    @NotNull(message = "firstName is required")
	private String firstName;
	
    @NotNull(message = "lastName is required")
	private String lastName;
	
    @Email(message = "invalid email address")
    @NotNull(message = "email is required")
	private String email;
	
	public BookingRequest() {}

}
