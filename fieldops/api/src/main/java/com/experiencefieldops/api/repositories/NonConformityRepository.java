package com.experiencefieldops.api.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.experiencefieldops.api.entities.NonConformity;

public interface NonConformityRepository extends JpaRepository<NonConformity, Long> {

    List<NonConformity> findByInspectionIdOrderByCreatedAtDesc(Long inspectionId);

    Optional<NonConformity> findByIdAndInspectionId(Long id, Long inspectionId);
}
