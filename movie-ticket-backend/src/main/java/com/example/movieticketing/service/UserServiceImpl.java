package com.example.movieticketing.service;

import com.example.movieticketing.entity.User;
import com.example.movieticketing.repository.UserRepository;
import jakarta.persistence.EntityExistsException; // Using standard JPA exception
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder; // Assuming this will be available
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) { // Removed (required = false)
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    @Override
    public User registerUser(User user) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new EntityExistsException("Username already exists: " + user.getUsername());
        }
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new EntityExistsException("Email already exists: " + user.getEmail());
        }

        // PasswordEncoder is now guaranteed to be present
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Default roles if not provided? Or handle in controller/DTO layer.
        // For now, assume roles are set on the incoming user object if desired.
        return userRepository.save(user);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}
