package com.ketrams.v2.controller.institution;

import com.ketrams.v2.dto.request.CourseDto;
import com.ketrams.v2.dto.response.ApiResponse;
import com.ketrams.v2.entity.Application;
import com.ketrams.v2.entity.AppUser;
import com.ketrams.v2.entity.Course;
import com.ketrams.v2.entity.Institution;
import com.ketrams.v2.entity.StudentProfile;
import com.ketrams.v2.entity.enums.ApplicationStatus;
import com.ketrams.v2.entity.enums.Role;
import com.ketrams.v2.repository.ApplicationRepository;
import com.ketrams.v2.repository.CourseRepository;
import com.ketrams.v2.repository.InstitutionRepository;
import com.ketrams.v2.repository.StudentProfileRepository;
import com.ketrams.v2.service.file.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/institution")
public class InstitutionController {

    @Autowired
    private InstitutionRepository institutionRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private StudentProfileRepository studentProfileRepository;

    // ---------- Helper ----------
    private Institution getCurrentInstitution() {
        AppUser user = (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!user.getRole().equals(Role.INSTITUTION)) {
            throw new RuntimeException("User is not an institution");
        }
        if (user.getInstitution() == null) {
            throw new RuntimeException("User is not associated with any institution");
        }
        return user.getInstitution();
    }

    // ---------- Public endpoints (any authenticated user) ----------
    @GetMapping
    public List<Institution> getAllInstitutions() {
        return institutionRepository.findAll();
    }

    @GetMapping("/{institutionId}/courses")
    public List<Course> getCoursesByInstitution(@PathVariable Long institutionId) {
        // Only return enabled courses for public view
        return courseRepository.findByInstitutionIdAndEnabledTrue(institutionId);
    }

    // ---------- Institution dashboard ----------
    @GetMapping("/applications")
    @PreAuthorize("hasRole('INSTITUTION')")
    public ResponseEntity<ApiResponse> getApplications(
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) String subCounty,
            @RequestParam(required = false) String disabilityStatus) {

        Institution inst = getCurrentInstitution();
        List<Application> apps = applicationRepository.findByInstitutionId(inst.getId());

        if (courseId != null) {
            apps = apps.stream()
                    .filter(a -> a.getCourse().getId().equals(courseId))
                    .collect(Collectors.toList());
        }
        if (subCounty != null) {
            apps = apps.stream()
                    .filter(a -> subCounty.equals(a.getStudent().getSubCounty()))
                    .collect(Collectors.toList());
        }
        if (disabilityStatus != null) {
            apps = apps.stream()
                    .filter(a -> disabilityStatus.equals(a.getStudent().getDisabilityStatus()))
                    .collect(Collectors.toList());
        }

        return ResponseEntity.ok(ApiResponse.success("Applications retrieved", apps));
    }

    @PostMapping("/applications/{id}/approve")
    @PreAuthorize("hasRole('INSTITUTION')")
    public ResponseEntity<ApiResponse> approveApplication(
            @PathVariable Long id,
            @RequestParam(required = false) String remarks) {

        Institution inst = getCurrentInstitution();
        Application app = applicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        if (!app.getInstitution().getId().equals(inst.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Not authorized to review this application"));
        }

        app.setStatus(ApplicationStatus.APPROVED_BY_INSTITUTION);
        app.setInstitutionRemarks(remarks);
        app.setInstitutionReviewedAt(LocalDateTime.now());
        applicationRepository.save(app);

        return ResponseEntity.ok(ApiResponse.success("Application approved", null));
    }

    @PostMapping("/applications/{id}/reject")
    @PreAuthorize("hasRole('INSTITUTION')")
    public ResponseEntity<ApiResponse> rejectApplication(
            @PathVariable Long id,
            @RequestParam String remarks) {

        Institution inst = getCurrentInstitution();
        Application app = applicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        if (!app.getInstitution().getId().equals(inst.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Not authorized"));
        }

        app.setStatus(ApplicationStatus.REJECTED_BY_INSTITUTION);
        app.setInstitutionRemarks(remarks);
        app.setInstitutionReviewedAt(LocalDateTime.now());
        applicationRepository.save(app);

        return ResponseEntity.ok(ApiResponse.success("Application rejected", null));
    }

    // ---------- Course Management ----------
    @GetMapping("/courses")
    @PreAuthorize("hasRole('INSTITUTION')")
    public ResponseEntity<List<Course>> getMyCourses() {
        Institution inst = getCurrentInstitution();
        List<Course> courses = courseRepository.findByInstitutionId(inst.getId());
        return ResponseEntity.ok(courses);
    }

    @PostMapping("/courses")
    @PreAuthorize("hasRole('INSTITUTION')")
    public ResponseEntity<ApiResponse> addCourse(@RequestBody CourseDto dto) {
        Institution inst = getCurrentInstitution();
        Course course = new Course();
        course.setName(dto.getName());
        course.setLevel(dto.getLevel());
        course.setCategory(dto.getCategory());
        course.setInstitution(inst);
        course.setEnabled(true); // default enabled
        courseRepository.save(course);
        return ResponseEntity.ok(ApiResponse.success("Course added successfully", course));
    }

    @PutMapping("/courses/{id}")
    @PreAuthorize("hasRole('INSTITUTION')")
    public ResponseEntity<ApiResponse> updateCourse(@PathVariable Long id, @RequestBody CourseDto dto) {
        Institution inst = getCurrentInstitution();
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        if (!course.getInstitution().getId().equals(inst.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Not authorized to modify this course"));
        }
        course.setName(dto.getName());
        course.setLevel(dto.getLevel());
        course.setCategory(dto.getCategory());
        courseRepository.save(course);
        return ResponseEntity.ok(ApiResponse.success("Course updated successfully", course));
    }

    @DeleteMapping("/courses/{id}")
    @PreAuthorize("hasRole('INSTITUTION')")
    public ResponseEntity<ApiResponse> deleteCourse(@PathVariable Long id) {
        Institution inst = getCurrentInstitution();
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        if (!course.getInstitution().getId().equals(inst.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Not authorized to delete this course"));
        }
        courseRepository.delete(course);
        return ResponseEntity.ok(ApiResponse.success("Course deleted successfully", null));
    }

    // NEW: Toggle course enabled/disabled
    @PatchMapping("/courses/{id}/toggle")
    @PreAuthorize("hasRole('INSTITUTION')")
    public ResponseEntity<ApiResponse> toggleCourse(@PathVariable Long id) {
        Institution inst = getCurrentInstitution();
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        if (!course.getInstitution().getId().equals(inst.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Not authorized to modify this course"));
        }
        course.setEnabled(!course.isEnabled());
        courseRepository.save(course);
        return ResponseEntity.ok(ApiResponse.success("Course status toggled", course));
    }

    // ---------- Course Document Management ----------
    @PostMapping("/courses/{courseId}/documents")
    @PreAuthorize("hasRole('INSTITUTION')")
    public ResponseEntity<ApiResponse> uploadCourseDocuments(
            @PathVariable Long courseId,
            @RequestParam("files") List<MultipartFile> files) {

        Institution inst = getCurrentInstitution();
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        if (!course.getInstitution().getId().equals(inst.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Not authorized to modify this course"));
        }

        List<String> uploadedFilenames = new ArrayList<>();
        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                try {
                    String filename = fileStorageService.storeFile(file);
                    uploadedFilenames.add(filename);
                } catch (IOException e) {
                    throw new RuntimeException("Failed to store file: " + file.getOriginalFilename(), e);
                }
            }
        }

        List<String> currentDocs = course.getDocumentUrlsList();
        currentDocs.addAll(uploadedFilenames);
        course.setDocumentUrlsList(currentDocs);
        courseRepository.save(course);

        return ResponseEntity.ok(ApiResponse.success("Documents uploaded successfully", course.getDocumentUrlsList()));
    }

    @DeleteMapping("/courses/{courseId}/documents")
    @PreAuthorize("hasRole('INSTITUTION')")
    public ResponseEntity<ApiResponse> deleteCourseDocument(
            @PathVariable Long courseId,
            @RequestParam String filename) {

        Institution inst = getCurrentInstitution();
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        if (!course.getInstitution().getId().equals(inst.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Not authorized to modify this course"));
        }

        List<String> currentDocs = course.getDocumentUrlsList();
        boolean removed = currentDocs.removeIf(f -> f.equals(filename));
        if (!removed) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("File not found"));
        }
        course.setDocumentUrlsList(currentDocs);
        courseRepository.save(course);

        return ResponseEntity.ok(ApiResponse.success("Document deleted successfully", null));
    }

    // ---------- Reporting ----------
    @GetMapping("/reports/summary")
    @PreAuthorize("hasRole('INSTITUTION')")
    public ResponseEntity<ApiResponse> getReportSummary() {
        Institution inst = getCurrentInstitution();
        Map<String, Object> report = new HashMap<>();

        List<Object[]> byStatus = applicationRepository.countByStatusForInstitution(inst.getId());
        report.put("byStatus", byStatus);

        List<Object[]> byCourse = applicationRepository.countByCourseForInstitution(inst.getId());
        report.put("byCourse", byCourse);

        List<Object[]> bySubCounty = applicationRepository.countBySubCountyForInstitution(inst.getId());
        report.put("bySubCounty", bySubCounty);

        List<Object[]> byDisabilityStatus = applicationRepository.countByDisabilityStatusForInstitution(inst.getId());
        report.put("byDisabilityStatus", byDisabilityStatus);

        return ResponseEntity.ok(ApiResponse.success("Report generated", report));
    }

    @GetMapping("/reports/export/csv")
    @PreAuthorize("hasRole('INSTITUTION')")
    public ResponseEntity<byte[]> exportReportCsv(
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String subCounty,
            @RequestParam(required = false) String disabilityStatus) throws IOException {

        Institution inst = getCurrentInstitution();
        List<Application> apps = applicationRepository.findByInstitutionId(inst.getId());

        // Apply filters
        if (courseId != null) {
            apps = apps.stream().filter(a -> a.getCourse().getId().equals(courseId)).toList();
        }
        if (status != null) {
            apps = apps.stream().filter(a -> a.getStatus().name().equals(status)).toList();
        }
        if (subCounty != null) {
            apps = apps.stream().filter(a -> subCounty.equals(a.getStudent().getSubCounty())).toList();
        }
        if (disabilityStatus != null) {
            apps = apps.stream().filter(a -> disabilityStatus.equals(a.getStudent().getDisabilityStatus())).toList();
        }

        // Generate CSV
        StringBuilder csv = new StringBuilder();
        csv.append("Application ID,Student Name,Course,Status,Applied Date\n");
        for (Application app : apps) {
            csv.append(app.getId()).append(",")
                    .append(app.getStudent().getFullName()).append(",")
                    .append(app.getCourse().getName()).append(",")
                    .append(app.getStatus()).append(",")
                    .append(app.getAppliedAt()).append("\n");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        headers.setContentDispositionFormData("attachment", "report.csv");

        return new ResponseEntity<>(csv.toString().getBytes(), headers, HttpStatus.OK);
    }

    // ---------- Student Profile (for institution) ----------
    @GetMapping("/students/{studentId}")
    @PreAuthorize("hasRole('INSTITUTION')")
    public ResponseEntity<StudentProfile> getStudentProfile(@PathVariable Long studentId) {
        Institution inst = getCurrentInstitution();
        boolean hasApplied = applicationRepository.existsByStudent_UserIdAndInstitutionId(studentId, inst.getId());
        if (!hasApplied) {
            throw new RuntimeException("Not authorized to view this student");
        }
        StudentProfile profile = studentProfileRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        return ResponseEntity.ok(profile);
    }
}