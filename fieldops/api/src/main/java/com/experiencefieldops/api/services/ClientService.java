package com.experiencefieldops.api.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.experiencefieldops.api.dto.ClientRequest;
import com.experiencefieldops.api.dto.ClientResponse;
import com.experiencefieldops.api.dto.ClientStatusRequest;
import com.experiencefieldops.api.enums.ClientStatus;

public interface ClientService {

    ClientResponse create(ClientRequest request);

    Page<ClientResponse> findAll(String name, ClientStatus status, Pageable pageable);

    ClientResponse findById(Long id);

    ClientResponse update(Long id, ClientRequest request);

    ClientResponse updateStatus(Long id, ClientStatusRequest request);
}
