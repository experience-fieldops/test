package com.experiencefieldops.api.services;

import com.experiencefieldops.api.dto.TemplateSectionRequest;
import com.experiencefieldops.api.dto.TemplateSectionResponse;

public interface TemplateSectionService {

    TemplateSectionResponse create(Long versionId, TemplateSectionRequest request);

    TemplateSectionResponse update(Long versionId, Long sectionId, TemplateSectionRequest request);

    void delete(Long versionId, Long sectionId);
}
