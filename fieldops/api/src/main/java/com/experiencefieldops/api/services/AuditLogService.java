package com.experiencefieldops.api.services;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.experiencefieldops.api.dto.AuditLogResponse;
import com.experiencefieldops.api.enums.AuditAction;

public interface AuditLogService {

    /**
     * Registra uma ação auditável.
     * Nunca deve lançar exceção — falha silenciosa para não interromper o fluxo principal.
     *
     * @param actorId    ID do usuário que executou (null = sistema)
     * @param actorName  Nome do usuário (snapshot para sobreviver a deletes)
     * @param action     Tipo da ação
     * @param entityType Nome da entidade (ex: "Inspection")
     * @param entityId   ID do registro afetado
     * @param oldValue   Estado anterior serializado (sem campos sensíveis)
     * @param newValue   Novo estado serializado (sem campos sensíveis)
     * @param details    Informações extras opcionais
     * @param ipAddress  IP de origem (opcional)
     */
    void log(Long actorId, String actorName, AuditAction action,
             String entityType, Long entityId,
             String oldValue, String newValue,
             String details, String ipAddress);

    // Atalho para quando não há old/new value (ex: login, upload)
    void log(Long actorId, String actorName, AuditAction action,
             String entityType, Long entityId, String details);

    Page<AuditLogResponse> findAll(String entityType, AuditAction action,
            Long actorId, LocalDate from, LocalDate to, Pageable pageable);

    AuditLogResponse findById(Long id);
}
