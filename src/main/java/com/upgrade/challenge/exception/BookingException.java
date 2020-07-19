package com.upgrade.challenge.exception;

public class BookingException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public BookingException(String message, Throwable cause) {
		super(message, cause);
	}

	public BookingException(String message) {
		super(message);
	}
	
}
