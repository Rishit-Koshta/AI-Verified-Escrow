package com.escrow.backend.project.escrow.transaction;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EscrowTransactionRepository
        extends JpaRepository<EscrowTransaction, Long> {

    List<EscrowTransaction> findByEscrowIdOrderByCreatedAtDesc(Long escrowId);

    Optional<EscrowTransaction> findByTransactionHash(String transactionHash);

    boolean existsByTransactionHash(String transactionHash);
}
