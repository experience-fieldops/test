package com.experiencefieldops.api.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InspectionStartRequest {

    // Data/hora capturada no dispositivo do técnico (RN-034)
    @NotNull(message = "Data/hora do dispositivo é obrigatória")
    private LocalDateTime startedAtDevice;
}
