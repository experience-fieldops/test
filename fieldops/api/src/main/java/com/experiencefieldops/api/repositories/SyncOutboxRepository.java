package com.experiencefieldops.api.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.experiencefieldops.api.entities.SyncOutbox;
import com.experiencefieldops.api.enums.SyncStatus;

public interface SyncOutboxRepository extends JpaRepository<SyncOutbox, Long> {

    Optional<SyncOutbox> findByIdempotencyKey(String idempotencyKey);

    List<SyncOutbox> findByInspectionIdOrderByOperationTypeAsc(Long inspectionId);

    List<SyncOutbox> findByInspectionIdAndStatusOrderByOperationTypeAsc(Long inspectionId, SyncStatus status);

    long countByInspectionIdAndStatus(Long inspectionId, SyncStatus status);
}
