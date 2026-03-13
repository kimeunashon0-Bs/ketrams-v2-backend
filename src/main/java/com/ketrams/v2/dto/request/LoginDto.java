package com.ketrams.v2.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginDto {
    @NotBlank
    private String phoneNumber;
    @NotBlank
    private String password;
    private boolean rememberMe; // default false
}