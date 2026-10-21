package com.experiencefieldops.api.services.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.experiencefieldops.api.dto.ApproveRequest;
import com.experiencefieldops.api.dto.RejectRequest;
import com.experiencefieldops.api.dto.ReviewRequest;
import com.experiencefieldops.api.dto.ReviewResponse;
import com.experiencefieldops.api.entities.Inspection;
import com.experiencefieldops.api.entities.InspectionReview;
import com.experiencefieldops.api.entities.User;
import com.experiencefieldops.api.enums.AuditAction;
import com.experiencefieldops.api.enums.InspectionStatus;
import com.experiencefieldops.api.enums.ReviewDecision;
import com.experiencefieldops.api.enums.UserRole;
import com.experiencefieldops.api.exception.ConflictException;
import com.experiencefieldops.api.exception.ForbiddenException;
import com.experiencefieldops.api.exception.ResourceNotFoundException;
import com.experiencefieldops.api.repositories.InspectionRepository;
import com.experiencefieldops.api.repositories.InspectionReviewRepository;
import com.experiencefieldops.api.repositories.UserRepository;
import com.experiencefieldops.api.services.AuditLogService;
import com.experiencefieldops.api.services.InspectionReviewService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InspectionReviewServiceImpl implements InspectionReviewService {

    private final InspectionReviewRepository reviewRepository;
    private final InspectionRepository inspectionRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    @Override
    @Transactional
    public ReviewResponse startReview(Long inspectionId, ReviewRequest request, Long reviewerId) {
        Inspection inspection = findInspection(inspectionId);
        User reviewer = findSupervisor(reviewerId);

        if (inspection.getStatus() != InspectionStatus.COMPLETED) {
            throw new ConflictException(
                    "Somente inspeções concluídas podem ser revisadas. Status atual: "
                            + inspection.getStatus());
        }

        if (reviewRepository.existsByInspectionIdAndDecision(inspectionId, ReviewDecision.PENDING)) {
            throw new ConflictException("Já existe uma revisão em andamento para esta inspeção");
        }

        int nextRound = reviewRepository
                .findTopByInspectionIdOrderByRoundNumberDesc(inspectionId)
                .map(r -> r.getRoundNumber() + 1)
                .orElse(1);

        InspectionReview review = InspectionReview.builder()
                .inspection(inspection)
                .reviewer(reviewer)
                .decision(ReviewDecision.PENDING)
                .roundNumber(nextRound)
                .comment(request.getComment())
                .build();

        inspection.setStatus(InspectionStatus.UNDER_REVIEW);
        inspectionRepository.save(inspection);

        InspectionReview saved = reviewRepository.save(review);

        auditLogService.log(reviewer.getId(), reviewer.getName(),
                AuditAction.STATUS_CHANGE, "Inspection", inspectionId,
                "Revisão iniciada — rodada " + nextRound);

        return ReviewResponse.from(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewResponse> findAll(Long inspectionId) {
        findInspection(inspectionId);
        return reviewRepository.findByInspectionIdOrderByRoundNumberDesc(inspectionId)
                .stream().map(ReviewResponse::from).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewResponse findById(Long reviewId) {
        return ReviewResponse.from(findReview(reviewId));
    }

    @Override
    @Transactional
    public ReviewResponse approve(Long reviewId, ApproveRequest request, Long reviewerId) {
        InspectionReview review = findReview(reviewId);
        User reviewer = findSupervisor(reviewerId);

        assertPending(review);
        assertSameReviewer(review, reviewer);

        review.setDecision(ReviewDecision.APPROVED);
        review.setComment(request.getComment());
        review.setDecidedAt(LocalDateTime.now());

        Inspection inspection = review.getInspection();
        inspection.setStatus(InspectionStatus.APPROVED);
        inspectionRepository.save(inspection);

        InspectionReview saved = reviewRepository.save(review);

        auditLogService.log(reviewer.getId(), reviewer.getName(),
                AuditAction.APPROVE, "Inspection", inspection.getId(),
                "Aprovada na rodada " + review.getRoundNumber());

        return ReviewResponse.from(saved);
    }

    @Override
    @Transactional
    public ReviewResponse reject(Long reviewId, RejectRequest request, Long reviewerId) {
        InspectionReview review = findReview(reviewId);
        User reviewer = findSupervisor(reviewerId);

        assertPending(review);
        assertSameReviewer(review, reviewer);

        review.setDecision(ReviewDecision.REJECTED);
        review.setRejectionReason(request.getRejectionReason());
        review.setDecidedAt(LocalDateTime.now());

        Inspection inspection = review.getInspection();
        inspection.setStatus(InspectionStatus.REJECTED);
        inspectionRepository.save(inspection);

        InspectionReview saved = reviewRepository.save(review);

        auditLogService.log(reviewer.getId(), reviewer.getName(),
                AuditAction.REJECT, "Inspection", inspection.getId(),
                "Reprovada na rodada " + review.getRoundNumber()
                        + " — motivo: " + request.getRejectionReason());

        return ReviewResponse.from(saved);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private User findSupervisor(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        if (user.getRole() != UserRole.SUPERVISOR && user.getRole() != UserRole.ADMIN) {
            throw new ForbiddenException(
                    "Somente SUPERVISOR ou ADMIN pode revisar inspeções (RN-079)");
        }
        return user;
    }

    private void assertPending(InspectionReview review) {
        if (review.getDecision() != ReviewDecision.PENDING) {
            throw new ConflictException(
                    "Esta revisão já foi encerrada com decisão: " + review.getDecision());
        }
    }

    private void assertSameReviewer(InspectionReview review, User reviewer) {
        if (!review.getReviewer().getId().equals(reviewer.getId())) {
            throw new ForbiddenException(
                    "Somente o supervisor que iniciou a revisão pode decidir sobre ela");
        }
    }

    private Inspection findInspection(Long id) {
        return inspectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inspection", id));
    }

    private InspectionReview findReview(Long id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("InspectionReview", id));
    }
}
