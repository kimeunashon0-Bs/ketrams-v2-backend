package com.ketrams.v2.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResetPasswordDto {
    @NotBlank
    private String phoneNumber;

    @NotBlank
    private String newPassword;
}