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

import com.experiencefieldops.api.dto.EquipmentRequest;
import com.experiencefieldops.api.dto.EquipmentResponse;
import com.experiencefieldops.api.dto.EquipmentStatusRequest;
import com.experiencefieldops.api.enums.EquipmentStatus;
import com.experiencefieldops.api.services.EquipmentService;
import com.experiencefieldops.api.util.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class EquipmentController {

    private final EquipmentService equipmentService;

    /**
     * POST /api/v1/sites/{siteId}/equipments
     * Cria um equipamento vinculado ao local. QR Code gerado automaticamente.
     */
    @PostMapping("/api/v1/sites/{siteId}/equipments")
    public ResponseEntity<ApiResponse<EquipmentResponse>> create(
            @PathVariable Long siteId,
            @Valid @RequestBody EquipmentRequest request) {

        EquipmentResponse response = equipmentService.create(siteId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Equipamento criado com sucesso"));
    }

    /**
     * GET /api/v1/sites/{siteId}/equipments
     * Lista equipamentos de um local, paginado, com filtros opcionais.
     */
    @GetMapping("/api/v1/sites/{siteId}/equipments")
    public ResponseEntity<ApiResponse<Page<EquipmentResponse>>> findAllBySite(
            @PathVariable Long siteId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) EquipmentStatus status,
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {

        return ResponseEntity.ok(ApiResponse.success(
                equipmentService.findAllBySite(siteId, name, status, pageable)));
    }

    /**
     * GET /api/v1/equipments/{id}
     * Busca equipamento por ID.
     */
    @GetMapping("/api/v1/equipments/{id}")
    public ResponseEntity<ApiResponse<EquipmentResponse>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(equipmentService.findById(id)));
    }

    /**
     * GET /api/v1/equipments/qrcode/{qrCode}
     * Busca equipamento pelo QR Code (usado pelo app mobile ao escanear).
     */
    @GetMapping("/api/v1/equipments/qrcode/{qrCode}")
    public ResponseEntity<ApiResponse<EquipmentResponse>> findByQrCode(@PathVariable String qrCode) {
        return ResponseEntity.ok(ApiResponse.success(equipmentService.findByQrCode(qrCode)));
    }

    /**
     * PUT /api/v1/equipments/{id}
     * Atualiza nome e descrição do equipamento.
     */
    @PutMapping("/api/v1/equipments/{id}")
    public ResponseEntity<ApiResponse<EquipmentResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody EquipmentRequest request) {

        return ResponseEntity.ok(ApiResponse.success(
                equipmentService.update(id, request), "Equipamento atualizado com sucesso"));
    }

    /**
     * PATCH /api/v1/equipments/{id}/status
     * Altera status: ACTIVE, INACTIVE ou DECOMMISSIONED.
     */
    @PatchMapping("/api/v1/equipments/{id}/status")
    public ResponseEntity<ApiResponse<EquipmentResponse>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody EquipmentStatusRequest request) {

        return ResponseEntity.ok(ApiResponse.success(
                equipmentService.updateStatus(id, request), "Status do equipamento atualizado com sucesso"));
    }
}
