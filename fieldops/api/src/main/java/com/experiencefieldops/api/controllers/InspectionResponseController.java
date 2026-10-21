package com.experiencefieldops.api.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.experiencefieldops.api.dto.InspectionProgressResponse;
import com.experiencefieldops.api.dto.ResponseRequest;
import com.experiencefieldops.api.dto.ResponseResponse;
import com.experiencefieldops.api.services.InspectionResponseService;
import com.experiencefieldops.api.util.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/inspections/{inspectionId}/responses")
@RequiredArgsConstructor
public class InspectionResponseController {

    private final InspectionResponseService responseService;

    /**
     * POST /api/v1/inspections/{inspectionId}/responses
     * Registra ou atualiza a resposta de um item (upsert por snapshotItemId).
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ResponseResponse>> save(
            @PathVariable Long inspectionId,
            @Valid @RequestBody ResponseRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        responseService.save(inspectionId, request),
                        "Resposta registrada com sucesso"));
    }

    /**
     * GET /api/v1/inspections/{inspectionId}/responses
     * Lista todas as respostas da inspeção.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ResponseResponse>>> findAll(
            @PathVariable Long inspectionId) {

        return ResponseEntity.ok(ApiResponse.success(responseService.findAll(inspectionId)));
    }

    /**
     * GET /api/v1/inspections/{inspectionId}/responses/{responseId}
     * Busca resposta específica.
     */
    @GetMapping("/{responseId}")
    public ResponseEntity<ApiResponse<ResponseResponse>> findById(
            @PathVariable Long inspectionId,
            @PathVariable Long responseId) {

        return ResponseEntity.ok(ApiResponse.success(
                responseService.findById(inspectionId, responseId)));
    }

    /**
     * PUT /api/v1/inspections/{inspectionId}/responses/{responseId}
     * Atualiza resposta existente — bloqueado para inspeções concluídas/aprovadas.
     */
    @PutMapping("/{responseId}")
    public ResponseEntity<ApiResponse<ResponseResponse>> update(
            @PathVariable Long inspectionId,
            @PathVariable Long responseId,
            @Valid @RequestBody ResponseRequest request) {

        return ResponseEntity.ok(ApiResponse.success(
                responseService.update(inspectionId, responseId, request),
                "Resposta atualizada com sucesso"));
    }

    /**
     * GET /api/v1/inspections/{inspectionId}/responses/progress
     * Retorna progresso da inspeção: itens respondidos / aplicáveis.
     */
    @GetMapping("/progress")
    public ResponseEntity<ApiResponse<InspectionProgressResponse>> getProgress(
            @PathVariable Long inspectionId) {

        return ResponseEntity.ok(ApiResponse.success(responseService.getProgress(inspectionId)));
    }
}
