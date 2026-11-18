
package com.escrow.backend.project.escrow.dispute;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DisputeRepository extends JpaRepository<Dispute, Long> {

    Optional<Dispute> findByEscrowId(Long escrowId);

    boolean existsByEscrowId(Long escrowId);
}
