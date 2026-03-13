package com.ketrams.v2.controller;

import com.ketrams.v2.dto.request.InstitutionRequestDto;
import com.ketrams.v2.dto.response.ApiResponse;
import com.ketrams.v2.entity.InstitutionRequest;
import com.ketrams.v2.entity.SubCounty;
import com.ketrams.v2.entity.Ward;
import com.ketrams.v2.repository.InstitutionRequestRepository;
import com.ketrams.v2.repository.SubCountyRepository;
import com.ketrams.v2.repository.WardRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public/institution-requests")
public class InstitutionRequestController {

    @Autowired
    private InstitutionRequestRepository requestRepository;

    @Autowired
    private SubCountyRepository subCountyRepository;

    @Autowired
    private WardRepository wardRepository;

    @PostMapping
    public ResponseEntity<ApiResponse> submitRequest(@Valid @RequestBody InstitutionRequestDto dto) {
        SubCounty subCounty = subCountyRepository.findById(dto.getSubCountyId())
                .orElseThrow(() -> new RuntimeException("Sub-county not found"));
        Ward ward = wardRepository.findById(dto.getWardId())
                .orElseThrow(() -> new RuntimeException("Ward not found"));

        InstitutionRequest request = new InstitutionRequest();
        request.setInstitutionName(dto.getInstitutionName());
        request.setCategory(dto.getCategory());
        request.setAdminFullName(dto.getAdminFullName());
        request.setAdminPhone(dto.getAdminPhone());
        request.setAdminEmail(dto.getAdminEmail());
        request.setAdminGender(dto.getAdminGender());
        request.setAdminTitle(dto.getAdminTitle());
        request.setSubCounty(subCounty);
        request.setWard(ward);
        requestRepository.save(request);

        // Mock email to admin (in production, send actual email)
        System.out.println("New institution request from " + dto.getAdminEmail());

        return ResponseEntity.ok(ApiResponse.success("Request submitted successfully", request));
    }
}