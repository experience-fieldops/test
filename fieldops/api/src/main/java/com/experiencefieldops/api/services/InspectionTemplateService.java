package com.experiencefieldops.api.services;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.experiencefieldops.api.dto.TemplateRequest;
import com.experiencefieldops.api.dto.TemplateResponse;
import com.experiencefieldops.api.dto.TemplateStatusRequest;
import com.experiencefieldops.api.dto.TemplateVersionResponse;
import com.experiencefieldops.api.enums.TemplateStatus;

public interface InspectionTemplateService {

    TemplateResponse create(TemplateRequest request);

    Page<TemplateResponse> findAll(String title, String category, TemplateStatus status, Pageable pageable);

    TemplateResponse findById(Long id);

    TemplateResponse update(Long id, TemplateRequest request);

    TemplateResponse updateStatus(Long id, TemplateStatusRequest request);

    // Publica uma nova versão do modelo (RN-015: valida seções e itens antes de publicar)
    TemplateVersionResponse publish(Long templateId);

    List<TemplateVersionResponse> listVersions(Long templateId);

    // Retorna versão completa com seções e itens
    TemplateVersionResponse getVersionWithSections(Long versionId);
}
