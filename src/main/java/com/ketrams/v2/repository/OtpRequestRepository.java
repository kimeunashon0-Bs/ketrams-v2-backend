package com.ketrams.v2.repository;

import com.ketrams.v2.entity.OtpRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface OtpRequestRepository extends JpaRepository<OtpRequest, Long> {
    Optional<OtpRequest> findTopByPhoneNumberAndVerifiedFalseOrderByCreatedAtDesc(String phoneNumber);
    void deleteByPhoneNumber(String phoneNumber);
}