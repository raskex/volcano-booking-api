package com.upgrade.challenge.exception;

public class BookingNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private static final String MESSAGE = "Booking not found for ID: %d";

	public BookingNotFoundException(Long id, Throwable cause) {
		super(String.format(MESSAGE, id), cause);
	}

	public BookingNotFoundException(Long id) {
		super(String.format(MESSAGE, id));
	}
	
}
