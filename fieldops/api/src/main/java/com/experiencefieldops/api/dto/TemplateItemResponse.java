package com.experiencefieldops.api.dto;

import java.time.LocalDateTime;

import com.experiencefieldops.api.entities.TemplateItem;
import com.experiencefieldops.api.enums.ResponseType;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TemplateItemResponse {

    private Long id;
    private Long sectionId;
    private String code;
    private String title;
    private String description;
    private ResponseType responseType;
    private Boolean required;
    private Boolean observationRequiredOnFailure;
    private Boolean evidenceRequiredOnFailure;
    private String optionsJson;
    private Integer displayOrder;
    private LocalDateTime createdAt;

    public static TemplateItemResponse from(TemplateItem item) {
        return TemplateItemResponse.builder()
                .id(item.getId())
                .sectionId(item.getSection().getId())
                .code(item.getCode())
                .title(item.getTitle())
                .description(item.getDescription())
                .responseType(item.getResponseType())
                .required(item.getRequired())
                .observationRequiredOnFailure(item.getObservationRequiredOnFailure())
                .evidenceRequiredOnFailure(item.getEvidenceRequiredOnFailure())
                .optionsJson(item.getOptionsJson())
                .displayOrder(item.getDisplayOrder())
                .createdAt(item.getCreatedAt())
                .build();
    }
}
