package ru.volunteer.testutil;

import ru.volunteer.model.entity.*;
import ru.volunteer.model.enums.ApplicationStatus;
import ru.volunteer.model.enums.UserStatus;

import java.util.HashSet;
import java.util.Set;

public class TestDataFactory {

    public static User createTestUser(Long id, String login) {
        User user = new User();
        user.setId(id);
        user.setLogin(login);
        user.setEmail(login + "@test.com");
        user.setPassword("Password123");
        user.setFirstName("Test");
        user.setSecondName("User");
        UserProfile profile = new UserProfile();
        profile.setId(id);
        profile.setRatingPoints(0);
        profile.setStatus(UserStatus.NO_PARTICIPATION);
        profile.setUser(user);
        user.setProfile(profile);
        Role userRole = new Role();
        userRole.setId(1L);
        userRole.setName("ROLE_USER");
        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        user.setRoles(roles);
        return user;
    }

    public static User createTestUser(Long id, String login, String firstName, String secondName, int rating) {
        User user = createTestUser(id, login);
        user.setFirstName(firstName);
        user.setSecondName(secondName);
        if (user.getProfile() != null) {
            user.getProfile().setRatingPoints(rating);
            user.getProfile().setStatus(getStatusByRating(rating));
        }
        return user;
    }

    public static Initiative createTestInitiative(Long id, User author, String title, String category) {
        Initiative initiative = new Initiative();
        initiative.setId(id);
        initiative.setTitle(title);
        initiative.setCategory(category);
        initiative.setDescription("Тестовое описание для " + title);
        initiative.setAuthor(author);
        return initiative;
    }

    public static Application createTestApplication(Long id, User applicant, Initiative initiative, ApplicationStatus status) {
        Application application = new Application();
        application.setId(id);
        application.setApplicant(applicant);
        application.setInitiative(initiative);
        application.setStatus(status);
        application.setMessage("Тестовое сообщение");
        return application;
    }

    private static UserStatus getStatusByRating(int rating) {
        if (rating == 0) return UserStatus.NO_PARTICIPATION;
        if (rating <= 3) return UserStatus.BEGINNER;
        if (rating <= 7) return UserStatus.INTERMEDIATE;
        return UserStatus.ADVANCED;
    }

    public static Role createTestRole(String name) {
        Role role = new Role();
        role.setId(1L);
        role.setName(name);
        return role;
    }
}