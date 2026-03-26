package com.ketrams.v2.controller.auth;

import com.ketrams.v2.dto.request.*;
import com.ketrams.v2.dto.response.ApiResponse;
import com.ketrams.v2.dto.response.JwtResponse;
import com.ketrams.v2.entity.AppUser;
import com.ketrams.v2.entity.OtpRequest;
import com.ketrams.v2.entity.enums.Role;
import com.ketrams.v2.repository.AppUserRepository;
import com.ketrams.v2.security.JwtUtil;
import com.ketrams.v2.service.auth.OtpService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private OtpService otpService;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/request-otp")
    public ResponseEntity<ApiResponse> requestOtp(@Valid @RequestBody OtpRequestDto dto) {
        String phone = dto.getPhoneNumber();

        // Check if user already exists and is fully registered (has password)
        Optional<AppUser> existingUser = appUserRepository.findByPhoneNumber(phone);
        if (existingUser.isPresent() && existingUser.get().getPasswordHash() != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("This phone number is already registered. Please use 'Forgot Password' to reset your password."));
        }

        OtpRequest otpRequest = otpService.createOtpRequest(phone);
        otpService.sendOtp(phone, dto.getEmail(), dto.getDeliveryMethod(), otpRequest.getOtpCode());
        return ResponseEntity.ok(ApiResponse.success("OTP sent successfully", null));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse> verifyOtp(@Valid @RequestBody OtpVerifyDto dto) {
        boolean verified = otpService.verifyOtp(dto.getPhoneNumber(), dto.getOtpCode());
        if (!verified) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Invalid or expired OTP"));
        }

        Optional<AppUser> existingUser = appUserRepository.findByPhoneNumber(dto.getPhoneNumber());

        // If user exists AND already has a password, they are fully registered – block
        if (existingUser.isPresent() && existingUser.get().getPasswordHash() != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("This phone number is already registered. Please use 'Forgot Password' to reset your password."));
        }

        // Otherwise, either create new user or continue with existing (incomplete) user
        AppUser user = existingUser.orElseGet(() -> {
            AppUser newUser = new AppUser();
            newUser.setPhoneNumber(dto.getPhoneNumber());
            newUser.setRole(Role.STUDENT);
            newUser.setEnabled(false);
            return newUser;
        });

        if (dto.getEmail() != null && !dto.getEmail().isEmpty() && user.getEmail() == null) {
            user.setEmail(dto.getEmail());
        }
        appUserRepository.save(user);

        return ResponseEntity.ok(ApiResponse.success("OTP verified. Please set your password.", null));
    }

    @PostMapping("/verify-reset-otp")
    public ResponseEntity<ApiResponse> verifyResetOtp(@Valid @RequestBody OtpVerifyDto dto) {
        boolean verified = otpService.verifyOtp(dto.getPhoneNumber(), dto.getOtpCode());
        if (!verified) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Invalid or expired OTP"));
        }

        // No check for existing user – allow reset even if fully registered
        return ResponseEntity.ok(ApiResponse.success("OTP verified for password reset", null));
    }

    @PostMapping("/set-password")
    public ResponseEntity<ApiResponse> setPassword(@Valid @RequestBody SetPasswordDto dto) {
        AppUser user = appUserRepository.findByPhoneNumber(dto.getPhoneNumber())
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        user.setEnabled(true);
        appUserRepository.save(user);

        return ResponseEntity.ok(ApiResponse.success("Password set successfully. You can now login.", null));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@Valid @RequestBody LoginDto dto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getPhoneNumber(), dto.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        AppUser user = (AppUser) authentication.getPrincipal();
        user.setLastLogin(LocalDateTime.now());
        appUserRepository.save(user);

        long expiration = dto.isRememberMe() ? 7 * 24 * 60 * 60 * 1000 : 24 * 60 * 60 * 1000;
        String token = jwtUtil.generateToken(user, expiration);

        JwtResponse response = new JwtResponse();
        response.setToken(token);
        response.setRole(user.getRole().name());
        response.setUserId(user.getId());
        response.setFullName(user.getFullName());
        response.setGender(user.getGender());
        response.setTitle(user.getTitle());
        if (user.getRole() == Role.SUB_COUNTY) {
            response.setSubCounty(user.getSubCounty());
        }
        if (user.getInstitution() != null) {
            response.setInstitutionId(user.getInstitution().getId());
            response.setInstitutionName(user.getInstitution().getName());
        }

        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse> forgotPassword(@Valid @RequestBody OtpRequestDto dto) {
        // Ensure user exists and is fully registered before sending reset OTP
        AppUser user = appUserRepository.findByPhoneNumber(dto.getPhoneNumber())
                .orElseThrow(() -> new RuntimeException("User not found with this phone number"));

        if (user.getPasswordHash() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Account not activated. Please complete registration first."));
        }

        OtpRequest otpRequest = otpService.createOtpRequest(dto.getPhoneNumber());

        // Use the email from request if provided, otherwise fallback to the user's stored email
        String email = dto.getEmail();
        if (email == null || email.isEmpty()) {
            email = user.getEmail();
        }

        otpService.sendOtp(dto.getPhoneNumber(), email, dto.getDeliveryMethod(), otpRequest.getOtpCode());
        return ResponseEntity.ok(ApiResponse.success("OTP sent for password reset", null));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse> resetPassword(@Valid @RequestBody ResetPasswordDto dto) {
        AppUser user = appUserRepository.findByPhoneNumber(dto.getPhoneNumber())
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setPasswordHash(passwordEncoder.encode(dto.getNewPassword()));
        appUserRepository.save(user);
        return ResponseEntity.ok(ApiResponse.success("Password reset successfully", null));
    }
}