package com.experiencefieldops.api.dto;

import com.experiencefieldops.api.enums.SyncOperationType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SyncOperationRequest {

    /**
     * UUID gerado pelo dispositivo — garante idempotência (RN-067).
     * Mesmo valor reenviado nunca cria duplicata (RN-068).
     */
    @NotBlank(message = "idempotencyKey é obrigatório")
    private String idempotencyKey;

    @NotNull(message = "inspectionId é obrigatório")
    private Long inspectionId;

    @NotNull(message = "operationType é obrigatório")
    private SyncOperationType operationType;

    /**
     * Payload JSON da operação.
     * Formato varia por operationType:
     *   START_INSPECTION    → { "startedAtDevice": "2026-09-14T08:00:00" }
     *   SAVE_RESPONSE       → { "snapshotItemId": 1, "value": "true", "observation": null, "notApplicable": false }
     *   SAVE_NON_CONFORMITY → { "title": "...", "severity": "HIGH", ... }
     *   UPLOAD_EVIDENCE     → { "responseId": 1, "nonConformityId": null, "fileKey": "uuid.jpg" }
     *   COMPLETE_INSPECTION → { "completedAtDevice": "2026-09-14T10:00:00" }
     */
    @NotBlank(message = "payload é obrigatório")
    private String payload;
}
