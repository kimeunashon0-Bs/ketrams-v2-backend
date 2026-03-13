package com.ketrams.v2.entity;

import com.ketrams.v2.entity.enums.ApplicationStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "application")
@Data
@NoArgsConstructor
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private StudentProfile student;

    @ManyToOne
    @JoinColumn(name = "institution_id", nullable = false)
    private Institution institution;

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Enumerated(EnumType.STRING)
    private ApplicationStatus status = ApplicationStatus.PENDING_INSTITUTION;

    private String institutionRemarks;
    private String subCountyRemarks;
    private BigDecimal facilitationAmount;   // if facilitated

    @CreationTimestamp
    private LocalDateTime appliedAt;
    private LocalDateTime institutionReviewedAt;
    private LocalDateTime subCountyReviewedAt;
}