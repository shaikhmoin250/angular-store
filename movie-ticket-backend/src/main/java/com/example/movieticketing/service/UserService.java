package com.example.movieticketing.service;

import com.example.movieticketing.entity.User;
import java.util.Optional;

public interface UserService {
    User registerUser(User user);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email); // Added as per repository, good to expose
}
