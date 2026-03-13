package com.ketrams.v2.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class InstitutionRequestDto {
    @NotBlank
    private String institutionName;
    private String category;
    @NotBlank
    private String adminFullName;
    @NotBlank
    @Pattern(regexp = "^[0-9]{10,15}$")
    private String adminPhone;
    @Email
    @NotBlank
    private String adminEmail;
    @NotBlank
    private String adminGender;
    @NotBlank
    private String adminTitle;
    @NotNull
    private Long subCountyId;
    @NotNull
    private Long wardId;
}