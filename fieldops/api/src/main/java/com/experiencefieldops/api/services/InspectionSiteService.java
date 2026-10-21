package com.experiencefieldops.api.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.experiencefieldops.api.dto.SiteRequest;
import com.experiencefieldops.api.dto.SiteResponse;
import com.experiencefieldops.api.dto.SiteStatusRequest;
import com.experiencefieldops.api.enums.SiteStatus;

public interface InspectionSiteService {

    SiteResponse create(Long clientId, SiteRequest request);

    Page<SiteResponse> findAllByClient(Long clientId, String name, SiteStatus status, Pageable pageable);

    SiteResponse findById(Long id);

    SiteResponse update(Long id, SiteRequest request);

    SiteResponse updateStatus(Long id, SiteStatusRequest request);
}
