package com.example.movieticketing.service;

import com.example.movieticketing.entity.Screen;
import com.example.movieticketing.entity.Theater;
import java.util.List;
import java.util.Optional;

public interface TheaterService {
    Theater addTheater(Theater theater);
    List<Theater> getAllTheaters();
    Optional<Theater> getTheaterById(Long id);
    List<Screen> getScreensByTheaterId(Long theaterId); // From spec
    Theater updateTheater(Long id, Theater theaterDetails);
    void deleteTheater(Long id);
}
