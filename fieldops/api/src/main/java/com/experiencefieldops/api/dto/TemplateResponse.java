package com.experiencefieldops.api.dto;

import java.time.LocalDateTime;

import com.experiencefieldops.api.entities.InspectionTemplate;
import com.experiencefieldops.api.enums.TemplateStatus;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TemplateResponse {

    private Long id;
    private String title;
    private String description;
    private String category;
    private TemplateStatus status;
    private Integer currentVersion;
    private Long version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static TemplateResponse from(InspectionTemplate template) {
        return TemplateResponse.builder()
                .id(template.getId())
                .title(template.getTitle())
                .description(template.getDescription())
                .category(template.getCategory())
                .status(template.getStatus())
                .currentVersion(template.getCurrentVersion())
                .version(template.getVersion())
                .createdAt(template.getCreatedAt())
                .updatedAt(template.getUpdatedAt())
                .build();
    }
}
