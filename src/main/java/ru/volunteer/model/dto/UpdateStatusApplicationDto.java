package ru.volunteer.model.dto;

import ru.volunteer.model.enums.ApplicationStatus;

public class UpdateStatusApplicationDto {
    private ApplicationStatus status;

    public UpdateStatusApplicationDto() {}

    public UpdateStatusApplicationDto(ApplicationStatus status) {
        this.status = status;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }
}