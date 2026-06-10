package ru.volunteer.model.enums;

public enum ApplicationStatus {
    PENDING("На рассмотрении"),
    APPROVED("Принят"),
    REJECTED("Отклонён"),
    COMPLETED("Завершён");

    private final String displayName;

    ApplicationStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}