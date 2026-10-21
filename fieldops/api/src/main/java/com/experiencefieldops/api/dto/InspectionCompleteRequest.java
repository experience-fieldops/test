package com.experiencefieldops.api.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InspectionCompleteRequest {

    // Data/hora de conclusão capturada no dispositivo (RN-042)
    @NotNull(message = "Data/hora de conclusão do dispositivo é obrigatória")
    private LocalDateTime completedAtDevice;
}
