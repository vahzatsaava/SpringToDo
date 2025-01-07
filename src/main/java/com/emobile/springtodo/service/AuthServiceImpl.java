package com.emobile.springtodo.service;

import com.emobile.springtodo.entity.User;
import com.emobile.springtodo.dto.AuthRequest;
import com.emobile.springtodo.dto.AuthResponse;
import com.emobile.springtodo.dto.UserRegistrationRequest;
import com.emobile.springtodo.exception.UserAuthException;
import com.emobile.springtodo.repository.UserRepository;
import com.emobile.springtodo.security.CustomUserDetails;
import com.emobile.springtodo.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public AuthResponse register(UserRegistrationRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new UserAuthException("User already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("USER");
        userRepository.save(user);

        String token = jwtUtil.generateToken(new CustomUserDetails(user.getId(),
                user.getUsername(), user.getPassword(), new ArrayList<>())
        );
        return new AuthResponse(token);
    }

    @Override
    public AuthResponse login(AuthRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UserAuthException("Invalid credentials");
        }

        String token = jwtUtil.generateToken(new CustomUserDetails(user.getId(),
                user.getUsername(), user.getPassword(), new ArrayList<>())
        );
        return new AuthResponse(token);
    }

    @Override
    public AuthResponse refreshAccessToken(String refreshToken) {
        String username = jwtUtil.extractUsername(refreshToken);
        log.error(username);
        if (username == null || jwtUtil.isTokenExpired(refreshToken)) {
            throw new UserAuthException("Invalid refresh token");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        UserDetails userDetails = new CustomUserDetails(user.getId(),
                user.getUsername(), user.getPassword(), new ArrayList<>()
        );

        String newAccessToken = jwtUtil.generateToken(userDetails);

        return new AuthResponse(newAccessToken);
    }


}
