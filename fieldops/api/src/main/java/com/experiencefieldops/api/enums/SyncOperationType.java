package com.experiencefieldops.api.enums;

/**
 * Tipos de operação suportados na outbox de sincronização.
 * A ordem aqui reflete a precedência de processamento (RN-069).
 */
public enum SyncOperationType {
    START_INSPECTION,       // 1. Iniciar inspeção
    SAVE_RESPONSE,          // 2. Salvar resposta de item
    SAVE_NON_CONFORMITY,    // 3. Registrar não conformidade
    UPLOAD_EVIDENCE,        // 4. Enviar evidência (pode ser separado — RN-078)
    COMPLETE_INSPECTION     // 5. Concluir inspeção (deve ser o último)
}
