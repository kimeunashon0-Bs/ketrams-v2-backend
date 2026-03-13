package com.ketrams.v2.controller.student;

import com.ketrams.v2.dto.request.ApplicationDto;
import com.ketrams.v2.dto.request.StudentProfileDto;
import com.ketrams.v2.dto.response.ApiResponse;
import com.ketrams.v2.entity.AppUser;
import com.ketrams.v2.entity.Application;
import com.ketrams.v2.entity.Course;
import com.ketrams.v2.entity.Institution;
import com.ketrams.v2.entity.StudentProfile;
import com.ketrams.v2.entity.enums.Role;
import com.ketrams.v2.repository.ApplicationRepository;
import com.ketrams.v2.repository.CourseRepository;
import com.ketrams.v2.repository.InstitutionRepository;
import com.ketrams.v2.repository.StudentProfileRepository;
import com.ketrams.v2.service.student.StudentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/student")
public class StudentController {

    private final StudentService studentService;
    private final StudentProfileRepository profileRepository;
    private final InstitutionRepository institutionRepository;
    private final CourseRepository courseRepository;
    private final ApplicationRepository applicationRepository;

    // Constructor injection
    public StudentController(StudentService studentService,
                             StudentProfileRepository profileRepository,
                             InstitutionRepository institutionRepository,
                             CourseRepository courseRepository,
                             ApplicationRepository applicationRepository) {
        this.studentService = studentService;
        this.profileRepository = profileRepository;
        this.institutionRepository = institutionRepository;
        this.courseRepository = courseRepository;
        this.applicationRepository = applicationRepository;
    }

    private AppUser getCurrentUser() {
        return (AppUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @GetMapping("/test")
    public String test() {
        return "Student controller is working";
    }

    @PostMapping(value = "/profile", consumes = {"multipart/form-data"})
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse> createProfile(@ModelAttribute StudentProfileDto dto) {
        AppUser user = getCurrentUser();
        try {
            StudentProfile profile = studentService.createOrUpdateProfile(user, dto);
            return ResponseEntity.ok(ApiResponse.success("Profile saved successfully", profile));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("File upload failed: " + e.getMessage()));
        }
    }

    @GetMapping("/profile")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse> getProfile() {
        AppUser user = getCurrentUser();
        StudentProfile profile = profileRepository.findById(user.getId()).orElse(null);
        if (profile == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Profile not found"));
        }
        return ResponseEntity.ok(ApiResponse.success("Profile retrieved", profile));
    }

    @PostMapping("/applications")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse> submitApplication(@RequestBody ApplicationDto dto) {
        AppUser user = getCurrentUser();

        StudentProfile student = profileRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("Student profile not found. Please complete your profile first."));

        Institution institution = institutionRepository.findById(dto.getInstitutionId())
                .orElseThrow(() -> new RuntimeException("Institution not found"));

        Course course = courseRepository.findById(dto.getCourseId())
                .orElseThrow(() -> new RuntimeException("Course not found"));

        if (!course.getInstitution().getId().equals(institution.getId())) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Course does not belong to the selected institution"));
        }

        Application application = new Application();
        application.setStudent(student);
        application.setInstitution(institution);
        application.setCourse(course);
        applicationRepository.save(application);

        return ResponseEntity.ok(ApiResponse.success("Application submitted successfully", application));
    }

    @GetMapping("/applications")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse> getMyApplications() {
        AppUser user = getCurrentUser();
        List<Application> applications = applicationRepository.findByStudent_UserId(user.getId());
        return ResponseEntity.ok(ApiResponse.success("Applications retrieved", applications));
    }
}