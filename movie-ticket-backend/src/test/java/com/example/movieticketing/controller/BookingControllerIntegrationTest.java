package com.example.movieticketing.controller;

import com.example.movieticketing.dto.BookingRequest;
import com.example.movieticketing.dto.LoginRequest;
import com.example.movieticketing.dto.UserRegistrationRequest;
import com.example.movieticketing.entity.*;
import com.example.movieticketing.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class BookingControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private MovieRepository movieRepository;
    @Autowired private TheaterRepository theaterRepository;
    @Autowired private ScreenRepository screenRepository;
    @Autowired private ShowtimeRepository showtimeRepository;
    @Autowired private BookingRepository bookingRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private String jwtToken;
    private User testUser;
    private Showtime testShowtime;

    @BeforeEach
    void setUp() throws Exception {
        // Clear all relevant repositories
        bookingRepository.deleteAll();
        showtimeRepository.deleteAll();
        screenRepository.deleteAll();
        theaterRepository.deleteAll();
        movieRepository.deleteAll();
        userRepository.deleteAll();

        // 1. Create User and Login
        UserRegistrationRequest userReq = new UserRegistrationRequest("booker", "password", "booker@example.com");
        testUser = new User();
        testUser.setUsername(userReq.getUsername());
        testUser.setEmail(userReq.getEmail());
        testUser.setPassword(passwordEncoder.encode(userReq.getPassword()));
        testUser.setRoles("ROLE_USER");
        userRepository.save(testUser);

        LoginRequest loginReq = new LoginRequest(testUser.getUsername(), userReq.getPassword());
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andReturn();
        jwtToken = objectMapper.readTree(loginResult.getResponse().getContentAsString()).get("token").asText();

        // 2. Create Movie
        Movie movie = new Movie(null, "Test Movie for Booking", "Desc", 120, LocalDate.now(), "Action", "poster.url", Collections.emptySet());
        movieRepository.save(movie);

        // 3. Create Theater
        Theater theater = new Theater(null, "Test Theater", "Location", Collections.emptySet());
        theaterRepository.save(theater);

        // 4. Create Screen
        Screen screen = new Screen(null, "Screen 1", 100, theater, Collections.emptySet());
        screenRepository.save(screen);

        // 5. Create Showtime
        testShowtime = new Showtime(null, movie, screen, LocalDateTime.now().plusDays(1).withHour(18).withMinute(0), LocalDateTime.now().plusDays(1).withHour(20).withMinute(0), BigDecimal.valueOf(12.50), Collections.emptySet());
        showtimeRepository.save(testShowtime);
    }

    @Test
    void createBooking_whenAuthenticatedAndSeatsAvailable_shouldCreateBooking() throws Exception {
        BookingRequest bookingRequest = new BookingRequest();
        bookingRequest.setUserId(testUser.getId());
        bookingRequest.setShowtimeId(testShowtime.getId());
        bookingRequest.setNumberOfTickets(2);
        bookingRequest.setSeatsBooked("C1,C2");

        mockMvc.perform(post("/api/bookings")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bookingRequest)))
                .andExpect(status().isCreated()) // Expecting 201 from BookingController
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.numberOfTickets").value(2))
                .andExpect(jsonPath("$.totalPrice").value(25.00)); // 12.50 * 2

        assertTrue(bookingRepository.findByUserId(testUser.getId()).stream()
                .anyMatch(b -> b.getShowtime().getId().equals(testShowtime.getId()) && b.getNumberOfTickets() == 2));
    }

    @Test
    void createBooking_whenNotAuthenticated_shouldReturnForbidden() throws Exception {
        BookingRequest bookingRequest = new BookingRequest();
        bookingRequest.setUserId(testUser.getId());
        bookingRequest.setShowtimeId(testShowtime.getId());
        bookingRequest.setNumberOfTickets(2);
        bookingRequest.setSeatsBooked("C1,C2");

        mockMvc.perform(post("/api/bookings")
                // No Authorization header
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bookingRequest)))
                .andExpect(status().isForbidden()); // Or isUnauthorized()
    }

    @Test
    void createBooking_whenShowtimeNotFound_shouldReturnNotFound() throws Exception {
         BookingRequest bookingRequest = new BookingRequest();
        bookingRequest.setUserId(testUser.getId());
        bookingRequest.setShowtimeId(9999L); // Non-existent showtime
        bookingRequest.setNumberOfTickets(2);
        bookingRequest.setSeatsBooked("D1,D2");

        mockMvc.perform(post("/api/bookings")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bookingRequest)))
                .andExpect(status().isNotFound());
    }

    // Add more tests: e.g., not enough seats (requires more control over ShowtimeService.checkSeatAvailability mock or complex setup)
    // For an integration test, this might involve creating many existing bookings first.
    // For now, focusing on happy path authenticated and basic error cases.
}
