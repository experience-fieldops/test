package com.experiencefieldops.api.services;

import java.util.List;

import com.experiencefieldops.api.dto.NonConformityRequest;
import com.experiencefieldops.api.dto.NonConformityResponse;
import com.experiencefieldops.api.dto.NonConformityStatusRequest;

public interface NonConformityService {

    NonConformityResponse create(Long inspectionId, NonConformityRequest request);

    List<NonConformityResponse> findAll(Long inspectionId);

    NonConformityResponse findById(Long id);

    NonConformityResponse update(Long id, NonConformityRequest request);

    NonConformityResponse updateStatus(Long id, NonConformityStatusRequest request);
}
