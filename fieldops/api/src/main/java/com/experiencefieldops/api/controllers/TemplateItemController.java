package com.experiencefieldops.api.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.experiencefieldops.api.dto.TemplateItemRequest;
import com.experiencefieldops.api.dto.TemplateItemResponse;
import com.experiencefieldops.api.services.TemplateItemService;
import com.experiencefieldops.api.util.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class TemplateItemController {

    private final TemplateItemService itemService;

    /**
     * POST /api/v1/sections/{sectionId}/items
     */
    @PostMapping("/api/v1/sections/{sectionId}/items")
    public ResponseEntity<ApiResponse<TemplateItemResponse>> create(
            @PathVariable Long sectionId,
            @Valid @RequestBody TemplateItemRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(itemService.create(sectionId, request), "Item criado com sucesso"));
    }

    /**
     * PUT /api/v1/sections/{sectionId}/items/{itemId}
     */
    @PutMapping("/api/v1/sections/{sectionId}/items/{itemId}")
    public ResponseEntity<ApiResponse<TemplateItemResponse>> update(
            @PathVariable Long sectionId,
            @PathVariable Long itemId,
            @Valid @RequestBody TemplateItemRequest request) {

        return ResponseEntity.ok(ApiResponse.success(
                itemService.update(sectionId, itemId, request), "Item atualizado com sucesso"));
    }

    /**
     * DELETE /api/v1/sections/{sectionId}/items/{itemId}
     * Apenas em versão rascunho (RN-019).
     */
    @DeleteMapping("/api/v1/sections/{sectionId}/items/{itemId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long sectionId,
            @PathVariable Long itemId) {

        itemService.delete(sectionId, itemId);
        return ResponseEntity.noContent().build();
    }
}
