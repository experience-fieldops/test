package com.experiencefieldops.api.services.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.experiencefieldops.api.dto.TemplateItemResponse;
import com.experiencefieldops.api.dto.TemplateRequest;
import com.experiencefieldops.api.dto.TemplateResponse;
import com.experiencefieldops.api.dto.TemplateSectionResponse;
import com.experiencefieldops.api.dto.TemplateStatusRequest;
import com.experiencefieldops.api.dto.TemplateVersionResponse;
import com.experiencefieldops.api.entities.InspectionTemplate;
import com.experiencefieldops.api.entities.InspectionTemplateVersion;
import com.experiencefieldops.api.entities.TemplateSection;
import com.experiencefieldops.api.enums.TemplateStatus;
import com.experiencefieldops.api.exception.ResourceNotFoundException;
import com.experiencefieldops.api.repositories.InspectionTemplateRepository;
import com.experiencefieldops.api.repositories.InspectionTemplateVersionRepository;
import com.experiencefieldops.api.repositories.TemplateItemRepository;
import com.experiencefieldops.api.repositories.TemplateSectionRepository;
import com.experiencefieldops.api.services.InspectionTemplateService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InspectionTemplateServiceImpl implements InspectionTemplateService {

    private final InspectionTemplateRepository templateRepository;
    private final InspectionTemplateVersionRepository versionRepository;
    private final TemplateSectionRepository sectionRepository;
    private final TemplateItemRepository itemRepository;

    @Override
    @Transactional
    public TemplateResponse create(TemplateRequest request) {
        InspectionTemplate template = InspectionTemplate.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .category(request.getCategory())
                .status(TemplateStatus.DRAFT)
                .currentVersion(0)
                .build();
        return TemplateResponse.from(templateRepository.save(template));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TemplateResponse> findAll(String title, String category, TemplateStatus status, Pageable pageable) {
        Specification<InspectionTemplate> spec = Specification
                .where(notDeleted())
                .and(hasTitle(title))
                .and(hasCategory(category))
                .and(hasStatus(status));
        return templateRepository.findAll(spec, pageable).map(TemplateResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public TemplateResponse findById(Long id) {
        return TemplateResponse.from(findActiveById(id));
    }

    @Override
    @Transactional
    public TemplateResponse update(Long id, TemplateRequest request) {
        InspectionTemplate template = findActiveById(id);

        // Só permite edição livre no DRAFT (RN-018)
        // Modelos ACTIVE/INACTIVE atualizam apenas metadados — alterações estruturais devem gerar versão
        template.setTitle(request.getTitle());
        template.setDescription(request.getDescription());
        template.setCategory(request.getCategory());

        return TemplateResponse.from(templateRepository.save(template));
    }

    @Override
    @Transactional
    public TemplateResponse updateStatus(Long id, TemplateStatusRequest request) {
        InspectionTemplate template = findActiveById(id);

        // DRAFT só pode ir para ACTIVE via publish(); não via este endpoint
        if (request.getStatus() == TemplateStatus.ACTIVE && template.getCurrentVersion() == 0) {
            throw new IllegalArgumentException("Modelo precisa ser publicado antes de ser ativado");
        }

        template.setStatus(request.getStatus());
        return TemplateResponse.from(templateRepository.save(template));
    }

    @Override
    @Transactional
    public TemplateVersionResponse publish(Long templateId) {
        InspectionTemplate template = findActiveById(templateId);

        // Calcula o próximo número de versão
        int nextVersionNumber = template.getCurrentVersion() + 1;

        // Cria a nova versão como snapshot
        InspectionTemplateVersion version = InspectionTemplateVersion.builder()
                .template(template)
                .versionNumber(nextVersionNumber)
                .titleSnapshot(template.getTitle())
                .descriptionSnapshot(template.getDescription())
                .activeForNewInspections(true)
                .publishedAt(LocalDateTime.now())
                .build();

        InspectionTemplateVersion saved = versionRepository.save(version);

        // Valida RN-015: ao menos uma seção com ao menos um item
        List<TemplateSection> sections = sectionRepository.findByTemplateVersionIdOrderByDisplayOrder(saved.getId());
        if (sections.isEmpty()) {
            throw new IllegalArgumentException("O modelo precisa ter ao menos uma seção para ser publicado (RN-015)");
        }
        boolean hasItem = sections.stream()
                .anyMatch(s -> itemRepository.existsBySectionId(s.getId()));
        if (!hasItem) {
            throw new IllegalArgumentException("O modelo precisa ter ao menos um item válido para ser publicado (RN-015)");
        }

        // Atualiza o modelo pai
        template.setCurrentVersion(nextVersionNumber);
        template.setStatus(TemplateStatus.ACTIVE);
        templateRepository.save(template);

        return buildVersionWithSections(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TemplateVersionResponse> listVersions(Long templateId) {
        findActiveById(templateId); // garante que o template existe
        return versionRepository.findByTemplateIdOrderByVersionNumberDesc(templateId)
                .stream()
                .map(TemplateVersionResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public TemplateVersionResponse getVersionWithSections(Long versionId) {
        InspectionTemplateVersion version = versionRepository.findById(versionId)
                .orElseThrow(() -> new ResourceNotFoundException("TemplateVersion", versionId));
        return buildVersionWithSections(version);
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private InspectionTemplate findActiveById(Long id) {
        return templateRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("InspectionTemplate", id));
    }

    private TemplateVersionResponse buildVersionWithSections(InspectionTemplateVersion version) {
        List<TemplateSectionResponse> sections = sectionRepository
                .findByTemplateVersionIdOrderByDisplayOrder(version.getId())
                .stream()
                .map(section -> {
                    List<TemplateItemResponse> items = itemRepository
                            .findBySectionIdOrderByDisplayOrder(section.getId())
                            .stream()
                            .map(TemplateItemResponse::from)
                            .collect(Collectors.toList());
                    return TemplateSectionResponse.from(section, items);
                })
                .collect(Collectors.toList());

        return TemplateVersionResponse.from(version, sections);
    }

    private Specification<InspectionTemplate> notDeleted() {
        return (root, query, cb) -> cb.isNull(root.get("deletedAt"));
    }

    private Specification<InspectionTemplate> hasTitle(String title) {
        if (title == null || title.isBlank()) return null;
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("title")), "%" + title.toLowerCase() + "%");
    }

    private Specification<InspectionTemplate> hasCategory(String category) {
        if (category == null || category.isBlank()) return null;
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("category")), "%" + category.toLowerCase() + "%");
    }

    private Specification<InspectionTemplate> hasStatus(TemplateStatus status) {
        if (status == null) return null;
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }
}
