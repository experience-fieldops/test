package com.experiencefieldops.api.dto;

import com.experiencefieldops.api.enums.EquipmentStatus;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EquipmentStatusRequest {

    @NotNull(message = "Status é obrigatório")
    private EquipmentStatus status;
}
