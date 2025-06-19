package com.example.movieticketing.service;

import com.example.movieticketing.entity.Screen;
import java.util.List;
import java.util.Optional;

public interface ScreenService {
    Screen addScreen(Screen screen); // Screen object will contain theater_id
    Optional<Screen> getScreenById(Long id);
    List<Screen> getScreensByTheater(Long theaterId);
}
