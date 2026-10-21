package com.experiencefieldops.api.entities;

import java.time.LocalDateTime;

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

@Entity
@Table(name = "template_items")
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "section_id", nullable = false)
    private TemplateSection section;

    // Código legível opcional (ex: "EXT-001")
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

    // Se true: exige observação quando a resposta for não conforme
    @Column(nullable = false)
    private Boolean observationRequiredOnFailure;

    // Se true: exige ao menos uma evidência quando a resposta for não conforme
    @Column(nullable = false)
    private Boolean evidenceRequiredOnFailure;

    // JSON livre para configurar opções (ex: choices do SINGLE_CHOICE)
    @Column(columnDefinition = "TEXT")
    private String optionsJson;

    @Column(nullable = false)
    private Integer displayOrder;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
