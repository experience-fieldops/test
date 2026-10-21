package com.experiencefieldops.api.entities;

import java.time.LocalDateTime;

import com.experiencefieldops.api.enums.NonConformityStatus;
import com.experiencefieldops.api.enums.Severity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "non_conformities")
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NonConformity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── Vínculos ──────────────────────────────────────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "inspection_id", nullable = false)
    private Inspection inspection;

    // Associação opcional ao item do snapshot (RN-053)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "snapshot_item_id")
    private InspectionItemSnapshot snapshotItem;

    // ── Dados da NC ───────────────────────────────────────────────────────────
    @Column(nullable = false, length = 255)
    private String title;

    // CRITICAL exige descrição não vazia (RN-055)
    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Severity severity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NonConformityStatus status;

    // Justificativa obrigatória ao descartar (RN-056)
    @Column(length = 500)
    private String dismissalReason;

    // Flag: indica se evidência já foi anexada (RN-055 / RN-039)
    // A validação real é feita ao publicar/concluir — aqui é informativo
    @Column(nullable = false)
    private Boolean evidenceAttached;

    // ── Auditoria ─────────────────────────────────────────────────────────────
    @Version
    @Column(nullable = false)
    private Long version;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
