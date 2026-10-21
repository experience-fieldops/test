package com.experiencefieldops.api.entities;

import java.time.LocalDateTime;

import com.experiencefieldops.api.enums.AuditAction;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * Registro imutável de ações críticas no sistema (RN-086).
 * Nunca expõe campos sensíveis como passwordHash ou tokens (RN-007).
 */
@Entity
@Table(name = "audit_logs")
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Quem executou a ação (null para ações de sistema)
    @Column(name = "actor_id")
    private Long actorId;

    @Column(name = "actor_name", length = 150)
    private String actorName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private AuditAction action;

    // Nome da entidade afetada (ex: "Inspection", "User", "Evidence")
    @Column(nullable = false, length = 50)
    private String entityType;

    // ID do registro afetado
    @Column(nullable = false)
    private Long entityId;

    // Snapshot do estado anterior — campos sensíveis nunca incluídos (RN-007)
    @Column(columnDefinition = "TEXT")
    private String oldValue;

    // Snapshot do novo estado — campos sensíveis nunca incluídos (RN-007)
    @Column(columnDefinition = "TEXT")
    private String newValue;

    // Informações extras (ex: motivo do cancelamento, versão publicada)
    @Column(length = 500)
    private String details;

    // IP de origem quando disponível
    @Column(length = 45)
    private String ipAddress;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
