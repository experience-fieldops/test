package com.experiencefieldops.api.dto;

import java.time.LocalDateTime;

import com.experiencefieldops.api.entities.InspectionSite;
import com.experiencefieldops.api.enums.SiteStatus;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SiteResponse {

    private Long id;
    private String name;
    private String address;
    private Double latitude;
    private Double longitude;
    private SiteStatus status;
    private Long clientId;
    private String clientName;
    private Long version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static SiteResponse from(InspectionSite site) {
        return SiteResponse.builder()
                .id(site.getId())
                .name(site.getName())
                .address(site.getAddress())
                .latitude(site.getLatitude())
                .longitude(site.getLongitude())
                .status(site.getStatus())
                .clientId(site.getClient().getId())
                .clientName(site.getClient().getName())
                .version(site.getVersion())
                .createdAt(site.getCreatedAt())
                .updatedAt(site.getUpdatedAt())
                .build();
    }
}
