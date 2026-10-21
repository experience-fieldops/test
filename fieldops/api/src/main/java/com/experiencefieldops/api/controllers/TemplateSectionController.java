package com.experiencefieldops.api.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.experiencefieldops.api.dto.TemplateSectionRequest;
import com.experiencefieldops.api.dto.TemplateSectionResponse;
import com.experiencefieldops.api.services.TemplateSectionService;
import com.experiencefieldops.api.util.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class TemplateSectionController {

    private final TemplateSectionService sectionService;

    /**
     * POST /api/v1/templates/versions/{versionId}/sections
     */
    @PostMapping("/api/v1/templates/versions/{versionId}/sections")
    public ResponseEntity<ApiResponse<TemplateSectionResponse>> create(
            @PathVariable Long versionId,
            @Valid @RequestBody TemplateSectionRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(sectionService.create(versionId, request), "Seção criada com sucesso"));
    }

    /**
     * PUT /api/v1/templates/versions/{versionId}/sections/{sectionId}
     */
    @PutMapping("/api/v1/templates/versions/{versionId}/sections/{sectionId}")
    public ResponseEntity<ApiResponse<TemplateSectionResponse>> update(
            @PathVariable Long versionId,
            @PathVariable Long sectionId,
            @Valid @RequestBody TemplateSectionRequest request) {

        return ResponseEntity.ok(ApiResponse.success(
                sectionService.update(versionId, sectionId, request), "Seção atualizada com sucesso"));
    }

    /**
     * DELETE /api/v1/templates/versions/{versionId}/sections/{sectionId}
     * Apenas em versão rascunho (RN-019).
     */
    @DeleteMapping("/api/v1/templates/versions/{versionId}/sections/{sectionId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long versionId,
            @PathVariable Long sectionId) {

        sectionService.delete(versionId, sectionId);
        return ResponseEntity.noContent().build();
    }
}
