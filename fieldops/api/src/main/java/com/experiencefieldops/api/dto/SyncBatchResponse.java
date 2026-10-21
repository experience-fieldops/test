package com.experiencefieldops.api.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SyncBatchResponse {

    private int total;
    private int succeeded;
    private int failed;
    private int skipped;
    private List<SyncOperationResult> results;
    private LocalDateTime processedAt;
}
