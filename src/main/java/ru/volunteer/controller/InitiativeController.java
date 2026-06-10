package ru.volunteer.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import ru.volunteer.model.dto.InitiativeRequestDto;
import ru.volunteer.model.dto.InitiativeResponseDto;
import ru.volunteer.model.entity.User;
import ru.volunteer.service.AuthService;
import ru.volunteer.service.InitiativeService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/initiatives")
public class InitiativeController {
    private static final Logger log = LoggerFactory.getLogger(InitiativeController.class);
    private final InitiativeService initiativeService;
    private final AuthService authService;

    public InitiativeController(InitiativeService initiativeService,
                                AuthService authService) {
        this.initiativeService = initiativeService;
        this.authService = authService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Page<InitiativeResponseDto>> getAllInitiatives(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        log.debug("GET /api/v1/initiatives - page={}, size={}", page, size);
        Page<InitiativeResponseDto> initiativesPage = initiativeService.getAllInitiativesPaged(page, size);
        return ResponseEntity.ok(initiativesPage);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<InitiativeResponseDto> createInitiative(
            @RequestBody @Valid InitiativeRequestDto dto
    ) {
        User currentUser = authService.getCurrentUser();
        InitiativeResponseDto initiative = initiativeService.createInitiative(currentUser, dto);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(initiative.getId())
                .toUri();
        log.info("Инициатива успешно создана пользователем {} с ID {}",
                currentUser.getLogin(), initiative.getId());
        return ResponseEntity.created(location).body(initiative);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<InitiativeResponseDto> getInitiative(@PathVariable Long id) {
        log.debug("Ищем инициативу с ID: {}", id);
        InitiativeResponseDto initiative = initiativeService.findById(id);
        return ResponseEntity.ok(initiative);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Void> deleteInitiative(@PathVariable long id) {
        User currentUser = authService.getCurrentUser();
        log.info("Удаляем инициативу {} пользователем {}", id, currentUser.getLogin());
        initiativeService.deleteByAuthor(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<InitiativeResponseDto> updateInitiative(
            @PathVariable long id,
            @RequestBody @Valid InitiativeRequestDto dto
    ) {
        User currentUser = authService.getCurrentUser();
        log.info("Обновляем инициативу {} пользователем {}", id, currentUser.getLogin());
        // Передаём city
        InitiativeResponseDto initiativeUpdated =
                initiativeService.updateInitiativeByAuthor(
                        id,
                        currentUser,
                        dto.getTitle(),
                        dto.getCategory(),
                        dto.getDescription(),
                        dto.getCity()
                );
        return ResponseEntity.ok(initiativeUpdated);
    }

    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<InitiativeResponseDto>> getMyInitiatives() {
        User currentUser = authService.getCurrentUser();
        log.debug("Инициативы пользователя: {}", currentUser.getLogin());
        List<InitiativeResponseDto> myInitiatives =
                initiativeService.getInitiativeByAuthor(currentUser);
        return ResponseEntity.ok(myInitiatives);
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<InitiativeResponseDto>> searchByCategory(@RequestParam String category) {
        log.debug("Поиск по категории: {}", category);
        List<InitiativeResponseDto> results = initiativeService.findByCategory(category);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/paged")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Page<InitiativeResponseDto>> getAllPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        log.debug("GET /api/v1/initiatives/paged - page={}, size={}", page, size);
        Page<InitiativeResponseDto> result = initiativeService.getAllInitiativesPaged(page, size);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/search/paged")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Page<InitiativeResponseDto>> searchByCategoryPaged(
            @RequestParam String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        log.debug("GET /api/v1/initiatives/search/paged - category={}, page={}, size={}",
                category, page, size);
        Page<InitiativeResponseDto> result = initiativeService.findByCategoryPaged(category, page, size);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<InitiativeResponseDto>> getPendingInitiatives() {
        log.debug("Получение инициатив на модерацию");
        List<InitiativeResponseDto> initiatives = initiativeService.getPendingInitiatives();
        return ResponseEntity.ok(initiatives);
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InitiativeResponseDto> approveInitiative(@PathVariable Long id) {
        log.info("Одобрение инициативы {} администратором", id);
        InitiativeResponseDto initiative = initiativeService.approveInitiative(id);
        return ResponseEntity.ok(initiative);
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InitiativeResponseDto> rejectInitiative(@PathVariable Long id, @RequestBody(required = false) String reason) {
        log.info("Отклонение инициативы {} администратором. Причина: {}", id, reason);
        InitiativeResponseDto initiative = initiativeService.rejectInitiative(id, reason);
        return ResponseEntity.ok(initiative);
    }

    @PutMapping("/{id}/resubmit")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<InitiativeResponseDto> resubmitInitiative(
            @PathVariable Long id,
            @RequestBody @Valid InitiativeRequestDto dto) {
        User currentUser = authService.getCurrentUser();
        InitiativeResponseDto updated = initiativeService.resubmitInitiative(id, currentUser, dto);
        return ResponseEntity.ok(updated);
    }
}