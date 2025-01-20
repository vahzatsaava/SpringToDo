package com.emobile.springtodo.controller;

import com.emobile.springtodo.dto.AuthRequest;
import com.emobile.springtodo.dto.AuthResponse;
import com.emobile.springtodo.dto.RefreshTokenDto;
import com.emobile.springtodo.dto.UserRegistrationRequest;
import com.emobile.springtodo.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @Operation(summary = "Регистрация пользователя", description = "Создаёт нового пользователя в системе.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь успешно зарегистрирован"),
            @ApiResponse(responseCode = "400", description = "Некорректный запрос", content = @Content)
    })
    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody UserRegistrationRequest request) {
        return authService.register(request);
    }

    @Operation(summary = "Авторизация пользователя", description = "Авторизует пользователя и возвращает токены.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь успешно авторизован"),
            @ApiResponse(responseCode = "401", description = "Неверные учетные данные", content = @Content)
    })
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody AuthRequest request) {
        return authService.login(request);
    }

    @Operation(summary = "Обновление access токена", description = "Обновляет access токен с использованием refresh токена.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Access токен успешно обновлён"),
            @ApiResponse(responseCode = "401", description = "Недействительный refresh токен", content = @Content)
    })
    @PostMapping("/refresh")
    public AuthResponse refreshToken(@RequestBody RefreshTokenDto refreshToken) {
        return authService.refreshAccessToken(refreshToken.getRefreshToken());
    }
}
