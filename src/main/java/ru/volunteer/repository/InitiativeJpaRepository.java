package ru.volunteer.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.volunteer.model.entity.Initiative;
import ru.volunteer.model.enums.InitiativeStatus;

import java.util.List;

@Repository
public interface InitiativeJpaRepository extends JpaRepository<Initiative, Long> {
    List<Initiative> findByAuthorId(Long authorId);
    List<Initiative> findByCategory(String category);
    Page<Initiative> findByCategory(String category, Pageable pageable);
    List<Initiative> findByStatus(InitiativeStatus status);
    Page<Initiative> findByStatus(InitiativeStatus status, Pageable pageable);
}