package com.emobile.springtodo.repository;

import com.emobile.springtodo.entity.User;

import java.util.Optional;

public interface UserRepository {
    String SELECT_BY_USERNAME = "FROM User WHERE username = :username";

    void save(User user);
    Optional<User> findByUsername(String username);
}
