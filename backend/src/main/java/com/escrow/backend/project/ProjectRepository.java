package com.escrow.backend.project;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository
        extends JpaRepository<Project, Long> {

    List<Project> findByStatusOrderByCreatedAtDesc(
            ProjectStatus status);

    List<Project> findByClientIdOrderByCreatedAtDesc(
            Long clientId);

    List<Project> findByFreelancerIdOrderByCreatedAtDesc(
            Long freelancerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Project p WHERE p.id = :id")
    Optional<Project> findByIdForUpdate(@Param("id") Long id);
}
