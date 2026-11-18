package com.escrow.backend.project.application;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ApplicationRepository
        extends JpaRepository<ProjectApplication, Long> {

    List<ProjectApplication> findByProjectIdOrderByAppliedAtDesc(
            Long projectId);

    List<ProjectApplication> findByFreelancerIdOrderByAppliedAtDesc(
            Long freelancerId);

    Optional<ProjectApplication> findByProjectIdAndFreelancerId(
            Long projectId,
            Long freelancerId);

    Optional<ProjectApplication> findByIdAndProjectId(
            Long applicationId,
            Long projectId);

    boolean existsByProjectIdAndFreelancerId(
            Long projectId,
            Long freelancerId);
}
