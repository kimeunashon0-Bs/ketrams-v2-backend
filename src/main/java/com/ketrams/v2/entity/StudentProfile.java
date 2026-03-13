package com.ketrams.v2.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "student_profile")
@Data
@NoArgsConstructor
public class StudentProfile {

    @Id
    private Long userId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"}) // Prevents proxy serialization
    private AppUser user;

    private String fullName;
    private String gender;
    private String disabilityStatus;
    private String disabilityType;

    @Getter(AccessLevel.NONE)
    private String passportPhotoUrl;
    private String idNumber;
    private String birthCertNumber;
    @Getter(AccessLevel.NONE)
    private String idDocumentUrl;
    private String parentName;
    private String parentPhone;
    private String parentRelationship;
    private String county;
    private String subCounty;
    private String ward;
    private String previousSchool;
    private String highestQualification;

    @Getter(AccessLevel.NONE)
    private String academicCertificatesUrl;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @Column(columnDefinition = "TEXT")
    private String documentUrls;

    // --- Custom getters for single fields to extract filename from legacy paths ---
    @JsonProperty("passportPhotoUrl")
    public String getPassportPhotoUrl() {
        return extractFilename(this.passportPhotoUrl);
    }

    public void setPassportPhotoUrl(String passportPhotoUrl) {
        this.passportPhotoUrl = passportPhotoUrl;
    }

    @JsonProperty("idDocumentUrl")
    public String getIdDocumentUrl() {
        return extractFilename(this.idDocumentUrl);
    }

    public void setIdDocumentUrl(String idDocumentUrl) {
        this.idDocumentUrl = idDocumentUrl;
    }

    @JsonProperty("academicCertificatesUrl")
    public String getAcademicCertificatesUrl() {
        return extractFilename(this.academicCertificatesUrl);
    }

    public void setAcademicCertificatesUrl(String academicCertificatesUrl) {
        this.academicCertificatesUrl = academicCertificatesUrl;
    }

    private String extractFilename(String path) {
        if (path == null || path.isEmpty()) return path;
        if (path.contains(File.separator)) {
            return path.substring(path.lastIndexOf(File.separator) + 1);
        }
        return path;
    }

    @JsonProperty("documentUrls")
    public List<String> getDocumentUrlsList() {
        if (this.documentUrls == null || this.documentUrls.isEmpty()) {
            return new ArrayList<>();
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            List<String> list = mapper.readValue(this.documentUrls, new TypeReference<List<String>>() {});
            return list.stream()
                    .map(s -> s.contains(File.separator) ? s.substring(s.lastIndexOf(File.separator) + 1) : s)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    public void setDocumentUrlsList(List<String> urls) {
        if (urls == null) {
            this.documentUrls = null;
            return;
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            this.documentUrls = mapper.writeValueAsString(urls);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize document URLs", e);
        }
    }
}