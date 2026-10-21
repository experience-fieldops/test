package com.experiencefieldops.api.services.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.experiencefieldops.api.dto.AuditLogResponse;
import com.experiencefieldops.api.entities.AuditLog;
import com.experiencefieldops.api.enums.AuditAction;
import com.experiencefieldops.api.exception.ResourceNotFoundException;
import com.experiencefieldops.api.repositories.AuditLogRepository;
import com.experiencefieldops.api.services.AuditLogService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private static final Logger log = LoggerFactory.getLogger(AuditLogServiceImpl.class);

    private final AuditLogRepository auditLogRepository;

    /**
     * REQUIRES_NEW garante que o log é salvo mesmo que a transação principal
     * seja revertida por algum motivo.
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(Long actorId, String actorName, AuditAction action,
                    String entityType, Long entityId,
                    String oldValue, String newValue,
                    String details, String ipAddress) {
        try {
            AuditLog entry = AuditLog.builder()
                    .actorId(actorId)
                    .actorName(actorName)
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .oldValue(sanitize(oldValue))
                    .newValue(sanitize(newValue))
                    .details(details)
                    .ipAddress(ipAddress)
                    .build();

            auditLogRepository.save(entry);
        } catch (Exception e) {
            // Falha silenciosa — auditoria nunca deve interromper o fluxo principal
            log.error("Falha ao registrar audit log: entity={} id={} action={} error={}",
                    entityType, entityId, action, e.getMessage());
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(Long actorId, String actorName, AuditAction action,
                    String entityType, Long entityId, String details) {
        log(actorId, actorName, action, entityType, entityId, null, null, details, null);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogResponse> findAll(String entityType, AuditAction action,
            Long actorId, LocalDate from, LocalDate to, Pageable pageable) {

        Specification<AuditLog> spec = Specification
                .where(hasEntityType(entityType))
                .and(hasAction(action))
                .and(hasActor(actorId))
                .and(fromDate(from))
                .and(toDate(to));

        return auditLogRepository.findAll(spec, pageable).map(AuditLogResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public AuditLogResponse findById(Long id) {
        return AuditLogResponse.from(
                auditLogRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("AuditLog", id)));
    }

    // ── sanitização (RN-007) ──────────────────────────────────────────────────

    private static final String[] SENSITIVE_PATTERNS = {
            "\"passwordHash\"", "\"password\"", "\"token\"",
            "\"accessToken\"", "\"refreshToken\"", "\"secret\""
    };

    /**
     * Remove campos sensíveis do JSON antes de persistir.
     * Substitui o valor por "***REDACTED***".
     */
    private String sanitize(String json) {
        if (json == null) return null;
        String result = json;
        for (String pattern : SENSITIVE_PATTERNS) {
            // Substitui "campo":"qualquerValor" → "campo":"***REDACTED***"
            result = result.replaceAll(
                    pattern + "\\s*:\\s*\"[^\"]*\"",
                    pattern + ":\"***REDACTED***\"");
        }
        return result;
    }

    // ── Specifications ────────────────────────────────────────────────────────

    private Specification<AuditLog> hasEntityType(String entityType) {
        if (entityType == null || entityType.isBlank()) return null;
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("entityType")), entityType.toLowerCase() + "%");
    }

    private Specification<AuditLog> hasAction(AuditAction action) {
        if (action == null) return null;
        return (root, query, cb) -> cb.equal(root.get("action"), action);
    }

    private Specification<AuditLog> hasActor(Long actorId) {
        if (actorId == null) return null;
        return (root, query, cb) -> cb.equal(root.get("actorId"), actorId);
    }

    private Specification<AuditLog> fromDate(LocalDate from) {
        if (from == null) return null;
        LocalDateTime start = from.atStartOfDay();
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), start);
    }

    private Specification<AuditLog> toDate(LocalDate to) {
        if (to == null) return null;
        LocalDateTime end = to.plusDays(1).atStartOfDay();
        return (root, query, cb) -> cb.lessThan(root.get("createdAt"), end);
    }
}
