package com.example.movieticketing.service;

import com.example.movieticketing.entity.Screen;
import com.example.movieticketing.entity.Theater;
import com.example.movieticketing.repository.ScreenRepository;
import com.example.movieticketing.repository.TheaterRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ScreenServiceImpl implements ScreenService {

    private final ScreenRepository screenRepository;
    private final TheaterRepository theaterRepository;

    @Autowired
    public ScreenServiceImpl(ScreenRepository screenRepository, TheaterRepository theaterRepository) {
        this.screenRepository = screenRepository;
        this.theaterRepository = theaterRepository;
    }

    @Transactional
    @Override
    public Screen addScreen(Screen screen) {
        // Ensure the theater exists
        if (screen.getTheater() == null || screen.getTheater().getId() == null) {
            throw new IllegalArgumentException("Screen must be associated with a Theater and Theater ID must be provided.");
        }
        Long theaterId = screen.getTheater().getId();
        Theater theater = theaterRepository.findById(theaterId)
                .orElseThrow(() -> new EntityNotFoundException("Theater not found with id: " + theaterId + " when adding screen."));

        // Set the fetched theater to ensure it's a managed entity
        screen.setTheater(theater);

        // Additional validation can be added (e.g., screen number unique within a theater)
        return screenRepository.save(screen);
    }

    @Override
    public Optional<Screen> getScreenById(Long id) {
        return screenRepository.findById(id);
    }

    @Override
    public List<Screen> getScreensByTheater(Long theaterId) {
        if (!theaterRepository.existsById(theaterId)) {
            throw new EntityNotFoundException("Theater not found with id: " + theaterId);
        }
        return screenRepository.findByTheaterId(theaterId);
    }
}
