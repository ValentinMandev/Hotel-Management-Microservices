package com.hotel.bookingservice.repository;

import com.hotel.bookingservice.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, Long> {
}
