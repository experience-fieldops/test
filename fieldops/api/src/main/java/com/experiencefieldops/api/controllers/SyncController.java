package com.experiencefieldops.api.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.experiencefieldops.api.dto.SyncBatchRequest;
import com.experiencefieldops.api.dto.SyncBatchResponse;
import com.experiencefieldops.api.dto.SyncStatusResponse;
import com.experiencefieldops.api.services.SyncService;
import com.experiencefieldops.api.util.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class SyncController {

    private final SyncService syncService;

    /**
     * POST /api/v1/sync/batch
     * Recebe e processa lote de operações da outbox do dispositivo.
     *
     * Cada operação tem um idempotencyKey único — reenvios são ignorados (RN-068).
     * Operações são ordenadas por dependência antes do processamento (RN-069).
     * Falha em uma operação não cancela as demais (RN-070).
     * A resposta detalha o resultado individual de cada operação.
     *
     * TODO: technicianId virá do JWT quando auth estiver pronto.
     */
    @PostMapping("/sync/batch")
    public ResponseEntity<ApiResponse<SyncBatchResponse>> processBatch(
            @Valid @RequestBody SyncBatchRequest request,
            @RequestParam Long technicianId) {

        SyncBatchResponse response = syncService.processBatch(request, technicianId);

        // Sempre retorna 200 — o status individual de cada op está no body
        // Isso evita que o app trate um 4xx/5xx como falha total do lote
        return ResponseEntity.ok(ApiResponse.success(response,
                "Lote processado: " + response.getSucceeded() + " ok, "
                        + response.getFailed() + " falhas, "
                        + response.getSkipped() + " ignoradas"));
    }

    /**
     * GET /api/v1/inspections/{id}/sync-status
     * Consulta o status de sincronização de uma inspeção.
     * Retorna contadores de operações pendentes, bem-sucedidas, falhas e ignoradas.
     */
    @GetMapping("/inspections/{id}/sync-status")
    public ResponseEntity<ApiResponse<SyncStatusResponse>> getSyncStatus(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(syncService.getSyncStatus(id)));
    }
}
