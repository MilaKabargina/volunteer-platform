package ru.volunteer.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ru.volunteer.model.dto.AuthResponseDto;
import ru.volunteer.model.dto.LoginRequestDto;
import ru.volunteer.model.dto.UserRegistrationDto;
import ru.volunteer.model.dto.UserResponseDto;
import ru.volunteer.model.entity.User;
import ru.volunteer.repository.UserJpaRepository;
import ru.volunteer.service.AuthService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import ru.volunteer.service.security.JwtService;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private final AuthService authService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserJpaRepository userRepository;

    public AuthController(AuthService authService,
                          AuthenticationManager authenticationManager,
                          JwtService jwtService,
                          UserJpaRepository userRepository) {
        this.authService = authService;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> register(@RequestBody @Valid UserRegistrationDto dto) {
        log.debug("Регистрация пользователя {}", dto.getLogin());
        User user = authService.register(
                dto.getLogin(),
                dto.getPassword(),
                dto.getEmail(),
                dto.getFirstName(),
                dto.getSecondName()
        );

        UserResponseDto response = UserResponseDto.from(user);
        log.info("Пользователь {} зарегистрирован", dto.getLogin());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequestDto dto) {
        log.debug("Попытка входа пользователя {}", dto.getLogin());
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            dto.getLogin(),
                            dto.getPassword()
                    )
            );
        } catch (BadCredentialsException ex) {
            log.warn("Неверный логин или пароль для {}", dto.getLogin());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Неверный логин или пароль");
        }

        User user = userRepository.findByLogin(dto.getLogin())
                .orElseThrow(() -> new RuntimeException("User not found after authentication"));
        String token = jwtService.generateToken(user);
        UserResponseDto userResponse = UserResponseDto.from(user);
        AuthResponseDto authResponse = new AuthResponseDto(token, userResponse);
        log.info("Пользователь {} вошел в систему", user.getLogin());
        return ResponseEntity.ok(authResponse);
    }

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<UserResponseDto> getCurrentUser(Authentication authentication) {
        String login = authentication.getName();
        log.debug("GET /api/v1/auth/me - current user: {}", login);
        User user = userRepository.findByLogin(login)
                .orElseThrow(() -> new RuntimeException("User not found: " + login));
        UserResponseDto response = UserResponseDto.from(user);
        return ResponseEntity.ok(response);
    }
}