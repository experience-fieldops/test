package com.experiencefieldops.api.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.experiencefieldops.api.entities.InspectionSite;

public interface InspectionSiteRepository extends JpaRepository<InspectionSite, Long>, JpaSpecificationExecutor<InspectionSite> {

    Optional<InspectionSite> findByIdAndDeletedAtIsNull(Long id);
}
