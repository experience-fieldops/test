package com.experiencefieldops.api.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.experiencefieldops.api.dto.InspectionCancelRequest;
import com.experiencefieldops.api.dto.InspectionCompleteRequest;
import com.experiencefieldops.api.dto.InspectionRequest;
import com.experiencefieldops.api.dto.InspectionResponse;
import com.experiencefieldops.api.dto.InspectionStartRequest;
import com.experiencefieldops.api.enums.InspectionStatus;

public interface InspectionService {

    InspectionResponse create(InspectionRequest request);

    Page<InspectionResponse> findAll(Long technicianId, Long clientId, InspectionStatus status, Pageable pageable);

    InspectionResponse findById(Long id);

    // Lista inspeções atribuídas ao técnico informado
    Page<InspectionResponse> findAssigned(Long technicianId, Pageable pageable);

    InspectionResponse start(Long id, InspectionStartRequest request, Long requestingUserId);

    InspectionResponse complete(Long id, InspectionCompleteRequest request, Long requestingUserId);

    InspectionResponse cancel(Long id, InspectionCancelRequest request, Long requestingUserId);
}
