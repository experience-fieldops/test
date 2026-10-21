package com.experiencefieldops.api.dto;

import com.experiencefieldops.api.enums.ResponseType;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TemplateItemRequest {

    @Size(max = 50, message = "Código deve ter no máximo 50 caracteres")
    private String code;

    @NotBlank(message = "Título é obrigatório")
    @Size(max = 255, message = "Título deve ter no máximo 255 caracteres")
    private String title;

    @Size(max = 500, message = "Descrição deve ter no máximo 500 caracteres")
    private String description;

    @NotNull(message = "Tipo de resposta é obrigatório")
    private ResponseType responseType;

    @NotNull(message = "Campo 'required' é obrigatório")
    private Boolean required;

    @NotNull(message = "Campo 'observationRequiredOnFailure' é obrigatório")
    private Boolean observationRequiredOnFailure;

    @NotNull(message = "Campo 'evidenceRequiredOnFailure' é obrigatório")
    private Boolean evidenceRequiredOnFailure;

    // JSON com as opções (necessário para SINGLE_CHOICE)
    private String optionsJson;

    @NotNull(message = "Ordem é obrigatória")
    @Min(value = 0, message = "Ordem deve ser maior ou igual a 0")
    private Integer displayOrder;
}
