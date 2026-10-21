package com.experiencefieldops.api.entities;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.experiencefieldops.api.enums.InspectionStatus;

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
@Table(name = "inspections")
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Inspection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── Vínculos obrigatórios (RN-025) ────────────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "site_id", nullable = false)
    private InspectionSite site;

    // Técnico responsável — MVP: apenas um (RN-031)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assigned_to", nullable = false)
    private User assignedTo;

    // Versão publicada do modelo (RN-024)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "template_version_id", nullable = false)
    private InspectionTemplateVersion templateVersion;

    // Equipamento — opcional quando tipo for local/ambiente (RN-026)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_id")
    private Equipment equipment;

    // ── Agendamento ───────────────────────────────────────────────────────
    @Column(nullable = false)
    private LocalDate scheduledDate;

    // ── Estado ───────────────────────────────────────────────────────────
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private InspectionStatus status;

    // ── Execução (RN-034) ─────────────────────────────────────────────────
    // Data/hora capturada no dispositivo do técnico
    @Column
    private LocalDateTime startedAtDevice;

    // Data/hora de recebimento no servidor
    @Column
    private LocalDateTime startedAtServer;

    // Data/hora de conclusão capturada no dispositivo
    @Column
    private LocalDateTime completedAtDevice;

    // Data/hora de recebimento da conclusão no servidor
    @Column
    private LocalDateTime completedAtServer;

    // ── Cancelamento (RN-029) ─────────────────────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cancelled_by")
    private User cancelledBy;

    @Column
    private LocalDateTime cancelledAt;

    @Column(length = 500)
    private String cancellationReason;

    // ── Auditoria ─────────────────────────────────────────────────────────
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
