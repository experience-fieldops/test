package com.experiencefieldops.api.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.experiencefieldops.api.dto.EvidenceResponse;
import com.experiencefieldops.api.services.EvidenceService;
import com.experiencefieldops.api.util.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class EvidenceController {

    private final EvidenceService evidenceService;

    /**
     * POST /api/v1/inspections/{id}/evidences
     * Upload de evidência vinculada à inspeção.
     * responseId e nonConformityId são opcionais.
     * TODO: uploadedByUserId virá do JWT quando auth estiver pronto.
     */
    @PostMapping(value = "/api/v1/inspections/{id}/evidences",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<EvidenceResponse>> upload(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) Long responseId,
            @RequestParam(required = false) Long nonConformityId,
            @RequestParam Long uploadedByUserId) {

        EvidenceResponse response = evidenceService.upload(id, responseId, nonConformityId,
                file, uploadedByUserId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Evidência enviada com sucesso"));
    }

    /**
     * GET /api/v1/inspections/{id}/evidences
     * Lista evidências ativas da inspeção.
     */
    @GetMapping("/api/v1/inspections/{id}/evidences")
    public ResponseEntity<ApiResponse<List<EvidenceResponse>>> findAll(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(evidenceService.findAll(id)));
    }

    /**
     * GET /api/v1/evidences/{id}
     * Busca metadados de uma evidência pelo ID.
     */
    @GetMapping("/api/v1/evidences/{id}")
    public ResponseEntity<ApiResponse<EvidenceResponse>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(evidenceService.findById(id)));
    }

    /**
     * DELETE /api/v1/evidences/{id}
     * Soft delete auditado — remove arquivo do storage e registra quem deletou (RN-048).
     * Bloqueado para inspeções aprovadas (RN-049).
     * TODO: deletedByUserId e reason virão do JWT/body quando auth estiver pronto.
     */
    @DeleteMapping("/api/v1/evidences/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id,
            @RequestParam Long deletedByUserId,
            @RequestParam(required = false, defaultValue = "") String reason) {

        evidenceService.delete(id, deletedByUserId, reason);
        return ResponseEntity.ok(ApiResponse.success("Evidência removida com sucesso"));
    }
}
