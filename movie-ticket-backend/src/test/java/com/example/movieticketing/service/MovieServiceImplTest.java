package com.example.movieticketing.service;

import com.example.movieticketing.entity.Movie;
import com.example.movieticketing.repository.MovieRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MovieServiceImplTest {

    @Mock
    private MovieRepository movieRepository;

    @InjectMocks
    private MovieServiceImpl movieService;

    private Movie movie;

    @BeforeEach
    void setUp() {
        movie = new Movie();
        movie.setId(1L);
        movie.setTitle("Inception");
        movie.setDescription("A mind-bending thriller");
        movie.setDurationMinutes(148);
        movie.setReleaseDate(LocalDate.of(2010, 7, 16));
        movie.setGenre("Sci-Fi");
    }

    @Test
    void addMovie_shouldSaveAndReturnMovie() {
        when(movieRepository.save(any(Movie.class))).thenReturn(movie);

        Movie savedMovie = movieService.addMovie(movie);

        assertNotNull(savedMovie);
        assertEquals("Inception", savedMovie.getTitle());
        verify(movieRepository, times(1)).save(movie);
    }

    @Test
    void getMovieById_whenMovieExists_shouldReturnMovie() {
        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));

        Optional<Movie> foundMovieOptional = movieService.getMovieById(1L);

        assertTrue(foundMovieOptional.isPresent());
        assertEquals("Inception", foundMovieOptional.get().getTitle());
        verify(movieRepository, times(1)).findById(1L);
    }

    @Test
    void getMovieById_whenMovieDoesNotExist_shouldReturnEmptyOptional() {
        when(movieRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<Movie> foundMovieOptional = movieService.getMovieById(1L);

        assertFalse(foundMovieOptional.isPresent());
        verify(movieRepository, times(1)).findById(1L);
    }

    @Test
    void updateMovie_whenMovieExists_shouldUpdateAndReturnMovie() {
        Movie movieDetails = new Movie();
        movieDetails.setTitle("Inception Remastered");
        movieDetails.setDescription("Updated description");
        movieDetails.setDurationMinutes(150);
        // Assume other fields might be updated as well

        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
        when(movieRepository.save(any(Movie.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Movie updatedMovie = movieService.updateMovie(1L, movieDetails);

        assertNotNull(updatedMovie);
        assertEquals("Inception Remastered", updatedMovie.getTitle());
        assertEquals("Updated description", updatedMovie.getDescription());
        assertEquals(150, updatedMovie.getDurationMinutes());
        verify(movieRepository, times(1)).findById(1L);
        verify(movieRepository, times(1)).save(movie); // 'movie' is the instance that was updated
    }

    @Test
    void updateMovie_whenMovieDoesNotExist_shouldThrowEntityNotFoundException() {
        Movie movieDetails = new Movie(); // Details to update with
        when(movieRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            movieService.updateMovie(1L, movieDetails);
        });

        verify(movieRepository, times(1)).findById(1L);
        verify(movieRepository, never()).save(any(Movie.class));
    }

    @Test
    void deleteMovie_whenMovieExists_shouldDeleteMovie() {
        when(movieRepository.existsById(1L)).thenReturn(true);
        // doNothing().when(movieRepository).deleteById(1L); // For void methods

        movieService.deleteMovie(1L);

        verify(movieRepository, times(1)).existsById(1L);
        verify(movieRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteMovie_whenMovieDoesNotExist_shouldThrowEntityNotFoundException() {
        when(movieRepository.existsById(1L)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> {
            movieService.deleteMovie(1L);
        });

        verify(movieRepository, times(1)).existsById(1L);
        verify(movieRepository, never()).deleteById(1L);
    }
}
