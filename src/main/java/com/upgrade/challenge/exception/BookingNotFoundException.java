package com.upgrade.challenge.exception;

public class BookingNotFoundException extends Exception {

	private static final long serialVersionUID = 1L;

	private static final String MESSAGE = "Booking not found for ID: %s";

	public BookingNotFoundException(String id, Throwable cause) {
		super(String.format(MESSAGE, id), cause);
	}

	public BookingNotFoundException(String id) {
		super(String.format(MESSAGE, id));
	}
	
}
