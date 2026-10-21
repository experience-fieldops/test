package com.experiencefieldops.api.entities;

import com.experiencefieldops.api.enums.ResponseType;

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
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Snapshot imutável dos itens do checklist no momento da criação da inspeção (RN-021).
 * Garante que alterações posteriores no template não afetem inspeções já criadas.
 */
@Entity
@Table(name = "inspection_item_snapshots")
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InspectionItemSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "inspection_id", nullable = false)
    private Inspection inspection;

    // Referência ao item original (pode ser nula se o item for deletado futuramente)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "original_item_id")
    private TemplateItem originalItem;

    // Snapshot dos campos do item no momento da criação
    @Column(length = 50)
    private String code;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ResponseType responseType;

    @Column(nullable = false)
    private Boolean required;

    @Column(nullable = false)
    private Boolean observationRequiredOnFailure;

    @Column(nullable = false)
    private Boolean evidenceRequiredOnFailure;

    @Column(columnDefinition = "TEXT")
    private String optionsJson;

    // Seção e ordem preservadas para renderização correta no app
    @Column(nullable = false, length = 150)
    private String sectionTitle;

    @Column(nullable = false)
    private Integer sectionDisplayOrder;

    @Column(nullable = false)
    private Integer itemDisplayOrder;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
