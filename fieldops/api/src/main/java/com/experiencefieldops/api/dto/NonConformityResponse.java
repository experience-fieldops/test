package com.experiencefieldops.api.dto;

import java.time.LocalDateTime;

import com.experiencefieldops.api.entities.NonConformity;
import com.experiencefieldops.api.enums.NonConformityStatus;
import com.experiencefieldops.api.enums.Severity;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NonConformityResponse {

    private Long id;
    private Long inspectionId;
    private Long snapshotItemId;
    private String snapshotItemTitle;
    private String title;
    private String description;
    private Severity severity;
    private NonConformityStatus status;
    private String dismissalReason;
    private Boolean evidenceAttached;
    private Long version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static NonConformityResponse from(NonConformity nc) {
        return NonConformityResponse.builder()
                .id(nc.getId())
                .inspectionId(nc.getInspection().getId())
                .snapshotItemId(nc.getSnapshotItem() != null ? nc.getSnapshotItem().getId() : null)
                .snapshotItemTitle(nc.getSnapshotItem() != null ? nc.getSnapshotItem().getTitle() : null)
                .title(nc.getTitle())
                .description(nc.getDescription())
                .severity(nc.getSeverity())
                .status(nc.getStatus())
                .dismissalReason(nc.getDismissalReason())
                .evidenceAttached(nc.getEvidenceAttached())
                .version(nc.getVersion())
                .createdAt(nc.getCreatedAt())
                .updatedAt(nc.getUpdatedAt())
                .build();
    }
}
