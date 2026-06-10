package ru.volunteer.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.volunteer.model.dto.NotificationDto;
import ru.volunteer.model.entity.User;
import ru.volunteer.service.AuthService;
import ru.volunteer.service.NotificationService;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {
    private static final Logger log = LoggerFactory.getLogger(NotificationController.class);
    private final NotificationService notificationService;
    private final AuthService authService;

    public NotificationController(NotificationService notificationService, AuthService authService) {
        this.notificationService = notificationService;
        this.authService = authService;
    }

    @GetMapping("/unread/count")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Long> getUnreadCount() {
        User currentUser = authService.getCurrentUser();
        long count = notificationService.getUnreadCount(currentUser);
        log.debug("Unread notifications count for user {}: {}", currentUser.getLogin(), count);
        return ResponseEntity.ok(count);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Page<NotificationDto>> getNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        User currentUser = authService.getCurrentUser();
        log.debug("Fetching notifications for user: {}", currentUser.getLogin());
        Page<NotificationDto> notifications = notificationService.getUserNotifications(currentUser, page, size);
        log.debug("Found {} notifications for user {}", notifications.getTotalElements(), currentUser.getLogin());
        return ResponseEntity.ok(notifications);
    }

    @PutMapping("/{id}/read")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        log.debug("Notification {} marked as read", id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/read-all")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Void> markAllAsRead() {
        User currentUser = authService.getCurrentUser();
        notificationService.markAllAsRead(currentUser);
        log.debug("All notifications marked as read for user {}", currentUser.getLogin());
        return ResponseEntity.ok().build();
    }
}