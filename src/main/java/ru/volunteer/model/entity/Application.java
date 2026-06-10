package ru.volunteer.model.entity;

import jakarta.persistence.*;
import ru.volunteer.model.enums.ApplicationStatus;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "applications",
        indexes = {
                @Index(name = "idx_applications_applicant_id", columnList = "applicant_id"),
                @Index(name = "idx_applications_initiative_id", columnList = "initiative_id")
        }
)
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applicant_id", nullable = false)
    private User applicant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "initiative_id", nullable = false)
    private Initiative initiative;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private ApplicationStatus status;

    @Column(name = "message", nullable = false, length = 1000)
    private String message;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Application() {
    }

    public Application(User applicant, Initiative initiative, ApplicationStatus status, String message) {
        this.applicant = applicant;
        this.initiative = initiative;
        this.status = status;
        this.message = message;
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public User getApplicant() {
        return applicant;
    }

    public Initiative getInitiative() {
        return initiative;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setApplicant(User applicant) {
        this.applicant = applicant;
    }

    public void setInitiative(Initiative initiative) {
        this.initiative = initiative;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return id + " | " +
                (applicant != null ? applicant.getId() : null) + " | " +
                (initiative != null ? initiative.getTitle() : null) + " | " +
                status + " | " + message;
    }
}