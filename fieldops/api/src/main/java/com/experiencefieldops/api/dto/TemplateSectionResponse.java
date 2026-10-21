package com.experiencefieldops.api.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.experiencefieldops.api.entities.TemplateSection;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TemplateSectionResponse {

    private Long id;
    private Long templateVersionId;
    private String title;
    private String description;
    private Integer displayOrder;
    private LocalDateTime createdAt;
    private List<TemplateItemResponse> items;

    public static TemplateSectionResponse from(TemplateSection section) {
        return TemplateSectionResponse.builder()
                .id(section.getId())
                .templateVersionId(section.getTemplateVersion().getId())
                .title(section.getTitle())
                .description(section.getDescription())
                .displayOrder(section.getDisplayOrder())
                .createdAt(section.getCreatedAt())
                .build();
    }

    public static TemplateSectionResponse from(TemplateSection section, List<TemplateItemResponse> items) {
        return TemplateSectionResponse.builder()
                .id(section.getId())
                .templateVersionId(section.getTemplateVersion().getId())
                .title(section.getTitle())
                .description(section.getDescription())
                .displayOrder(section.getDisplayOrder())
                .createdAt(section.getCreatedAt())
                .items(items)
                .build();
    }
}
