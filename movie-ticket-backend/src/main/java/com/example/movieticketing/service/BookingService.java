package com.example.movieticketing.service;

import com.example.movieticketing.entity.Booking;
import java.util.List;
import java.util.Optional;

public interface BookingService {
    Booking createBooking(Booking booking); // Booking DTO might be better here in future
    Optional<Booking> getBookingById(Long id);
    List<Booking> getBookingsByUserId(Long userId);
    Booking cancelBooking(Long bookingId);
}
