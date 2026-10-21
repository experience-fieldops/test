package com.experiencefieldops.api.controllers;

import java.util.List;

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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.experiencefieldops.api.dto.TemplateRequest;
import com.experiencefieldops.api.dto.TemplateResponse;
import com.experiencefieldops.api.dto.TemplateStatusRequest;
import com.experiencefieldops.api.dto.TemplateVersionResponse;
import com.experiencefieldops.api.enums.TemplateStatus;
import com.experiencefieldops.api.services.InspectionTemplateService;
import com.experiencefieldops.api.util.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/templates")
@RequiredArgsConstructor
public class InspectionTemplateController {

    private final InspectionTemplateService templateService;

    /**
     * POST /api/v1/templates
     * Cria modelo em rascunho (status = DRAFT).
     */
    @PostMapping
    public ResponseEntity<ApiResponse<TemplateResponse>> create(
            @Valid @RequestBody TemplateRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(templateService.create(request), "Modelo criado com sucesso"));
    }

    /**
     * GET /api/v1/templates
     * Lista modelos paginados com filtros opcionais.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<TemplateResponse>>> findAll(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) TemplateStatus status,
            @PageableDefault(size = 20, sort = "title") Pageable pageable) {

        return ResponseEntity.ok(ApiResponse.success(
                templateService.findAll(title, category, status, pageable)));
    }

    /**
     * GET /api/v1/templates/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TemplateResponse>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(templateService.findById(id)));
    }

    /**
     * PUT /api/v1/templates/{id}
     * Atualiza metadados do modelo.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TemplateResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody TemplateRequest request) {

        return ResponseEntity.ok(ApiResponse.success(
                templateService.update(id, request), "Modelo atualizado com sucesso"));
    }

    /**
     * PATCH /api/v1/templates/{id}/status
     * Ativa ou desativa o modelo (RN-022).
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<TemplateResponse>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody TemplateStatusRequest request) {

        return ResponseEntity.ok(ApiResponse.success(
                templateService.updateStatus(id, request), "Status do modelo atualizado com sucesso"));
    }

    /**
     * POST /api/v1/templates/{id}/versions
     * Publica nova versão do modelo (RN-015, RN-020).
     */
    @PostMapping("/{id}/versions")
    public ResponseEntity<ApiResponse<TemplateVersionResponse>> publish(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(templateService.publish(id), "Versão publicada com sucesso"));
    }

    /**
     * GET /api/v1/templates/{id}/versions
     * Lista todas as versões do modelo, da mais recente para a mais antiga.
     */
    @GetMapping("/{id}/versions")
    public ResponseEntity<ApiResponse<List<TemplateVersionResponse>>> listVersions(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(templateService.listVersions(id)));
    }

    /**
     * GET /api/v1/templates/versions/{versionId}
     * Retorna versão completa com seções e itens ordenados.
     */
    @GetMapping("/versions/{versionId}")
    public ResponseEntity<ApiResponse<TemplateVersionResponse>> getVersion(@PathVariable Long versionId) {
        return ResponseEntity.ok(ApiResponse.success(templateService.getVersionWithSections(versionId)));
    }
}
