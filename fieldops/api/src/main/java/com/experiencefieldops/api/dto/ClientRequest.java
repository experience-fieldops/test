package com.experiencefieldops.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClientRequest {

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 150, message = "Nome deve ter no máximo 150 caracteres")
    private String name;

    @Size(max = 20, message = "Documento deve ter no máximo 20 caracteres")
    private String document;

    @Email(message = "E-mail deve ter formato válido")
    @Size(max = 150, message = "E-mail deve ter no máximo 150 caracteres")
    private String email;

    @Size(max = 30, message = "Telefone deve ter no máximo 30 caracteres")
    private String phone;
}
