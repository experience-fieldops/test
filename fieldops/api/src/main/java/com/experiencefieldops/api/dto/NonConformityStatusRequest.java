package com.experiencefieldops.api.dto;

import com.experiencefieldops.api.enums.NonConformityStatus;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NonConformityStatusRequest {

    @NotNull(message = "Status é obrigatório")
    private NonConformityStatus status;

    // Obrigatório ao DISMISSED (RN-056)
    @Size(max = 500, message = "Justificativa deve ter no máximo 500 caracteres")
    private String dismissalReason;
}
