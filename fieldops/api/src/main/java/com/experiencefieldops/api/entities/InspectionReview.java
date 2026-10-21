package com.experiencefieldops.api.entities;

import java.time.LocalDateTime;

import com.experiencefieldops.api.enums.ReviewDecision;

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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * Representa uma rodada de revisão de uma inspeção concluída (RN-083).
 * Cada reprovação + reenvio gera uma nova revisão, preservando o histórico completo.
 */
@Entity
@Table(name = "inspection_reviews")
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InspectionReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "inspection_id", nullable = false)
    private Inspection inspection;

    // Supervisor que iniciou a revisão (RN-079)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reviewer_id", nullable = false)
    private User reviewer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private ReviewDecision decision;

    // Número sequencial da rodada de revisão (1, 2, 3...) — para rastreabilidade (RN-083)
    @Column(nullable = false)
    private Integer roundNumber;

    // Comentário opcional na aprovação (RN-081)
    @Column(length = 1000)
    private String comment;

    // Motivo obrigatório na reprovação (RN-080)
    @Column(length = 1000)
    private String rejectionReason;

    // Data/hora da decisão final (null enquanto PENDING)
    @Column
    private LocalDateTime decidedAt;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
