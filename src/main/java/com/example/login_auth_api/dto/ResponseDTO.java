package com.example.login_auth_api.dto;

import com.example.login_auth_api.domain.user.Role;

public record ResponseDTO (String name, String token, Role role) {}
