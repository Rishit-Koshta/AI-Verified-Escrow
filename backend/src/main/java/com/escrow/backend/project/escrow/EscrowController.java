package com.escrow.backend.project.escrow;

import com.escrow.backend.project.escrow.deliverable.Deliverable;
import com.escrow.backend.project.escrow.deliverable.DeliverableService;
import com.escrow.backend.project.escrow.dto.*;
import com.escrow.backend.project.escrow.dto.RecordBlockchainTransactionRequest;
import com.escrow.backend.project.escrow.transaction.EscrowTransaction;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/escrows")
public class EscrowController {

    private final EscrowService escrowService;

    private final DeliverableService deliverableService;

    public EscrowController(
            EscrowService escrowService,
            DeliverableService deliverableService
    ) {
        this.escrowService = escrowService;
        this.deliverableService = deliverableService;
    }

    // --------------------------------------------------
    // CREATE ESCROW
    // --------------------------------------------------

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EscrowResponse createEscrow(
            @Valid @RequestBody CreateEscrowRequest request,
            Authentication authentication
    ) {
        Escrow escrow = escrowService.createEscrow(
                request,
                authentication.getName()
        );

        return EscrowResponse.from(escrow);
    }

    // --------------------------------------------------
    // GET MY ESCROWS
    // --------------------------------------------------

    @GetMapping("/my-escrows")
    public List<EscrowResponse> getMyEscrows(
            Authentication authentication
    ) {
        return escrowService.getMyEscrows(authentication.getName())
                .stream()
                .map(EscrowResponse::from)
                .toList();
    }

    // --------------------------------------------------
    // GET ESCROW BY ID
    // --------------------------------------------------

    @GetMapping("/{escrowId}")
    public EscrowResponse getEscrow(
            @PathVariable Long escrowId,
            Authentication authentication
    ) {
        return EscrowResponse.from(
                escrowService.getEscrow(
                        escrowId,
                        authentication.getName()
                )
        );
    }

    // --------------------------------------------------
    // GET ESCROW BY PROJECT ID
    // --------------------------------------------------

    @GetMapping("/project/{projectId}")
    public EscrowResponse getEscrowByProjectId(
            @PathVariable Long projectId,
            Authentication authentication
    ) {
        return EscrowResponse.from(
                escrowService.getEscrowByProjectId(
                        projectId,
                        authentication.getName()
                )
        );
    }

    // --------------------------------------------------
    // GET DELIVERABLES LIST
    // --------------------------------------------------

    @GetMapping("/{escrowId}/deliverables")
    public List<DeliverableResponse> getDeliverables(
            @PathVariable Long escrowId,
            Authentication authentication
    ) {
        return deliverableService.getDeliverables(
                escrowId,
                authentication.getName()
        ).stream().map(DeliverableResponse::from).toList();
    }

    // --------------------------------------------------
    // GET SINGLE DELIVERABLE
    // --------------------------------------------------

    @GetMapping("/{escrowId}/deliverables/{deliverableId}")
    public DeliverableResponse getDeliverable(
            @PathVariable Long escrowId,
            @PathVariable Long deliverableId,
            Authentication authentication
    ) {
        return DeliverableResponse.from(
                deliverableService.getDeliverable(
                        escrowId,
                        deliverableId,
                        authentication.getName()
                )
        );
    }

    // --------------------------------------------------
    // GET TRANSACTIONS
    // --------------------------------------------------

    @GetMapping("/{escrowId}/transactions")
    public List<TransactionResponse> getTransactions(
            @PathVariable Long escrowId,
            Authentication authentication
    ) {
        return escrowService.getTransactions(
                escrowId,
                authentication.getName()
        ).stream().map(TransactionResponse::from).toList();
    }

    // --------------------------------------------------
    // RAISE DISPUTE
    // --------------------------------------------------

    @PostMapping("/{escrowId}/dispute")
    public EscrowResponse raiseDispute(
            @PathVariable Long escrowId,
            @Valid @RequestBody DisputeRequest request,
            Authentication authentication
    ) {
        return EscrowResponse.from(
                escrowService.raiseDispute(
                        escrowId,
                        request,
                        authentication.getName()
                )
        );
    }

    // --------------------------------------------------
    // RECORD A META MASK TRANSACTION
    // --------------------------------------------------

    @PostMapping("/{escrowId}/transactions/record")
    public TransactionResponse recordBlockchainTransaction(
            @PathVariable Long escrowId,
            @Valid @RequestBody RecordBlockchainTransactionRequest request,
            Authentication authentication
    ) {
        EscrowTransaction transaction =
                escrowService.recordBlockchainTransaction(
                        escrowId,
                        request,
                        authentication.getName()
                );

        return TransactionResponse.from(transaction);
    }

    // --------------------------------------------------
    // SUBMIT DELIVERABLE
    // --------------------------------------------------

    @PostMapping("/{escrowId}/submit")
    @ResponseStatus(HttpStatus.CREATED)
    public DeliverableResponse submitDeliverable(
            @PathVariable Long escrowId,
            @Valid @RequestBody SubmitDeliverableRequest request,
            Authentication authentication
    ) {
        Deliverable deliverable = escrowService.submitDeliverable(
                escrowId,
                request,
                authentication.getName()
        );

        return DeliverableResponse.from(deliverable);
    }

    // --------------------------------------------------
    // RETRY AI VERIFICATION
    // --------------------------------------------------

    @PostMapping("/{escrowId}/deliverables/{deliverableId}/retry-verification")
    public DeliverableResponse retryVerification(
            @PathVariable Long escrowId,
            @PathVariable Long deliverableId,
            Authentication authentication
    ) {
        Deliverable deliverable = escrowService.retryVerification(
                escrowId,
                deliverableId,
                authentication.getName()
        );

        return DeliverableResponse.from(deliverable);
    }

    // --------------------------------------------------
    // APPROVE DELIVERABLE
    // --------------------------------------------------

    @PostMapping("/{escrowId}/deliverables/{deliverableId}/approve")
    public DeliverableResponse approveDeliverable(
            @PathVariable Long escrowId,
            @PathVariable Long deliverableId,
            Authentication authentication
    ) {
        Deliverable deliverable = escrowService.approveDeliverable(
                escrowId,
                deliverableId,
                authentication.getName()
        );

        return DeliverableResponse.from(deliverable);
    }

    // --------------------------------------------------
    // REQUEST REVISION
    // --------------------------------------------------

    @PostMapping("/{escrowId}/deliverables/{deliverableId}/revision")
    public DeliverableResponse requestRevision(
            @PathVariable Long escrowId,
            @PathVariable Long deliverableId,
            @Valid @RequestBody RevisionRequest request,
            Authentication authentication
    ) {
        Deliverable deliverable = escrowService.requestRevision(
                escrowId,
                deliverableId,
                request.feedback(),
                authentication.getName()
        );

        return DeliverableResponse.from(deliverable);
    }
}