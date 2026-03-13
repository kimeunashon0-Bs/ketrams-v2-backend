package com.ketrams.v2.controller;

import com.ketrams.v2.entity.Course;
import com.ketrams.v2.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    @Autowired
    private CourseRepository courseRepository;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }
}