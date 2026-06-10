package ru.volunteer.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.volunteer.model.dto.ProfileDto;
import ru.volunteer.model.dto.UserResponseDto;
import ru.volunteer.model.entity.User;
import ru.volunteer.service.AuthService;

@RestController
@RequestMapping("/api/v1")
public class UserController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    private final AuthService authService;
    public UserController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/profile")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<UserResponseDto> getCurrentProfile() {
        User currentUser = authService.getCurrentUser();
        log.debug("Получение профиля пользователя {}", currentUser.getLogin());
        UserResponseDto response = UserResponseDto.from(currentUser);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/profile")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<UserResponseDto> updateProfile(@RequestBody @Valid ProfileDto dto) {
        User user = authService.getCurrentUser();
        log.debug("Обновление профиля пользователя {}", user.getLogin());
        user.setFirstName(dto.getFirstName());
        user.setSecondName(dto.getSecondName());
        UserResponseDto response = UserResponseDto.from(user);
        return ResponseEntity.ok(response);
    }
}