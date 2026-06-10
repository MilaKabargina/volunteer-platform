package ru.volunteer.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.volunteer.model.entity.Application;
import java.util.List;

@Repository
public interface ApplicationJpaRepository extends JpaRepository<Application, Long> {
    List<Application> findByApplicantId(Long applicantId);
    List<Application> findByInitiativeId(Long initiativeId);
    Page<Application> findByApplicantId(Long applicantId, Pageable pageable);
    Page<Application> findByInitiativeId(Long initiativeId, Pageable pageable);
}