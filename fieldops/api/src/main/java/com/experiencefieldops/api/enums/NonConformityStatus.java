package com.experiencefieldops.api.enums;

public enum NonConformityStatus {
    OPEN,       // Registrada, aguardando tratamento
    RESOLVED,   // Tratada/corrigida
    DISMISSED,  // Descartada com justificativa (substitui exclusão física — RN-056)
    PENDING_EVIDENCE // Crítica aguardando evidência
}
