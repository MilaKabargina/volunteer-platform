package ru.volunteer.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import ru.volunteer.model.entity.Role;
import ru.volunteer.model.entity.User;
import ru.volunteer.repository.UserJpaRepository;
import ru.volunteer.service.security.CustomUserDetailsService;
import ru.volunteer.testutil.TestDataFactory;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserJpaRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = TestDataFactory.createTestUser(1L, "testUser");
        Role role = new Role();
        role.setName("ROLE_USER");
        testUser.setRoles(Set.of(role));
    }

    @Test
    @DisplayName("loadUserByUsername - успешная загрузка пользователя")
    void loadUserByUsername_shouldReturnUserDetails_whenUserExists() {
        when(userRepository.findByLogin("testUser")).thenReturn(Optional.of(testUser));
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("testUser");
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo("testUser");
        assertThat(userDetails.getAuthorities()).isNotEmpty();
        assertThat(userDetails.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_USER");
    }

    @Test
    @DisplayName("loadUserByUsername - исключение когда пользователь не найден")
    void loadUserByUsername_shouldThrowException_whenUserNotFound() {
        when(userRepository.findByLogin("nonExistentUser")).thenReturn(Optional.empty());
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class,
                () -> customUserDetailsService.loadUserByUsername("nonExistentUser"));
        assertThat(exception.getMessage()).isEqualTo("User not found: nonExistentUser");
    }

    @Test
    @DisplayName("loadUserByUsername - загрузка пользователя с несколькими ролями")
    void loadUserByUsername_shouldLoadUserWithMultipleRoles() {
        Role roleAdmin = new Role();
        roleAdmin.setName("ROLE_ADMIN");
        testUser.setRoles(Set.of(new Role("ROLE_USER"), roleAdmin));
        when(userRepository.findByLogin("testUser")).thenReturn(Optional.of(testUser));
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("testUser");
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getAuthorities()).hasSize(2);
    }
}