package com.upgrade.challenge.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.upgrade.challenge.model.dto.Booking;

@Repository
public interface BookingRepository extends CrudRepository<Booking, Integer> {

}
