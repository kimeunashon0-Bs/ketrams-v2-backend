package com.ketrams.v2.repository;

import com.ketrams.v2.entity.AppUser;
import com.ketrams.v2.entity.Institution;
import com.ketrams.v2.entity.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByPhoneNumber(String phoneNumber);
    boolean existsByPhoneNumber(String phoneNumber);
    List<AppUser> findByRole(Role role);

    @Query("SELECT a.institution FROM AppUser a WHERE a.id = :userId")
    Optional<Institution> findInstitutionByUserId(@Param("userId") Long userId);
}