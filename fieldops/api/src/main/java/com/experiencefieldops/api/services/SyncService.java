package com.experiencefieldops.api.services;

import com.experiencefieldops.api.dto.SyncBatchRequest;
import com.experiencefieldops.api.dto.SyncBatchResponse;
import com.experiencefieldops.api.dto.SyncStatusResponse;

public interface SyncService {

    SyncBatchResponse processBatch(SyncBatchRequest request, Long technicianId);

    SyncStatusResponse getSyncStatus(Long inspectionId);
}
