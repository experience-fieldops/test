package com.experiencefieldops.api.enums;

public enum SyncStatus {
    PENDING,    // Recebida, aguardando processamento
    PROCESSING, // Em processamento
    SUCCESS,    // Processada com sucesso
    FAILED,     // Falhou — detalhes em error_message
    SKIPPED     // Ignorada por ser duplicata (RN-068)
}
