package com.escrow.backend.project.escrow.blockchain;

import com.escrow.backend.project.escrow.exception.EscrowOperationException;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Profile("!blockchain")
public class UnavailableBlockchainEscrowGateway
        implements BlockchainEscrowGateway {

    private EscrowOperationException unavailable() {
        return new EscrowOperationException(
                "Blockchain integration is not configured yet"
        );
    }

    @Override
    public BlockchainTransactionResult deposit(
            Long escrowId,
            BigDecimal amount,
            String currency,
            String freelancerWalletAddress
    ) {
        throw unavailable();
    }

    @Override
    public BlockchainTransactionResult release(
            Long escrowId,
            String freelancerWalletAddress
    ) {
        throw unavailable();
    }

    @Override
    public BlockchainTransactionResult refund(
            Long escrowId,
            String clientWalletAddress
    ) {
        throw unavailable();
    }

    @Override
    public BlockchainTransactionResult getTransactionStatus(
            String transactionHash
    ) {
        throw unavailable();
    }
}
