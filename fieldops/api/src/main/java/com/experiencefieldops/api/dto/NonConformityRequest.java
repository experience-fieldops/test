package com.experiencefieldops.api.dto;

import com.experiencefieldops.api.enums.Severity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NonConformityRequest {

    @NotBlank(message = "Título é obrigatório")
    @Size(max = 255, message = "Título deve ter no máximo 255 caracteres")
    private String title;

    // Obrigatório quando severity = CRITICAL (validado no service — RN-055)
    private String description;

    @NotNull(message = "Severidade é obrigatória")
    private Severity severity;

    // ID do item do snapshot ao qual a NC está associada (opcional — RN-053)
    private Long snapshotItemId;

    // Indica se o técnico já anexou evidência (RN-055)
    private Boolean evidenceAttached;
}
