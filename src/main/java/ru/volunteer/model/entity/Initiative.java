package ru.volunteer.model.entity;

import jakarta.persistence.*;
import ru.volunteer.model.enums.InitiativeStatus;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "initiatives",
        indexes = {
                @Index(name = "idx_initiatives_author_id", columnList = "author_id"),
                @Index(name = "idx_initiatives_category", columnList = "category"),
                @Index(name = "idx_initiatives_city", columnList = "city")  // новый индекс
        }
)
public class Initiative {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "category", length = 100)
    private String category;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "city", nullable = false, length = 100)   // новое поле
    private String city;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private InitiativeStatus status = InitiativeStatus.PENDING;

    @Column(name = "contact_info", length = 500)
    private String contactInfo;

    @Column(name = "moderation_reason", length = 500)
    private String moderationReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Initiative() {
    }

    public Initiative(User author, String title, String category, String description, String city) {
        this.author = author;
        this.title = title;
        this.category = category;
        this.description = description;
        this.city = city;
        this.status = InitiativeStatus.PENDING;
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

    public Long getId() { return id; }
    public User getAuthor() { return author; }
    public String getTitle() { return title; }
    public String getCategory() { return category; }
    public String getDescription() { return description; }
    public String getCity() { return city; }
    public InitiativeStatus getStatus() { return status; }
    public String getModerationReason() { return moderationReason; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public String getContactInfo() { return contactInfo; }

    public void setId(Long id) { this.id = id; }
    public void setAuthor(User author) { this.author = author; }
    public void setTitle(String title) { this.title = title; }
    public void setCategory(String category) { this.category = category; }
    public void setDescription(String description) { this.description = description; }
    public void setCity(String city) { this.city = city; }
    public void setStatus(InitiativeStatus status) { this.status = status; }
    public void setModerationReason(String moderationReason) { this.moderationReason = moderationReason; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public void setContactInfo(String contactInfo) { this.contactInfo = contactInfo; }

    @Override
    public String toString() {
        return id + " | " + title + " | город: " + city + " | автор: " + (author != null ? author.getId() : null) + " | статус: " + status;
    }
}