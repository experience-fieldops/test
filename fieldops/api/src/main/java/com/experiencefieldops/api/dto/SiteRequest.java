package com.experiencefieldops.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SiteRequest {

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 150, message = "Nome deve ter no máximo 150 caracteres")
    private String name;

    @Size(max = 255, message = "Endereço deve ter no máximo 255 caracteres")
    private String address;

    private Double latitude;

    private Double longitude;
}
