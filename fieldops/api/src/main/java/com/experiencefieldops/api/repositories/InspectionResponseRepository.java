package com.experiencefieldops.api.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.experiencefieldops.api.entities.InspectionResponse;

public interface InspectionResponseRepository extends JpaRepository<InspectionResponse, Long> {

    List<InspectionResponse> findByInspectionId(Long inspectionId);

    Optional<InspectionResponse> findByInspectionIdAndId(Long inspectionId, Long responseId);

    Optional<InspectionResponse> findByInspectionIdAndSnapshotItemId(Long inspectionId, Long snapshotItemId);

    long countByInspectionId(Long inspectionId);

    // Conta respostas não marcadas como N/A (para calcular progresso)
    long countByInspectionIdAndNotApplicableFalse(Long inspectionId);
}
