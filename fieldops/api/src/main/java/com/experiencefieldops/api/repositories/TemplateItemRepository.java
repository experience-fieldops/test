package com.experiencefieldops.api.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.experiencefieldops.api.entities.TemplateItem;

public interface TemplateItemRepository extends JpaRepository<TemplateItem, Long> {

    List<TemplateItem> findBySectionIdOrderByDisplayOrder(Long sectionId);

    boolean existsBySectionId(Long sectionId);
}
