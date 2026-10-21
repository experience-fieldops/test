package com.experiencefieldops.api.services.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.experiencefieldops.api.dto.InspectionProgressResponse;
import com.experiencefieldops.api.dto.ResponseRequest;
import com.experiencefieldops.api.dto.ResponseResponse;
import com.experiencefieldops.api.entities.Inspection;
import com.experiencefieldops.api.entities.InspectionItemSnapshot;
import com.experiencefieldops.api.entities.InspectionResponse;
import com.experiencefieldops.api.enums.ConformityValue;
import com.experiencefieldops.api.enums.InspectionStatus;
import com.experiencefieldops.api.enums.ResponseType;
import com.experiencefieldops.api.exception.ConflictException;
import com.experiencefieldops.api.exception.ResourceNotFoundException;
import com.experiencefieldops.api.repositories.InspectionItemSnapshotRepository;
import com.experiencefieldops.api.repositories.InspectionRepository;
import com.experiencefieldops.api.repositories.InspectionResponseRepository;
import com.experiencefieldops.api.services.InspectionResponseService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InspectionResponseServiceImpl implements InspectionResponseService {

    private final InspectionResponseRepository responseRepository;
    private final InspectionRepository inspectionRepository;
    private final InspectionItemSnapshotRepository snapshotRepository;

    @Override
    @Transactional
    public ResponseResponse save(Long inspectionId, ResponseRequest request) {
        Inspection inspection = findEditableInspection(inspectionId);
        InspectionItemSnapshot snapshotItem = findSnapshotItem(inspectionId, request.getSnapshotItemId());

        boolean notApplicable = Boolean.TRUE.equals(request.getNotApplicable());

        if (!notApplicable) {
            validateValue(snapshotItem, request.getValue());
            validateObservation(snapshotItem, request.getValue(), request.getObservation());
        }

        // Upsert: se já existe resposta para este item, atualiza
        InspectionResponse response = responseRepository
                .findByInspectionIdAndSnapshotItemId(inspectionId, snapshotItem.getId())
                .orElse(InspectionResponse.builder()
                        .inspection(inspection)
                        .snapshotItem(snapshotItem)
                        .build());

        response.setValue(notApplicable ? null : request.getValue());
        response.setObservation(notApplicable ? null : request.getObservation());
        response.setNotApplicable(notApplicable);

        return ResponseResponse.from(responseRepository.save(response));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResponseResponse> findAll(Long inspectionId) {
        findInspection(inspectionId);
        return responseRepository.findByInspectionId(inspectionId)
                .stream()
                .map(ResponseResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseResponse findById(Long inspectionId, Long responseId) {
        return ResponseResponse.from(findResponse(inspectionId, responseId));
    }

    @Override
    @Transactional
    public ResponseResponse update(Long inspectionId, Long responseId, ResponseRequest request) {
        findEditableInspection(inspectionId);
        InspectionResponse response = findResponse(inspectionId, responseId);

        InspectionItemSnapshot snapshotItem = response.getSnapshotItem();
        boolean notApplicable = Boolean.TRUE.equals(request.getNotApplicable());

        if (!notApplicable) {
            validateValue(snapshotItem, request.getValue());
            validateObservation(snapshotItem, request.getValue(), request.getObservation());
        }

        response.setValue(notApplicable ? null : request.getValue());
        response.setObservation(notApplicable ? null : request.getObservation());
        response.setNotApplicable(notApplicable);

        return ResponseResponse.from(responseRepository.save(response));
    }

    @Override
    @Transactional(readOnly = true)
    public InspectionProgressResponse getProgress(Long inspectionId) {
        findInspection(inspectionId);

        List<InspectionItemSnapshot> allItems = snapshotRepository
                .findByInspectionIdOrderBySectionDisplayOrderAscItemDisplayOrderAsc(inspectionId);

        int totalItems = allItems.size();

        List<InspectionResponse> responses = responseRepository.findByInspectionId(inspectionId);

        long notApplicableItems = responses.stream()
                .filter(r -> Boolean.TRUE.equals(r.getNotApplicable()))
                .count();

        long answeredItems = responses.stream()
                .filter(r -> !Boolean.TRUE.equals(r.getNotApplicable()))
                .count();

        int applicableItems = totalItems - (int) notApplicableItems;
        int pendingItems = Math.max(0, applicableItems - (int) answeredItems);
        double progressPercentage = applicableItems == 0 ? 100.0
                : Math.round((answeredItems * 100.0 / applicableItems) * 10.0) / 10.0;

        return InspectionProgressResponse.builder()
                .inspectionId(inspectionId)
                .totalItems(totalItems)
                .answeredItems((int) answeredItems)
                .notApplicableItems((int) notApplicableItems)
                .applicableItems(applicableItems)
                .pendingItems(pendingItems)
                .progressPercentage(progressPercentage)
                .build();
    }

    // ── validações ────────────────────────────────────────────────────────────

    /**
     * Valida o valor conforme o response_type do item (RN-036).
     */
    private void validateValue(InspectionItemSnapshot item, String value) {
        ResponseType type = item.getResponseType();

        if (value == null || value.isBlank()) {
            if (Boolean.TRUE.equals(item.getRequired())) {
                throw new IllegalArgumentException(
                        "Item obrigatório não pode ter resposta vazia: " + item.getTitle());
            }
            return; // opcional sem valor — ok
        }

        switch (type) {
            case NUMBER -> {
                try {
                    Double.parseDouble(value);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(
                            "Valor inválido para tipo NUMBER: '" + value + "'. Esperado: número decimal.");
                }
            }
            case BOOLEAN -> {
                if (!value.equalsIgnoreCase("true") && !value.equalsIgnoreCase("false")) {
                    throw new IllegalArgumentException(
                            "Valor inválido para tipo BOOLEAN: '" + value + "'. Esperado: 'true' ou 'false'.");
                }
            }
            case CONFORMITY -> {
                try {
                    ConformityValue.valueOf(value.toUpperCase());
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException(
                            "Valor inválido para tipo CONFORMITY: '" + value
                                    + "'. Esperado: CONFORMING, NON_CONFORMING ou NOT_APPLICABLE.");
                }
            }
            case DATE -> {
                if (!value.matches("\\d{4}-\\d{2}-\\d{2}")) {
                    throw new IllegalArgumentException(
                            "Valor inválido para tipo DATE: '" + value + "'. Esperado: formato ISO-8601 (yyyy-MM-dd).");
                }
            }
            case SINGLE_CHOICE -> {
                if (item.getOptionsJson() == null || item.getOptionsJson().isBlank()) {
                    throw new IllegalArgumentException(
                            "Item do tipo SINGLE_CHOICE não possui opções configuradas.");
                }
                // Valida apenas que o valor foi informado — validação das opções do JSON
                // pode ser expandida futuramente sem quebrar o contrato
            }
            // TEXT_SHORT e TEXT_LONG aceitam qualquer string — sem validação adicional
            default -> { }
        }
    }

    /**
     * Exige observação quando o item for CONFORMITY = NON_CONFORMING
     * e observationRequiredOnFailure = true (RN-038).
     */
    private void validateObservation(InspectionItemSnapshot item, String value, String observation) {
        if (item.getResponseType() == ResponseType.CONFORMITY
                && Boolean.TRUE.equals(item.getObservationRequiredOnFailure())
                && value != null
                && value.equalsIgnoreCase(ConformityValue.NON_CONFORMING.name())) {

            if (observation == null || observation.isBlank()) {
                throw new IllegalArgumentException(
                        "Observação obrigatória para item não conforme: " + item.getTitle() + " (RN-038)");
            }
        }
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    /**
     * Retorna a inspeção somente se ainda for editável.
     * - COMPLETED, UNDER_REVIEW → bloqueado (RN-043)
     * - APPROVED → somente leitura, retorna 403 (RN-082)
     * - CANCELLED → bloqueado
     */
    private Inspection findEditableInspection(Long inspectionId) {
        Inspection inspection = findInspection(inspectionId);

        if (inspection.getStatus() == InspectionStatus.APPROVED) {
            throw new com.experiencefieldops.api.exception.ForbiddenException(
                    "Respostas de inspeção aprovada são somente leitura (RN-082)");
        }

        if (inspection.getStatus() == InspectionStatus.COMPLETED
                || inspection.getStatus() == InspectionStatus.UNDER_REVIEW
                || inspection.getStatus() == InspectionStatus.CANCELLED) {
            throw new ConflictException(
                    "Não é possível alterar respostas de inspeção com status: " + inspection.getStatus() + " (RN-043)");
        }

        return inspection;
    }

    private Inspection findInspection(Long inspectionId) {
        return inspectionRepository.findById(inspectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Inspection", inspectionId));
    }

    private InspectionItemSnapshot findSnapshotItem(Long inspectionId, Long snapshotItemId) {
        return snapshotRepository.findById(snapshotItemId)
                .filter(s -> s.getInspection().getId().equals(inspectionId))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "InspectionItemSnapshot " + snapshotItemId + " não encontrado na inspeção " + inspectionId));
    }

    private InspectionResponse findResponse(Long inspectionId, Long responseId) {
        return responseRepository.findByInspectionIdAndId(inspectionId, responseId)
                .orElseThrow(() -> new ResourceNotFoundException("InspectionResponse", responseId));
    }
}
