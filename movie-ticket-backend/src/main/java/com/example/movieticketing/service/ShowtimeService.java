package com.example.movieticketing.service;

import com.example.movieticketing.entity.Showtime;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ShowtimeService {
    Showtime addShowtime(Showtime showtime); // Showtime object will contain movie_id and screen_id
    Optional<Showtime> getShowtimeById(Long id);
    List<Showtime> getShowtimesByMovieId(Long movieId);
    List<Showtime> getShowtimesByScreenId(Long screenId);
    List<Showtime> getShowtimesForMovieOnDate(Long movieId, LocalDate date);
    boolean checkSeatAvailability(Long showtimeId, int numberOfSeats); // Simplified
}
