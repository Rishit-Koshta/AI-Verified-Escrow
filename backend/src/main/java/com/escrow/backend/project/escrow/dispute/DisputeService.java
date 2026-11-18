
package com.escrow.backend.project.escrow.dispute;

import com.escrow.backend.project.auth.AppUser;
import com.escrow.backend.project.auth.AuthRepository;
import com.escrow.backend.project.escrow.Escrow;
import com.escrow.backend.project.escrow.EscrowRepository;
import com.escrow.backend.project.escrow.EscrowStatus;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

@Service
public class DisputeService {

    private final DisputeRepository disputeRepository;
    private final EscrowRepository escrowRepository;
    private final AuthRepository authRepository;

    public DisputeService(
            DisputeRepository disputeRepository,
            EscrowRepository escrowRepository,
            AuthRepository authRepository
    ) {
        this.disputeRepository = disputeRepository;
        this.escrowRepository = escrowRepository;
        this.authRepository = authRepository;
    }

    @Transactional
    public Dispute raiseDispute(
            Long escrowId,
            String reason,
            String email
    ) {
        AppUser user = getUser(email);
        Escrow escrow = getEscrow(escrowId);

        boolean isClient =
                escrow.getClient().getId().equals(user.getId());

        boolean isFreelancer =
                escrow.getFreelancer().getId().equals(user.getId());

        if (!isClient && !isFreelancer) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only escrow participants can raise a dispute"
            );
        }

        if (escrow.getStatus() != EscrowStatus.SUBMITTED
                && escrow.getStatus() != EscrowStatus.APPROVED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A dispute cannot be raised in the current escrow state"
            );
        }

        if (disputeRepository.existsByEscrowId(escrowId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A dispute already exists for this escrow"
            );
        }

        Dispute dispute = new Dispute(
                escrow,
                user,
                reason.trim()
        );

        escrow.updateStatus(EscrowStatus.DISPUTED);

        return disputeRepository.save(dispute);
    }

    @Transactional
    public Dispute getDispute(Long escrowId, String email) {
        AppUser user = getUser(email);
        Escrow escrow = getEscrow(escrowId);

        boolean isClient =
                escrow.getClient().getId().equals(user.getId());

        boolean isFreelancer =
                escrow.getFreelancer().getId().equals(user.getId());

        if (!isClient && !isFreelancer) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You cannot view this dispute"
            );
        }

        return disputeRepository.findByEscrowId(escrowId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Dispute not found"
                ));
    }

    private AppUser getUser(String email) {
        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authenticated user email is missing"
            );
        }

        return authRepository.findByEmail(
                email.trim().toLowerCase(Locale.ROOT)
        ).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "User not found"
        ));
    }

    private Escrow getEscrow(Long escrowId) {
        return escrowRepository.findById(escrowId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Escrow not found"
                ));
    }
}
