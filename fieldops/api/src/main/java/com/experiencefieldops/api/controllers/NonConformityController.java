package com.experiencefieldops.api.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.experiencefieldops.api.dto.NonConformityRequest;
import com.experiencefieldops.api.dto.NonConformityResponse;
import com.experiencefieldops.api.dto.NonConformityStatusRequest;
import com.experiencefieldops.api.services.NonConformityService;
import com.experiencefieldops.api.util.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class NonConformityController {

    private final NonConformityService ncService;

    /**
     * POST /api/v1/inspections/{id}/non-conformities
     * Registra nova NC vinculada à inspeção.
     */
    @PostMapping("/api/v1/inspections/{id}/non-conformities")
    public ResponseEntity<ApiResponse<NonConformityResponse>> create(
            @PathVariable Long id,
            @Valid @RequestBody NonConformityRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        ncService.create(id, request),
                        "Não conformidade registrada com sucesso"));
    }

    /**
     * GET /api/v1/inspections/{id}/non-conformities
     * Lista todas as NCs da inspeção, da mais recente para a mais antiga.
     */
    @GetMapping("/api/v1/inspections/{id}/non-conformities")
    public ResponseEntity<ApiResponse<List<NonConformityResponse>>> findAll(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(ncService.findAll(id)));
    }

    /**
     * GET /api/v1/non-conformities/{id}
     * Busca NC por ID.
     */
    @GetMapping("/api/v1/non-conformities/{id}")
    public ResponseEntity<ApiResponse<NonConformityResponse>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(ncService.findById(id)));
    }

    /**
     * PUT /api/v1/non-conformities/{id}
     * Atualiza dados da NC (bloqueado para inspeções aprovadas/canceladas).
     */
    @PutMapping("/api/v1/non-conformities/{id}")
    public ResponseEntity<ApiResponse<NonConformityResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody NonConformityRequest request) {

        return ResponseEntity.ok(ApiResponse.success(
                ncService.update(id, request),
                "Não conformidade atualizada com sucesso"));
    }

    /**
     * PATCH /api/v1/non-conformities/{id}/status
     * Altera status da NC. DISMISSED exige justificativa (RN-056).
     * Substitui a exclusão física pelo fluxo de descarte.
     */
    @PatchMapping("/api/v1/non-conformities/{id}/status")
    public ResponseEntity<ApiResponse<NonConformityResponse>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody NonConformityStatusRequest request) {

        return ResponseEntity.ok(ApiResponse.success(
                ncService.updateStatus(id, request),
                "Status atualizado com sucesso"));
    }
}
