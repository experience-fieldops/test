package com.experiencefieldops.api.dto;

import java.time.LocalDateTime;

import com.experiencefieldops.api.entities.Evidence;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EvidenceResponse {

    private Long id;
    private Long inspectionId;
    private Long inspectionResponseId;
    private Long nonConformityId;
    private String fileKey;
    private String fileUrl;
    private String originalFilename;
    private String contentType;
    private Long fileSizeBytes;
    private LocalDateTime createdAt;

    public static EvidenceResponse from(Evidence evidence, String fileUrl) {
        return EvidenceResponse.builder()
                .id(evidence.getId())
                .inspectionId(evidence.getInspection().getId())
                .inspectionResponseId(evidence.getInspectionResponse() != null
                        ? evidence.getInspectionResponse().getId() : null)
                .nonConformityId(evidence.getNonConformity() != null
                        ? evidence.getNonConformity().getId() : null)
                .fileKey(evidence.getFileKey())
                .fileUrl(fileUrl)
                .originalFilename(evidence.getOriginalFilename())
                .contentType(evidence.getContentType())
                .fileSizeBytes(evidence.getFileSizeBytes())
                .createdAt(evidence.getCreatedAt())
                .build();
    }
}
