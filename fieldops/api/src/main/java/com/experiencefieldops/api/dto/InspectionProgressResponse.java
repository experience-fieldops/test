package com.experiencefieldops.api.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class InspectionProgressResponse {

    private Long inspectionId;
    private int totalItems;
    private int answeredItems;
    private int notApplicableItems;
    private int applicableItems;       // totalItems - notApplicableItems
    private int pendingItems;          // applicableItems - answeredItems
    private double progressPercentage; // (answeredItems / applicableItems) * 100
}
