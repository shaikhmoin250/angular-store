package com.example.movieticketing.controller;

import com.example.movieticketing.dto.JwtResponse;
import com.example.movieticketing.dto.LoginRequest;
import com.example.movieticketing.dto.UserRegistrationRequest;
import com.example.movieticketing.entity.User;
import com.example.movieticketing.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional // Rollback transactions after each test
public class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll(); // Clean slate for each test
    }

    @Test
    void registerUser_whenValidRequest_shouldReturnSuccess() throws Exception {
        UserRegistrationRequest registrationRequest = new UserRegistrationRequest(
                "testuser", "password123", "test@example.com");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().string("User registered successfully: testuser"));

        assertTrue(userRepository.findByUsername("testuser").isPresent());
    }

    @Test
    void registerUser_whenUsernameExists_shouldReturnConflict() throws Exception {
        // Pre-register a user
        User existingUser = new User();
        existingUser.setUsername("testuser");
        existingUser.setEmail("existing@example.com");
        existingUser.setPassword(passwordEncoder.encode("password123"));
        existingUser.setRoles("ROLE_USER");
        userRepository.save(existingUser);

        UserRegistrationRequest registrationRequest = new UserRegistrationRequest(
                "testuser", "newpassword", "new@example.com");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationRequest)))
                .andExpect(status().isConflict())
                .andExpect(content().string("Username already exists: testuser"));
    }

    @Test
    void loginUser_whenValidCredentials_shouldReturnJwt() throws Exception {
        // Register user first
        UserRegistrationRequest registrationRequest = new UserRegistrationRequest(
                "loginuser", "password123", "login@example.com");
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationRequest)))
                .andExpect(status().isCreated());

        LoginRequest loginRequest = new LoginRequest("loginuser", "password123");

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.username").value("loginuser"))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_USER"))
                .andReturn();

        String responseString = result.getResponse().getContentAsString();
        JwtResponse jwtResponse = objectMapper.readValue(responseString, JwtResponse.class);
        assertNotNull(jwtResponse.getToken());
        assertTrue(jwtResponse.getToken().length() > 0);
    }

    @Test
    void loginUser_whenInvalidPassword_shouldReturnUnauthorized() throws Exception {
         // Register user first
        UserRegistrationRequest registrationRequest = new UserRegistrationRequest(
                "loginuserfail", "password123", "loginfail@example.com");
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registrationRequest)))
                .andExpect(status().isCreated());

        LoginRequest loginRequest = new LoginRequest("loginuserfail", "wrongpassword");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid username or password"));
    }

    @Test
    void loginUser_whenUserNotFound_shouldReturnUnauthorized() throws Exception {
        LoginRequest loginRequest = new LoginRequest("nonexistentuser", "password123");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                 .andExpect(content().string("Invalid username or password")); // UserDetailsService throws UsernameNotFoundException which results in BadCredentials
    }
}
