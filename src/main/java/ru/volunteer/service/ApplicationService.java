package ru.volunteer.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.volunteer.exception.ValidationException;
import ru.volunteer.model.dto.ApplicationResponseDto;
import ru.volunteer.exception.ResourceNotFoundException;
import ru.volunteer.model.entity.Application;
import ru.volunteer.model.enums.ApplicationStatus;
import ru.volunteer.model.entity.Initiative;
import ru.volunteer.model.entity.User;
import ru.volunteer.repository.ApplicationJpaRepository;

import java.util.List;

@Service
public class ApplicationService {
    private static final Logger log = LoggerFactory.getLogger(ApplicationService.class);
    private final ApplicationJpaRepository applicationRepository;
    private final UserService userService;
    private final NotificationService notificationService;

    public ApplicationService(ApplicationJpaRepository applicationRepository,
                              UserService userService,
                              NotificationService notificationService) {
        this.applicationRepository = applicationRepository;
        this.userService = userService;
        this.notificationService = notificationService;
    }

    @Transactional
    public ApplicationResponseDto respondToInitiative(User applicant,
                                                      Initiative initiative,
                                                      String message) {
        boolean alreadyApplied = applicationRepository.findAll().stream()
                .anyMatch(app -> app.getApplicant().getId().equals(applicant.getId())
                        && app.getInitiative().getId().equals(initiative.getId()));

        if (alreadyApplied) {
            throw new ValidationException("Вы уже отправили заявку на эту инициативу");
        }

        Application application = new Application(
                applicant,
                initiative,
                ApplicationStatus.PENDING,
                message
        );

        Application saved = applicationRepository.save(application);

        log.info("Создана заявка id={}, автор заявки={}, ID инициативы={}",
                saved.getId(), applicant.getLogin(), initiative.getId());

        notificationService.createNotification(
                initiative.getAuthor(),
                "Новый отклик на вашу инициативу \"" + initiative.getTitle() + "\" от " + applicant.getLogin(),
                "/initiatives/" + initiative.getId()
        );

        return ApplicationResponseDto.from(saved);
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponseDto> getAllApplicationsAsDto() {
        return applicationRepository.findAll().stream()
                .map(ApplicationResponseDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<ApplicationResponseDto> getAllApplicationsPaged(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Application> appPage = applicationRepository.findAll(pageable);
        return appPage.map(ApplicationResponseDto::from);
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponseDto> getUserApplicationsAsDto(User user) {
        return applicationRepository.findByApplicantId(user.getId()).stream()
                .map(ApplicationResponseDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<ApplicationResponseDto> getUserApplicationsPaged(User user, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Application> appPage = applicationRepository.findByApplicantId(user.getId(), pageable);
        return appPage.map(ApplicationResponseDto::from);
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponseDto> getInitiativeApplicationsAsDto(Initiative initiative) {
        return applicationRepository.findByInitiativeId(initiative.getId()).stream()
                .map(ApplicationResponseDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<ApplicationResponseDto> getInitiativeApplicationsPaged(Initiative initiative, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Application> appPage = applicationRepository.findByInitiativeId(initiative.getId(), pageable);
        return appPage.map(ApplicationResponseDto::from);
    }

    @Transactional(readOnly = true)
    public ApplicationResponseDto findDtoById(long id) {
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application", id));
        return ApplicationResponseDto.from(application);
    }

    @Transactional
    public ApplicationResponseDto updateStatusDto(long applicationId, ApplicationStatus status, User currentUser) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application", applicationId));

        boolean isAuthor = application.getInitiative().getAuthor().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_ADMIN"));

        if (!isAuthor && !isAdmin) {
            throw new AccessDeniedException("Вы можете изменять статус только заявок на свои инициативы");
        }

        ApplicationStatus oldStatus = application.getStatus();
        application.setStatus(status);

        if (status == ApplicationStatus.APPROVED && oldStatus != ApplicationStatus.APPROVED) {
            // userService.increaseRating(application.getApplicant()); // УДАЛЕНО
            String contactInfo = application.getInitiative().getContactInfo();
            String contactMessage = contactInfo != null && !contactInfo.isEmpty()
                    ? " Контакты организатора: " + contactInfo
                    : "";
            notificationService.createNotification(
                    application.getApplicant(),
                    "Ваша заявка на инициативу \"" + application.getInitiative().getTitle() + "\" принята!" + contactMessage,
                    "/initiatives/" + application.getInitiative().getId()
            );
        }

        Application saved = applicationRepository.save(application);
        log.info("Статус заявки с ID={} изменен на {}", applicationId, saved.getStatus());

        return ApplicationResponseDto.from(saved);
    }

    @Transactional(readOnly = true)
    public List<Application> getAllApplications() {
        return applicationRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Application> getUserApplications(User user) {
        return applicationRepository.findByApplicantId(user.getId());
    }

    @Transactional(readOnly = true)
    public List<Application> getInitiativeApplications(Initiative initiative) {
        return applicationRepository.findByInitiativeId(initiative.getId());
    }

    @Transactional(readOnly = true)
    public Application findById(long id) {
        return applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application", id));
    }

    @Transactional
    public void deleteById(long id) {
        applicationRepository.deleteById(id);
        log.info("Удалена заявка ID={}", id);
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponseDto> getApplicationsForAuthor(User author) {
        return applicationRepository.findAll().stream()
                .filter(app -> app.getInitiative().getAuthor().getId().equals(author.getId()))
                .map(ApplicationResponseDto::from)
                .toList();
    }

    @Transactional
    public ApplicationResponseDto thankParticipant(long applicationId, User currentUser) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application", applicationId));

        boolean isAuthor = application.getInitiative().getAuthor().getId().equals(currentUser.getId());
        if (!isAuthor) {
            throw new AccessDeniedException("Только автор инициативы может благодарить участников");
        }

        if (application.getStatus() != ApplicationStatus.APPROVED) {
            throw new ValidationException("Можно благодарить только принятых участников");
        }

        userService.increaseRating(application.getApplicant());
        application.setStatus(ApplicationStatus.COMPLETED);
        Application saved = applicationRepository.save(application);

        log.info("Участник {} получил благодарность за инициативу {}",
                application.getApplicant().getLogin(), application.getInitiative().getTitle());

        notificationService.createNotification(
                application.getApplicant(),
                "Организатор поблагодарил вас за участие в инициативе \"" + application.getInitiative().getTitle() + "\"",
                "/initiatives/" + application.getInitiative().getId()
        );

        return ApplicationResponseDto.from(saved);
    }
}