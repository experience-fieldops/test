package com.experiencefieldops.api.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.experiencefieldops.api.entities.InspectionItemSnapshot;

public interface InspectionItemSnapshotRepository extends JpaRepository<InspectionItemSnapshot, Long> {

    List<InspectionItemSnapshot> findByInspectionIdOrderBySectionDisplayOrderAscItemDisplayOrderAsc(Long inspectionId);
}
