package com.experiencefieldops.api.services.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.experiencefieldops.api.dto.SiteRequest;
import com.experiencefieldops.api.dto.SiteResponse;
import com.experiencefieldops.api.dto.SiteStatusRequest;
import com.experiencefieldops.api.entities.Client;
import com.experiencefieldops.api.entities.InspectionSite;
import com.experiencefieldops.api.enums.ClientStatus;
import com.experiencefieldops.api.enums.SiteStatus;
import com.experiencefieldops.api.exception.ResourceNotFoundException;
import com.experiencefieldops.api.repositories.ClientRepository;
import com.experiencefieldops.api.repositories.InspectionSiteRepository;
import com.experiencefieldops.api.services.InspectionSiteService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InspectionSiteServiceImpl implements InspectionSiteService {

    private final InspectionSiteRepository siteRepository;
    private final ClientRepository clientRepository;

    @Override
    @Transactional
    public SiteResponse create(Long clientId, SiteRequest request) {
        Client client = findActiveClient(clientId);

        InspectionSite site = InspectionSite.builder()
                .name(request.getName())
                .address(request.getAddress())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .status(SiteStatus.ACTIVE)
                .client(client)
                .build();

        return SiteResponse.from(siteRepository.save(site));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SiteResponse> findAllByClient(Long clientId, String name, SiteStatus status, Pageable pageable) {
        // garante que o cliente existe
        if (!clientRepository.existsById(clientId)) {
            throw new ResourceNotFoundException("Client", clientId);
        }

        Specification<InspectionSite> spec = Specification
                .where(notDeleted())
                .and(byClient(clientId))
                .and(hasName(name))
                .and(hasStatus(status));

        return siteRepository.findAll(spec, pageable).map(SiteResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public SiteResponse findById(Long id) {
        return SiteResponse.from(findActiveById(id));
    }

    @Override
    @Transactional
    public SiteResponse update(Long id, SiteRequest request) {
        InspectionSite site = findActiveById(id);
        site.setName(request.getName());
        site.setAddress(request.getAddress());
        site.setLatitude(request.getLatitude());
        site.setLongitude(request.getLongitude());
        return SiteResponse.from(siteRepository.save(site));
    }

    @Override
    @Transactional
    public SiteResponse updateStatus(Long id, SiteStatusRequest request) {
        InspectionSite site = findActiveById(id);
        site.setStatus(request.getStatus());
        return SiteResponse.from(siteRepository.save(site));
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private Client findActiveClient(Long clientId) {
        Client client = clientRepository.findByIdAndDeletedAtIsNull(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client", clientId));

        if (client.getStatus() == ClientStatus.INACTIVE) {
            throw new IllegalArgumentException("Não é possível criar local para cliente inativo");
        }
        return client;
    }

    private InspectionSite findActiveById(Long id) {
        return siteRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("InspectionSite", id));
    }

    private Specification<InspectionSite> notDeleted() {
        return (root, query, cb) -> cb.isNull(root.get("deletedAt"));
    }

    private Specification<InspectionSite> byClient(Long clientId) {
        return (root, query, cb) -> cb.equal(root.get("client").get("id"), clientId);
    }

    private Specification<InspectionSite> hasName(String name) {
        if (name == null || name.isBlank()) return null;
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
    }

    private Specification<InspectionSite> hasStatus(SiteStatus status) {
        if (status == null) return null;
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }
}
