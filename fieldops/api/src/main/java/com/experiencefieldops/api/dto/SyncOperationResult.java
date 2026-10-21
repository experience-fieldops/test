package com.experiencefieldops.api.dto;

import com.experiencefieldops.api.enums.SyncOperationType;
import com.experiencefieldops.api.enums.SyncStatus;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SyncOperationResult {

    private String idempotencyKey;
    private Long inspectionId;
    private SyncOperationType operationType;
    private SyncStatus status;
    private String errorMessage;

    public static SyncOperationResult success(String key, Long inspectionId, SyncOperationType type) {
        return SyncOperationResult.builder()
                .idempotencyKey(key)
                .inspectionId(inspectionId)
                .operationType(type)
                .status(SyncStatus.SUCCESS)
                .build();
    }

    public static SyncOperationResult skipped(String key, Long inspectionId, SyncOperationType type) {
        return SyncOperationResult.builder()
                .idempotencyKey(key)
                .inspectionId(inspectionId)
                .operationType(type)
                .status(SyncStatus.SKIPPED)
                .errorMessage("Operação já processada anteriormente (idempotência)")
                .build();
    }

    public static SyncOperationResult failed(String key, Long inspectionId,
            SyncOperationType type, String error) {
        return SyncOperationResult.builder()
                .idempotencyKey(key)
                .inspectionId(inspectionId)
                .operationType(type)
                .status(SyncStatus.FAILED)
                .errorMessage(error)
                .build();
    }
}
