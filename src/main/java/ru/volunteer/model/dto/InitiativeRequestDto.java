package ru.volunteer.model.dto;

import jakarta.validation.constraints.NotBlank;

public class InitiativeRequestDto {
    @NotBlank(message = "Название инициативы не может быть пустым")
    private String title;

    private String category;

    @NotBlank(message = "Описание не может быть пустым или состоять только из пробелов")
    private String description;

    @NotBlank(message = "Контактная информация обязательна")
    private String contactInfo;

    @NotBlank(message = "Город обязателен")
    private String city;

    public InitiativeRequestDto() {}

    public InitiativeRequestDto(String title, String category, String description, String contactInfo, String city) {
        this.title = title;
        this.category = category;
        this.description = description;
        this.contactInfo = contactInfo;
        this.city = city;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getContactInfo() { return contactInfo; }
    public void setContactInfo(String contactInfo) { this.contactInfo = contactInfo; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
}