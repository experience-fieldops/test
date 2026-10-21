package com.experiencefieldops.api.controllers;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.experiencefieldops.api.dto.ClientRequest;
import com.experiencefieldops.api.dto.ClientResponse;
import com.experiencefieldops.api.dto.ClientStatusRequest;
import com.experiencefieldops.api.enums.ClientStatus;
import com.experiencefieldops.api.services.ClientService;
import com.experiencefieldops.api.util.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;

    @PostMapping
    public ResponseEntity<ApiResponse<ClientResponse>> create(
            @Valid @RequestBody ClientRequest request) {
        ClientResponse response = clientService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Cliente criado com sucesso"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ClientResponse>>> findAll(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) ClientStatus status,
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(clientService.findAll(name, status, pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ClientResponse>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(clientService.findById(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ClientResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody ClientRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                clientService.update(id, request), "Cliente atualizado com sucesso"));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<ClientResponse>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody ClientStatusRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                clientService.updateStatus(id, request), "Status atualizado com sucesso"));
    }
}
