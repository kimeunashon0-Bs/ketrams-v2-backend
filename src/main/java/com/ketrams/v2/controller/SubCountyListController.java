package com.ketrams.v2.controller;

import com.ketrams.v2.entity.SubCounty;
import com.ketrams.v2.repository.SubCountyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/subcounties")
public class SubCountyListController {

    @Autowired
    private SubCountyRepository subCountyRepository;

    @GetMapping
    public List<SubCounty> getAllSubCounties() {
        return subCountyRepository.findAll();
    }
}