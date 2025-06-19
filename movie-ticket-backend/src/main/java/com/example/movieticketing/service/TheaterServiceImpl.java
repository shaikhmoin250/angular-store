package com.example.movieticketing.service;

import com.example.movieticketing.entity.Screen;
import com.example.movieticketing.entity.Theater;
import com.example.movieticketing.repository.ScreenRepository;
import com.example.movieticketing.repository.TheaterRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class TheaterServiceImpl implements TheaterService {

    private final TheaterRepository theaterRepository;
    private final ScreenRepository screenRepository;

    @Autowired
    public TheaterServiceImpl(TheaterRepository theaterRepository, ScreenRepository screenRepository) {
        this.theaterRepository = theaterRepository;
        this.screenRepository = screenRepository;
    }

    @Transactional
    @Override
    public Theater addTheater(Theater theater) {
        // Screens within the theater if provided in the Theater object will be cascaded if CascadeType.ALL is set.
        // Or, screens can be added separately via ScreenService.
        // For now, assume basic theater properties are set.
        return theaterRepository.save(theater);
    }

    @Override
    public List<Theater> getAllTheaters() {
        return theaterRepository.findAll();
    }

    @Override
    public Optional<Theater> getTheaterById(Long id) {
        return theaterRepository.findById(id);
    }

    @Override
    public List<Screen> getScreensByTheaterId(Long theaterId) {
        if (!theaterRepository.existsById(theaterId)) {
            // Or return empty list, depending on desired behavior
            throw new EntityNotFoundException("Theater not found with id: " + theaterId);
        }
        return screenRepository.findByTheaterId(theaterId);
    }

    @Transactional
    @Override
    public Theater updateTheater(Long id, Theater theaterDetails) {
        Theater existingTheater = theaterRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Theater not found with id: " + id));

        existingTheater.setName(theaterDetails.getName());
        existingTheater.setLocation(theaterDetails.getLocation());
        // Managing screens (add/remove) would typically be a more complex operation,
        // possibly handled by ScreenService or dedicated methods here.
        // For this basic update, we're only updating Theater's own properties.
        // If theaterDetails.getScreens() is populated, JPA might try to merge,
        // which requires careful handling of detached entities and collections.
        // Simplest is to not touch existingTheater.getScreens() here unless specific logic is added.

        return theaterRepository.save(existingTheater);
    }

    @Transactional
    @Override
    public void deleteTheater(Long id) {
        Theater theater = theaterRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Theater not found with id: " + id));
        // Due to CascadeType.ALL on Theater.screens, associated screens should be deleted too.
        // If there are Showtimes or Bookings linked to these screens,
        // further logic might be needed (e.g., prevent deletion or clean up).
        // For now, rely on cascade for screens.
        theaterRepository.delete(theater);
    }
}
