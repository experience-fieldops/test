package com.experiencefieldops.api.dto;

import com.experiencefieldops.api.entities.InspectionItemSnapshot;
import com.experiencefieldops.api.enums.ResponseType;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class InspectionItemSnapshotResponse {

    private Long id;
    private Long originalItemId;
    private String code;
    private String title;
    private String description;
    private ResponseType responseType;
    private Boolean required;
    private Boolean observationRequiredOnFailure;
    private Boolean evidenceRequiredOnFailure;
    private String optionsJson;
    private String sectionTitle;
    private Integer sectionDisplayOrder;
    private Integer itemDisplayOrder;

    public static InspectionItemSnapshotResponse from(InspectionItemSnapshot snapshot) {
        return InspectionItemSnapshotResponse.builder()
                .id(snapshot.getId())
                .originalItemId(snapshot.getOriginalItem() != null ? snapshot.getOriginalItem().getId() : null)
                .code(snapshot.getCode())
                .title(snapshot.getTitle())
                .description(snapshot.getDescription())
                .responseType(snapshot.getResponseType())
                .required(snapshot.getRequired())
                .observationRequiredOnFailure(snapshot.getObservationRequiredOnFailure())
                .evidenceRequiredOnFailure(snapshot.getEvidenceRequiredOnFailure())
                .optionsJson(snapshot.getOptionsJson())
                .sectionTitle(snapshot.getSectionTitle())
                .sectionDisplayOrder(snapshot.getSectionDisplayOrder())
                .itemDisplayOrder(snapshot.getItemDisplayOrder())
                .build();
    }
}
