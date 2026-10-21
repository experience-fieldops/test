package com.experiencefieldops.api.dto;

import com.experiencefieldops.api.enums.TemplateStatus;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TemplateStatusRequest {

    @NotNull(message = "Status é obrigatório")
    private TemplateStatus status;
}
