package com.experiencefieldops.api.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.experiencefieldops.api.dto.ApproveRequest;
import com.experiencefieldops.api.dto.RejectRequest;
import com.experiencefieldops.api.dto.ReviewRequest;
import com.experiencefieldops.api.dto.ReviewResponse;
import com.experiencefieldops.api.services.InspectionReviewService;
import com.experiencefieldops.api.util.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class InspectionReviewController {

    private final InspectionReviewService reviewService;

    /**
     * POST /api/v1/inspections/{id}/reviews
     * Supervisor inicia revisão de inspeção concluída.
     * TODO: reviewerId virá do JWT quando auth estiver pronto.
     */
    @PostMapping("/api/v1/inspections/{id}/reviews")
    public ResponseEntity<ApiResponse<ReviewResponse>> startReview(
            @PathVariable Long id,
            @Valid @RequestBody ReviewRequest request,
            @RequestParam Long reviewerId) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        reviewService.startReview(id, request, reviewerId),
                        "Revisão iniciada com sucesso"));
    }

    /**
     * GET /api/v1/inspections/{id}/reviews
     * Lista histórico completo de revisões da inspeção, da mais recente para a mais antiga.
     */
    @GetMapping("/api/v1/inspections/{id}/reviews")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> findAll(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(reviewService.findAll(id)));
    }

    /**
     * GET /api/v1/reviews/{id}
     * Busca revisão por ID.
     */
    @GetMapping("/api/v1/reviews/{id}")
    public ResponseEntity<ApiResponse<ReviewResponse>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(reviewService.findById(id)));
    }

    /**
     * POST /api/v1/reviews/{id}/approve
     * Supervisor aprova a inspeção (RN-081).
     * Inspeção passa para APPROVED e respostas ficam bloqueadas (RN-082).
     */
    @PostMapping("/api/v1/reviews/{id}/approve")
    public ResponseEntity<ApiResponse<ReviewResponse>> approve(
            @PathVariable Long id,
            @Valid @RequestBody ApproveRequest request,
            @RequestParam Long reviewerId) {

        return ResponseEntity.ok(ApiResponse.success(
                reviewService.approve(id, request, reviewerId),
                "Inspeção aprovada com sucesso"));
    }

    /**
     * POST /api/v1/reviews/{id}/reject
     * Supervisor reprova a inspeção com motivo obrigatório (RN-080).
     * Inspeção passa para REJECTED — técnico deve corrigir e reenviar.
     * Histórico da rodada anterior é preservado (RN-083).
     */
    @PostMapping("/api/v1/reviews/{id}/reject")
    public ResponseEntity<ApiResponse<ReviewResponse>> reject(
            @PathVariable Long id,
            @Valid @RequestBody RejectRequest request,
            @RequestParam Long reviewerId) {

        return ResponseEntity.ok(ApiResponse.success(
                reviewService.reject(id, request, reviewerId),
                "Inspeção reprovada"));
    }
}
