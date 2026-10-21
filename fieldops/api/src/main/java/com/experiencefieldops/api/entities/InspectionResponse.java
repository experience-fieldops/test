package com.experiencefieldops.api.entities;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * Resposta do técnico a um item específico do snapshot (RN-035).
 * Unique constraint garante no máximo uma resposta por item por inspeção.
 */
@Entity
@Table(
    name = "inspection_responses",
    uniqueConstraints = @UniqueConstraint(columnNames = {"inspection_id", "snapshot_item_id"})
)
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InspectionResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "inspection_id", nullable = false)
    private Inspection inspection;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "snapshot_item_id", nullable = false)
    private InspectionItemSnapshot snapshotItem;

    /**
     * Valor da resposta serializado como texto.
     * O formato depende do response_type do item:
     *   TEXT_SHORT / TEXT_LONG → texto livre
     *   NUMBER                 → número decimal (ex: "3.14")
     *   BOOLEAN                → "true" / "false"
     *   CONFORMITY             → CONFORMING / NON_CONFORMING / NOT_APPLICABLE
     *   SINGLE_CHOICE          → chave da opção escolhida
     *   DATE                   → ISO-8601 (ex: "2026-09-14")
     */
    @Column(name = "response_value", columnDefinition = "TEXT")
    private String value;

    // Observação do técnico — obrigatória em casos de não conformidade quando configurado (RN-038)
    @Column(columnDefinition = "TEXT")
    private String observation;

    // Indica se o técnico marcou como não aplicável
    @Column(nullable = false)
    private Boolean notApplicable;

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
