package com.example.movieticketing.repository;

import com.example.movieticketing.entity.Showtime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ShowtimeRepository extends JpaRepository<Showtime, Long> {

    List<Showtime> findByMovieId(Long movieId);

    List<Showtime> findByScreenId(Long screenId);

    List<Showtime> findByMovieIdAndStartTimeBetween(Long movieId, LocalDateTime startTime, LocalDateTime endTime);

    // Example of a more complex query: find showtimes for a specific movie in a specific theater after a certain time
    // This translates to showtime.screen.theater.id
    List<Showtime> findByScreenTheaterIdAndMovieIdAndStartTimeAfter(Long theaterId, Long movieId, LocalDateTime startTime);
}
