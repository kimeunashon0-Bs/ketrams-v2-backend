package com.ketrams.v2.repository;

import com.ketrams.v2.entity.SubCounty;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SubCountyRepository extends JpaRepository<SubCounty, Long> {
    Optional<SubCounty> findByName(String name);
}