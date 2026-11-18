
package com.escrow.backend.project.escrow.deliverable;

import com.escrow.backend.project.auth.AppUser;
import com.escrow.backend.project.auth.AuthRepository;
import com.escrow.backend.project.escrow.Escrow;
import com.escrow.backend.project.escrow.EscrowRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

@Service
public class DeliverableService {

    private final DeliverableRepository deliverableRepository;
    private final EscrowRepository escrowRepository;
    private final AuthRepository authRepository;

    public DeliverableService(
            DeliverableRepository deliverableRepository,
            EscrowRepository escrowRepository,
            AuthRepository authRepository
    ) {
        this.deliverableRepository = deliverableRepository;
        this.escrowRepository = escrowRepository;
        this.authRepository = authRepository;
    }

    @Transactional(readOnly = true)
    public List<Deliverable> getDeliverables(
            Long escrowId,
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
                    "You cannot access these deliverables"
            );
        }

        return deliverableRepository
                .findByEscrowIdOrderByVersionDesc(escrowId);
    }

    @Transactional(readOnly = true)
    public Deliverable getDeliverable(
            Long escrowId,
            Long deliverableId,
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
                    "You cannot access these deliverables"
            );
        }

        return deliverableRepository.findById(deliverableId)
                .filter(d -> d.getEscrow().getId().equals(escrowId))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Deliverable not found"
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

