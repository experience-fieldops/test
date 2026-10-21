package com.experiencefieldops.api.services;

import java.util.List;

import com.experiencefieldops.api.dto.ApproveRequest;
import com.experiencefieldops.api.dto.RejectRequest;
import com.experiencefieldops.api.dto.ReviewRequest;
import com.experiencefieldops.api.dto.ReviewResponse;

public interface InspectionReviewService {

    // Supervisor abre uma nova rodada de revisão
    ReviewResponse startReview(Long inspectionId, ReviewRequest request, Long reviewerId);

    List<ReviewResponse> findAll(Long inspectionId);

    ReviewResponse findById(Long reviewId);

    // Aprova a inspeção — bloqueia edição de respostas (RN-081, RN-082)
    ReviewResponse approve(Long reviewId, ApproveRequest request, Long reviewerId);

    // Reprova a inspeção — motivo obrigatório (RN-080)
    ReviewResponse reject(Long reviewId, RejectRequest request, Long reviewerId);
}
