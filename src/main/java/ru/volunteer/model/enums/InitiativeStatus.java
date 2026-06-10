package ru.volunteer.model.enums;

public enum InitiativeStatus {
    PENDING("На модерации"),
    APPROVED("Одобрена"),
    REJECTED("Отклонена");

    private final String displayName;

    InitiativeStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}