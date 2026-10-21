package com.experiencefieldops.api.services.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.experiencefieldops.api.dto.TemplateItemRequest;
import com.experiencefieldops.api.dto.TemplateItemResponse;
import com.experiencefieldops.api.entities.TemplateItem;
import com.experiencefieldops.api.entities.TemplateSection;
import com.experiencefieldops.api.exception.ConflictException;
import com.experiencefieldops.api.exception.ResourceNotFoundException;
import com.experiencefieldops.api.repositories.TemplateItemRepository;
import com.experiencefieldops.api.repositories.TemplateSectionRepository;
import com.experiencefieldops.api.services.TemplateItemService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TemplateItemServiceImpl implements TemplateItemService {

    private final TemplateItemRepository itemRepository;
    private final TemplateSectionRepository sectionRepository;

    @Override
    @Transactional
    public TemplateItemResponse create(Long sectionId, TemplateItemRequest request) {
        TemplateSection section = findSection(sectionId);
        assertVersionIsDraft(section);

        TemplateItem item = TemplateItem.builder()
                .section(section)
                .code(request.getCode())
                .title(request.getTitle())
                .description(request.getDescription())
                .responseType(request.getResponseType())
                .required(request.getRequired())
                .observationRequiredOnFailure(request.getObservationRequiredOnFailure())
                .evidenceRequiredOnFailure(request.getEvidenceRequiredOnFailure())
                .optionsJson(request.getOptionsJson())
                .displayOrder(request.getDisplayOrder())
                .build();

        return TemplateItemResponse.from(itemRepository.save(item));
    }

    @Override
    @Transactional
    public TemplateItemResponse update(Long sectionId, Long itemId, TemplateItemRequest request) {
        TemplateSection section = findSection(sectionId);
        assertVersionIsDraft(section);

        TemplateItem item = itemRepository.findById(itemId)
                .filter(i -> i.getSection().getId().equals(sectionId))
                .orElseThrow(() -> new ResourceNotFoundException("TemplateItem", itemId));

        item.setCode(request.getCode());
        item.setTitle(request.getTitle());
        item.setDescription(request.getDescription());
        item.setResponseType(request.getResponseType());
        item.setRequired(request.getRequired());
        item.setObservationRequiredOnFailure(request.getObservationRequiredOnFailure());
        item.setEvidenceRequiredOnFailure(request.getEvidenceRequiredOnFailure());
        item.setOptionsJson(request.getOptionsJson());
        item.setDisplayOrder(request.getDisplayOrder());

        return TemplateItemResponse.from(itemRepository.save(item));
    }

    @Override
    @Transactional
    public void delete(Long sectionId, Long itemId) {
        TemplateSection section = findSection(sectionId);
        assertVersionIsDraft(section);

        TemplateItem item = itemRepository.findById(itemId)
                .filter(i -> i.getSection().getId().equals(sectionId))
                .orElseThrow(() -> new ResourceNotFoundException("TemplateItem", itemId));

        itemRepository.delete(item);
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private TemplateSection findSection(Long sectionId) {
        return sectionRepository.findById(sectionId)
                .orElseThrow(() -> new ResourceNotFoundException("TemplateSection", sectionId));
    }

    /**
     * Bloqueio destrutivo em versão publicada (RN-019).
     * Uma versão é "draft" quando publishedAt é nulo.
     */
    private void assertVersionIsDraft(TemplateSection section) {
        if (section.getTemplateVersion().getPublishedAt() != null) {
            throw new ConflictException("Versão já publicada não pode ser alterada destrutivamente (RN-019)");
        }
    }
}
