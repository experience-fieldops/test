package com.experiencefieldops.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InspectionCancelRequest {

    @NotBlank(message = "Justificativa de cancelamento é obrigatória")
    @Size(max = 500, message = "Justificativa deve ter no máximo 500 caracteres")
    private String reason;
}
