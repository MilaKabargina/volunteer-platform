package ru.volunteer.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.volunteer.model.entity.User;
import ru.volunteer.model.entity.UserProfile;
import ru.volunteer.model.enums.UserStatus;
import ru.volunteer.repository.UserJpaRepository;

@Service
public class UserService {
    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UserJpaRepository userRepository;

    public UserService(UserJpaRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public void increaseRating(User user) {
        UserProfile profile = user.getProfile();
        profile.setRatingPoints(profile.getRatingPoints() + 1);
        profile.setStatus(getStatusByRating(profile.getRatingPoints()));
        log.info("Рейтинг пользователя {} увеличен: {}, статус {}",
                user.getLogin(), profile.getRatingPoints(), profile.getStatus());
        userRepository.save(user);
    }

    public UserStatus getStatusByRating(int rating) {
        if (rating <= 0) return UserStatus.NO_PARTICIPATION;
        if (rating <= 3) return UserStatus.BEGINNER;
        if (rating <= 7) return UserStatus.INTERMEDIATE;
        return UserStatus.ADVANCED;
    }
}