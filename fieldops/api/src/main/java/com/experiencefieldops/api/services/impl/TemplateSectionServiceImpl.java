package com.experiencefieldops.api.services.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.experiencefieldops.api.dto.TemplateSectionRequest;
import com.experiencefieldops.api.dto.TemplateSectionResponse;
import com.experiencefieldops.api.entities.InspectionTemplateVersion;
import com.experiencefieldops.api.entities.TemplateSection;
import com.experiencefieldops.api.exception.ConflictException;
import com.experiencefieldops.api.exception.ResourceNotFoundException;
import com.experiencefieldops.api.repositories.InspectionTemplateVersionRepository;
import com.experiencefieldops.api.repositories.TemplateSectionRepository;
import com.experiencefieldops.api.services.TemplateSectionService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TemplateSectionServiceImpl implements TemplateSectionService {

    private final TemplateSectionRepository sectionRepository;
    private final InspectionTemplateVersionRepository versionRepository;

    @Override
    @Transactional
    public TemplateSectionResponse create(Long versionId, TemplateSectionRequest request) {
        InspectionTemplateVersion version = findVersion(versionId);
        assertIsDraft(version);

        TemplateSection section = TemplateSection.builder()
                .templateVersion(version)
                .title(request.getTitle())
                .description(request.getDescription())
                .displayOrder(request.getDisplayOrder())
                .build();

        return TemplateSectionResponse.from(sectionRepository.save(section));
    }

    @Override
    @Transactional
    public TemplateSectionResponse update(Long versionId, Long sectionId, TemplateSectionRequest request) {
        InspectionTemplateVersion version = findVersion(versionId);
        assertIsDraft(version);

        TemplateSection section = sectionRepository.findById(sectionId)
                .filter(s -> s.getTemplateVersion().getId().equals(versionId))
                .orElseThrow(() -> new ResourceNotFoundException("TemplateSection", sectionId));

        section.setTitle(request.getTitle());
        section.setDescription(request.getDescription());
        section.setDisplayOrder(request.getDisplayOrder());

        return TemplateSectionResponse.from(sectionRepository.save(section));
    }

    @Override
    @Transactional
    public void delete(Long versionId, Long sectionId) {
        InspectionTemplateVersion version = findVersion(versionId);
        assertIsDraft(version);

        TemplateSection section = sectionRepository.findById(sectionId)
                .filter(s -> s.getTemplateVersion().getId().equals(versionId))
                .orElseThrow(() -> new ResourceNotFoundException("TemplateSection", sectionId));

        sectionRepository.delete(section);
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private InspectionTemplateVersion findVersion(Long versionId) {
        return versionRepository.findById(versionId)
                .orElseThrow(() -> new ResourceNotFoundException("TemplateVersion", versionId));
    }

    /**
     * Versão é draft enquanto publishedAt for nulo (RN-019).
     */
    private void assertIsDraft(InspectionTemplateVersion version) {
        if (version.getPublishedAt() != null) {
            throw new ConflictException("Versão já publicada não pode ser alterada destrutivamente (RN-019)");
        }
    }
}
