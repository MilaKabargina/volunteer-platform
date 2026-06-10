package ru.volunteer.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import ru.volunteer.exception.ResourceNotFoundException;
import ru.volunteer.model.dto.ApplicationResponseDto;
import ru.volunteer.model.entity.Application;
import ru.volunteer.model.entity.Initiative;
import ru.volunteer.model.entity.User;
import ru.volunteer.model.enums.ApplicationStatus;
import ru.volunteer.repository.ApplicationJpaRepository;
import ru.volunteer.testutil.TestDataFactory;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ApplicationService Unit тесты")
class ApplicationServiceTest {

    @Mock
    private ApplicationJpaRepository applicationRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private ApplicationService applicationService;

    private User testUser;
    private User initiativeAuthor;
    private Initiative testInitiative;
    private Application testApplication;

    @BeforeEach
    void setUp() {
        testUser = TestDataFactory.createTestUser(1L, "testUser");
        initiativeAuthor = TestDataFactory.createTestUser(2L, "authorUser");
        testInitiative = TestDataFactory.createTestInitiative(1L, initiativeAuthor, "Тестовая инициатива", "ПОМОЩЬ");
        testApplication = TestDataFactory.createTestApplication(1L, testUser, testInitiative, ApplicationStatus.PENDING);
    }

    @Test
    @DisplayName("respondToInitiative - успешное создание заявки")
    void respondToInitiative_shouldCreateApplication_whenValidData() {
        String message = "Хочу помочь";
        Application savedApplication = TestDataFactory.createTestApplication(1L, testUser, testInitiative, ApplicationStatus.PENDING);
        savedApplication.setMessage(message);
        when(applicationRepository.save(any(Application.class))).thenReturn(savedApplication);

        ApplicationResponseDto result = applicationService.respondToInitiative(testUser, testInitiative, message);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(applicationRepository, times(1)).save(any(Application.class));
    }

    @Test
    @DisplayName("getAllApplicationsAsDto - список всех заявок")
    void getAllApplicationsAsDto_shouldReturnListOfApplications() {
        Application app2 = TestDataFactory.createTestApplication(2L, testUser, testInitiative, ApplicationStatus.APPROVED);
        when(applicationRepository.findAll()).thenReturn(List.of(testApplication, app2));

        List<ApplicationResponseDto> result = applicationService.getAllApplicationsAsDto();

        assertThat(result).hasSize(2);
        verify(applicationRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("getAllApplicationsPaged - пагинация всех заявок")
    void getAllApplicationsPaged_shouldReturnPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Application> applicationPage = new PageImpl<>(List.of(testApplication), pageable, 1);
        when(applicationRepository.findAll(pageable)).thenReturn(applicationPage);

        Page<ApplicationResponseDto> result = applicationService.getAllApplicationsPaged(0, 10);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(applicationRepository, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("getUserApplicationsAsDto - заявки конкретного пользователя")
    void getUserApplicationsAsDto_shouldReturnUserApplications() {
        when(applicationRepository.findByApplicantId(testUser.getId())).thenReturn(List.of(testApplication));

        List<ApplicationResponseDto> result = applicationService.getUserApplicationsAsDto(testUser);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getApplicantId()).isEqualTo(testUser.getId());
        verify(applicationRepository, times(1)).findByApplicantId(testUser.getId());
    }

    @Test
    @DisplayName("getApplicationsForAuthor - заявки на инициативы автора")
    void getApplicationsForAuthor_shouldReturnApplicationsForAuthorInitiatives() {
        when(applicationRepository.findAll()).thenReturn(List.of(testApplication));

        List<ApplicationResponseDto> result = applicationService.getApplicationsForAuthor(initiativeAuthor);

        assertThat(result).hasSize(1);
        verify(applicationRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("findDtoById - успешный поиск DTO заявки по ID")
    void findDtoById_shouldReturnApplication_whenExists() {
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(testApplication));

        ApplicationResponseDto result = applicationService.findDtoById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(applicationRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("findDtoById - исключение когда заявка не найдена")
    void findDtoById_shouldThrowException_whenApplicationNotFound() {
        when(applicationRepository.findById(999L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> applicationService.findDtoById(999L));

        assertThat(exception.getMessage()).contains("Application", "999");
    }

    @Test
    @DisplayName("updateStatusDto - одобрение заявки повышает рейтинг (автор инициативы)")
    void updateStatusDto_whenApprovedByAuthor_shouldIncreaseRating() {
        User currentUser = initiativeAuthor;
        testApplication.setStatus(ApplicationStatus.PENDING);
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(testApplication));
        when(applicationRepository.save(any(Application.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ApplicationResponseDto result = applicationService.updateStatusDto(1L, ApplicationStatus.APPROVED, currentUser);

        assertThat(result.getStatus()).isEqualTo(ApplicationStatus.APPROVED);
        verify(userService, times(1)).increaseRating(testUser);
    }

    @Test
    @DisplayName("updateStatusDto - отклонение заявки НЕ повышает рейтинг (автор инициативы)")
    void updateStatusDto_whenRejectedByAuthor_shouldNotIncreaseRating() {
        User currentUser = initiativeAuthor;
        testApplication.setStatus(ApplicationStatus.PENDING);
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(testApplication));
        when(applicationRepository.save(any(Application.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ApplicationResponseDto result = applicationService.updateStatusDto(1L, ApplicationStatus.REJECTED, currentUser);

        assertThat(result.getStatus()).isEqualTo(ApplicationStatus.REJECTED);
        verify(userService, never()).increaseRating(any(User.class));
    }

    @Test
    @DisplayName("updateStatusDto - не автор инициативы не может менять статус")
    void updateStatusDto_whenUserNotAuthor_shouldThrowAccessDenied() {
        User otherUser = TestDataFactory.createTestUser(99L, "otherUser");
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(testApplication));

        assertThrows(AccessDeniedException.class, () -> {
            applicationService.updateStatusDto(1L, ApplicationStatus.APPROVED, otherUser);
        });

        verify(applicationRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateStatusDto - админ может менять статус")
    void updateStatusDto_whenAdmin_shouldUpdateStatus() {
        User admin = TestDataFactory.createTestUser(99L, "admin");
        admin.setRoles(java.util.Set.of(TestDataFactory.createTestRole("ROLE_ADMIN")));
        testApplication.setStatus(ApplicationStatus.PENDING);
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(testApplication));
        when(applicationRepository.save(any(Application.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ApplicationResponseDto result = applicationService.updateStatusDto(1L, ApplicationStatus.APPROVED, admin);

        assertThat(result.getStatus()).isEqualTo(ApplicationStatus.APPROVED);
        verify(applicationRepository, times(1)).save(any(Application.class));
    }

    @Test
    @DisplayName("findById - успешный поиск сущности заявки")
    void findById_shouldReturnEntity_whenExists() {
        when(applicationRepository.findById(1L)).thenReturn(Optional.of(testApplication));

        Application result = applicationService.findById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(applicationRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("deleteById - удаление заявки")
    void deleteById_shouldCallRepositoryDelete() {
        doNothing().when(applicationRepository).deleteById(1L);

        applicationService.deleteById(1L);

        verify(applicationRepository, times(1)).deleteById(1L);
    }
}