package com.ketrams.v2.dto.request;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class BatchFacilitateDto {
    private List<Long> applicationIds;
    private BigDecimal amount;
    private String remarks;
}