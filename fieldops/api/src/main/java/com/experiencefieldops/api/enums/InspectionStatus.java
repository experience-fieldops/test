package com.experiencefieldops.api.enums;

public enum InspectionStatus {
    SCHEDULED,      // Agendada e atribuída ao técnico
    IN_PROGRESS,    // Técnico iniciou
    COMPLETED,      // Técnico concluiu
    UNDER_REVIEW,   // Em revisão pelo supervisor
    APPROVED,       // Aprovada
    REJECTED,       // Reprovada — aguarda correção
    CANCELLED       // Cancelada com justificativa
}
