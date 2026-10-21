package com.experiencefieldops.api.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SyncStatusResponse {

    private Long inspectionId;
    private long totalOperations;
    private long pending;
    private long succeeded;
    private long failed;
    private long skipped;
    private LocalDateTime lastSyncAt;
}
