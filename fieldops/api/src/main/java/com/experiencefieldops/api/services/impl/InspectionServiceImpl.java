package com.experiencefieldops.api.services.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.experiencefieldops.api.dto.InspectionCancelRequest;
import com.experiencefieldops.api.dto.InspectionCompleteRequest;
import com.experiencefieldops.api.dto.InspectionItemSnapshotResponse;
import com.experiencefieldops.api.dto.InspectionRequest;
import com.experiencefieldops.api.dto.InspectionResponse;
import com.experiencefieldops.api.dto.InspectionStartRequest;
import com.experiencefieldops.api.entities.Client;
import com.experiencefieldops.api.entities.Equipment;
import com.experiencefieldops.api.entities.Inspection;
import com.experiencefieldops.api.entities.InspectionItemSnapshot;
import com.experiencefieldops.api.entities.InspectionSite;
import com.experiencefieldops.api.entities.InspectionTemplateVersion;
import com.experiencefieldops.api.entities.TemplateItem;
import com.experiencefieldops.api.entities.TemplateSection;
import com.experiencefieldops.api.entities.User;
import com.experiencefieldops.api.enums.AuditAction;
import com.experiencefieldops.api.enums.ClientStatus;
import com.experiencefieldops.api.enums.EquipmentStatus;
import com.experiencefieldops.api.enums.InspectionStatus;
import com.experiencefieldops.api.enums.SiteStatus;
import com.experiencefieldops.api.exception.ConflictException;
import com.experiencefieldops.api.exception.ResourceNotFoundException;
import com.experiencefieldops.api.repositories.ClientRepository;
import com.experiencefieldops.api.repositories.EquipmentRepository;
import com.experiencefieldops.api.repositories.InspectionItemSnapshotRepository;
import com.experiencefieldops.api.repositories.InspectionRepository;
import com.experiencefieldops.api.repositories.InspectionSiteRepository;
import com.experiencefieldops.api.repositories.InspectionTemplateVersionRepository;
import com.experiencefieldops.api.repositories.TemplateItemRepository;
import com.experiencefieldops.api.repositories.TemplateSectionRepository;
import com.experiencefieldops.api.repositories.UserRepository;
import com.experiencefieldops.api.services.AuditLogService;
import com.experiencefieldops.api.services.InspectionService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InspectionServiceImpl implements InspectionService {

    private final InspectionRepository inspectionRepository;
    private final InspectionItemSnapshotRepository snapshotRepository;
    private final ClientRepository clientRepository;
    private final InspectionSiteRepository siteRepository;
    private final UserRepository userRepository;
    private final InspectionTemplateVersionRepository versionRepository;
    private final EquipmentRepository equipmentRepository;
    private final TemplateSectionRepository sectionRepository;
    private final TemplateItemRepository itemRepository;
    private final AuditLogService auditLogService;

    @Override
    @Transactional
    public InspectionResponse create(InspectionRequest request) {
        Client client = findActiveClient(request.getClientId());
        InspectionSite site = findActiveSite(request.getSiteId());
        User technician = findActiveTechnician(request.getAssignedToId());
        InspectionTemplateVersion templateVersion = findPublishedVersion(request.getTemplateVersionId());
        Equipment equipment = resolveEquipment(request.getEquipmentId());

        if (equipment != null && !equipment.getSite().getId().equals(site.getId())) {
            throw new IllegalArgumentException("Equipamento não pertence ao local selecionado (RN-014)");
        }

        Inspection inspection = Inspection.builder()
                .client(client).site(site).assignedTo(technician)
                .templateVersion(templateVersion).equipment(equipment)
                .scheduledDate(request.getScheduledDate())
                .status(InspectionStatus.SCHEDULED)
                .build();

        Inspection saved = inspectionRepository.save(inspection);
        generateSnapshots(saved, templateVersion);

        auditLogService.log(null, "system", AuditAction.CREATE,
                "Inspection", saved.getId(),
                "Criada para técnico id=" + technician.getId());

        List<InspectionItemSnapshotResponse> snapshots = loadSnapshots(saved.getId());
        return InspectionResponse.from(saved, snapshots);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InspectionResponse> findAll(Long technicianId, Long clientId,
            InspectionStatus status, Pageable pageable) {
        Specification<Inspection> spec = Specification
                .where(hasTechnician(technicianId))
                .and(hasClient(clientId))
                .and(hasStatus(status));
        return inspectionRepository.findAll(spec, pageable).map(InspectionResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public InspectionResponse findById(Long id) {
        Inspection inspection = findInspection(id);
        return InspectionResponse.from(inspection, loadSnapshots(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InspectionResponse> findAssigned(Long technicianId, Pageable pageable) {
        return inspectionRepository
                .findAll(Specification.where(hasTechnician(technicianId)), pageable)
                .map(InspectionResponse::from);
    }

    @Override
    @Transactional
    public InspectionResponse start(Long id, InspectionStartRequest request, Long requestingUserId) {
        Inspection inspection = findInspection(id);

        if (inspection.getStatus() != InspectionStatus.SCHEDULED) {
            throw new ConflictException("Somente inspeções agendadas podem ser iniciadas (RN-032). Status: "
                    + inspection.getStatus());
        }
        if (!inspection.getAssignedTo().getId().equals(requestingUserId)) {
            throw new ConflictException("Somente o técnico responsável pode iniciar a inspeção (RN-033)");
        }

        inspection.setStatus(InspectionStatus.IN_PROGRESS);
        inspection.setStartedAtDevice(request.getStartedAtDevice());
        inspection.setStartedAtServer(LocalDateTime.now());

        Inspection saved = inspectionRepository.save(inspection);

        auditLogService.log(requestingUserId, inspection.getAssignedTo().getName(),
                AuditAction.STATUS_CHANGE, "Inspection", id, "Iniciada pelo técnico");

        return InspectionResponse.from(saved);
    }

    @Override
    @Transactional
    public InspectionResponse complete(Long id, InspectionCompleteRequest request, Long requestingUserId) {
        Inspection inspection = findInspection(id);

        if (inspection.getStatus() != InspectionStatus.IN_PROGRESS) {
            throw new ConflictException("Somente inspeções em andamento podem ser concluídas. Status: "
                    + inspection.getStatus());
        }
        if (!inspection.getAssignedTo().getId().equals(requestingUserId)) {
            throw new ConflictException("Somente o técnico responsável pode concluir a inspeção (RN-033)");
        }

        inspection.setStatus(InspectionStatus.COMPLETED);
        inspection.setCompletedAtDevice(request.getCompletedAtDevice());
        inspection.setCompletedAtServer(LocalDateTime.now());

        Inspection saved = inspectionRepository.save(inspection);

        auditLogService.log(requestingUserId, inspection.getAssignedTo().getName(),
                AuditAction.STATUS_CHANGE, "Inspection", id, "Concluída pelo técnico");

        return InspectionResponse.from(saved);
    }

    @Override
    @Transactional
    public InspectionResponse cancel(Long id, InspectionCancelRequest request, Long requestingUserId) {
        Inspection inspection = findInspection(id);

        if (inspection.getStatus() == InspectionStatus.APPROVED) {
            throw new ConflictException("Inspeção aprovada não pode ser cancelada pelo fluxo comum (RN-030)");
        }
        if (inspection.getStatus() == InspectionStatus.CANCELLED) {
            throw new ConflictException("Inspeção já está cancelada");
        }

        User canceller = userRepository.findById(requestingUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", requestingUserId));

        inspection.setStatus(InspectionStatus.CANCELLED);
        inspection.setCancelledBy(canceller);
        inspection.setCancelledAt(LocalDateTime.now());
        inspection.setCancellationReason(request.getReason());

        Inspection saved = inspectionRepository.save(inspection);

        auditLogService.log(requestingUserId, canceller.getName(),
                AuditAction.CANCEL, "Inspection", id,
                "Cancelada — motivo: " + request.getReason());

        return InspectionResponse.from(saved);
    }

    // ── snapshot ──────────────────────────────────────────────────────────────

    private void generateSnapshots(Inspection inspection, InspectionTemplateVersion version) {
        List<TemplateSection> sections = sectionRepository
                .findByTemplateVersionIdOrderByDisplayOrder(version.getId());
        for (TemplateSection section : sections) {
            List<TemplateItem> items = itemRepository.findBySectionIdOrderByDisplayOrder(section.getId());
            for (TemplateItem item : items) {
                snapshotRepository.save(InspectionItemSnapshot.builder()
                        .inspection(inspection).originalItem(item)
                        .code(item.getCode()).title(item.getTitle())
                        .description(item.getDescription()).responseType(item.getResponseType())
                        .required(item.getRequired())
                        .observationRequiredOnFailure(item.getObservationRequiredOnFailure())
                        .evidenceRequiredOnFailure(item.getEvidenceRequiredOnFailure())
                        .optionsJson(item.getOptionsJson())
                        .sectionTitle(section.getTitle())
                        .sectionDisplayOrder(section.getDisplayOrder())
                        .itemDisplayOrder(item.getDisplayOrder())
                        .build());
            }
        }
    }

    private List<InspectionItemSnapshotResponse> loadSnapshots(Long inspectionId) {
        return snapshotRepository
                .findByInspectionIdOrderBySectionDisplayOrderAscItemDisplayOrderAsc(inspectionId)
                .stream().map(InspectionItemSnapshotResponse::from).collect(Collectors.toList());
    }

    // ── validações ────────────────────────────────────────────────────────────

    private Client findActiveClient(Long id) {
        Client client = clientRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client", id));
        if (client.getStatus() == ClientStatus.INACTIVE)
            throw new IllegalArgumentException("Cliente inativo (RN-013)");
        return client;
    }

    private InspectionSite findActiveSite(Long id) {
        InspectionSite site = siteRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("InspectionSite", id));
        if (site.getStatus() == SiteStatus.INACTIVE)
            throw new IllegalArgumentException("Local inativo");
        return site;
    }

    private User findActiveTechnician(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        if (Boolean.FALSE.equals(user.getActive()))
            throw new IllegalArgumentException("Técnico inativo (RN-027)");
        return user;
    }

    private InspectionTemplateVersion findPublishedVersion(Long id) {
        InspectionTemplateVersion v = versionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TemplateVersion", id));
        if (v.getPublishedAt() == null)
            throw new IllegalArgumentException("Versão do modelo não publicada (RN-024)");
        if (Boolean.FALSE.equals(v.getActiveForNewInspections()))
            throw new IllegalArgumentException("Versão desativada para novos agendamentos (RN-024)");
        return v;
    }

    private Equipment resolveEquipment(Long id) {
        if (id == null) return null;
        Equipment eq = equipmentRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment", id));
        if (eq.getStatus() == EquipmentStatus.INACTIVE || eq.getStatus() == EquipmentStatus.DECOMMISSIONED)
            throw new IllegalArgumentException("Equipamento inativo (RN-012)");
        return eq;
    }

    private Inspection findInspection(Long id) {
        return inspectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inspection", id));
    }

    private Specification<Inspection> hasTechnician(Long id) {
        if (id == null) return null;
        return (root, query, cb) -> cb.equal(root.get("assignedTo").get("id"), id);
    }

    private Specification<Inspection> hasClient(Long id) {
        if (id == null) return null;
        return (root, query, cb) -> cb.equal(root.get("client").get("id"), id);
    }

    private Specification<Inspection> hasStatus(InspectionStatus status) {
        if (status == null) return null;
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }
}
