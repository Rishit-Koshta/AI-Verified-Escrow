package com.escrow.backend.project.escrow;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EscrowRepository extends JpaRepository<Escrow, Long> {

    Optional<Escrow> findByProjectId(Long projectId);

    List<Escrow> findByClientIdOrderByCreatedAtDesc(Long clientId);

    List<Escrow> findByFreelancerIdOrderByCreatedAtDesc(Long freelancerId);

    List<Escrow> findByStatusOrderByCreatedAtDesc(EscrowStatus status);

    boolean existsByProjectId(Long projectId);
}
