package com.experiencefieldops.api.services.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.experiencefieldops.api.dto.ClientRequest;
import com.experiencefieldops.api.dto.ClientResponse;
import com.experiencefieldops.api.dto.ClientStatusRequest;
import com.experiencefieldops.api.entities.Client;
import com.experiencefieldops.api.enums.ClientStatus;
import com.experiencefieldops.api.exception.ResourceNotFoundException;
import com.experiencefieldops.api.repositories.ClientRepository;
import com.experiencefieldops.api.services.ClientService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;

    @Override
    @Transactional
    public ClientResponse create(ClientRequest request) {
        Client client = Client.builder()
                .name(request.getName())
                .document(request.getDocument())
                .email(request.getEmail())
                .phone(request.getPhone())
                .status(ClientStatus.ACTIVE)
                .build();
        return ClientResponse.from(clientRepository.save(client));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ClientResponse> findAll(String name, ClientStatus status, Pageable pageable) {
        Specification<Client> spec = Specification
                .where(notDeleted())
                .and(hasName(name))
                .and(hasStatus(status));
        return clientRepository.findAll(spec, pageable).map(ClientResponse::from);
    }

    @Override
    @Transactional(readOnly = true)
    public ClientResponse findById(Long id) {
        return ClientResponse.from(findActiveById(id));
    }

    @Override
    @Transactional
    public ClientResponse update(Long id, ClientRequest request) {
        Client client = findActiveById(id);
        client.setName(request.getName());
        client.setDocument(request.getDocument());
        client.setEmail(request.getEmail());
        client.setPhone(request.getPhone());
        return ClientResponse.from(clientRepository.save(client));
    }

    @Override
    @Transactional
    public ClientResponse updateStatus(Long id, ClientStatusRequest request) {
        Client client = findActiveById(id);
        client.setStatus(request.getStatus());
        return ClientResponse.from(clientRepository.save(client));
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private Client findActiveById(Long id) {
        return clientRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client", id));
    }

    private Specification<Client> notDeleted() {
        return (root, query, cb) -> cb.isNull(root.get("deletedAt"));
    }

    private Specification<Client> hasName(String name) {
        if (name == null || name.isBlank()) return null;
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
    }

    private Specification<Client> hasStatus(ClientStatus status) {
        if (status == null) return null;
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }
}
