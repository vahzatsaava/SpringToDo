package com.emobile.springtodo.repository;

import com.emobile.springtodo.entity.User;

import java.util.Optional;

public interface UserRepository {
    String INSERT_USER = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";
    String SELECT_BY_USERNAME = "SELECT * FROM users WHERE username = ?";

    void save(User user);
    Optional<User> findByUsername(String username);
}
