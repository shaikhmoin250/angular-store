package com.example.movieticketing.service;

import com.example.movieticketing.entity.Movie;
import com.example.movieticketing.repository.MovieRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class MovieServiceImpl implements MovieService {

    private final MovieRepository movieRepository;

    @Autowired
    public MovieServiceImpl(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    @Transactional
    @Override
    public Movie addMovie(Movie movie) {
        // Additional validation can be added here if needed (e.g., check for existing title)
        return movieRepository.save(movie);
    }

    @Override
    public List<Movie> getAllMovies() {
        return movieRepository.findAll();
    }

    @Override
    public Optional<Movie> getMovieById(Long id) {
        return movieRepository.findById(id);
    }

    @Override
    public List<Movie> findMoviesByGenre(String genre) {
        return movieRepository.findByGenre(genre);
    }

    @Override
    public List<Movie> searchMoviesByTitle(String title) {
        return movieRepository.findByTitleContainingIgnoreCase(title);
    }

    @Transactional
    @Override
    public void deleteMovie(Long id) {
        if (!movieRepository.existsById(id)) {
            throw new EntityNotFoundException("Movie not found with id: " + id);
        }
        movieRepository.deleteById(id);
    }

    @Transactional
    @Override
    public Movie updateMovie(Long id, Movie movieDetails) {
        Movie existingMovie = movieRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Movie not found with id: " + id));

        existingMovie.setTitle(movieDetails.getTitle());
        existingMovie.setDescription(movieDetails.getDescription());
        existingMovie.setDurationMinutes(movieDetails.getDurationMinutes());
        existingMovie.setReleaseDate(movieDetails.getReleaseDate());
        existingMovie.setGenre(movieDetails.getGenre());
        existingMovie.setPosterUrl(movieDetails.getPosterUrl());
        // Showtimes are managed via ShowtimeService, typically not updated directly here.

        return movieRepository.save(existingMovie);
    }
}
