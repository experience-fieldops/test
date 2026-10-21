package com.experiencefieldops.api.controllers;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.experiencefieldops.api.dto.InspectionCancelRequest;
import com.experiencefieldops.api.dto.InspectionCompleteRequest;
import com.experiencefieldops.api.dto.InspectionRequest;
import com.experiencefieldops.api.dto.InspectionResponse;
import com.experiencefieldops.api.dto.InspectionStartRequest;
import com.experiencefieldops.api.enums.InspectionStatus;
import com.experiencefieldops.api.services.InspectionService;
import com.experiencefieldops.api.util.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/inspections")
@RequiredArgsConstructor
public class InspectionController {

    private final InspectionService inspectionService;

    /**
     * POST /api/v1/inspections
     * Cria e agenda inspeção. Gera snapshot dos itens automaticamente (RN-021).
     */
    @PostMapping
    public ResponseEntity<ApiResponse<InspectionResponse>> create(
            @Valid @RequestBody InspectionRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(inspectionService.create(request), "Inspeção criada com sucesso"));
    }

    /**
     * GET /api/v1/inspections
     * Lista inspeções paginadas com filtros opcionais.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<InspectionResponse>>> findAll(
            @RequestParam(required = false) Long technicianId,
            @RequestParam(required = false) Long clientId,
            @RequestParam(required = false) InspectionStatus status,
            @PageableDefault(size = 20, sort = "scheduledDate") Pageable pageable) {

        return ResponseEntity.ok(ApiResponse.success(
                inspectionService.findAll(technicianId, clientId, status, pageable)));
    }

    /**
     * GET /api/v1/inspections/{id}
     * Busca inspeção por ID com snapshot completo dos itens.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InspectionResponse>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(inspectionService.findById(id)));
    }

    /**
     * GET /api/v1/inspections/assigned
     * Lista inspeções atribuídas ao técnico informado.
     * TODO: quando auth estiver implementado, extrair technicianId do JWT.
     */
    @GetMapping("/assigned")
    public ResponseEntity<ApiResponse<Page<InspectionResponse>>> findAssigned(
            @RequestParam Long technicianId,
            @PageableDefault(size = 20, sort = "scheduledDate") Pageable pageable) {

        return ResponseEntity.ok(ApiResponse.success(
                inspectionService.findAssigned(technicianId, pageable)));
    }

    /**
     * PATCH /api/v1/inspections/{id}/start
     * Técnico inicia a inspeção (SCHEDULED → IN_PROGRESS).
     * TODO: requestingUserId virá do JWT quando auth estiver pronto.
     */
    @PatchMapping("/{id}/start")
    public ResponseEntity<ApiResponse<InspectionResponse>> start(
            @PathVariable Long id,
            @Valid @RequestBody InspectionStartRequest request,
            @RequestParam Long requestingUserId) {

        return ResponseEntity.ok(ApiResponse.success(
                inspectionService.start(id, request, requestingUserId),
                "Inspeção iniciada com sucesso"));
    }

    /**
     * PATCH /api/v1/inspections/{id}/complete
     * Técnico conclui a inspeção (IN_PROGRESS → COMPLETED).
     */
    @PatchMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<InspectionResponse>> complete(
            @PathVariable Long id,
            @Valid @RequestBody InspectionCompleteRequest request,
            @RequestParam Long requestingUserId) {

        return ResponseEntity.ok(ApiResponse.success(
                inspectionService.complete(id, request, requestingUserId),
                "Inspeção concluída com sucesso"));
    }

    /**
     * PATCH /api/v1/inspections/{id}/cancel
     * Cancela inspeção com justificativa obrigatória (RN-029, RN-030).
     */
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<InspectionResponse>> cancel(
            @PathVariable Long id,
            @Valid @RequestBody InspectionCancelRequest request,
            @RequestParam Long requestingUserId) {

        return ResponseEntity.ok(ApiResponse.success(
                inspectionService.cancel(id, request, requestingUserId),
                "Inspeção cancelada"));
    }
}
