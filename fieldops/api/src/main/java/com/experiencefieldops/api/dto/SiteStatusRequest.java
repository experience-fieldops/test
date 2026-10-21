package com.experiencefieldops.api.dto;

import com.experiencefieldops.api.enums.SiteStatus;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SiteStatusRequest {

    @NotNull(message = "Status é obrigatório")
    private SiteStatus status;
}
