package com.ketrams.v2.dto.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class StudentProfileDto {
    private String fullName;
    private String gender;
    private String disabilityStatus;
    private String disabilityType;
    private String idNumber;
    private String birthCertNumber;
    private String parentName;
    private String parentPhone;
    private String parentRelationship;
    private String county;
    private String subCounty;
    private String ward;
    private String previousSchool;
    private String highestQualification;

    // Single list for all supporting documents
    private List<MultipartFile> documents;
}