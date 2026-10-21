package com.experiencefieldops.api.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponse {

    private String token;
    private String type;
    private Long expiresIn;
    private UserResponse user;
}
