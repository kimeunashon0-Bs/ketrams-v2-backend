package com.ketrams.v2.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OtpVerifyDto {
    @NotBlank
    private String phoneNumber;
    @NotBlank
    private String otpCode;
    private String email; // optional
}