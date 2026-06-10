package ru.volunteer.model.dto;

import ru.volunteer.model.entity.Initiative;

public class InitiativeResponseDto {
    private long id;
    private String title;
    private String category;
    private String description;
    private String city;
    private Long authorId;
    private String authorLogin;
    private String authorFirstName;
    private String authorSecondName;
    private String status;
    private String contactInfo;
    private String moderationReason;

    public InitiativeResponseDto() {}

    public InitiativeResponseDto(long id, String title, String category, String description, String city,
                                 Long authorId, String authorLogin, String authorFirstName, String authorSecondName,
                                 String status, String moderationReason, String contactInfo) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.description = description;
        this.city = city;
        this.authorId = authorId;
        this.authorLogin = authorLogin;
        this.authorFirstName = authorFirstName;
        this.authorSecondName = authorSecondName;
        this.status = status;
        this.moderationReason = moderationReason;
        this.contactInfo = contactInfo;
    }

    public static InitiativeResponseDto from(Initiative initiative) {
        return new InitiativeResponseDto(
                initiative.getId(),
                initiative.getTitle(),
                initiative.getCategory(),
                initiative.getDescription(),
                initiative.getCity(),
                initiative.getAuthor().getId(),
                initiative.getAuthor().getLogin(),
                initiative.getAuthor().getFirstName(),
                initiative.getAuthor().getSecondName(),
                initiative.getStatus() != null ? initiative.getStatus().name() : "PENDING",
                initiative.getModerationReason(),
                initiative.getContactInfo()
        );
    }

    public long getId() { return id; }
    public String getTitle() { return title; }
    public String getCategory() { return category; }
    public String getDescription() { return description; }
    public String getCity() { return city; }
    public Long getAuthorId() { return authorId; }
    public String getAuthorLogin() { return authorLogin; }
    public String getAuthorFirstName() { return authorFirstName; }
    public String getAuthorSecondName() { return authorSecondName; }
    public String getStatus() { return status; }
    public String getModerationReason() { return moderationReason; }
    public String getContactInfo() { return contactInfo; }

    public void setId(long id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setCategory(String category) { this.category = category; }
    public void setDescription(String description) { this.description = description; }
    public void setCity(String city) { this.city = city; }
    public void setAuthorId(Long authorId) { this.authorId = authorId; }
    public void setAuthorLogin(String authorLogin) { this.authorLogin = authorLogin; }
    public void setAuthorFirstName(String authorFirstName) { this.authorFirstName = authorFirstName; }
    public void setAuthorSecondName(String authorSecondName) { this.authorSecondName = authorSecondName; }
    public void setStatus(String status) { this.status = status; }
    public void setModerationReason(String moderationReason) { this.moderationReason = moderationReason; }
    public void setContactInfo(String contactInfo) { this.contactInfo = contactInfo; }
}