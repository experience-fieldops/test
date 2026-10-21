package com.experiencefieldops.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RejectRequest {

    // Motivo obrigatório na reprovação (RN-080)
    @NotBlank(message = "Motivo da reprovação é obrigatório")
    @Size(max = 1000, message = "Motivo deve ter no máximo 1000 caracteres")
    private String rejectionReason;
}
