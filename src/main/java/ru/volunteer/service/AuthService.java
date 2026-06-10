package ru.volunteer.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.volunteer.exception.ResourceNotFoundException;
import ru.volunteer.exception.ValidationException;
import ru.volunteer.model.entity.Role;
import ru.volunteer.model.entity.User;
import ru.volunteer.model.entity.UserProfile;
import ru.volunteer.model.enums.UserStatus;
import ru.volunteer.repository.RoleJpaRepository;
import ru.volunteer.repository.UserJpaRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.Set;

@Service
public class AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private final UserJpaRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleJpaRepository roleRepository;

    public AuthService(UserJpaRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       RoleJpaRepository roleRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
    }

    @Transactional
    public User register(String login, String rawPassword, String email, String firstName, String secondName) {
        if (userRepository.findByLogin(login).isPresent()) {
            throw new ValidationException("Пользователь с таким логином уже существует");
        }

        if (userRepository.findByEmail(email).isPresent()) {
            throw new ValidationException("Пользователь с таким email уже существует");
        }

        String encodedPassword = passwordEncoder.encode(rawPassword);
        User user = new User();
        user.setLogin(login);
        user.setPassword(encodedPassword);
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setSecondName(secondName);
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new ResourceNotFoundException("Role ROLE_USER not found"));
        user.setRoles(Set.of(userRole));
        UserProfile profile = new UserProfile();
        profile.setRatingPoints(0);
        profile.setStatus(UserStatus.NO_PARTICIPATION);
        profile.setUser(user);
        user.setProfile(profile);
        User savedUser = userRepository.save(user);
        log.info("Пользователь {} зарегистрирован с id={}", savedUser.getLogin(), savedUser.getId());
        return savedUser;
    }

    @Transactional(readOnly = true)
    public User login(String login, String rawPassword) {
        log.debug("Попытка входа пользователя {}", login);
        User user = userRepository.findByLogin(login)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь с таким логином не найден"));

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            log.warn("Неверный пароль для пользователя {}", login);
            throw new ValidationException("Неверный логин или пароль");
        }

        log.info("Пользователь {} успешно вошел в аккаунт", login);
        return user;
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Пользователь не аутентифицирован");
        }

        String login = authentication.getName();
        return userRepository.findByLogin(login)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден: " + login));
    }
}