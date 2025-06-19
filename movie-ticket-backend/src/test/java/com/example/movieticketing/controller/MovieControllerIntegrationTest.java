package com.example.movieticketing.controller;

import com.example.movieticketing.dto.LoginRequest;
import com.example.movieticketing.dto.UserRegistrationRequest;
import com.example.movieticketing.entity.Movie;
import com.example.movieticketing.repository.MovieRepository;
import com.example.movieticketing.repository.UserRepository;
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

import java.time.LocalDate;
import java.util.Collections;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertTrue;


@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class MovieControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private UserRepository userRepository; // For creating users to login

    @Autowired
    private PasswordEncoder passwordEncoder; // For creating users

    private String jwtToken;
    private Movie movie1;

    @BeforeEach
    void setUp() throws Exception {
        movieRepository.deleteAll();
        userRepository.deleteAll(); // Clean user repo as well

        // Setup a user and login to get a token for secured endpoints
        UserRegistrationRequest userRequest = new UserRegistrationRequest("movieTestUser", "password", "movie@test.com");
        com.example.movieticketing.entity.User user = new com.example.movieticketing.entity.User();
        user.setUsername(userRequest.getUsername());
        user.setEmail(userRequest.getEmail());
        user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        user.setRoles("ROLE_USER,ROLE_ADMIN"); // Give admin role for posting movie if needed
        userRepository.save(user);

        LoginRequest loginRequest = new LoginRequest(userRequest.getUsername(), userRequest.getPassword());
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();
        String loginResponse = loginResult.getResponse().getContentAsString();
        jwtToken = objectMapper.readTree(loginResponse).get("token").asText();


        // Pre-populate some movie data
        movie1 = new Movie(null, "Inception", "Mind-bending thriller", 148, LocalDate.of(2010, 7, 16), "Sci-Fi", "url1", Collections.emptySet());
        movieRepository.save(movie1);
    }

    @Test
    void getAllMovies_shouldReturnMovieList() throws Exception {
        mockMvc.perform(get("/api/movies")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Inception")));
    }

    @Test
    void getMovieById_whenMovieExists_shouldReturnMovie() throws Exception {
        mockMvc.perform(get("/api/movies/" + movie1.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Inception")));
    }

    @Test
    void getMovieById_whenMovieNotExists_shouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/movies/" + 9999L) // Non-existent ID
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }


    @Test
    void addMovie_whenAuthenticated_shouldCreateMovie() throws Exception {
        Movie newMovie = new Movie(null, "Interstellar", "Space epic", 169, LocalDate.of(2014, 11, 7), "Sci-Fi", "url2", Collections.emptySet());

        mockMvc.perform(post("/api/movies")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newMovie)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title", is("Interstellar")));

        assertTrue(movieRepository.findByTitleContainingIgnoreCase("Interstellar").size() > 0);
    }

    @Test
    void addMovie_whenNotAuthenticated_shouldReturnUnauthorized() throws Exception {
        Movie newMovie = new Movie(null, "Interstellar", "Space epic", 169, LocalDate.of(2014, 11, 7), "Sci-Fi", "url2", Collections.emptySet());

        mockMvc.perform(post("/api/movies")
                // No Authorization header
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newMovie)))
                .andExpect(status().isForbidden()); // Or isUnauthorized() depending on exact filter chain config for missing token
    }
}
