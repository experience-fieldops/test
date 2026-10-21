package com.experiencefieldops.api.controllers;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.experiencefieldops.api.dto.AuditLogResponse;
import com.experiencefieldops.api.enums.AuditAction;
import com.experiencefieldops.api.enums.UserRole;
import com.experiencefieldops.api.exception.ForbiddenException;
import com.experiencefieldops.api.repositories.UserRepository;
import com.experiencefieldops.api.services.AuditLogService;
import com.experiencefieldops.api.util.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;
    private final UserRepository userRepository;

    /**
     * GET /api/v1/audit-logs
     * Somente ADMIN e SUPERVISOR podem consultar logs.
     * TODO: requestingUserId virá do JWT quando auth estiver pronto.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<AuditLogResponse>>> findAll(
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) AuditAction action,
            @RequestParam(required = false) Long actorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam Long requestingUserId,
            @PageableDefault(size = 50, sort = "createdAt") Pageable pageable) {

        assertAdminOrSupervisor(requestingUserId);

        return ResponseEntity.ok(ApiResponse.success(
                auditLogService.findAll(entityType, action, actorId, from, to, pageable)));
    }

    /**
     * GET /api/v1/audit-logs/{id}
     * Busca log específico por ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AuditLogResponse>> findById(
            @PathVariable Long id,
            @RequestParam Long requestingUserId) {

        assertAdminOrSupervisor(requestingUserId);
        return ResponseEntity.ok(ApiResponse.success(auditLogService.findById(id)));
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private void assertAdminOrSupervisor(Long userId) {
        userRepository.findByIdAndDeletedAtIsNull(userId)
                .filter(u -> u.getRole() == UserRole.ADMIN || u.getRole() == UserRole.SUPERVISOR)
                .orElseThrow(() -> new ForbiddenException(
                        "Somente ADMIN ou SUPERVISOR podem consultar logs de auditoria"));
    }
}
