package com.example.movieticketing.service;

import com.example.movieticketing.entity.Booking;
import com.example.movieticketing.entity.Showtime;
import com.example.movieticketing.entity.User;
import com.example.movieticketing.repository.BookingRepository;
import com.example.movieticketing.repository.ShowtimeRepository;
import com.example.movieticketing.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ShowtimeRepository showtimeRepository;
    private final ShowtimeService showtimeService; // For seat availability

    @Autowired
    public BookingServiceImpl(BookingRepository bookingRepository,
                              UserRepository userRepository,
                              ShowtimeRepository showtimeRepository,
                              ShowtimeService showtimeService) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.showtimeRepository = showtimeRepository;
        this.showtimeService = showtimeService;
    }

    @Transactional
    @Override
    public Booking createBooking(Booking booking) {
        // Validate User
        if (booking.getUser() == null || booking.getUser().getId() == null) {
            throw new IllegalArgumentException("Booking must be associated with a User and User ID must be provided.");
        }
        User user = userRepository.findById(booking.getUser().getId())
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + booking.getUser().getId()));
        booking.setUser(user);

        // Validate Showtime
        if (booking.getShowtime() == null || booking.getShowtime().getId() == null) {
            throw new IllegalArgumentException("Booking must be associated with a Showtime and Showtime ID must be provided.");
        }
        Showtime showtime = showtimeRepository.findById(booking.getShowtime().getId())
                .orElseThrow(() -> new EntityNotFoundException("Showtime not found with id: " + booking.getShowtime().getId()));
        booking.setShowtime(showtime);

        // Check seat availability
        if (!showtimeService.checkSeatAvailability(showtime.getId(), booking.getNumberOfTickets())) {
            throw new IllegalStateException("Not enough seats available for showtime id: " + showtime.getId());
        }

        // Calculate total price (simplified)
        if (showtime.getPrice() != null && booking.getNumberOfTickets() > 0) {
            booking.setTotalPrice(showtime.getPrice().multiply(BigDecimal.valueOf(booking.getNumberOfTickets())));
        } else {
            // Or throw an error if price is not set / invalid number of tickets
            booking.setTotalPrice(BigDecimal.ZERO);
        }

        // Set booking time (if not using @CreationTimestamp, though Booking entity uses it)
        if (booking.getBookingTime() == null) {
             booking.setBookingTime(LocalDateTime.now());
        }

        // Set initial status
        booking.setStatus("CONFIRMED"); // Or "PENDING_PAYMENT" etc.

        return bookingRepository.save(booking);
    }

    @Override
    public Optional<Booking> getBookingById(Long id) {
        return bookingRepository.findById(id);
    }

    @Override
    public List<Booking> getBookingsByUserId(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("User not found with id: " + userId);
        }
        return bookingRepository.findByUserId(userId);
    }

    @Transactional
    @Override
    public Booking cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found with id: " + bookingId));

        // Logic for refund, seat release etc. would go here
        // For now, just update status
        booking.setStatus("CANCELLED");
        return bookingRepository.save(booking);
    }
}
