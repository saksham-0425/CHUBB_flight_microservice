package com.auth.auth_service.dto;
import lombok.*;
@Data
public
class LoginRequest {
    private String email;
    private String password;
}
