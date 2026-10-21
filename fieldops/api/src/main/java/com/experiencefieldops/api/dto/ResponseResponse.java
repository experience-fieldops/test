package com.experiencefieldops.api.dto;

import java.time.LocalDateTime;

import com.experiencefieldops.api.entities.InspectionResponse;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResponseResponse {

    private Long id;
    private Long inspectionId;
    private Long snapshotItemId;
    private String snapshotItemTitle;
    private String value;
    private String observation;
    private Boolean notApplicable;
    private Long version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ResponseResponse from(InspectionResponse response) {
        return ResponseResponse.builder()
                .id(response.getId())
                .inspectionId(response.getInspection().getId())
                .snapshotItemId(response.getSnapshotItem().getId())
                .snapshotItemTitle(response.getSnapshotItem().getTitle())
                .value(response.getValue())
                .observation(response.getObservation())
                .notApplicable(response.getNotApplicable())
                .version(response.getVersion())
                .createdAt(response.getCreatedAt())
                .updatedAt(response.getUpdatedAt())
                .build();
    }
}
