package ru.volunteer.model.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public class ProfileDto {
    @NotBlank(message = "Имя не может быть пустым")
    private String firstName;

    @NotBlank(message = "Фамилия не может быть пустой")
    private String secondName;

    private LocalDate birthDay;

    public ProfileDto() {}

    public ProfileDto(String firstName, String secondName, LocalDate birthDay) {
        this.firstName = firstName;
        this.secondName = secondName;
        this.birthDay = birthDay;
    }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getSecondName() { return secondName; }
    public void setSecondName(String secondName) { this.secondName = secondName; }
    public LocalDate getBirthDay() { return birthDay; }
    public void setBirthDay(LocalDate birthDay) { this.birthDay = birthDay; }
}