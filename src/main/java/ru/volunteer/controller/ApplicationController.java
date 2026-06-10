package ru.volunteer.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.volunteer.model.dto.ApplicationRequestDto;
import ru.volunteer.model.dto.ApplicationResponseDto;
import ru.volunteer.model.entity.Application;
import ru.volunteer.model.enums.ApplicationStatus;
import ru.volunteer.model.entity.Initiative;
import ru.volunteer.model.entity.User;
import ru.volunteer.service.ApplicationService;
import ru.volunteer.service.AuthService;
import ru.volunteer.service.InitiativeService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/applications")
public class ApplicationController {
    private static final Logger log = LoggerFactory.getLogger(ApplicationController.class);
    private final ApplicationService applicationService;
    private final AuthService authService;
    private final InitiativeService initiativeService;

    public ApplicationController(ApplicationService applicationService,
                                 AuthService authService,
                                 InitiativeService initiativeService) {
        this.applicationService = applicationService;
        this.authService = authService;
        this.initiativeService = initiativeService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ApplicationResponseDto> createApplication(
            @RequestBody @Valid ApplicationRequestDto dto
    ) {
        log.debug("Создание заявки на инициативу: {}", dto.getIdInitiative());
        User applicant = authService.getCurrentUser();
        Initiative initiative = initiativeService.findEntityById(dto.getIdInitiative());
        ApplicationResponseDto application =
                applicationService.respondToInitiative(applicant, initiative, dto.getMessage());
        log.info("Заявка создана с ID {}", application.getId());
        return ResponseEntity.status(201).body(application);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ApplicationResponseDto> getApplication(@PathVariable long id) {
        log.debug("Получение заявки с ID {}", id);
        ApplicationResponseDto application = applicationService.findDtoById(id);
        return ResponseEntity.ok(application);
    }

    @PutMapping("/{id}/status/{status}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ApplicationResponseDto> updateApplicationStatus(
            @PathVariable long id,
            @PathVariable ApplicationStatus status
    ) {
        User currentUser = authService.getCurrentUser();
        ApplicationResponseDto application = applicationService.updateStatusDto(id, status, currentUser);
        log.info("Обновление статуса заявки с ID {} на {}", id, status);
        return ResponseEntity.ok(application);
    }

    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<ApplicationResponseDto>> getMyApplications() {
        User currentUser = authService.getCurrentUser();
        log.debug("Мои заявки пользователя {}", currentUser.getLogin());
        List<ApplicationResponseDto> applications = applicationService.getUserApplicationsAsDto(currentUser);
        return ResponseEntity.ok(applications);
    }

    @GetMapping("/my-initiatives")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<ApplicationResponseDto>> getApplicationsForMyInitiatives() {
        User currentUser = authService.getCurrentUser();
        log.debug("Заявки на мои инициативы пользователя {}", currentUser.getLogin());
        List<ApplicationResponseDto> applications = applicationService.getApplicationsForAuthor(currentUser);
        return ResponseEntity.ok(applications);
    }

    @GetMapping("/my/paged")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Page<ApplicationResponseDto>> getMyApplicationsPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        User currentUser = authService.getCurrentUser();
        log.debug("GET /api/v1/applications/my/paged - user={}, page={}, size={}",
                currentUser.getLogin(), page, size);
        Page<ApplicationResponseDto> result = applicationService.getUserApplicationsPaged(currentUser, page, size);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/initiative/{initiativeId}/paged")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Page<ApplicationResponseDto>> getInitiativeApplicationsPaged(
            @PathVariable long initiativeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Initiative initiative = initiativeService.findEntityById(initiativeId);
        User currentUser = authService.getCurrentUser();

        if (!initiative.getAuthor().getId().equals(currentUser.getId())) {
            log.warn("Пользователь {} попытался просмотреть отклики на чужую инициативу {}",
                    currentUser.getLogin(), initiativeId);
            return ResponseEntity.status(403).build();
        }

        log.debug("GET /api/v1/applications/initiative/{}/paged - author={}, page={}, size={}",
                initiativeId, currentUser.getLogin(), page, size);
        Page<ApplicationResponseDto> result = applicationService.getInitiativeApplicationsPaged(initiative, page, size);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}/thank")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ApplicationResponseDto> thankParticipant(@PathVariable long id) {
        User currentUser = authService.getCurrentUser();
        ApplicationResponseDto application = applicationService.thankParticipant(id, currentUser);
        log.info("Пользователь {} поблагодарил участника заявки {}", currentUser.getLogin(), id);
        return ResponseEntity.ok(application);
    }
}