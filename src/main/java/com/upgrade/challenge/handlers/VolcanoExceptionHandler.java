package com.upgrade.challenge.handlers;

import java.sql.BatchUpdateException;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolationException;

import org.hibernate.dialect.lock.OptimisticEntityLockException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.upgrade.challenge.exception.AvailabilityException;
import com.upgrade.challenge.exception.BookingException;
import com.upgrade.challenge.exception.BookingNotFoundException;
import com.upgrade.challenge.exception.InputFormatException;

@ControllerAdvice
public class VolcanoExceptionHandler extends ResponseEntityExceptionHandler {

	@ExceptionHandler(NumberFormatException.class)
	public ResponseEntity<?> handleNumberFormatException(NumberFormatException e) {
		return ResponseEntity.badRequest().body("Errors: " + e.getMessage());
	}
	
	@ExceptionHandler(AvailabilityException.class)
	public ResponseEntity<?> handleAvailabilityException(AvailabilityException e) {
		return ResponseEntity.badRequest().body("Errors: " + e.getMessage());
	}
	
	@ExceptionHandler(BookingNotFoundException.class)
	public ResponseEntity<?> handleBookingNotFoundException(BookingNotFoundException e) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Errors: " + e.getMessage());
	}
	
	@ExceptionHandler(BookingException.class)
	public ResponseEntity<?> handleBookingException(BookingException e) {
		return ResponseEntity.badRequest().body("Errors: " + e.getMessage());
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<?> handleConstraintViolationException(ConstraintViolationException e) {
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Errors: " + e.getMessage());
	}
	
	@ExceptionHandler(BatchUpdateException.class)
	public ResponseEntity<?> handleBatchUpdateException(BatchUpdateException e) {
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Errors: " + e.getMessage());
	}
	
	@ExceptionHandler(OptimisticEntityLockException.class)
	public ResponseEntity<?> handleOptimisticEntityLockException(OptimisticEntityLockException e) {
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Errors: " + e.getMessage());
	}
	
	@ExceptionHandler(InputFormatException.class)
	public ResponseEntity<?> handleInputFormatException(InputFormatException e) {
		return ResponseEntity.badRequest().body("Errors: " + e.getMessage());
	}
	
	@ExceptionHandler(InvalidFormatException.class)
	public ResponseEntity<?> handleInvalidFormatException(InvalidFormatException e) {
		return ResponseEntity.badRequest().body("Errors: " + e.getMessage());
	}
	
	@ExceptionHandler(EmptyResultDataAccessException.class)
	public ResponseEntity<?> handleEmptyResultDataAccessException(EmptyResultDataAccessException e) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Errors: " + e.getMessage());
	}
	
	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<?> handleIllegalArgumentException(IllegalArgumentException e) {
		return ResponseEntity.badRequest().body("Errors: " + e.getMessage());
	}
	
	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
			HttpHeaders headers, HttpStatus status, WebRequest request) {
		final String message = ex.getBindingResult().getAllErrors().stream().map(ObjectError::getDefaultMessage)
				.collect(Collectors.joining(", "));
		return ResponseEntity.badRequest().body("Errors: " + message);
	}
	
}
