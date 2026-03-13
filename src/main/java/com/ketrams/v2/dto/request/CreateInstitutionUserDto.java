package com.ketrams.v2.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateInstitutionUserDto {
    @NotBlank
    private String phoneNumber;
    @NotBlank
    @Size(min = 6)
    private String password;
    @NotNull
    private Long institutionId;
}