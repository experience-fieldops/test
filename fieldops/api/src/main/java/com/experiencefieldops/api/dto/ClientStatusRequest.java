package com.experiencefieldops.api.dto;

import com.experiencefieldops.api.enums.ClientStatus;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClientStatusRequest {

    @NotNull(message = "Status é obrigatório")
    private ClientStatus status;
}
