package com.example.movieticketing.service;

import com.example.movieticketing.entity.Booking;
import com.example.movieticketing.entity.Showtime;
import com.example.movieticketing.entity.User;
import com.example.movieticketing.entity.Screen; // Required for Showtime's screen capacity
import com.example.movieticketing.repository.BookingRepository;
import com.example.movieticketing.repository.ShowtimeRepository;
import com.example.movieticketing.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ShowtimeRepository showtimeRepository;

    @Mock
    private ShowtimeService showtimeService; // Mocking the service, not its impl

    @InjectMocks
    private BookingServiceImpl bookingService;

    private User user;
    private Screen screen;
    private Showtime showtime;
    private Booking bookingRequest;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testUser");

        screen = new Screen(); // Basic screen for capacity check via ShowtimeService
        screen.setId(1L);
        screen.setCapacity(100);


        showtime = new Showtime();
        showtime.setId(1L);
        showtime.setPrice(BigDecimal.valueOf(10.00));
        showtime.setStartTime(LocalDateTime.now().plusHours(2));
        showtime.setScreen(screen); // Associate screen with showtime

        bookingRequest = new Booking();
        bookingRequest.setUser(user); // User object with ID
        bookingRequest.setShowtime(showtime); // Showtime object with ID
        bookingRequest.setNumberOfTickets(2);
        bookingRequest.setSeatsBooked("A1,A2");
    }

    @Test
    void createBooking_whenValid_shouldSaveAndReturnBooking() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(showtimeRepository.findById(1L)).thenReturn(Optional.of(showtime));
        when(showtimeService.checkSeatAvailability(1L, 2)).thenReturn(true);
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking savedBooking = invocation.getArgument(0);
            savedBooking.setId(100L); // Simulate saving and getting an ID
            return savedBooking;
        });

        Booking createdBooking = bookingService.createBooking(bookingRequest);

        assertNotNull(createdBooking);
        assertEquals(100L, createdBooking.getId());
        assertEquals(user, createdBooking.getUser());
        assertEquals(showtime, createdBooking.getShowtime());
        assertEquals(2, createdBooking.getNumberOfTickets());
        assertEquals("A1,A2", createdBooking.getSeatsBooked());
        assertEquals("CONFIRMED", createdBooking.getStatus());
        // Price: 10.00 * 2 = 20.00
        assertEquals(0, BigDecimal.valueOf(20.00).compareTo(createdBooking.getTotalPrice()));
        assertNotNull(createdBooking.getBookingTime()); // Should be set by service or @CreationTimestamp

        verify(userRepository, times(1)).findById(1L);
        verify(showtimeRepository, times(1)).findById(1L);
        verify(showtimeService, times(1)).checkSeatAvailability(1L, 2);
        verify(bookingRepository, times(1)).save(any(Booking.class));
    }

    @Test
    void createBooking_whenUserNotFound_shouldThrowEntityNotFoundException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            bookingService.createBooking(bookingRequest);
        });
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void createBooking_whenShowtimeNotFound_shouldThrowEntityNotFoundException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(showtimeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            bookingService.createBooking(bookingRequest);
        });
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void createBooking_whenSeatsNotAvailable_shouldThrowIllegalStateException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(showtimeRepository.findById(1L)).thenReturn(Optional.of(showtime));
        when(showtimeService.checkSeatAvailability(1L, 2)).thenReturn(false);

        assertThrows(IllegalStateException.class, () -> {
            bookingService.createBooking(bookingRequest);
        });
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void cancelBooking_whenBookingExists_shouldUpdateStatusAndReturnBooking() {
        Booking existingBooking = new Booking();
        existingBooking.setId(1L);
        existingBooking.setStatus("CONFIRMED");

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(existingBooking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Booking cancelledBooking = bookingService.cancelBooking(1L);

        assertNotNull(cancelledBooking);
        assertEquals("CANCELLED", cancelledBooking.getStatus());
        verify(bookingRepository, times(1)).findById(1L);
        verify(bookingRepository, times(1)).save(existingBooking);
    }

    @Test
    void cancelBooking_whenBookingNotFound_shouldThrowEntityNotFoundException() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            bookingService.cancelBooking(1L);
        });
        verify(bookingRepository, never()).save(any(Booking.class));
    }
}
