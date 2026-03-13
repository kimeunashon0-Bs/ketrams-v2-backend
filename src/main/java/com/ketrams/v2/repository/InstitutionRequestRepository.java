package com.ketrams.v2.repository;

import com.ketrams.v2.entity.InstitutionRequest;
import com.ketrams.v2.entity.enums.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface InstitutionRequestRepository extends JpaRepository<InstitutionRequest, Long> {
    List<InstitutionRequest> findByStatus(RequestStatus status);
}