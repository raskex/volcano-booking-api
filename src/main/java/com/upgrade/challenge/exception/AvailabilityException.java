package com.upgrade.challenge.exception;

public class AvailabilityException extends Exception {

	private static final long serialVersionUID = 1L;

	public AvailabilityException(String message, Throwable cause) {
		super(message, cause);
	}

	public AvailabilityException(String message) {
		super(message);
	}

}
