package com.experiencefieldops.api.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.experiencefieldops.api.entities.Evidence;

public interface EvidenceRepository extends JpaRepository<Evidence, Long> {

    // Lista apenas evidências não deletadas da inspeção
    List<Evidence> findByInspectionIdAndDeletedAtIsNull(Long inspectionId);

    Optional<Evidence> findByIdAndDeletedAtIsNull(Long id);
}
