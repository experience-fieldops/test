package com.experiencefieldops.api.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InspectionRequest {

    @NotNull(message = "Cliente é obrigatório")
    private Long clientId;

    @NotNull(message = "Local é obrigatório")
    private Long siteId;

    @NotNull(message = "Técnico responsável é obrigatório")
    private Long assignedToId;

    @NotNull(message = "Versão do modelo é obrigatória")
    private Long templateVersionId;

    // Opcional — obrigatório somente quando o tipo de inspeção não for local/ambiente (RN-026)
    private Long equipmentId;

    @NotNull(message = "Data prevista é obrigatória")
    @FutureOrPresent(message = "Data prevista não pode ser no passado")
    private LocalDate scheduledDate;
}
