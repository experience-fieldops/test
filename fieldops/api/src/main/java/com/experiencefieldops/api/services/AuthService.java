package com.experiencefieldops.api.services;

import com.experiencefieldops.api.dto.LoginRequest;
import com.experiencefieldops.api.dto.LoginResponse;
import com.experiencefieldops.api.dto.UserRequest;
import com.experiencefieldops.api.dto.UserResponse;

public interface AuthService {

    LoginResponse login(LoginRequest request);

    UserResponse register(UserRequest request);
}
