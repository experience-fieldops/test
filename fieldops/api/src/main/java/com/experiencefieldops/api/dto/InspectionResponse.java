package com.experiencefieldops.api.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.experiencefieldops.api.entities.Inspection;
import com.experiencefieldops.api.enums.InspectionStatus;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class InspectionResponse {

    private Long id;
    private Long clientId;
    private String clientName;
    private Long siteId;
    private String siteName;
    private Long assignedToId;
    private String assignedToName;
    private Long templateVersionId;
    private Integer templateVersionNumber;
    private String templateTitle;
    private Long equipmentId;
    private String equipmentName;
    private LocalDate scheduledDate;
    private InspectionStatus status;
    private LocalDateTime startedAtDevice;
    private LocalDateTime startedAtServer;
    private LocalDateTime completedAtDevice;
    private LocalDateTime completedAtServer;
    private Long cancelledById;
    private LocalDateTime cancelledAt;
    private String cancellationReason;
    private Long version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Snapshot dos itens — presente apenas no GET /{id}
    private List<InspectionItemSnapshotResponse> itemSnapshots;

    public static InspectionResponse from(Inspection inspection) {
        return InspectionResponse.builder()
                .id(inspection.getId())
                .clientId(inspection.getClient().getId())
                .clientName(inspection.getClient().getName())
                .siteId(inspection.getSite().getId())
                .siteName(inspection.getSite().getName())
                .assignedToId(inspection.getAssignedTo().getId())
                .assignedToName(inspection.getAssignedTo().getName())
                .templateVersionId(inspection.getTemplateVersion().getId())
                .templateVersionNumber(inspection.getTemplateVersion().getVersionNumber())
                .templateTitle(inspection.getTemplateVersion().getTitleSnapshot())
                .equipmentId(inspection.getEquipment() != null ? inspection.getEquipment().getId() : null)
                .equipmentName(inspection.getEquipment() != null ? inspection.getEquipment().getName() : null)
                .scheduledDate(inspection.getScheduledDate())
                .status(inspection.getStatus())
                .startedAtDevice(inspection.getStartedAtDevice())
                .startedAtServer(inspection.getStartedAtServer())
                .completedAtDevice(inspection.getCompletedAtDevice())
                .completedAtServer(inspection.getCompletedAtServer())
                .cancelledById(inspection.getCancelledBy() != null ? inspection.getCancelledBy().getId() : null)
                .cancelledAt(inspection.getCancelledAt())
                .cancellationReason(inspection.getCancellationReason())
                .version(inspection.getVersion())
                .createdAt(inspection.getCreatedAt())
                .updatedAt(inspection.getUpdatedAt())
                .build();
    }

    public static InspectionResponse from(Inspection inspection, List<InspectionItemSnapshotResponse> itemSnapshots) {
        InspectionResponse response = from(inspection);
        return InspectionResponse.builder()
                .id(response.getId())
                .clientId(response.getClientId())
                .clientName(response.getClientName())
                .siteId(response.getSiteId())
                .siteName(response.getSiteName())
                .assignedToId(response.getAssignedToId())
                .assignedToName(response.getAssignedToName())
                .templateVersionId(response.getTemplateVersionId())
                .templateVersionNumber(response.getTemplateVersionNumber())
                .templateTitle(response.getTemplateTitle())
                .equipmentId(response.getEquipmentId())
                .equipmentName(response.getEquipmentName())
                .scheduledDate(response.getScheduledDate())
                .status(response.getStatus())
                .startedAtDevice(response.getStartedAtDevice())
                .startedAtServer(response.getStartedAtServer())
                .completedAtDevice(response.getCompletedAtDevice())
                .completedAtServer(response.getCompletedAtServer())
                .cancelledById(response.getCancelledById())
                .cancelledAt(response.getCancelledAt())
                .cancellationReason(response.getCancellationReason())
                .version(response.getVersion())
                .createdAt(response.getCreatedAt())
                .updatedAt(response.getUpdatedAt())
                .itemSnapshots(itemSnapshots)
                .build();
    }
}
