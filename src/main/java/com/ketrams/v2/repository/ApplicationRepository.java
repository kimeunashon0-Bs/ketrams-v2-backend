package com.ketrams.v2.repository;

import com.ketrams.v2.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

    List<Application> findByStudent_UserId(Long studentId);
    List<Application> findByInstitutionId(Long institutionId);
    List<Application> findByInstitution_SubCounty_Name(String subCountyName);

    // Check if a student has applied to an institution (for institution access)
    boolean existsByStudent_UserIdAndInstitutionId(Long studentId, Long institutionId);

    // Check if a student has applied to any institution in a sub‑county (for sub‑county access)
    boolean existsByStudent_UserIdAndInstitution_SubCounty_Name(Long studentId, String subCountyName); // NEW

    // Reporting – general
    @Query("SELECT a.student.subCounty, COUNT(a) FROM Application a GROUP BY a.student.subCounty")
    List<Object[]> countBySubCounty();

    @Query("SELECT a.student.gender, COUNT(a) FROM Application a GROUP BY a.student.gender")
    List<Object[]> countByGender();

    @Query("SELECT a.course.name, COUNT(a) FROM Application a GROUP BY a.course.name")
    List<Object[]> countByCourse();

    @Query("SELECT a.student.disabilityStatus, COUNT(a) FROM Application a GROUP BY a.student.disabilityStatus")
    List<Object[]> countByDisabilityStatus();

    @Query("SELECT a.institution.name, COUNT(a) FROM Application a GROUP BY a.institution.name")
    List<Object[]> countByInstitution();

    @Query("SELECT a.student.ward, COUNT(a) FROM Application a WHERE a.student.subCounty = :subCounty GROUP BY a.student.ward")
    List<Object[]> countByWard(@Param("subCounty") String subCounty);

    // Reporting – per institution
    @Query("SELECT a.status, COUNT(a) FROM Application a WHERE a.institution.id = :institutionId GROUP BY a.status")
    List<Object[]> countByStatusForInstitution(@Param("institutionId") Long institutionId);

    @Query("SELECT a.course.name, COUNT(a) FROM Application a WHERE a.institution.id = :institutionId GROUP BY a.course.name")
    List<Object[]> countByCourseForInstitution(@Param("institutionId") Long institutionId);

    @Query("SELECT a.student.subCounty, COUNT(a) FROM Application a WHERE a.institution.id = :institutionId GROUP BY a.student.subCounty")
    List<Object[]> countBySubCountyForInstitution(@Param("institutionId") Long institutionId);

    @Query("SELECT a.student.disabilityStatus, COUNT(a) FROM Application a WHERE a.institution.id = :institutionId GROUP BY a.student.disabilityStatus")
    List<Object[]> countByDisabilityStatusForInstitution(@Param("institutionId") Long institutionId);
}