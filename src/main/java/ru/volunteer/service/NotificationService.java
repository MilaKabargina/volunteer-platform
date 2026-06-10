package ru.volunteer.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.volunteer.model.dto.NotificationDto;
import ru.volunteer.model.dto.NotificationWebSocketDto;
import ru.volunteer.model.entity.Notification;
import ru.volunteer.model.entity.User;
import ru.volunteer.repository.NotificationJpaRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private final NotificationJpaRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public NotificationService(NotificationJpaRepository notificationRepository,
                               SimpMessagingTemplate messagingTemplate) {
        this.notificationRepository = notificationRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @Transactional
    public void createNotification(User user, String message, String link) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setMessage(message);
        notification.setLink(link);
        notification.setRead(false);
        Notification saved = notificationRepository.save(notification);
        log.debug("Created notification for user {}: {}", user.getLogin(), message);
        sendWebSocketNotification(user.getId(), saved);
    }

    private void sendWebSocketNotification(Long userId, Notification notification) {
        try {
            NotificationDto dto = toDto(notification);
            messagingTemplate.convertAndSend(
                    "/topic/notifications/" + userId,
                    new NotificationWebSocketDto("NEW_NOTIFICATION", dto)
            );

            long newCount = getUnreadCountByUserId(userId);
            messagingTemplate.convertAndSend(
                    "/topic/notifications/count/" + userId,
                    new NotificationWebSocketDto("COUNT_UPDATE", newCount)
            );

            log.debug("WebSocket notification sent to user {}", userId);
        } catch (Exception e) {
            log.error("Failed to send WebSocket notification to user {}: {}", userId, e.getMessage());
        }
    }

    private long getUnreadCountByUserId(Long userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(User user) {
        return notificationRepository.countByUserIdAndReadFalse(user.getId());
    }

    @Transactional(readOnly = true)
    public Page<NotificationDto> getUserNotifications(User user, int page, int size) {
        Page<Notification> notificationPage = notificationRepository.findByUserIdOrderByCreatedAtDesc(
                user.getId(),
                Pageable.ofSize(size).withPage(page)
        );

        List<NotificationDto> dtos = notificationPage.getContent().stream()
                .map(this::toDto)
                .collect(Collectors.toList());

        return new PageImpl<>(dtos, notificationPage.getPageable(), notificationPage.getTotalElements());
    }

    private NotificationDto toDto(Notification notification) {
        return new NotificationDto(
                notification.getId(),
                notification.getMessage(),
                notification.getLink(),
                notification.isRead(),
                notification.getCreatedAt()
        );
    }

    @Transactional
    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            n.setRead(true);
            notificationRepository.save(n);
            log.debug("Notification {} marked as read", notificationId);

            messagingTemplate.convertAndSend(
                    "/topic/notifications/count/" + n.getUser().getId(),
                    new NotificationWebSocketDto("COUNT_UPDATE", getUnreadCountByUserId(n.getUser().getId()))
            );
        });
    }

    @Transactional
    public void markAllAsRead(User user) {
        List<Notification> unreadNotifications = notificationRepository.findByUserIdAndReadFalse(user.getId());
        unreadNotifications.forEach(n -> {
            n.setRead(true);
            notificationRepository.save(n);
        });
        log.debug("All notifications marked as read for user {}", user.getLogin());
        messagingTemplate.convertAndSend(
                "/topic/notifications/count/" + user.getId(),
                new NotificationWebSocketDto("COUNT_UPDATE", 0)
        );
    }
}