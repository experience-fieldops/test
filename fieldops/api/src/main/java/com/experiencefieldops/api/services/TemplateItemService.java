package com.experiencefieldops.api.services;

import com.experiencefieldops.api.dto.TemplateItemRequest;
import com.experiencefieldops.api.dto.TemplateItemResponse;

public interface TemplateItemService {

    TemplateItemResponse create(Long sectionId, TemplateItemRequest request);

    TemplateItemResponse update(Long sectionId, Long itemId, TemplateItemRequest request);

    void delete(Long sectionId, Long itemId);
}
