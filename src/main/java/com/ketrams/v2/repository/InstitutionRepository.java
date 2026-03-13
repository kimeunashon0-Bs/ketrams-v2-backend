package com.ketrams.v2.repository;

import com.ketrams.v2.entity.Institution;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface InstitutionRepository extends JpaRepository<Institution, Long> {
    Optional<Institution> findByName(String name);
    // Remove findByUser_Id – it no longer exists
}