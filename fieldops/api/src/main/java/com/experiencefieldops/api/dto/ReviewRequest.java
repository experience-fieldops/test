package com.experiencefieldops.api.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewRequest {

    // Comentário inicial opcional ao abrir a revisão
    @Size(max = 1000, message = "Comentário deve ter no máximo 1000 caracteres")
    private String comment;
}
