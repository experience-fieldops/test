package com.experiencefieldops.api.dto;

import java.time.LocalDateTime;

import com.experiencefieldops.api.entities.Equipment;
import com.experiencefieldops.api.enums.EquipmentStatus;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EquipmentResponse {

    private Long id;
    private String name;
    private String description;
    private String qrCode;
    private EquipmentStatus status;
    private Long siteId;
    private String siteName;
    private Long version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static EquipmentResponse from(Equipment equipment) {
        return EquipmentResponse.builder()
                .id(equipment.getId())
                .name(equipment.getName())
                .description(equipment.getDescription())
                .qrCode(equipment.getQrCode())
                .status(equipment.getStatus())
                .siteId(equipment.getSite().getId())
                .siteName(equipment.getSite().getName())
                .version(equipment.getVersion())
                .createdAt(equipment.getCreatedAt())
                .updatedAt(equipment.getUpdatedAt())
                .build();
    }
}
