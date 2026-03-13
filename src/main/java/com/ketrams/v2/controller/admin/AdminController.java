package com.ketrams.v2.controller.admin;

import com.ketrams.v2.dto.request.ApproveRequestDto;
import com.ketrams.v2.dto.request.CreateInstitutionDto;
import com.ketrams.v2.dto.request.CreateInstitutionUserDto;
import com.ketrams.v2.dto.response.ApiResponse;
import com.ketrams.v2.entity.*;
import com.ketrams.v2.entity.enums.RequestStatus;
import com.ketrams.v2.entity.enums.Role;
import com.ketrams.v2.repository.*;
import com.ketrams.v2.service.EmailService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final InstitutionRepository institutionRepository;
    private final SubCountyRepository subCountyRepository;
    private final WardRepository wardRepository;
    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final InstitutionRequestRepository requestRepository;
    private final EmailService emailService;  // NEW

    // Constructor injection
    public AdminController(
            InstitutionRepository institutionRepository,
            SubCountyRepository subCountyRepository,
            WardRepository wardRepository,
            AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder,
            InstitutionRequestRepository requestRepository,
            EmailService emailService) {  // NEW
        this.institutionRepository = institutionRepository;
        this.subCountyRepository = subCountyRepository;
        this.wardRepository = wardRepository;
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.requestRepository = requestRepository;
        this.emailService = emailService;  // NEW
    }

    // ========== Institution Request Management ==========
    @GetMapping("/institution-requests")
    public ResponseEntity<List<InstitutionRequest>> getPendingRequests() {
        return ResponseEntity.ok(requestRepository.findByStatus(RequestStatus.PENDING));
    }

    @GetMapping("/institution-requests/{id}")
    public ResponseEntity<InstitutionRequest> getRequestDetails(@PathVariable Long id) {
        InstitutionRequest request = requestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Request not found"));
        return ResponseEntity.ok(request);
    }

    @PostMapping("/institution-requests/process")
    public ResponseEntity<ApiResponse> processRequest(@Valid @RequestBody ApproveRequestDto dto) {
        InstitutionRequest request = requestRepository.findById(dto.getRequestId())
                .orElseThrow(() -> new RuntimeException("Request not found"));

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("requestId", request.getId());
        responseData.put("status", dto.isApproved() ? "APPROVED" : "REJECTED");

        if (dto.isApproved()) {
            // Create institution
            Institution institution = new Institution();
            institution.setName(request.getInstitutionName());
            institution.setCategory(request.getCategory());
            institution.setSubCounty(request.getSubCounty());
            institution.setWard(request.getWard());
            institutionRepository.save(institution);

            // Create admin user
            AppUser user = new AppUser();
            user.setPhoneNumber(request.getAdminPhone());
            user.setEmail(request.getAdminEmail());
            user.setFullName(request.getAdminFullName());
            user.setGender(request.getAdminGender());
            user.setTitle(request.getAdminTitle());
            String tempPassword = generateTemporaryPassword();
            user.setPasswordHash(passwordEncoder.encode(tempPassword));
            user.setRole(Role.INSTITUTION);
            user.setEnabled(true);
            user.setInstitution(institution);
            appUserRepository.save(user);

            // Send email with credentials (NEW)
            emailService.sendCredentialsEmail(request.getAdminEmail(), user.getPhoneNumber(), tempPassword);

            // Add credentials to response (for display in modal)
            responseData.put("userPhone", user.getPhoneNumber());
            responseData.put("tempPassword", tempPassword);

            request.setStatus(RequestStatus.APPROVED);
        } else {
            request.setStatus(RequestStatus.REJECTED);
            request.setRejectionReason(dto.getRejectionReason());
            // Optionally send rejection email
        }
        request.setProcessedAt(LocalDateTime.now());
        requestRepository.save(request);

        return ResponseEntity.ok(ApiResponse.success("Request processed", responseData));
    }

    private String generateTemporaryPassword() {
        // Simple random password – in production use SecureRandom
        return "temp" + System.currentTimeMillis();
    }

    // ========== Institution Management ==========
    @PostMapping("/institutions")
    public ResponseEntity<ApiResponse> createInstitution(@Valid @RequestBody CreateInstitutionDto dto) {
        SubCounty subCounty = subCountyRepository.findById(dto.getSubCountyId())
                .orElseThrow(() -> new RuntimeException("Sub-county not found"));
        Ward ward = wardRepository.findById(dto.getWardId())
                .orElseThrow(() -> new RuntimeException("Ward not found"));

        Institution institution = new Institution();
        institution.setName(dto.getName());
        institution.setCategory(dto.getCategory());
        institution.setSubCounty(subCounty);
        institution.setWard(ward);

        institutionRepository.save(institution);
        return ResponseEntity.ok(ApiResponse.success("Institution created", institution));
    }

    @GetMapping("/institutions")
    public ResponseEntity<List<Institution>> getAllInstitutions() {
        return ResponseEntity.ok(institutionRepository.findAll());
    }

    @GetMapping("/institutions/{id}")
    public ResponseEntity<Institution> getInstitution(@PathVariable Long id) {
        Institution institution = institutionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Institution not found"));
        return ResponseEntity.ok(institution);
    }

    @PutMapping("/institutions/{id}")
    public ResponseEntity<ApiResponse> updateInstitution(@PathVariable Long id, @Valid @RequestBody CreateInstitutionDto dto) {
        Institution institution = institutionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Institution not found"));
        SubCounty subCounty = subCountyRepository.findById(dto.getSubCountyId())
                .orElseThrow(() -> new RuntimeException("Sub-county not found"));
        Ward ward = wardRepository.findById(dto.getWardId())
                .orElseThrow(() -> new RuntimeException("Ward not found"));

        institution.setName(dto.getName());
        institution.setCategory(dto.getCategory());
        institution.setSubCounty(subCounty);
        institution.setWard(ward);

        institutionRepository.save(institution);
        return ResponseEntity.ok(ApiResponse.success("Institution updated", institution));
    }

    @PatchMapping("/institutions/{id}/toggle")
    public ResponseEntity<ApiResponse> toggleInstitutionStatus(@PathVariable Long id) {
        Institution institution = institutionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Institution not found"));
        institution.setEnabled(!institution.isEnabled());
        institutionRepository.save(institution);
        return ResponseEntity.ok(ApiResponse.success("Institution status toggled", institution));
    }

    // ========== Institution User Management ==========
    @PostMapping("/institution-users")
    public ResponseEntity<ApiResponse> createInstitutionUser(@Valid @RequestBody CreateInstitutionUserDto dto) {
        if (appUserRepository.findByPhoneNumber(dto.getPhoneNumber()).isPresent()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Phone number already registered"));
        }

        Institution institution = institutionRepository.findById(dto.getInstitutionId())
                .orElseThrow(() -> new RuntimeException("Institution not found"));

        AppUser user = new AppUser();
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        user.setRole(Role.INSTITUTION);
        user.setEnabled(true);
        user.setInstitution(institution);

        appUserRepository.save(user);
        return ResponseEntity.ok(ApiResponse.success("Institution user created", user));
    }

    @GetMapping("/institution-users")
    public ResponseEntity<List<AppUser>> getAllInstitutionUsers() {
        List<AppUser> users = appUserRepository.findByRole(Role.INSTITUTION);
        return ResponseEntity.ok(users);
    }

    @PatchMapping("/institution-users/{id}/toggle-status")
    public ResponseEntity<ApiResponse> toggleUserStatus(@PathVariable Long id) {
        AppUser user = appUserRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!user.getRole().equals(Role.INSTITUTION)) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("User is not an institution admin"));
        }
        user.setEnabled(!user.isEnabled());
        appUserRepository.save(user);
        return ResponseEntity.ok(ApiResponse.success("User status toggled", user));
    }
}