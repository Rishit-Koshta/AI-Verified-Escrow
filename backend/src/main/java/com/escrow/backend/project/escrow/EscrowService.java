package com.escrow.backend.project.escrow;

import com.escrow.backend.project.Project;
import com.escrow.backend.project.ProjectRepository;
import com.escrow.backend.project.ProjectStatus;
import com.escrow.backend.project.ProjectService;
import com.escrow.backend.project.auth.AppUser;
import com.escrow.backend.project.auth.AuthRepository;
import com.escrow.backend.project.auth.UserRole;
import com.escrow.backend.project.escrow.deliverable.Deliverable;
import com.escrow.backend.project.escrow.deliverable.DeliverableRepository;
import com.escrow.backend.project.escrow.deliverable.DeliverableStatus;
import com.escrow.backend.project.escrow.dispute.DisputeService;
import com.escrow.backend.project.escrow.dto.CreateEscrowRequest;
import com.escrow.backend.project.escrow.dto.DisputeRequest;
import com.escrow.backend.project.escrow.dto.RecordBlockchainTransactionRequest;
import com.escrow.backend.project.escrow.dto.RepositoryVerificationRequest;
import com.escrow.backend.project.escrow.dto.RepositoryVerificationResponse;
import com.escrow.backend.project.escrow.dto.SubmitDeliverableRequest;
import com.escrow.backend.project.escrow.exception.EscrowNotFoundException;
import com.escrow.backend.project.escrow.exception.EscrowOperationException;
import com.escrow.backend.project.escrow.transaction.EscrowTransaction;
import com.escrow.backend.project.escrow.transaction.EscrowTransactionRepository;
import com.escrow.backend.project.escrow.verification.RepositoryVerificationClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

@Service
public class EscrowService {

    private static final Logger log =
            LoggerFactory.getLogger(EscrowService.class);

    private final EscrowRepository escrowRepository;
    private final ProjectRepository projectRepository;
    private final AuthRepository authRepository;
    private final EscrowTransactionRepository transactionRepository;
    private final DeliverableRepository deliverableRepository;
    private final DisputeService disputeService;
    private final ProjectService projectService;
    private final RepositoryVerificationClient verificationClient;
    private final ObjectMapper objectMapper;

    public EscrowService(
            EscrowRepository escrowRepository,
            ProjectRepository projectRepository,
            AuthRepository authRepository,
            EscrowTransactionRepository transactionRepository,
            DeliverableRepository deliverableRepository,
            DisputeService disputeService,
            ProjectService projectService,
            RepositoryVerificationClient verificationClient,
            ObjectMapper objectMapper
    ) {
        this.escrowRepository = escrowRepository;
        this.projectRepository = projectRepository;
        this.authRepository = authRepository;
        this.transactionRepository = transactionRepository;
        this.deliverableRepository = deliverableRepository;
        this.disputeService = disputeService;
        this.projectService = projectService;
        this.verificationClient = verificationClient;
        this.objectMapper = objectMapper;
    }

    // --------------------------------------------------
    // CREATE ESCROW
    // --------------------------------------------------

    @Transactional
    public Escrow createEscrow(
            CreateEscrowRequest request,
            String email
    ) {
        AppUser client = getUser(email);

        requireRole(client, UserRole.CLIENT);

        Project project = projectRepository.findById(request.projectId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Project not found"
                ));

        if (project.getClient() == null
                || !project.getClient().getId().equals(client.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You do not own this project"
            );
        }

        if (project.getStatus() != ProjectStatus.ASSIGNED
                || project.getFreelancer() == null) {
            throw new EscrowOperationException(
                    "An escrow can only be created for an assigned project"
            );
        }

        if (escrowRepository.existsByProjectId(project.getId())) {
            throw new EscrowOperationException(
                    "An escrow already exists for this project"
            );
        }

        BigDecimal amount = project.getBudget();

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new EscrowOperationException(
                    "Project budget must be greater than zero"
            );
        }

        String currency = request.currency().trim().toUpperCase(Locale.ROOT);

        if (!currency.matches("[A-Z]{3}")) {
            throw new EscrowOperationException(
                    "Currency must be a valid three-letter currency code"
            );
        }

        Escrow escrow = new Escrow(
                project,
                client,
                project.getFreelancer(),
                amount,
                currency
        );

        escrow.updateStatus(EscrowStatus.AWAITING_DEPOSIT);

        return escrowRepository.save(escrow);
    }

    // --------------------------------------------------
    // GET ESCROWS
    // --------------------------------------------------

    @Transactional
    public Escrow getEscrow(Long escrowId, String email) {
        AppUser user = getUser(email);
        Escrow escrow = findEscrow(escrowId);

        verifyParticipant(escrow, user);

        return escrow;
    }

    @Transactional
    public List<Escrow> getMyEscrows(String email) {
        AppUser user = getUser(email);

        if (user.getRole() == UserRole.CLIENT) {
            return escrowRepository
                    .findByClientIdOrderByCreatedAtDesc(user.getId());
        }

        if (user.getRole() == UserRole.FREELANCER) {
            return escrowRepository
                    .findByFreelancerIdOrderByCreatedAtDesc(user.getId());
        }

        throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Arbitrators cannot access this endpoint"
        );
    }

    @Transactional(readOnly = true)
    public Escrow getEscrowByProjectId(Long projectId, String email) {
        AppUser user = getUser(email);
        Escrow escrow = escrowRepository.findByProjectId(projectId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Escrow not found for project " + projectId
                ));

        verifyParticipant(escrow, user);
        return escrow;
    }

    // --------------------------------------------------
    // RECORD BLOCKCHAIN TRANSACTION
    // --------------------------------------------------

    @Transactional
    public EscrowTransaction recordBlockchainTransaction(
            Long escrowId,
            RecordBlockchainTransactionRequest request,
            String email
    ) {
        AppUser user = getUser(email);
        Escrow escrow = findEscrow(escrowId);

        verifyParticipant(escrow, user);

        // Only the client can perform these three escrow actions.
        verifyClientOwnership(escrow, user);

        // Prevent recording the same transaction more than once.
        if (transactionRepository.existsByTransactionHash(
                request.transactionHash()
        )) {
            // Check if this deposit was recorded previously but escrow was stuck in AWAITING_DEPOSIT
            if (request.type() == com.escrow.backend.project.escrow.transaction.TransactionType.DEPOSIT
                    && escrow.getStatus() == EscrowStatus.AWAITING_DEPOSIT) {
                escrow.setDepositTransaction(request.transactionHash());
                escrow.setBlockchainDetails(request.contractAddress(), request.chainId());
                escrow.updateStatus(EscrowStatus.FUNDED);
                projectService.markProjectInProgress(escrow.getProject().getId());
                escrowRepository.save(escrow);
                return transactionRepository.findByEscrowIdOrderByCreatedAtDesc(escrowId).stream()
                        .filter(t -> request.transactionHash().equalsIgnoreCase(t.getTransactionHash()))
                        .findFirst()
                        .orElseThrow(() -> new EscrowOperationException("This transaction has already been recorded"));
            }
            throw new EscrowOperationException(
                    "This transaction has already been recorded"
            );
        }

        // Validate the expected action for the current escrow state.
        switch (request.type()) {
            case DEPOSIT -> {
                if (escrow.getStatus() != EscrowStatus.AWAITING_DEPOSIT) {
                    throw new EscrowOperationException(
                            "Escrow is not awaiting a deposit"
                    );
                }
            }

            case RELEASE -> {
                if (escrow.getStatus() != EscrowStatus.APPROVED) {
                    throw new EscrowOperationException(
                            "Deliverable must be approved before releasing funds"
                    );
                }
            }

            case REFUND -> {
                if (escrow.getStatus() != EscrowStatus.DISPUTED) {
                    throw new EscrowOperationException(
                            "Only disputed escrows can be refunded"
                    );
                }
            }
        }

        EscrowTransaction transaction = new EscrowTransaction(
                escrow,
                request.type(),
                escrow.getAmount(),
                escrow.getCurrency()
        );

        transaction.markSubmitted(
                request.transactionHash(),
                request.walletAddress(),
                request.contractAddress()
        );

        transaction.markConfirmed(request.blockNumber());

        // Save the deployed contract information.
        escrow.setBlockchainDetails(
                request.contractAddress(),
                request.chainId()
        );

        // Update escrow and project status after the confirmed receipt.
        switch (request.type()) {
            case DEPOSIT -> {
                escrow.setDepositTransaction(request.transactionHash());
                escrow.updateStatus(EscrowStatus.FUNDED);

                projectService.markProjectInProgress(
                        escrow.getProject().getId()
                );
            }

            case RELEASE -> {
                escrow.setReleaseTransaction(request.transactionHash());
                escrow.updateStatus(EscrowStatus.RELEASED);

                projectService.markProjectCompleted(
                        escrow.getProject().getId()
                );
            }

            case REFUND -> {
                escrow.setRefundTransaction(request.transactionHash());
                escrow.updateStatus(EscrowStatus.REFUNDED);

                projectService.markProjectCancelled(
                        escrow.getProject().getId()
                );
            }
        }

        escrowRepository.save(escrow);
        return transactionRepository.save(transaction);
    }

    // --------------------------------------------------
    // SUBMIT DELIVERABLE
    // --------------------------------------------------

    @Transactional
    public Deliverable submitDeliverable(
            Long escrowId,
            SubmitDeliverableRequest request,
            String email
    ) {
        AppUser freelancer = getUser(email);
        requireRole(freelancer, UserRole.FREELANCER);

        Escrow escrow = findEscrow(escrowId);

        verifyFreelancerOwnership(escrow, freelancer);

        if (escrow.getStatus() != EscrowStatus.FUNDED
                && escrow.getStatus() != EscrowStatus.IN_PROGRESS) {
            throw new EscrowOperationException(
                    "Escrow is not accepting deliverables"
            );
        }

        int version = deliverableRepository
                .findFirstByEscrowIdOrderByVersionDesc(escrowId)
                .map(d -> d.getVersion() + 1)
                .orElse(1);

        Deliverable deliverable = new Deliverable(
                escrow,
                freelancer,
                request.description().trim(),
                request.fileUrl().trim(),
                version
        );

        deliverable.setRepositoryUrl(request.repoUrl().trim());

        // Save first so the deliverable exists even if AI verification fails.
        deliverable = deliverableRepository.save(deliverable);

        escrow.updateStatus(EscrowStatus.SUBMITTED);
        escrow = escrowRepository.save(escrow);

        try {
            RepositoryVerificationRequest verificationRequest =
                    new RepositoryVerificationRequest(
                            request.repoUrl().trim(),
                            request.taskDescription().trim(),
                            request.acceptanceCriteria()
                    );

            // One HTTP request to FastAPI.
            RepositoryVerificationResponse result =
                    verificationClient.verify(verificationRequest);

            if (result == null) {
                throw new IllegalStateException(
                        "AI verification returned no result"
                );
            }

            String details = objectMapper.writeValueAsString(result);

            deliverable.setVerificationResult(
                    result.verdict(),
                    result.score(),
                    result.summary(),
                    details
            );

        } catch (Exception ex) {

            log.error(
                    "AI verification failed for escrow {} and deliverable {}",
                    escrowId,
                    deliverable.getId(),
                    ex
            );

            deliverable.markVerificationFailed(
                    "Repository verification could not be completed. "
                            + "Please try again later."
            );
        }

        return deliverableRepository.save(deliverable);
    }

    // --------------------------------------------------
    // RETRY AI VERIFICATION
    // --------------------------------------------------

    @Transactional
    public Deliverable retryVerification(
            Long escrowId,
            Long deliverableId,
            String email
    ) {
        AppUser user = getUser(email);
        Escrow escrow = findEscrow(escrowId);

        verifyParticipant(escrow, user);

        if (escrow.getStatus() != EscrowStatus.SUBMITTED) {
            throw new EscrowOperationException(
                    "Escrow is not awaiting deliverable verification"
            );
        }

        Deliverable deliverable = findDeliverable(
                escrowId,
                deliverableId
        );

        if (deliverable.getStatus() != DeliverableStatus.SUBMITTED) {
            throw new EscrowOperationException(
                    "Only submitted deliverables can be re-verified"
            );
        }

        if (!"FAILED".equalsIgnoreCase(
                deliverable.getVerificationStatus())) {
            throw new EscrowOperationException(
                    "Only failed AI verifications can be retried"
            );
        }

        if (deliverable.getRepoUrl() == null
                || deliverable.getRepoUrl().isBlank()) {
            throw new EscrowOperationException(
                    "Deliverable repository URL is missing"
            );
        }

        deliverable.retryVerification();

        try {
            RepositoryVerificationRequest verificationRequest =
                    new RepositoryVerificationRequest(
                            deliverable.getRepoUrl(),
                            deliverable.getDescription(),
                            List.of(
                                    "The repository must be publicly accessible.",
                                    "The repository must contain relevant source code.",
                                    "The repository must contain project documentation."
                            )
                    );

            RepositoryVerificationResponse result =
                    verificationClient.verify(verificationRequest);

            if (result == null) {
                throw new IllegalStateException(
                        "AI verification returned no result"
                );
            }

            String details = objectMapper.writeValueAsString(result);

            deliverable.setVerificationResult(
                    result.verdict(),
                    result.score(),
                    result.summary(),
                    details
            );

        } catch (Exception ex) {

            log.error(
                    "AI verification retry failed for escrow {} and deliverable {}",
                    escrowId,
                    deliverableId,
                    ex
            );

            deliverable.markVerificationFailed(
                    "Repository verification could not be completed. "
                            + "Please try again later."
            );
        }

        return deliverableRepository.save(deliverable);
    }

    // --------------------------------------------------
    // APPROVE DELIVERABLE
    // --------------------------------------------------

    @Transactional
    public Deliverable approveDeliverable(
            Long escrowId,
            Long deliverableId,
            String email
    ) {
        AppUser client = getUser(email);
        Escrow escrow = findEscrow(escrowId);

        verifyClientOwnership(escrow, client);

        if (escrow.getStatus() != EscrowStatus.SUBMITTED) {
            throw new EscrowOperationException(
                    "Escrow is not awaiting review"
            );
        }

        Deliverable deliverable = findDeliverable(
                escrowId,
                deliverableId
        );

        if (deliverable.getStatus() != DeliverableStatus.SUBMITTED) {
            throw new EscrowOperationException(
                    "Deliverable is not awaiting review"
            );
        }

        // AI verification must finish before client approval.
        if (!"COMPLETED".equalsIgnoreCase(
                deliverable.getVerificationStatus())) {

            throw new EscrowOperationException(
                    "AI verification has not completed. "
                            + "Please retry verification before approval."
            );
        }

        // Do not allow approval of a rejected submission.
        if ("REJECTED".equalsIgnoreCase(deliverable.getAiVerdict())) {

            throw new EscrowOperationException(
                    "AI verification rejected this submission. "
                            + "Review the feedback or request a revision."
            );
        }

        // Only these verdicts are eligible for client review.
        if (!"APPROVED".equalsIgnoreCase(deliverable.getAiVerdict())
                && !"NEEDS_REVIEW".equalsIgnoreCase(
                        deliverable.getAiVerdict())) {

            throw new EscrowOperationException(
                    "AI verification returned an invalid verdict."
            );
        }

        deliverable.approve();
        deliverableRepository.save(deliverable);
        escrow.updateStatus(EscrowStatus.APPROVED);
        escrowRepository.save(escrow);

        return deliverable;
    }

    // --------------------------------------------------
    // REQUEST REVISION
    // --------------------------------------------------

    @Transactional
    public Deliverable requestRevision(
            Long escrowId,
            Long deliverableId,
            String feedback,
            String email
    ) {
        AppUser client = getUser(email);
        Escrow escrow = findEscrow(escrowId);

        verifyClientOwnership(escrow, client);

        if (escrow.getStatus() != EscrowStatus.SUBMITTED) {
            throw new EscrowOperationException(
                    "Escrow is not awaiting review"
            );
        }

        Deliverable deliverable = findDeliverable(
                escrowId,
                deliverableId
        );

        if (deliverable.getStatus() != DeliverableStatus.SUBMITTED) {
            throw new EscrowOperationException(
                    "Deliverable is not awaiting review"
            );
        }

        deliverable.requestRevision(feedback.trim());
        deliverableRepository.save(deliverable);
        escrow.updateStatus(EscrowStatus.IN_PROGRESS);
        escrowRepository.save(escrow);

        return deliverable;
    }

    // --------------------------------------------------
    // DISPUTE
    // --------------------------------------------------

    @Transactional
    public Escrow raiseDispute(
            Long escrowId,
            DisputeRequest request,
            String email
    ) {
        disputeService.raiseDispute(
                escrowId,
                request.reason(),
                email
        );

        deliverableRepository
                .findFirstByEscrowIdOrderByVersionDesc(escrowId)
                .filter(d -> d.getStatus() == DeliverableStatus.SUBMITTED)
                .ifPresent(Deliverable::markDisputed);

        return findEscrow(escrowId);
    }

    // --------------------------------------------------
    // TRANSACTION HISTORY
    // --------------------------------------------------

    @Transactional
    public List<EscrowTransaction> getTransactions(
            Long escrowId,
            String email
    ) {
        AppUser user = getUser(email);
        Escrow escrow = findEscrow(escrowId);

        verifyParticipant(escrow, user);

        return transactionRepository
                .findByEscrowIdOrderByCreatedAtDesc(escrowId);
    }

    // --------------------------------------------------
    // HELPERS
    // --------------------------------------------------

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

    private Escrow findEscrow(Long id) {
        return escrowRepository.findById(id)
                .orElseThrow(() -> new EscrowNotFoundException(id));
    }

    private Deliverable findDeliverable(
            Long escrowId,
            Long deliverableId
    ) {
        return deliverableRepository.findById(deliverableId)
                .filter(d -> d.getEscrow().getId().equals(escrowId))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Deliverable not found for this escrow"
                ));
    }

    private void requireRole(AppUser user, UserRole role) {
        if (user.getRole() != role) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "This operation requires the " + role + " role"
            );
        }
    }

    private void verifyClientOwnership(Escrow escrow, AppUser user) {
        requireRole(user, UserRole.CLIENT);

        if (!escrow.getClient().getId().equals(user.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only the escrow client can perform this operation"
            );
        }
    }

    private void verifyFreelancerOwnership(Escrow escrow, AppUser user) {
        requireRole(user, UserRole.FREELANCER);

        if (!escrow.getFreelancer().getId().equals(user.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only the assigned freelancer can perform this operation"
            );
        }
    }

    private void verifyParticipant(Escrow escrow, AppUser user) {
        boolean client = escrow.getClient().getId().equals(user.getId());
        boolean freelancer =
                escrow.getFreelancer().getId().equals(user.getId());

        if (!client && !freelancer) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You are not a participant in this escrow"
            );
        }
    }
}