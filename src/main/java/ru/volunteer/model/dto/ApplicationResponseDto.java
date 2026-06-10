package ru.volunteer.model.dto;

import ru.volunteer.model.entity.Application;
import ru.volunteer.model.enums.ApplicationStatus;

public class ApplicationResponseDto {
    private long id;
    private long initiativeId;
    private String initiativeTitle;
    private long applicantId;
    private String applicantLogin;
    private ApplicationStatus status;
    private String message;
    private String contactInfo;

    public ApplicationResponseDto() {}

    public ApplicationResponseDto(long id, long initiativeId, String initiativeTitle,
                                  long applicantId, String applicantLogin,
                                  ApplicationStatus status, String message, String contactInfo) {
        this.id = id;
        this.initiativeId = initiativeId;
        this.initiativeTitle = initiativeTitle;
        this.applicantId = applicantId;
        this.applicantLogin = applicantLogin;
        this.status = status;
        this.message = message;
        this.contactInfo = contactInfo;
    }

    public static ApplicationResponseDto from(Application application) {
        String contactInfo = (application.getStatus() == ApplicationStatus.APPROVED ||
                application.getStatus() == ApplicationStatus.COMPLETED)
                ? application.getInitiative().getContactInfo()
                : null;

        return new ApplicationResponseDto(
                application.getId(),
                application.getInitiative().getId(),
                application.getInitiative().getTitle(),
                application.getApplicant().getId(),
                application.getApplicant().getLogin(),
                application.getStatus(),
                application.getMessage(),
                contactInfo
        );
    }

    public long getId() { return id; }
    public long getInitiativeId() { return initiativeId; }
    public String getInitiativeTitle() { return initiativeTitle; }
    public long getApplicantId() { return applicantId; }
    public String getApplicantLogin() { return applicantLogin; }
    public ApplicationStatus getStatus() { return status; }
    public String getMessage() { return message; }
    public String getContactInfo() { return contactInfo; }

    public void setId(long id) { this.id = id; }
    public void setInitiativeId(long initiativeId) { this.initiativeId = initiativeId; }
    public void setInitiativeTitle(String initiativeTitle) { this.initiativeTitle = initiativeTitle; }
    public void setApplicantId(long applicantId) { this.applicantId = applicantId; }
    public void setApplicantLogin(String applicantLogin) { this.applicantLogin = applicantLogin; }
    public void setStatus(ApplicationStatus status) { this.status = status; }
    public void setMessage(String message) { this.message = message; }
    public void setContactInfo(String contactInfo) { this.contactInfo = contactInfo; }
}