package ru.volunteer.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import ru.volunteer.model.entity.Role;
import ru.volunteer.model.entity.User;
import ru.volunteer.service.security.JwtService;
import ru.volunteer.testutil.TestDataFactory;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @InjectMocks
    private JwtService jwtService;

    private User testUser;
    private User adminUser;

    @BeforeEach
    void setUp() {
        testUser = TestDataFactory.createTestUser(1L, "testUser");
        ReflectionTestUtils.setField(jwtService, "secret", "mySuperSecretKeyForJwtTokenGeneration1234567890");
        ReflectionTestUtils.setField(jwtService, "expiration", 86400000L);
        adminUser = TestDataFactory.createTestUser(2L, "adminUser");
        Role adminRole = new Role();
        adminRole.setName("ROLE_ADMIN");
        adminUser.setRoles(Set.of(adminRole));
    }

    @Test
    @DisplayName("generateToken - успешная генерация JWT токена для пользователя")
    void generateToken_shouldReturnValidToken() {
        String token = jwtService.generateToken(testUser);
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    @DisplayName("generateToken - генерация токена для пользователя с ролью ADMIN")
    void generateToken_shouldGenerateTokenForAdminUser() {
        String token = jwtService.generateToken(adminUser);
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
    }

    @Test
    @DisplayName("getUsernameFromToken - извлечение логина из токена")
    void getUsernameFromToken_shouldReturnLogin() {
        String token = jwtService.generateToken(testUser);
        String username = jwtService.getUsernameFromToken(token);
        assertThat(username).isEqualTo(testUser.getLogin());
    }

    @Test
    @DisplayName("getRolesFromToken - извлечение ролей из токена")
    void getRolesFromToken_shouldReturnRoles() {
        Role role = new Role();
        role.setName("ROLE_USER");
        testUser.setRoles(Set.of(role));
        String token = jwtService.generateToken(testUser);
        java.util.List<String> roles = jwtService.getRolesFromToken(token);
        assertThat(roles).isNotNull();
        assertThat(roles).contains("ROLE_USER");
    }

    @Test
    @DisplayName("getRolesFromToken - извлечение нескольких ролей")
    void getRolesFromToken_shouldReturnMultipleRoles() {
        Role roleUser = new Role();
        roleUser.setName("ROLE_USER");
        Role roleAdmin = new Role();
        roleAdmin.setName("ROLE_ADMIN");
        testUser.setRoles(Set.of(roleUser, roleAdmin));
        String token = jwtService.generateToken(testUser);
        java.util.List<String> roles = jwtService.getRolesFromToken(token);
        assertThat(roles).isNotNull();
        assertThat(roles).hasSize(2);
        assertThat(roles).contains("ROLE_USER", "ROLE_ADMIN");
    }

    @Test
    @DisplayName("validateToken - валидный токен возвращает true")
    void validateToken_withValidToken_shouldReturnTrue() {
        String token = jwtService.generateToken(testUser);
        boolean isValid = jwtService.validateToken(token);
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("validateToken - невалидный токен возвращает false")
    void validateToken_withInvalidToken_shouldReturnFalse() {
        String invalidToken = "invalid.token.here";
        boolean isValid = jwtService.validateToken(invalidToken);
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("validateToken - пустой токен возвращает false")
    void validateToken_withEmptyToken_shouldReturnFalse() {
        String emptyToken = "";
        boolean isValid = jwtService.validateToken(emptyToken);
        assertThat(isValid).isFalse();
    }
}