package com.ketrams.v2.dto.response;

import lombok.Data;

@Data
public class JwtResponse {
    private String token;
    private String role;
    private Long userId;
    private String subCounty; // for SUB_COUNTY users
    private String fullName;
    private String gender;
    private String title;
    private Long institutionId;
    private String institutionName;
}