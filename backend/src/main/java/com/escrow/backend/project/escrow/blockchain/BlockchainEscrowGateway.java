package com.escrow.backend.project.escrow.blockchain;

import java.math.BigDecimal;

public interface BlockchainEscrowGateway {

    BlockchainTransactionResult deposit(
            Long escrowId,
            BigDecimal amount,
            String currency,
            String freelancerWalletAddress
    );

    BlockchainTransactionResult release(
            Long escrowId,
            String freelancerWalletAddress
    );

    BlockchainTransactionResult refund(
            Long escrowId,
            String clientWalletAddress
    );

    BlockchainTransactionResult getTransactionStatus(
            String transactionHash
    );
}
