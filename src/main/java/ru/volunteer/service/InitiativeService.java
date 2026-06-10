package ru.volunteer.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.volunteer.model.dto.InitiativeRequestDto;
import ru.volunteer.model.dto.InitiativeResponseDto;
import ru.volunteer.exception.ResourceNotFoundException;
import ru.volunteer.exception.ValidationException;
import ru.volunteer.model.entity.Initiative;
import ru.volunteer.model.entity.User;
import ru.volunteer.model.enums.InitiativeStatus;
import ru.volunteer.repository.InitiativeJpaRepository;

import java.util.List;

@Service
public class InitiativeService {
    private static final Logger log = LoggerFactory.getLogger(InitiativeService.class);
    private final InitiativeJpaRepository initiativeRepository;
    private final NotificationService notificationService;

    public InitiativeService(InitiativeJpaRepository initiativeRepository,
                             NotificationService notificationService) {
        this.initiativeRepository = initiativeRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public InitiativeResponseDto createInitiative(User author, InitiativeRequestDto dto) {
        log.info("Создание инициативы: {}", dto.getTitle());
        Initiative initiative = new Initiative(
                author,
                dto.getTitle(),
                dto.getCategory(),
                dto.getDescription(),
                dto.getCity()
        );
        initiative.setContactInfo(dto.getContactInfo());
        Initiative savedInitiative = initiativeRepository.save(initiative);
        return InitiativeResponseDto.from(savedInitiative);
    }

    @Transactional(readOnly = true)
    public Page<InitiativeResponseDto> getAllInitiativesPaged(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Initiative> initiativePage = initiativeRepository.findByStatus(InitiativeStatus.APPROVED, pageable);
        return initiativePage.map(InitiativeResponseDto::from);
    }

    @Transactional(readOnly = true)
    public List<InitiativeResponseDto> getInitiativeByAuthor(User author) {
        return initiativeRepository.findByAuthorId(author.getId()).stream()
                .map(InitiativeResponseDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public InitiativeResponseDto findById(long id) {
        Initiative initiative = initiativeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Initiative", id));
        return InitiativeResponseDto.from(initiative);
    }

    @Transactional
    public void deleteById(long id) {
        initiativeRepository.deleteById(id);
        log.info("Инициатива {} удалена", id);
    }

    @Transactional
    public InitiativeResponseDto updateInitiative(long id, String title, String category, String description, String city) {
        Initiative initiative = initiativeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Initiative", id));

        initiative.setTitle(title);
        initiative.setCategory(category);
        initiative.setDescription(description);
        initiative.setCity(city);

        Initiative savedInitiative = initiativeRepository.save(initiative);
        log.info("Инициатива {} обновлена", id);
        return InitiativeResponseDto.from(savedInitiative);
    }

    @Transactional(readOnly = true)
    public List<InitiativeResponseDto> findByCategory(String category) {
        return initiativeRepository.findByCategory(category).stream()
                .map(InitiativeResponseDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<InitiativeResponseDto> findByCategoryPaged(String category, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Initiative> initiativePage = initiativeRepository.findByCategory(category, pageable);
        return initiativePage.map(InitiativeResponseDto::from);
    }

    @Transactional(readOnly = true)
    public Initiative findEntityById(long id) {
        return initiativeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Initiative", id));
    }

    @Transactional
    public InitiativeResponseDto updateInitiativeByAuthor(
            Long id,
            User currentUser,
            String title,
            String category,
            String description,
            String city
    ) {
        Initiative initiative = initiativeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Initiative", id));

        if (!initiative.getAuthor().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Вы можете редактировать только свои инициативы");
        }

        initiative.setTitle(title);
        initiative.setCategory(category);
        initiative.setDescription(description);
        initiative.setCity(city);

        Initiative updated = initiativeRepository.save(initiative);
        log.info("Инициатива {} обновлена пользователем {}", id, currentUser.getLogin());

        return InitiativeResponseDto.from(updated);
    }

    @Transactional
    public void deleteByAuthor(long id, User currentUser) {
        Initiative initiative = initiativeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Initiative", id));

        if (!initiative.getAuthor().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Вы можете удалять только свои инициативы");
        }

        initiativeRepository.deleteById(id);
        log.info("Инициатива {} удалена пользователем {}", id, currentUser.getLogin());
    }

    @Transactional
    public InitiativeResponseDto approveInitiative(Long id) {
        Initiative initiative = initiativeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Initiative", id));
        initiative.setStatus(InitiativeStatus.APPROVED);
        Initiative saved = initiativeRepository.save(initiative);
        log.info("Инициатива {} одобрена", id);

        notificationService.createNotification(
                initiative.getAuthor(),
                "Ваша инициатива \"" + initiative.getTitle() + "\" одобрена и опубликована",
                "/initiatives/" + initiative.getId()
        );

        return InitiativeResponseDto.from(saved);
    }

    @Transactional
    public InitiativeResponseDto rejectInitiative(Long id, String reason) {
        Initiative initiative = initiativeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Initiative", id));
        initiative.setStatus(InitiativeStatus.REJECTED);
        initiative.setModerationReason(reason);
        Initiative saved = initiativeRepository.save(initiative);
        log.info("Инициатива {} отклонена. Причина: {}", id, reason);

        notificationService.createNotification(
                initiative.getAuthor(),
                "Ваша инициатива \"" + initiative.getTitle() + "\" отклонена. Причина: " + reason,
                "/initiatives/" + initiative.getId()
        );

        return InitiativeResponseDto.from(saved);
    }

    @Transactional(readOnly = true)
    public List<InitiativeResponseDto> getPendingInitiatives() {
        return initiativeRepository.findAll().stream()
                .filter(i -> i.getStatus() == InitiativeStatus.PENDING)
                .map(InitiativeResponseDto::from)
                .toList();
    }

    @Transactional
    public InitiativeResponseDto resubmitInitiative(Long id, User currentUser, InitiativeRequestDto dto) {
        Initiative initiative = initiativeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Initiative", id));
        if (!initiative.getAuthor().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Вы можете редактировать только свои инициативы");
        }
        if (initiative.getStatus() != InitiativeStatus.REJECTED && initiative.getStatus() != InitiativeStatus.PENDING) {
            throw new ValidationException("Редактировать можно только отклонённые или ожидающие модерации инициативы");
        }
        initiative.setTitle(dto.getTitle());
        initiative.setCategory(dto.getCategory());
        initiative.setDescription(dto.getDescription());
        initiative.setContactInfo(dto.getContactInfo());
        initiative.setCity(dto.getCity());
        initiative.setStatus(InitiativeStatus.PENDING);
        initiative.setModerationReason(null);
        Initiative saved = initiativeRepository.save(initiative);
        log.info("Инициатива {} обновлена и отправлена на повторную модерацию пользователем {}", id, currentUser.getLogin());
        return InitiativeResponseDto.from(saved);
    }
}