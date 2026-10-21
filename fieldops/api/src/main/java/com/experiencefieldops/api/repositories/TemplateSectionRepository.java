package com.experiencefieldops.api.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.experiencefieldops.api.entities.TemplateSection;

public interface TemplateSectionRepository extends JpaRepository<TemplateSection, Long> {

    List<TemplateSection> findByTemplateVersionIdOrderByDisplayOrder(Long templateVersionId);

    boolean existsByTemplateVersionId(Long templateVersionId);
}
