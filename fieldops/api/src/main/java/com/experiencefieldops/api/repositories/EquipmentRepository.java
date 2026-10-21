package com.experiencefieldops.api.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.experiencefieldops.api.entities.Equipment;

public interface EquipmentRepository extends JpaRepository<Equipment, Long>, JpaSpecificationExecutor<Equipment> {

    Optional<Equipment> findByIdAndDeletedAtIsNull(Long id);

    Optional<Equipment> findByQrCodeAndDeletedAtIsNull(String qrCode);

    boolean existsByQrCode(String qrCode);
}
