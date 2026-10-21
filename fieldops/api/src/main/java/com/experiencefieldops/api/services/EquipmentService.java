package com.experiencefieldops.api.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.experiencefieldops.api.dto.EquipmentRequest;
import com.experiencefieldops.api.dto.EquipmentResponse;
import com.experiencefieldops.api.dto.EquipmentStatusRequest;
import com.experiencefieldops.api.enums.EquipmentStatus;

public interface EquipmentService {

    EquipmentResponse create(Long siteId, EquipmentRequest request);

    Page<EquipmentResponse> findAllBySite(Long siteId, String name, EquipmentStatus status, Pageable pageable);

    EquipmentResponse findById(Long id);

    EquipmentResponse findByQrCode(String qrCode);

    EquipmentResponse update(Long id, EquipmentRequest request);

    EquipmentResponse updateStatus(Long id, EquipmentStatusRequest request);
}
