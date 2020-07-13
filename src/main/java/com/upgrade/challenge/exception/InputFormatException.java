package com.upgrade.challenge.exception;

public class InputFormatException extends Exception {

	private static final long serialVersionUID = 1L;

	public InputFormatException(String message, Throwable cause) {
		super(message, cause);
	}

	public InputFormatException(String message) {
		super(message);
	}

}
