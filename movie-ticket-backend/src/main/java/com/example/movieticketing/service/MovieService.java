package com.example.movieticketing.service;

import com.example.movieticketing.entity.Movie;
import java.util.List;
import java.util.Optional;

public interface MovieService {
    Movie addMovie(Movie movie);
    List<Movie> getAllMovies();
    Optional<Movie> getMovieById(Long id);
    List<Movie> findMoviesByGenre(String genre);
    List<Movie> searchMoviesByTitle(String title);
    void deleteMovie(Long id);
    Movie updateMovie(Long id, Movie movieDetails);
}
