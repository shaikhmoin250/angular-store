package com.example.movieticketing.service;

import com.example.movieticketing.entity.Booking;
import com.example.movieticketing.entity.Movie;
import com.example.movieticketing.entity.Screen;
import com.example.movieticketing.entity.Showtime;
import com.example.movieticketing.repository.BookingRepository;
import com.example.movieticketing.repository.MovieRepository;
import com.example.movieticketing.repository.ScreenRepository;
import com.example.movieticketing.repository.ShowtimeRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class ShowtimeServiceImpl implements ShowtimeService {

    private final ShowtimeRepository showtimeRepository;
    private final MovieRepository movieRepository;
    private final ScreenRepository screenRepository;
    private final BookingRepository bookingRepository; // For checkSeatAvailability

    @Autowired
    public ShowtimeServiceImpl(ShowtimeRepository showtimeRepository,
                               MovieRepository movieRepository,
                               ScreenRepository screenRepository,
                               BookingRepository bookingRepository) {
        this.showtimeRepository = showtimeRepository;
        this.movieRepository = movieRepository;
        this.screenRepository = screenRepository;
        this.bookingRepository = bookingRepository;
    }

    @Transactional
    @Override
    public Showtime addShowtime(Showtime showtime) {
        // Validate movie
        if (showtime.getMovie() == null || showtime.getMovie().getId() == null) {
            throw new IllegalArgumentException("Showtime must be associated with a Movie and Movie ID must be provided.");
        }
        Movie movie = movieRepository.findById(showtime.getMovie().getId())
                .orElseThrow(() -> new EntityNotFoundException("Movie not found with id: " + showtime.getMovie().getId()));
        showtime.setMovie(movie);

        // Validate screen
        if (showtime.getScreen() == null || showtime.getScreen().getId() == null) {
            throw new IllegalArgumentException("Showtime must be associated with a Screen and Screen ID must be provided.");
        }
        Screen screen = screenRepository.findById(showtime.getScreen().getId())
                .orElseThrow(() -> new EntityNotFoundException("Screen not found with id: " + showtime.getScreen().getId()));
        showtime.setScreen(screen);

        // Further validation (e.g., showtime clashes, within operational hours) can be added
        return showtimeRepository.save(showtime);
    }

    @Override
    public Optional<Showtime> getShowtimeById(Long id) {
        return showtimeRepository.findById(id);
    }

    @Override
    public List<Showtime> getShowtimesByMovieId(Long movieId) {
        if (!movieRepository.existsById(movieId)) {
            throw new EntityNotFoundException("Movie not found with id: " + movieId);
        }
        return showtimeRepository.findByMovieId(movieId);
    }

    @Override
    public List<Showtime> getShowtimesByScreenId(Long screenId) {
        if (!screenRepository.existsById(screenId)) {
            throw new EntityNotFoundException("Screen not found with id: " + screenId);
        }
        return showtimeRepository.findByScreenId(screenId);
    }

    @Override
    public List<Showtime> getShowtimesForMovieOnDate(Long movieId, LocalDate date) {
        if (!movieRepository.existsById(movieId)) {
            throw new EntityNotFoundException("Movie not found with id: " + movieId);
        }
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
        return showtimeRepository.findByMovieIdAndStartTimeBetween(movieId, startOfDay, endOfDay);
    }

    @Override
    public boolean checkSeatAvailability(Long showtimeId, int numberOfSeats) {
        Showtime showtime = showtimeRepository.findById(showtimeId)
                .orElseThrow(() -> new EntityNotFoundException("Showtime not found with id: " + showtimeId));

        List<Booking> bookingsForShowtime = bookingRepository.findByShowtimeId(showtimeId);
        int bookedSeats = bookingsForShowtime.stream()
                                .filter(b -> !"CANCELLED".equalsIgnoreCase(b.getStatus())) // Only count active bookings
                                .mapToInt(Booking::getNumberOfTickets)
                                .sum();

        Screen screen = showtime.getScreen(); // Assuming screen is eagerly fetched or accessible
        if (screen == null) { // Should not happen if data is consistent
             screen = screenRepository.findById(showtime.getScreen().getId())
                .orElseThrow(() -> new EntityNotFoundException("Screen not found for showtime id: " + showtimeId));
        }


        return screen.getCapacity() >= (bookedSeats + numberOfSeats);
    }
}
