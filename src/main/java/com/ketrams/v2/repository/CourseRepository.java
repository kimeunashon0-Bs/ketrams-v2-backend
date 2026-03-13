package com.ketrams.v2.repository;

import com.ketrams.v2.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByInstitutionId(Long institutionId);
    List<Course> findByInstitutionIdAndEnabledTrue(Long institutionId); // for public listing
}