package com.ketrams.v2.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateInstitutionDto {
    @NotBlank
    private String name;
    private String category;
    @NotNull
    private Long subCountyId;
    @NotNull
    private Long wardId;
}