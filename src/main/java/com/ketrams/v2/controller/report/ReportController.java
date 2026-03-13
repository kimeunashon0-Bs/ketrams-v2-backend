package com.ketrams.v2.controller.report;

import com.ketrams.v2.dto.response.ApiResponse;
import com.ketrams.v2.repository.ApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    @Autowired
    private ApplicationRepository applicationRepository;

    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('SUB_COUNTY', 'ADMIN')")
    public ApiResponse getSummary() {
        Map<String, Object> report = new HashMap<>();

        List<Object[]> subCountyCounts = applicationRepository.countBySubCounty();
        report.put("applicantsPerSubCounty", subCountyCounts);

        List<Object[]> genderCounts = applicationRepository.countByGender();
        report.put("genderDistribution", genderCounts);

        List<Object[]> courseDemand = applicationRepository.countByCourse();
        report.put("courseDemand", courseDemand);

        return ApiResponse.success("Report generated", report);
    }
}