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
import ru.volunteer.model.dto.InitiativeRequestDto;
import ru.volunteer.model.dto.InitiativeResponseDto;
import ru.volunteer.model.entity.Initiative;
import ru.volunteer.model.entity.User;
import ru.volunteer.model.enums.InitiativeStatus;
import ru.volunteer.repository.InitiativeJpaRepository;
import ru.volunteer.testutil.TestDataFactory;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InitiativeServiceTest {

    @Mock
    private InitiativeJpaRepository initiativeRepository;

    @InjectMocks
    private InitiativeService initiativeService;

    private User testUser;
    private User anotherUser;
    private Initiative testInitiative;
    private InitiativeRequestDto testRequestDto;

    @BeforeEach
    void setUp() {
        testUser = TestDataFactory.createTestUser(1L, "testUser");
        testUser.setFirstName("Иван");
        testUser.setSecondName("Иванов");
        anotherUser = TestDataFactory.createTestUser(2L, "anotherUser");
        anotherUser.setFirstName("Петр");
        anotherUser.setSecondName("Петров");

        testInitiative = TestDataFactory.createTestInitiative(1L, testUser, "Тестовая инициатива", "ПОМОЩЬ");
        testInitiative.setCity("Москва");
        testInitiative.setContactInfo("contact@test.com");
        testInitiative.setStatus(InitiativeStatus.APPROVED);

        testRequestDto = new InitiativeRequestDto();
        testRequestDto.setTitle("Новая инициатива");
        testRequestDto.setCategory("ОБРАЗОВАНИЕ");
        testRequestDto.setDescription("Описание новой инициативы");
        testRequestDto.setCity("Санкт-Петербург");
        testRequestDto.setContactInfo("new@test.com");
    }

    @Test
    @DisplayName("createInitiative - успешное создание инициативы")
    void createInitiative_shouldCreateInitiative_whenValidData() {
        Initiative savedInitiative = TestDataFactory.createTestInitiative(1L, testUser, "Новая инициатива", "ОБРАЗОВАНИЕ");
        savedInitiative.setDescription("Описание новой инициативы");
        savedInitiative.setCity("Санкт-Петербург");
        savedInitiative.setContactInfo("new@test.com");

        when(initiativeRepository.save(any(Initiative.class))).thenReturn(savedInitiative);

        InitiativeResponseDto result = initiativeService.createInitiative(testUser, testRequestDto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Новая инициатива");
        verify(initiativeRepository, times(1)).save(any(Initiative.class));
    }

    @Test
    @DisplayName("getAllInitiativesPaged - пагинация одобренных инициатив")
    void getAllInitiativesPaged_shouldReturnPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Initiative> initiativePage = new PageImpl<>(List.of(testInitiative), pageable, 1);
        when(initiativeRepository.findByStatus(eq(InitiativeStatus.APPROVED), any(Pageable.class)))
                .thenReturn(initiativePage);

        Page<InitiativeResponseDto> result = initiativeService.getAllInitiativesPaged(0, 10);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(initiativeRepository, times(1))
                .findByStatus(eq(InitiativeStatus.APPROVED), any(Pageable.class));
    }

    @Test
    @DisplayName("getInitiativeByAuthor - инициативы автора")
    void getInitiativeByAuthor_shouldReturnUserInitiatives() {
        when(initiativeRepository.findByAuthorId(testUser.getId())).thenReturn(List.of(testInitiative));

        List<InitiativeResponseDto> result = initiativeService.getInitiativeByAuthor(testUser);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAuthorLogin()).isEqualTo(testUser.getLogin());
        verify(initiativeRepository, times(1)).findByAuthorId(testUser.getId());
    }

    @Test
    @DisplayName("findById - успешный поиск по ID")
    void findById_shouldReturnInitiative_whenExists() {
        when(initiativeRepository.findById(1L)).thenReturn(Optional.of(testInitiative));

        InitiativeResponseDto result = initiativeService.findById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(initiativeRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("findById - исключение когда не найдена")
    void findById_shouldThrowException_whenNotFound() {
        when(initiativeRepository.findById(999L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> initiativeService.findById(999L));

        assertThat(exception.getMessage()).contains("999");
    }

    @Test
    @DisplayName("findByCategory - поиск по категории")
    void findByCategory_shouldReturnInitiativesByCategory() {
        Initiative initiative2 = TestDataFactory.createTestInitiative(2L, testUser, "Другая помощь", "ПОМОЩЬ");
        initiative2.setCity("Москва");
        when(initiativeRepository.findByCategory("ПОМОЩЬ")).thenReturn(List.of(testInitiative, initiative2));

        List<InitiativeResponseDto> result = initiativeService.findByCategory("ПОМОЩЬ");

        assertThat(result).hasSize(2);
        verify(initiativeRepository, times(1)).findByCategory("ПОМОЩЬ");
    }

    @Test
    @DisplayName("findByCategoryPaged - пагинация по категории")
    void findByCategoryPaged_shouldReturnPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Initiative> initiativePage = new PageImpl<>(List.of(testInitiative), pageable, 1);
        when(initiativeRepository.findByCategory(eq("ПОМОЩЬ"), any(Pageable.class)))
                .thenReturn(initiativePage);

        Page<InitiativeResponseDto> result = initiativeService.findByCategoryPaged("ПОМОЩЬ", 0, 10);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(initiativeRepository, times(1)).findByCategory(eq("ПОМОЩЬ"), any(Pageable.class));
    }

    @Test
    @DisplayName("findEntityById - возвращает сущность")
    void findEntityById_shouldReturnInitiativeEntity_whenExists() {
        when(initiativeRepository.findById(1L)).thenReturn(Optional.of(testInitiative));

        Initiative result = initiativeService.findEntityById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("updateInitiativeByAuthor - успешное обновление своей инициативы")
    void updateInitiativeByAuthor_shouldUpdateInitiative_whenUserIsAuthor() {
        when(initiativeRepository.findById(1L)).thenReturn(Optional.of(testInitiative));
        when(initiativeRepository.save(any(Initiative.class))).thenAnswer(invocation -> invocation.getArgument(0));

        InitiativeResponseDto result = initiativeService.updateInitiativeByAuthor(
                1L, testUser, "Обновленный заголовок", "НОВАЯ_КАТЕГОРИЯ", "Обновленное описание", "Казань");

        assertThat(result.getTitle()).isEqualTo("Обновленный заголовок");
        assertThat(result.getCategory()).isEqualTo("НОВАЯ_КАТЕГОРИЯ");
        assertThat(result.getCity()).isEqualTo("Казань");
        verify(initiativeRepository, times(1)).save(any(Initiative.class));
    }

    @Test
    @DisplayName("updateInitiativeByAuthor - исключение когда не автор")
    void updateInitiativeByAuthor_shouldThrowAccessDenied_whenUserIsNotAuthor() {
        when(initiativeRepository.findById(1L)).thenReturn(Optional.of(testInitiative));

        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> initiativeService.updateInitiativeByAuthor(1L, anotherUser, "Новый заголовок", "КАТЕГОРИЯ", "Описание", "Москва"));

        assertThat(exception.getMessage()).isEqualTo("Вы можете редактировать только свои инициативы");
        verify(initiativeRepository, never()).save(any(Initiative.class));
    }

    @Test
    @DisplayName("deleteByAuthor - успешное удаление своей инициативы")
    void deleteByAuthor_shouldDeleteInitiative_whenUserIsAuthor() {
        when(initiativeRepository.findById(1L)).thenReturn(Optional.of(testInitiative));
        doNothing().when(initiativeRepository).deleteById(1L);

        initiativeService.deleteByAuthor(1L, testUser);

        verify(initiativeRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("deleteByAuthor - исключение когда не автор")
    void deleteByAuthor_shouldThrowAccessDenied_whenUserIsNotAuthor() {
        when(initiativeRepository.findById(1L)).thenReturn(Optional.of(testInitiative));

        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> initiativeService.deleteByAuthor(1L, anotherUser));

        assertThat(exception.getMessage()).isEqualTo("Вы можете удалять только свои инициативы");
        verify(initiativeRepository, never()).deleteById(anyLong());
    }
}