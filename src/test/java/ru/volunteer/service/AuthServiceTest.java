package ru.volunteer.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.volunteer.exception.ResourceNotFoundException;
import ru.volunteer.exception.ValidationException;
import ru.volunteer.model.entity.Role;
import ru.volunteer.model.entity.User;
import ru.volunteer.repository.RoleJpaRepository;
import ru.volunteer.repository.UserJpaRepository;
import ru.volunteer.testutil.TestDataFactory;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    private UserJpaRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RoleJpaRepository roleRepository;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("register - успешная регистрация нового пользователя")
    void register_shouldCreateNewUser_whenValidData() {
        String login = "newUser";
        String password = "password123";
        String email = "newuser@test.com";
        String firstName = "Новый";
        String secondName = "Пользователь";
        Role userRole = new Role();
        userRole.setId(1L);
        userRole.setName("ROLE_USER");
        when(userRepository.findByLogin(login)).thenReturn(Optional.empty());
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(password)).thenReturn("encodedPassword");
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(userRole));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        User result = authService.register(login, password, email, firstName, secondName);
        assertThat(result).isNotNull();
        assertThat(result.getLogin()).isEqualTo(login);
        assertThat(result.getEmail()).isEqualTo(email);
        assertThat(result.getFirstName()).isEqualTo(firstName);
        assertThat(result.getSecondName()).isEqualTo(secondName);
        assertThat(result.getProfile()).isNotNull();
        assertThat(result.getProfile().getRatingPoints()).isZero();
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("login - успешный вход с валидными данными")
    void login_shouldReturnUser_whenValidCredentials() {
        String login = "testUser";
        String rawPassword = "password123";
        User existingUser = TestDataFactory.createTestUser(1L, login);
        existingUser.setPassword("encodedPassword");
        when(userRepository.findByLogin(login)).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches(rawPassword, existingUser.getPassword())).thenReturn(true);
        User result = authService.login(login, rawPassword);
        assertThat(result).isNotNull();
        assertThat(result.getLogin()).isEqualTo(login);
        verify(userRepository, times(1)).findByLogin(login);
    }

    @Test
    @DisplayName("register - исключение при дубликате логина")
    void register_shouldThrowException_whenLoginAlreadyExists() {
        String login = "existingUser";
        User existingUser = TestDataFactory.createTestUser(1L, login);
        when(userRepository.findByLogin(login)).thenReturn(Optional.of(existingUser));
        ValidationException exception = assertThrows(ValidationException.class,
                () -> authService.register(login, "pass", "email@test.com", "Имя", "Фамилия"));
        assertThat(exception.getMessage()).isEqualTo("Пользователь с таким логином уже существует");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("register - исключение при дубликате email")
    void register_shouldThrowException_whenEmailAlreadyExists() {
        String login = "newUser";
        String email = "existing@test.com";
        when(userRepository.findByLogin(login)).thenReturn(Optional.empty());
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(TestDataFactory.createTestUser(2L, "otherUser")));
        ValidationException exception = assertThrows(ValidationException.class,
                () -> authService.register(login, "pass", email, "Имя", "Фамилия"));
        assertThat(exception.getMessage()).isEqualTo("Пользователь с таким email уже существует");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("login - исключение при неверном пароле")
    void login_shouldThrowException_whenInvalidPassword() {
        String login = "testUser";
        String wrongPassword = "wrongPassword";
        User existingUser = TestDataFactory.createTestUser(1L, login);
        existingUser.setPassword("encodedPassword");
        when(userRepository.findByLogin(login)).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.matches(wrongPassword, existingUser.getPassword())).thenReturn(false);
        ValidationException exception = assertThrows(ValidationException.class,
                () -> authService.login(login, wrongPassword));
        assertThat(exception.getMessage()).isEqualTo("Неверный логин или пароль");
    }

    @Test
    @DisplayName("login - исключение если пользователь не найден")
    void login_shouldThrowException_whenUserNotFound() {
        String login = "nonExistentUser";
        when(userRepository.findByLogin(login)).thenReturn(Optional.empty());
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> authService.login(login, "pass"));
        assertThat(exception.getMessage()).isEqualTo("Пользователь с таким логином не найден");
    }

    @Test
    @DisplayName("getCurrentUser - успешное получение текущего пользователя")
    void getCurrentUser_shouldReturnCurrentUser_whenAuthenticated() {
        String login = "testUser";
        User currentUser = TestDataFactory.createTestUser(1L, login);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(login);
        when(userRepository.findByLogin(login)).thenReturn(Optional.of(currentUser));
        SecurityContextHolder.setContext(securityContext);
        User result = authService.getCurrentUser();
        assertThat(result).isNotNull();
        assertThat(result.getLogin()).isEqualTo(login);
        verify(userRepository, times(1)).findByLogin(login);
    }

    @Test
    @DisplayName("getCurrentUser - исключение когда пользователь не аутентифицирован")
    void getCurrentUser_shouldThrowException_whenNotAuthenticated() {
        when(securityContext.getAuthentication()).thenReturn(null);
        SecurityContextHolder.setContext(securityContext);
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authService.getCurrentUser());
        assertThat(exception.getMessage()).isEqualTo("Пользователь не аутентифицирован");
    }

    @Test
    @DisplayName("getCurrentUser - исключение когда пользователь не найден в БД")
    void getCurrentUser_shouldThrowException_whenUserNotFoundInDb() {
        String login = "testUser";
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn(login);
        when(userRepository.findByLogin(login)).thenReturn(Optional.empty());
        SecurityContextHolder.setContext(securityContext);
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authService.getCurrentUser());
        assertThat(exception.getMessage()).isEqualTo("Пользователь не найден: " + login);
    }

    @Test
    @DisplayName("register - сохранение пользователя с правильными данными (ArgumentCaptor)")
    void register_shouldSaveUserWithCorrectData_usingArgumentCaptor() {
        String login = "captorUser";
        String password = "pass123";
        String email = "captor@test.com";
        String firstName = "Имя";
        String secondName = "Фамилия";
        Role userRole = new Role();
        userRole.setId(1L);
        userRole.setName("ROLE_USER");
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        when(userRepository.findByLogin(login)).thenReturn(Optional.empty());
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(password)).thenReturn("encodedPass");
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(userRole));
        when(userRepository.save(userCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));
        authService.register(login, password, email, firstName, secondName);
        User capturedUser = userCaptor.getValue();
        assertThat(capturedUser.getLogin()).isEqualTo(login);
        assertThat(capturedUser.getEmail()).isEqualTo(email);
        assertThat(capturedUser.getFirstName()).isEqualTo(firstName);
        assertThat(capturedUser.getSecondName()).isEqualTo(secondName);
        assertThat(capturedUser.getProfile()).isNotNull();
    }
}