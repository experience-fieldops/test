package com.experiencefieldops.api.services.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.experiencefieldops.api.dto.NonConformityRequest;
import com.experiencefieldops.api.dto.NonConformityResponse;
import com.experiencefieldops.api.dto.NonConformityStatusRequest;
import com.experiencefieldops.api.entities.Inspection;
import com.experiencefieldops.api.entities.InspectionItemSnapshot;
import com.experiencefieldops.api.entities.NonConformity;
import com.experiencefieldops.api.enums.InspectionStatus;
import com.experiencefieldops.api.enums.NonConformityStatus;
import com.experiencefieldops.api.enums.Severity;
import com.experiencefieldops.api.exception.ConflictException;
import com.experiencefieldops.api.exception.ForbiddenException;
import com.experiencefieldops.api.exception.ResourceNotFoundException;
import com.experiencefieldops.api.repositories.InspectionItemSnapshotRepository;
import com.experiencefieldops.api.repositories.InspectionRepository;
import com.experiencefieldops.api.repositories.NonConformityRepository;
import com.experiencefieldops.api.services.NonConformityService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NonConformityServiceImpl implements NonConformityService {

    private final NonConformityRepository ncRepository;
    private final InspectionRepository inspectionRepository;
    private final InspectionItemSnapshotRepository snapshotRepository;

    @Override
    @Transactional
    public NonConformityResponse create(Long inspectionId, NonConformityRequest request) {
        Inspection inspection = findEditableInspection(inspectionId);

        validateCritical(request.getSeverity(), request.getDescription(), request.getEvidenceAttached());

        InspectionItemSnapshot snapshotItem = resolveSnapshotItem(inspectionId, request.getSnapshotItemId());

        NonConformity nc = NonConformity.builder()
                .inspection(inspection)
                .snapshotItem(snapshotItem)
                .title(request.getTitle())
                .description(request.getDescription())
                .severity(request.getSeverity())
                .status(NonConformityStatus.OPEN)
                .evidenceAttached(Boolean.TRUE.equals(request.getEvidenceAttached()))
                .build();

        return NonConformityResponse.from(ncRepository.save(nc));
    }

    @Override
    @Transactional(readOnly = true)
    public List<NonConformityResponse> findAll(Long inspectionId) {
        findInspection(inspectionId);
        return ncRepository.findByInspectionIdOrderByCreatedAtDesc(inspectionId)
                .stream()
                .map(NonConformityResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public NonConformityResponse findById(Long id) {
        return NonConformityResponse.from(findNc(id));
    }

    @Override
    @Transactional
    public NonConformityResponse update(Long id, NonConformityRequest request) {
        NonConformity nc = findNc(id);
        assertEditable(nc);

        validateCritical(request.getSeverity(), request.getDescription(), request.getEvidenceAttached());

        InspectionItemSnapshot snapshotItem = resolveSnapshotItem(
                nc.getInspection().getId(), request.getSnapshotItemId());

        nc.setTitle(request.getTitle());
        nc.setDescription(request.getDescription());
        nc.setSeverity(request.getSeverity());
        nc.setSnapshotItem(snapshotItem);
        nc.setEvidenceAttached(Boolean.TRUE.equals(request.getEvidenceAttached()));

        return NonConformityResponse.from(ncRepository.save(nc));
    }

    @Override
    @Transactional
    public NonConformityResponse updateStatus(Long id, NonConformityStatusRequest request) {
        NonConformity nc = findNc(id);

        // DISMISSED exige justificativa (RN-056)
        if (request.getStatus() == NonConformityStatus.DISMISSED) {
            if (request.getDismissalReason() == null || request.getDismissalReason().isBlank()) {
                throw new IllegalArgumentException(
                        "Justificativa obrigatória ao descartar uma não conformidade (RN-056)");
            }
            nc.setDismissalReason(request.getDismissalReason());
        }

        // NC crítica só pode ir para RESOLVED se já tiver evidência (RN-055)
        if (nc.getSeverity() == Severity.CRITICAL
                && request.getStatus() == NonConformityStatus.RESOLVED
                && !Boolean.TRUE.equals(nc.getEvidenceAttached())) {
            throw new IllegalArgumentException(
                    "Não conformidade crítica exige evidência antes de ser resolvida (RN-055)");
        }

        nc.setStatus(request.getStatus());
        return NonConformityResponse.from(ncRepository.save(nc));
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    /**
     * NC crítica exige descrição e evidência (RN-055).
     */
    private void validateCritical(Severity severity, String description, Boolean evidenceAttached) {
        if (severity == Severity.CRITICAL) {
            if (description == null || description.isBlank()) {
                throw new IllegalArgumentException(
                        "Não conformidade crítica exige descrição obrigatória (RN-055)");
            }
            if (!Boolean.TRUE.equals(evidenceAttached)) {
                throw new IllegalArgumentException(
                        "Não conformidade crítica exige ao menos uma evidência (RN-055)");
            }
        }
    }

    /**
     * NC só pode ser editada enquanto a inspeção estiver em andamento.
     * Inspeção aprovada → 403; demais estados finais → 409.
     */
    private void assertEditable(NonConformity nc) {
        InspectionStatus status = nc.getInspection().getStatus();
        if (status == InspectionStatus.APPROVED) {
            throw new ForbiddenException("Não conformidade de inspeção aprovada é somente leitura (RN-082)");
        }
        if (status == InspectionStatus.CANCELLED) {
            throw new ConflictException("Não é possível editar NC de inspeção cancelada");
        }
    }

    private Inspection findEditableInspection(Long inspectionId) {
        Inspection inspection = findInspection(inspectionId);
        if (inspection.getStatus() == InspectionStatus.APPROVED) {
            throw new ForbiddenException("Inspeção aprovada não aceita novas não conformidades (RN-082)");
        }
        if (inspection.getStatus() == InspectionStatus.CANCELLED) {
            throw new ConflictException("Inspeção cancelada não aceita não conformidades");
        }
        return inspection;
    }

    private Inspection findInspection(Long inspectionId) {
        return inspectionRepository.findById(inspectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Inspection", inspectionId));
    }

    private NonConformity findNc(Long id) {
        return ncRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("NonConformity", id));
    }

    private InspectionItemSnapshot resolveSnapshotItem(Long inspectionId, Long snapshotItemId) {
        if (snapshotItemId == null) return null;
        return snapshotRepository.findById(snapshotItemId)
                .filter(s -> s.getInspection().getId().equals(inspectionId))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "InspectionItemSnapshot " + snapshotItemId + " não pertence à inspeção " + inspectionId));
    }
}
