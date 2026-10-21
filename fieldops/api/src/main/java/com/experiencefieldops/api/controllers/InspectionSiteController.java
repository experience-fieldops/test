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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.experiencefieldops.api.dto.SiteRequest;
import com.experiencefieldops.api.dto.SiteResponse;
import com.experiencefieldops.api.dto.SiteStatusRequest;
import com.experiencefieldops.api.enums.SiteStatus;
import com.experiencefieldops.api.services.InspectionSiteService;
import com.experiencefieldops.api.util.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class InspectionSiteController {

    private final InspectionSiteService siteService;

    /**
     * POST /api/v1/clients/{clientId}/sites
     * Cria um local vinculado ao cliente informado.
     */
    @PostMapping("/api/v1/clients/{clientId}/sites")
    public ResponseEntity<ApiResponse<SiteResponse>> create(
            @PathVariable Long clientId,
            @Valid @RequestBody SiteRequest request) {

        SiteResponse response = siteService.create(clientId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Local criado com sucesso"));
    }

    /**
     * GET /api/v1/clients/{clientId}/sites
     * Lista locais de um cliente, com paginação e filtros opcionais.
     */
    @GetMapping("/api/v1/clients/{clientId}/sites")
    public ResponseEntity<ApiResponse<Page<SiteResponse>>> findAllByClient(
            @PathVariable Long clientId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) SiteStatus status,
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {

        return ResponseEntity.ok(ApiResponse.success(
                siteService.findAllByClient(clientId, name, status, pageable)));
    }

    /**
     * GET /api/v1/sites/{id}
     * Busca um local pelo ID.
     */
    @GetMapping("/api/v1/sites/{id}")
    public ResponseEntity<ApiResponse<SiteResponse>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(siteService.findById(id)));
    }

    /**
     * PUT /api/v1/sites/{id}
     * Atualiza os dados de um local.
     */
    @PutMapping("/api/v1/sites/{id}")
    public ResponseEntity<ApiResponse<SiteResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody SiteRequest request) {

        return ResponseEntity.ok(ApiResponse.success(
                siteService.update(id, request), "Local atualizado com sucesso"));
    }

    /**
     * PATCH /api/v1/sites/{id}/status
     * Ativa ou inativa um local.
     */
    @PatchMapping("/api/v1/sites/{id}/status")
    public ResponseEntity<ApiResponse<SiteResponse>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody SiteStatusRequest request) {

        return ResponseEntity.ok(ApiResponse.success(
                siteService.updateStatus(id, request), "Status do local atualizado com sucesso"));
    }
}
