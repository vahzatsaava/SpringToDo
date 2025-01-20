package com.emobile.springtodo.repository;

import com.emobile.springtodo.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {
    private final JdbcTemplate jdbcTemplate;


    @Override
    public void save(User user) {
        jdbcTemplate.update(INSERT_USER, user.getUsername(), user.getPassword(), user.getRole());
    }

    @Override
    public Optional<User> findByUsername(String username) {

        return jdbcTemplate.query(SELECT_BY_USERNAME, rs -> {
            if (rs.next()) {
                User user = new User();
                user.setId(rs.getLong("id"));
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));
                user.setRole(rs.getString("role"));
                return Optional.of(user);
            }
            return Optional.empty();
        }, username);
    }

}
