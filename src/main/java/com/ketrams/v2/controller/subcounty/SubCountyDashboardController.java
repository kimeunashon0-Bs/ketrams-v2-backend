package com.ketrams.v2.controller.subcounty;

import com.ketrams.v2.dto.request.BatchFacilitateDto;
import com.ketrams.v2.dto.response.ApiResponse;
import com.ketrams.v2.entity.Application;
import com.ketrams.v2.entity.AppUser;
import com.ketrams.v2.entity.StudentProfile;
import com.ketrams.v2.entity.enums.ApplicationStatus;
import com.ketrams.v2.entity.enums.Role;
import com.ketrams.v2.repository.ApplicationRepository;
import com.ketrams.v2.repository.StudentProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/subcounty")
public class SubCountyDashboardController {

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private StudentProfileRepository studentProfileRepository; // NEW

    private AppUser getCurrentUser() {
        return (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    // ---------- List applications with filters ----------
    @GetMapping("/applications")
    @PreAuthorize("hasRole('SUB_COUNTY')")
    public ResponseEntity<ApiResponse> getApplications(
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) String ward,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) String disabilityStatus,
            @RequestParam(required = false) String status) {

        AppUser user = getCurrentUser();
        String subCountyName = user.getSubCounty();

        List<Application> apps = applicationRepository.findByInstitution_SubCounty_Name(subCountyName);

        if (courseId != null) {
            apps = apps.stream()
                    .filter(a -> a.getCourse().getId().equals(courseId))
                    .collect(Collectors.toList());
        }
        if (ward != null) {
            apps = apps.stream()
                    .filter(a -> ward.equals(a.getStudent().getWard()))
                    .collect(Collectors.toList());
        }
        if (gender != null) {
            apps = apps.stream()
                    .filter(a -> gender.equals(a.getStudent().getGender()))
                    .collect(Collectors.toList());
        }
        if (disabilityStatus != null) {
            apps = apps.stream()
                    .filter(a -> disabilityStatus.equals(a.getStudent().getDisabilityStatus()))
                    .collect(Collectors.toList());
        }
        if (status != null) {
            apps = apps.stream()
                    .filter(a -> a.getStatus().name().equals(status))
                    .collect(Collectors.toList());
        }

        return ResponseEntity.ok(ApiResponse.success("Applications retrieved", apps));
    }

    // ---------- Single actions ----------
    @PostMapping("/applications/{id}/verify")
    @PreAuthorize("hasRole('SUB_COUNTY')")
    public ResponseEntity<ApiResponse> verifyApplication(
            @PathVariable Long id,
            @RequestParam(required = false) String remarks) {

        AppUser user = getCurrentUser();
        String subCountyName = user.getSubCounty();

        Application app = applicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        if (!app.getInstitution().getSubCounty().getName().equals(subCountyName)) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Application does not belong to your sub-county"));
        }

        app.setStatus(ApplicationStatus.VERIFIED);
        app.setSubCountyRemarks(remarks);
        app.setSubCountyReviewedAt(LocalDateTime.now());
        applicationRepository.save(app);

        return ResponseEntity.ok(ApiResponse.success("Application verified", null));
    }

    @PostMapping("/applications/{id}/facilitate")
    @PreAuthorize("hasRole('SUB_COUNTY')")
    public ResponseEntity<ApiResponse> facilitateApplication(
            @PathVariable Long id,
            @RequestParam BigDecimal amount,
            @RequestParam(required = false) String remarks) {

        AppUser user = getCurrentUser();
        String subCountyName = user.getSubCounty();

        Application app = applicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        if (!app.getInstitution().getSubCounty().getName().equals(subCountyName)) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Application does not belong to your sub-county"));
        }

        app.setStatus(ApplicationStatus.FACILITATED);
        app.setFacilitationAmount(amount);
        app.setSubCountyRemarks(remarks);
        app.setSubCountyReviewedAt(LocalDateTime.now());
        applicationRepository.save(app);

        return ResponseEntity.ok(ApiResponse.success("Application facilitated", null));
    }

    @PostMapping("/applications/{id}/flag")
    @PreAuthorize("hasRole('SUB_COUNTY')")
    public ResponseEntity<ApiResponse> flagApplication(
            @PathVariable Long id,
            @RequestParam(required = false) String remarks) {

        AppUser user = getCurrentUser();
        String subCountyName = user.getSubCounty();

        Application app = applicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        if (!app.getInstitution().getSubCounty().getName().equals(subCountyName)) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Application does not belong to your sub-county"));
        }

        app.setStatus(ApplicationStatus.PENDING_CLARIFICATION);
        app.setSubCountyRemarks(remarks);
        app.setSubCountyReviewedAt(LocalDateTime.now());
        applicationRepository.save(app);

        return ResponseEntity.ok(ApiResponse.success("Application flagged for clarification", null));
    }

    @PostMapping("/applications/{id}/waitlist")
    @PreAuthorize("hasRole('SUB_COUNTY')")
    public ResponseEntity<ApiResponse> waitlistApplication(
            @PathVariable Long id,
            @RequestParam(required = false) String remarks) {

        AppUser user = getCurrentUser();
        String subCountyName = user.getSubCounty();

        Application app = applicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        if (!app.getInstitution().getSubCounty().getName().equals(subCountyName)) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Application does not belong to your sub-county"));
        }

        app.setStatus(ApplicationStatus.WAITLISTED);
        app.setSubCountyRemarks(remarks);
        app.setSubCountyReviewedAt(LocalDateTime.now());
        applicationRepository.save(app);

        return ResponseEntity.ok(ApiResponse.success("Application waitlisted", null));
    }

    // ---------- Batch facilitation ----------
    @PostMapping("/applications/batch/facilitate")
    @PreAuthorize("hasRole('SUB_COUNTY')")
    public ResponseEntity<ApiResponse> batchFacilitate(@RequestBody BatchFacilitateDto dto) {

        AppUser user = getCurrentUser();
        String subCountyName = user.getSubCounty();

        List<Application> apps = applicationRepository.findAllById(dto.getApplicationIds());
        List<String> errors = new ArrayList<>();

        for (Application app : apps) {
            if (!app.getInstitution().getSubCounty().getName().equals(subCountyName)) {
                errors.add("Application " + app.getId() + " does not belong to your sub-county");
                continue;
            }
            if (app.getStatus() != ApplicationStatus.VERIFIED) {
                errors.add("Application " + app.getId() + " is not verified");
                continue;
            }
            app.setStatus(ApplicationStatus.FACILITATED);
            app.setFacilitationAmount(dto.getAmount());
            app.setSubCountyRemarks(dto.getRemarks());
            app.setSubCountyReviewedAt(LocalDateTime.now());
            applicationRepository.save(app);
        }

        if (errors.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.success("All applications facilitated successfully", null));
        } else {
            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                    .body(ApiResponse.success("Some applications could not be facilitated", errors));
        }
    }

    // ---------- CSV Export ----------
    @GetMapping("/applications/export/csv")
    @PreAuthorize("hasRole('SUB_COUNTY')")
    public ResponseEntity<byte[]> exportApplicationsCsv(
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) String ward,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) String disabilityStatus,
            @RequestParam(required = false) String status) throws IOException {

        AppUser user = getCurrentUser();
        String subCountyName = user.getSubCounty();

        List<Application> apps = applicationRepository.findByInstitution_SubCounty_Name(subCountyName);

        // Apply filters
        if (courseId != null) {
            apps = apps.stream().filter(a -> a.getCourse().getId().equals(courseId)).toList();
        }
        if (ward != null) {
            apps = apps.stream().filter(a -> ward.equals(a.getStudent().getWard())).toList();
        }
        if (gender != null) {
            apps = apps.stream().filter(a -> gender.equals(a.getStudent().getGender())).toList();
        }
        if (disabilityStatus != null) {
            apps = apps.stream().filter(a -> disabilityStatus.equals(a.getStudent().getDisabilityStatus())).toList();
        }
        if (status != null) {
            apps = apps.stream().filter(a -> a.getStatus().name().equals(status)).toList();
        }

        // Generate CSV
        StringBuilder csv = new StringBuilder();
        csv.append("ID,Student Name,Gender,PWD,Ward,Institution,Course,Status,Applied Date,Facilitation Amount\n");
        for (Application app : apps) {
            csv.append(app.getId()).append(",")
                    .append(app.getStudent().getFullName()).append(",")
                    .append(app.getStudent().getGender()).append(",")
                    .append(app.getStudent().getDisabilityStatus()).append(",")
                    .append(app.getStudent().getWard()).append(",")
                    .append(app.getInstitution().getName()).append(",")
                    .append(app.getCourse().getName()).append(",")
                    .append(app.getStatus()).append(",")
                    .append(app.getAppliedAt()).append(",")
                    .append(app.getFacilitationAmount() != null ? app.getFacilitationAmount() : "").append("\n");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        headers.setContentDispositionFormData("attachment", "subcounty-applications.csv");

        return new ResponseEntity<>(csv.toString().getBytes(), headers, HttpStatus.OK);
    }

    // ---------- Reports Summary (enhanced) ----------
    @GetMapping("/reports/summary")
    @PreAuthorize("hasRole('SUB_COUNTY')")
    public ResponseEntity<ApiResponse> getReportSummary() {
        AppUser user = getCurrentUser();
        String subCountyName = user.getSubCounty();

        List<Application> apps = applicationRepository.findByInstitution_SubCounty_Name(subCountyName);

        Map<String, Object> report = new HashMap<>();

        // By status
        Map<String, Long> byStatus = apps.stream()
                .filter(a -> a.getStatus() != null)
                .collect(Collectors.groupingBy(a -> a.getStatus().name(), Collectors.counting()));
        long nullStatusCount = apps.stream().filter(a -> a.getStatus() == null).count();
        if (nullStatusCount > 0) byStatus.put("UNKNOWN", nullStatusCount);
        report.put("byStatus", byStatus);

        // By gender
        Map<String, Long> byGender = apps.stream()
                .filter(a -> a.getStudent() != null && a.getStudent().getGender() != null)
                .collect(Collectors.groupingBy(a -> a.getStudent().getGender(), Collectors.counting()));
        long nullGenderCount = apps.stream().filter(a -> a.getStudent() == null || a.getStudent().getGender() == null).count();
        if (nullGenderCount > 0) byGender.put("UNKNOWN", nullGenderCount);
        report.put("byGender", byGender);

        // By disability
        Map<String, Long> byDisability = apps.stream()
                .filter(a -> a.getStudent() != null && a.getStudent().getDisabilityStatus() != null)
                .collect(Collectors.groupingBy(a -> a.getStudent().getDisabilityStatus(), Collectors.counting()));
        long nullDisabilityCount = apps.stream().filter(a -> a.getStudent() == null || a.getStudent().getDisabilityStatus() == null).count();
        if (nullDisabilityCount > 0) byDisability.put("UNKNOWN", nullDisabilityCount);
        report.put("byDisability", byDisability);

        // By ward
        Map<String, Long> byWard = apps.stream()
                .filter(a -> a.getStudent() != null && a.getStudent().getWard() != null)
                .collect(Collectors.groupingBy(a -> a.getStudent().getWard(), Collectors.counting()));
        long nullWardCount = apps.stream().filter(a -> a.getStudent() == null || a.getStudent().getWard() == null).count();
        if (nullWardCount > 0) byWard.put("UNKNOWN", nullWardCount);
        report.put("byWard", byWard);

        // By institution (NEW)
        Map<String, Long> byInstitution = apps.stream()
                .filter(a -> a.getInstitution() != null && a.getInstitution().getName() != null)
                .collect(Collectors.groupingBy(a -> a.getInstitution().getName(), Collectors.counting()));
        report.put("byInstitution", byInstitution);

        // By course (NEW)
        Map<String, Long> byCourse = apps.stream()
                .filter(a -> a.getCourse() != null && a.getCourse().getName() != null)
                .collect(Collectors.groupingBy(a -> a.getCourse().getName(), Collectors.counting()));
        report.put("byCourse", byCourse);

        report.put("totalApplications", apps.size());

        return ResponseEntity.ok(ApiResponse.success("Report generated", report));
    }

    // ---------- Get Student Profile (NEW) ----------
    @GetMapping("/students/{studentId}")
    @PreAuthorize("hasRole('SUB_COUNTY')")
    public ResponseEntity<StudentProfile> getStudentProfile(@PathVariable Long studentId) {
        AppUser user = getCurrentUser();
        String subCountyName = user.getSubCounty();

        // Verify that this student has applied to an institution in this sub‑county
        boolean hasApplication = applicationRepository.existsByStudent_UserIdAndInstitution_SubCounty_Name(studentId, subCountyName);
        if (!hasApplication) {
            throw new RuntimeException("Not authorized to view this student");
        }

        StudentProfile profile = studentProfileRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        return ResponseEntity.ok(profile);
    }
}