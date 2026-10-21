package com.experiencefieldops.api.dto;

import java.time.LocalDateTime;

import com.experiencefieldops.api.entities.InspectionReview;
import com.experiencefieldops.api.enums.ReviewDecision;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReviewResponse {

    private Long id;
    private Long inspectionId;
    private Long reviewerId;
    private String reviewerName;
    private ReviewDecision decision;
    private Integer roundNumber;
    private String comment;
    private String rejectionReason;
    private LocalDateTime decidedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ReviewResponse from(InspectionReview review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .inspectionId(review.getInspection().getId())
                .reviewerId(review.getReviewer().getId())
                .reviewerName(review.getReviewer().getName())
                .decision(review.getDecision())
                .roundNumber(review.getRoundNumber())
                .comment(review.getComment())
                .rejectionReason(review.getRejectionReason())
                .decidedAt(review.getDecidedAt())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}
