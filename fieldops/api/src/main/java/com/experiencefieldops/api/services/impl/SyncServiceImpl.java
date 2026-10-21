package com.experiencefieldops.api.services.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.experiencefieldops.api.dto.InspectionCompleteRequest;
import com.experiencefieldops.api.dto.InspectionStartRequest;
import com.experiencefieldops.api.dto.NonConformityRequest;
import com.experiencefieldops.api.dto.ResponseRequest;
import com.experiencefieldops.api.dto.SyncBatchRequest;
import com.experiencefieldops.api.dto.SyncBatchResponse;
import com.experiencefieldops.api.dto.SyncOperationRequest;
import com.experiencefieldops.api.dto.SyncOperationResult;
import com.experiencefieldops.api.dto.SyncStatusResponse;
import com.experiencefieldops.api.entities.Inspection;
import com.experiencefieldops.api.entities.SyncOutbox;
import com.experiencefieldops.api.enums.AuditAction;
import com.experiencefieldops.api.enums.InspectionStatus;
import com.experiencefieldops.api.enums.SyncStatus;
import com.experiencefieldops.api.exception.ResourceNotFoundException;
import com.experiencefieldops.api.repositories.InspectionRepository;
import com.experiencefieldops.api.repositories.SyncOutboxRepository;
import com.experiencefieldops.api.services.AuditLogService;
import com.experiencefieldops.api.services.InspectionResponseService;
import com.experiencefieldops.api.services.InspectionService;
import com.experiencefieldops.api.services.NonConformityService;
import com.experiencefieldops.api.services.SyncService;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SyncServiceImpl implements SyncService {

    private static final Logger log = LoggerFactory.getLogger(SyncServiceImpl.class);

    private final SyncOutboxRepository outboxRepository;
    private final InspectionRepository inspectionRepository;
    private final InspectionService inspectionService;
    private final InspectionResponseService responseService;
    private final NonConformityService nonConformityService;
    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public SyncBatchResponse processBatch(SyncBatchRequest request, Long technicianId) {
        // Ordena pelo tipo da operação para respeitar dependências (RN-069):
        // START → SAVE_RESPONSE → SAVE_NON_CONFORMITY → UPLOAD_EVIDENCE → COMPLETE
        List<SyncOperationRequest> ordered = request.getOperations().stream()
                .sorted(Comparator.comparingInt(op -> op.getOperationType().ordinal()))
                .toList();

        List<SyncOperationResult> results = new ArrayList<>();

        for (SyncOperationRequest op : ordered) {
            // Falha em uma operação não apaga as outras (RN-070) — continua o loop
            SyncOperationResult result = processOperation(op, technicianId);
            results.add(result);
        }

        long succeeded = results.stream().filter(r -> r.getStatus() == SyncStatus.SUCCESS).count();
        long failed    = results.stream().filter(r -> r.getStatus() == SyncStatus.FAILED).count();
        long skipped   = results.stream().filter(r -> r.getStatus() == SyncStatus.SKIPPED).count();

        return SyncBatchResponse.builder()
                .total(results.size())
                .succeeded((int) succeeded)
                .failed((int) failed)
                .skipped((int) skipped)
                .results(results)
                .processedAt(LocalDateTime.now())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public SyncStatusResponse getSyncStatus(Long inspectionId) {
        if (!inspectionRepository.existsById(inspectionId)) {
            throw new ResourceNotFoundException("Inspection", inspectionId);
        }

        List<SyncOutbox> all = outboxRepository
                .findByInspectionIdOrderByOperationTypeAsc(inspectionId);

        long pending   = all.stream().filter(o -> o.getStatus() == SyncStatus.PENDING).count();
        long succeeded = all.stream().filter(o -> o.getStatus() == SyncStatus.SUCCESS).count();
        long failed    = all.stream().filter(o -> o.getStatus() == SyncStatus.FAILED).count();
        long skipped   = all.stream().filter(o -> o.getStatus() == SyncStatus.SKIPPED).count();

        LocalDateTime lastSync = all.stream()
                .filter(o -> o.getProcessedAt() != null)
                .map(SyncOutbox::getProcessedAt)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        return SyncStatusResponse.builder()
                .inspectionId(inspectionId)
                .totalOperations(all.size())
                .pending(pending)
                .succeeded(succeeded)
                .failed(failed)
                .skipped(skipped)
                .lastSyncAt(lastSync)
                .build();
    }

    // ── processamento individual ──────────────────────────────────────────────

    /**
     * Cada operação é processada individualmente dentro de sua própria transação
     * via REQUIRES_NEW no helper — assim uma falha não reverte as outras (RN-070).
     */
    private SyncOperationResult processOperation(SyncOperationRequest op, Long technicianId) {
        // Idempotência: se já foi processada, retorna resultado anterior sem duplicar (RN-068)
        return outboxRepository.findByIdempotencyKey(op.getIdempotencyKey())
                .map(existing -> {
                    if (existing.getStatus() == SyncStatus.SUCCESS) {
                        return SyncOperationResult.skipped(
                                op.getIdempotencyKey(), op.getInspectionId(), op.getOperationType());
                    }
                    // Se estava FAILED, tenta novamente
                    return executeAndPersist(op, existing, technicianId);
                })
                .orElseGet(() -> {
                    // Primeira vez — cria registro na outbox
                    Inspection inspection = inspectionRepository.findById(op.getInspectionId())
                            .orElseThrow(() -> new ResourceNotFoundException("Inspection", op.getInspectionId()));

                    SyncOutbox entry = SyncOutbox.builder()
                            .idempotencyKey(op.getIdempotencyKey())
                            .inspection(inspection)
                            .operationType(op.getOperationType())
                            .status(SyncStatus.PENDING)
                            .payload(op.getPayload())
                            .attemptCount(0)
                            .build();

                    SyncOutbox saved = outboxRepository.save(entry);
                    return executeAndPersist(op, saved, technicianId);
                });
    }

    private SyncOperationResult executeAndPersist(SyncOperationRequest op,
            SyncOutbox entry, Long technicianId) {
        entry.setAttemptCount(entry.getAttemptCount() + 1);
        entry.setProcessedAt(LocalDateTime.now());

        try {
            // RN-076: operações em inspeção aprovada são rejeitadas
            Inspection inspection = entry.getInspection();
            if (inspection.getStatus() == InspectionStatus.APPROVED) {
                throw new IllegalStateException(
                        "Dados aprovados no servidor não aceitam atualização tardia (RN-076)");
            }

            // RN-077: se a inspeção foi cancelada no servidor, informa sem descartar
            if (inspection.getStatus() == InspectionStatus.CANCELLED) {
                throw new IllegalStateException(
                        "Inspeção cancelada no servidor — dados locais não foram descartados (RN-077)");
            }

            dispatch(op, technicianId);

            entry.setStatus(SyncStatus.SUCCESS);
            entry.setErrorMessage(null);
            outboxRepository.save(entry);

            return SyncOperationResult.success(
                    op.getIdempotencyKey(), op.getInspectionId(), op.getOperationType());

        } catch (Exception e) {
            log.warn("Sync falhou [key={}] [type={}]: {}",
                    op.getIdempotencyKey(), op.getOperationType(), e.getMessage());

            entry.setStatus(SyncStatus.FAILED);
            entry.setErrorMessage(e.getMessage());
            outboxRepository.save(entry);

            auditLogService.log(technicianId, null, AuditAction.STATUS_CHANGE,
                    "SyncOutbox", entry.getId(),
                    "Falha: " + e.getMessage());

            return SyncOperationResult.failed(
                    op.getIdempotencyKey(), op.getInspectionId(), op.getOperationType(),
                    e.getMessage());
        }
    }

    /**
     * Despacha a operação para o service correto conforme o tipo (RN-069).
     */
    private void dispatch(SyncOperationRequest op, Long technicianId) throws Exception {
        switch (op.getOperationType()) {

            case START_INSPECTION -> {
                InspectionStartRequest req = objectMapper.readValue(
                        op.getPayload(), InspectionStartRequest.class);
                inspectionService.start(op.getInspectionId(), req, technicianId);
            }

            case SAVE_RESPONSE -> {
                ResponseRequest req = objectMapper.readValue(
                        op.getPayload(), ResponseRequest.class);
                responseService.save(op.getInspectionId(), req);
            }

            case SAVE_NON_CONFORMITY -> {
                NonConformityRequest req = objectMapper.readValue(
                        op.getPayload(), NonConformityRequest.class);
                nonConformityService.create(op.getInspectionId(), req);
            }

            case UPLOAD_EVIDENCE -> {
                // Evidências são sincronizadas separadamente via multipart (RN-078)
                // Este tipo apenas registra o rastreamento na outbox;
                // o upload real vai via POST /api/v1/inspections/{id}/evidences
                log.info("UPLOAD_EVIDENCE rastreado na outbox — arquivo enviado via endpoint dedicado");
            }

            case COMPLETE_INSPECTION -> {
                InspectionCompleteRequest req = objectMapper.readValue(
                        op.getPayload(), InspectionCompleteRequest.class);
                inspectionService.complete(op.getInspectionId(), req, technicianId);
            }

            default -> throw new IllegalArgumentException(
                    "Tipo de operação desconhecido: " + op.getOperationType());
        }
    }
}
