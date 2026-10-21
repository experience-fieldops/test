package com.experiencefieldops.api.services.impl;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.experiencefieldops.api.dto.EquipmentRequest;
import com.experiencefieldops.api.dto.EquipmentResponse;
import com.experiencefieldops.api.dto.EquipmentStatusRequest;
import com.experiencefieldops.api.entities.Equipment;
import com.experiencefieldops.api.entities.InspectionSite;
import com.experiencefieldops.api.enums.EquipmentStatus;
import com.experiencefieldops.api.enums.SiteStatus;
import com.experiencefieldops.api.exception.ResourceNotFoundException;
import com.experiencefieldops.api.repositories.EquipmentRepository;
import com.experiencefieldops.api.repositories.InspectionSiteRepository;
import com.experiencefieldops.api.services.EquipmentService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EquipmentServiceImpl implements EquipmentService {

    private final EquipmentRepository equipmentRepository;
    private final InspectionSiteRepository siteRepository;

    @Override
    @Transactional
    public EquipmentResponse create(Long siteId, EquipmentRequest request) {
        InspectionSite site = findActiveSite(siteId);

        // Gera QR Code único como UUID — garante unicidade sem input do usuário
        String qrCode = generateUniqueQrCode();

        Equipment equipment = Equipment.builder()
                .name(request.getName())
                .description(request.getDescription())
                .qrCode(qrCode)
                .status(EquipmentStatus.ACTIVE)
                .site(site)
                .build();

        return EquipmentResponse.from(equipmentRepository.save(equipment));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EquipmentResponse> findAllBySite(Long siteId, String name, EquipmentStatus status, Pageable pageable) {
        if (!siteRepository.existsById(siteId)) {
            throw new ResourceNotFoundException("InspectionSite", siteId);
        }

        Specification<Equipment> spec = Specification
                .where(notDeleted())
                .and(bySite(siteId))
                .and(hasName(name))
                .and(hasStatus(status));

        return equipmentRepository.findAll(spec, pageable).map(EquipmentResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public EquipmentResponse findById(Long id) {
        return EquipmentResponse.from(findActiveById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public EquipmentResponse findByQrCode(String qrCode) {
        Equipment equipment = equipmentRepository.findByQrCodeAndDeletedAtIsNull(qrCode)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment com qrCode " + qrCode + " não encontrado"));
        return EquipmentResponse.from(equipment);
    }

    @Override
    @Transactional
    public EquipmentResponse update(Long id, EquipmentRequest request) {
        Equipment equipment = findActiveById(id);
        equipment.setName(request.getName());
        equipment.setDescription(request.getDescription());
        return EquipmentResponse.from(equipmentRepository.save(equipment));
    }

    @Override
    @Transactional
    public EquipmentResponse updateStatus(Long id, EquipmentStatusRequest request) {
        Equipment equipment = findActiveById(id);
        equipment.setStatus(request.getStatus());
        return EquipmentResponse.from(equipmentRepository.save(equipment));
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private InspectionSite findActiveSite(Long siteId) {
        InspectionSite site = siteRepository.findByIdAndDeletedAtIsNull(siteId)
                .orElseThrow(() -> new ResourceNotFoundException("InspectionSite", siteId));

        if (site.getStatus() == SiteStatus.INACTIVE) {
            throw new IllegalArgumentException("Não é possível criar equipamento para local inativo");
        }
        return site;
    }

    private Equipment findActiveById(Long id) {
        return equipmentRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment", id));
    }

    private String generateUniqueQrCode() {
        String qrCode;
        do {
            qrCode = UUID.randomUUID().toString();
        } while (equipmentRepository.existsByQrCode(qrCode));
        return qrCode;
    }

    private Specification<Equipment> notDeleted() {
        return (root, query, cb) -> cb.isNull(root.get("deletedAt"));
    }

    private Specification<Equipment> bySite(Long siteId) {
        return (root, query, cb) -> cb.equal(root.get("site").get("id"), siteId);
    }

    private Specification<Equipment> hasName(String name) {
        if (name == null || name.isBlank()) return null;
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
    }

    private Specification<Equipment> hasStatus(EquipmentStatus status) {
        if (status == null) return null;
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }
}
