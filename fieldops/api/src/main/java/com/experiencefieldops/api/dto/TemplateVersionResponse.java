package com.experiencefieldops.api.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.experiencefieldops.api.entities.InspectionTemplateVersion;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TemplateVersionResponse {

    private Long id;
    private Long templateId;
    private Integer versionNumber;
    private String titleSnapshot;
    private String descriptionSnapshot;
    private Boolean activeForNewInspections;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private List<TemplateSectionResponse> sections;

    public static TemplateVersionResponse from(InspectionTemplateVersion version) {
        return TemplateVersionResponse.builder()
                .id(version.getId())
                .templateId(version.getTemplate().getId())
                .versionNumber(version.getVersionNumber())
                .titleSnapshot(version.getTitleSnapshot())
                .descriptionSnapshot(version.getDescriptionSnapshot())
                .activeForNewInspections(version.getActiveForNewInspections())
                .publishedAt(version.getPublishedAt())
                .createdAt(version.getCreatedAt())
                .build();
    }

    public static TemplateVersionResponse from(InspectionTemplateVersion version, List<TemplateSectionResponse> sections) {
        return TemplateVersionResponse.builder()
                .id(version.getId())
                .templateId(version.getTemplate().getId())
                .versionNumber(version.getVersionNumber())
                .titleSnapshot(version.getTitleSnapshot())
                .descriptionSnapshot(version.getDescriptionSnapshot())
                .activeForNewInspections(version.getActiveForNewInspections())
                .publishedAt(version.getPublishedAt())
                .createdAt(version.getCreatedAt())
                .sections(sections)
                .build();
    }
}
