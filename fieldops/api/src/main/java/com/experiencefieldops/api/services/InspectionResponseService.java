package com.experiencefieldops.api.services;

import java.util.List;

import com.experiencefieldops.api.dto.InspectionProgressResponse;
import com.experiencefieldops.api.dto.ResponseRequest;
import com.experiencefieldops.api.dto.ResponseResponse;

public interface InspectionResponseService {

    // Registra nova resposta ou atualiza se já existir para o mesmo snapshotItem (upsert)
    ResponseResponse save(Long inspectionId, ResponseRequest request);

    List<ResponseResponse> findAll(Long inspectionId);

    ResponseResponse findById(Long inspectionId, Long responseId);

    ResponseResponse update(Long inspectionId, Long responseId, ResponseRequest request);

    InspectionProgressResponse getProgress(Long inspectionId);
}
