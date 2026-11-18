package com.escrow.backend.project.escrow.deliverable;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeliverableRepository
        extends JpaRepository<Deliverable, Long> {

    List<Deliverable> findByEscrowIdOrderByVersionDesc(Long escrowId);

    Optional<Deliverable> findFirstByEscrowIdOrderByVersionDesc(Long escrowId);

    boolean existsByEscrowIdAndVersion(Long escrowId, Integer version);
}
