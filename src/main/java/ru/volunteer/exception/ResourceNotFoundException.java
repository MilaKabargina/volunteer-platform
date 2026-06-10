package ru.volunteer.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resourceName, long id) {
        super(String.format("%s with ID %d not found", resourceName, id));
    }
}