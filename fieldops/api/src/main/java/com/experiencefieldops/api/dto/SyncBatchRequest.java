package com.experiencefieldops.api.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SyncBatchRequest {

    @NotEmpty(message = "O lote não pode ser vazio")
    @Valid
    private List<SyncOperationRequest> operations;
}
