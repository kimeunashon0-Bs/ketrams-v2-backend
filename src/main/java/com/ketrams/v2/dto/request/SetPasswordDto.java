package com.ketrams.v2.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SetPasswordDto {
    @NotBlank
    private String phoneNumber;
    @NotBlank
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;
}