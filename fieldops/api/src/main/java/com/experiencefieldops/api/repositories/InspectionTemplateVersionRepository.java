package com.experiencefieldops.api.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.experiencefieldops.api.entities.InspectionTemplateVersion;

public interface InspectionTemplateVersionRepository extends JpaRepository<InspectionTemplateVersion, Long> {

    List<InspectionTemplateVersion> findByTemplateIdOrderByVersionNumberDesc(Long templateId);

    Optional<InspectionTemplateVersion> findTopByTemplateIdOrderByVersionNumberDesc(Long templateId);

    boolean existsByTemplateId(Long templateId);
}
