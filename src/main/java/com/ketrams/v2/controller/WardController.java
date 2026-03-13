package com.ketrams.v2.controller;

import com.ketrams.v2.entity.Ward;
import com.ketrams.v2.repository.WardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wards")
public class WardController {

    @Autowired
    private WardRepository wardRepository;

    @GetMapping
    public List<Ward> getWardsBySubCounty(@RequestParam Long subCountyId) {
        return wardRepository.findBySubCountyId(subCountyId);
    }
}