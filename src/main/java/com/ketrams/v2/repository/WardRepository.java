package com.ketrams.v2.repository;

import com.ketrams.v2.entity.Ward;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface WardRepository extends JpaRepository<Ward, Long> {
    List<Ward> findBySubCountyId(Long subCountyId);
}