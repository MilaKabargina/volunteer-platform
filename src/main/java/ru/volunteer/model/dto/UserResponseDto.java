package ru.volunteer.model.dto;

import ru.volunteer.model.entity.User;
import ru.volunteer.model.entity.UserProfile;

public class UserResponseDto {
    private long id;
    private String login;
    private String email;
    private String firstName;
    private String secondName;
    private String status;
    private int ratingPoints;

    public UserResponseDto() {}

    public UserResponseDto(long id, String login, String email, String firstName,
                           String secondName, String status, int ratingPoints) {
        this.id = id;
        this.login = login;
        this.email = email;
        this.firstName = firstName;
        this.secondName = secondName;
        this.status = status;
        this.ratingPoints = ratingPoints;
    }

    public static UserResponseDto from(User user) {
        UserProfile profile = user.getProfile();
        return new UserResponseDto(
                user.getId(),
                user.getLogin(),
                user.getEmail(),
                user.getFirstName(),
                user.getSecondName(),
                profile != null && profile.getStatus() != null ? profile.getStatus().name() : null,
                profile != null ? profile.getRatingPoints() : 0
        );
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getSecondName() { return secondName; }
    public void setSecondName(String secondName) { this.secondName = secondName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getRatingPoints() { return ratingPoints; }
    public void setRatingPoints(int ratingPoints) { this.ratingPoints = ratingPoints; }
}