package com.upgrade.challenge.repository;

import org.springframework.data.repository.CrudRepository;

import com.upgrade.challenge.model.Booking;

public interface BookingRepository extends CrudRepository<Booking, Integer> {

}
