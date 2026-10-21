package com.experiencefieldops.api.enums;

public enum ReviewDecision {
    PENDING,   // Revisão iniciada, aguardando decisão
    APPROVED,  // Inspeção aprovada
    REJECTED   // Inspeção reprovada — técnico deve corrigir
}
