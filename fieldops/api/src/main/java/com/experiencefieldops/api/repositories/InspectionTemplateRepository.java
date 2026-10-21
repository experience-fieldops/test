package com.experiencefieldops.api.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.experiencefieldops.api.entities.InspectionTemplate;

public interface InspectionTemplateRepository extends JpaRepository<InspectionTemplate, Long>, JpaSpecificationExecutor<InspectionTemplate> {

    Optional<InspectionTemplate> findByIdAndDeletedAtIsNull(Long id);
}
