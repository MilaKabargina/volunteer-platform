package ru.volunteer.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ApplicationRequestDto {
    @NotNull(message = "ID инициативы обязателен")
    @Min(value = 1, message = "ID инициативы должен быть положительным")
    private Long idInitiative;

    @NotBlank(message = "Сообщение заявки не может быть пустым")
    private String message;

    public ApplicationRequestDto() {}

    public ApplicationRequestDto(Long idInitiative, String message) {
        this.idInitiative = idInitiative;
        this.message = message;
    }

    public Long getIdInitiative() {
        return idInitiative;
    }

    public void setIdInitiative(Long idInitiative) {
        this.idInitiative = idInitiative;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}