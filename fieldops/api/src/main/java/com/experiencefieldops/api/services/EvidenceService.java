package com.experiencefieldops.api.services;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.experiencefieldops.api.dto.EvidenceResponse;

public interface EvidenceService {

    EvidenceResponse upload(Long inspectionId, Long responseId, Long nonConformityId,
            MultipartFile file, Long uploadedByUserId);

    List<EvidenceResponse> findAll(Long inspectionId);

    EvidenceResponse findById(Long id);

    void delete(Long id, Long deletedByUserId, String reason);
}
