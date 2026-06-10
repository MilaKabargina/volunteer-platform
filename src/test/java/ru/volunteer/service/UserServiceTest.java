package ru.volunteer.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.volunteer.model.entity.User;
import ru.volunteer.model.entity.UserProfile;
import ru.volunteer.model.enums.UserStatus;
import ru.volunteer.repository.UserJpaRepository;
import ru.volunteer.testutil.TestDataFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserJpaRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = TestDataFactory.createTestUser(1L, "testUser");
        // Убедимся, что профиль существует
        if (testUser.getProfile() == null) {
            UserProfile profile = new UserProfile();
            profile.setRatingPoints(0);
            profile.setStatus(UserStatus.NO_PARTICIPATION);
            profile.setUser(testUser);
            testUser.setProfile(profile);
        }
    }

    @Test
    @DisplayName("increaseRating - увеличение рейтинга на 1 и обновление статуса")
    void increaseRating_shouldIncreaseRatingAndUpdateStatus() {
        UserProfile profile = testUser.getProfile();
        int initialRating = profile.getRatingPoints();

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.increaseRating(testUser);

        assertThat(profile.getRatingPoints()).isEqualTo(initialRating + 1);
        assertThat(profile.getStatus()).isEqualTo(UserStatus.BEGINNER);
        verify(userRepository, times(1)).save(testUser);
    }

    @ParameterizedTest(name = "Рейтинг {0} -> статус {1}")
    @CsvSource({
            "-5, NO_PARTICIPATION",
            "-1, NO_PARTICIPATION",
            "0, NO_PARTICIPATION",
            "1, BEGINNER",
            "2, BEGINNER",
            "3, BEGINNER",
            "4, INTERMEDIATE",
            "5, INTERMEDIATE",
            "6, INTERMEDIATE",
            "7, INTERMEDIATE",
            "8, ADVANCED",
            "10, ADVANCED",
            "100, ADVANCED"
    })
    @DisplayName("getStatusByRating - правильный статус для разных рейтингов")
    void getStatusByRating_shouldReturnCorrectStatus(int rating, UserStatus expectedStatus) {
        UserStatus actualStatus = userService.getStatusByRating(rating);
        assertThat(actualStatus).isEqualTo(expectedStatus);
    }

    @Test
    @DisplayName("increaseRating - сохранение пользователя с обновленными данными")
    void increaseRating_shouldSaveUserWithUpdatedData() {
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        when(userRepository.save(userCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

        int initialRating = testUser.getProfile().getRatingPoints();
        userService.increaseRating(testUser);

        User capturedUser = userCaptor.getValue();
        assertThat(capturedUser.getProfile().getRatingPoints()).isEqualTo(initialRating + 1);
        assertThat(capturedUser.getProfile().getStatus()).isEqualTo(UserStatus.BEGINNER);
    }

    @Test
    @DisplayName("increaseRating - вызов save ровно один раз")
    void increaseRating_shouldCallSaveExactlyOnce() {
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.increaseRating(testUser);

        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    @DisplayName("increaseRating - проброс исключения при ошибке репозитория")
    void increaseRating_whenRepositoryFails_shouldThrowException() {
        RuntimeException dbException = new RuntimeException("Ошибка подключения к базе данных");
        when(userRepository.save(any(User.class))).thenThrow(dbException);

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> userService.increaseRating(testUser));

        assertThat(thrown.getMessage()).isEqualTo("Ошибка подключения к базе данных");
        verify(userRepository, times(1)).save(testUser);
    }
}