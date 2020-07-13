package com.upgrade.challenge.exception;

public class BookingException extends Exception {

	private static final long serialVersionUID = 1L;

	public BookingException(String message, Throwable cause) {
		super(message, cause);
	}

	public BookingException(String message) {
		super(message);
	}
	
}
