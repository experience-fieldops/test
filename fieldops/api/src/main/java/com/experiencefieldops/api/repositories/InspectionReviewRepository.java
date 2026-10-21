package com.experiencefieldops.api.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.experiencefieldops.api.entities.InspectionReview;
import com.experiencefieldops.api.enums.ReviewDecision;

public interface InspectionReviewRepository extends JpaRepository<InspectionReview, Long> {

    List<InspectionReview> findByInspectionIdOrderByRoundNumberDesc(Long inspectionId);

    // Verifica se já existe revisão pendente para a inspeção
    boolean existsByInspectionIdAndDecision(Long inspectionId, ReviewDecision decision);

    // Número da última rodada para calcular o próximo
    Optional<InspectionReview> findTopByInspectionIdOrderByRoundNumberDesc(Long inspectionId);
}
