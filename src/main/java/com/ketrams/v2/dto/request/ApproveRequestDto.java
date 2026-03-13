package com.ketrams.v2.dto.request;

import lombok.Data;

@Data
public class ApproveRequestDto {
    private Long requestId;
    private boolean approved;
    private String rejectionReason; // if rejected
}