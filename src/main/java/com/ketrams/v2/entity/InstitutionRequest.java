package com.ketrams.v2.entity;

import com.ketrams.v2.entity.enums.RequestStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "institution_request")
@Data
@NoArgsConstructor
public class InstitutionRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String institutionName;
    private String category;
    private String adminFullName;
    private String adminPhone;
    private String adminEmail;
    private String adminGender; // M, F, OTHER
    private String adminTitle; // Mr, Mrs, Ms

    @ManyToOne
    @JoinColumn(name = "sub_county_id")
    private SubCounty subCounty;

    @ManyToOne
    @JoinColumn(name = "ward_id")
    private Ward ward;

    @Enumerated(EnumType.STRING)
    private RequestStatus status = RequestStatus.PENDING;

    private String rejectionReason;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime processedAt;
}