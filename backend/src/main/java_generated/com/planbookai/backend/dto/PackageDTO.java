package com.planbookai.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PackageDTO {
    private Integer id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer durationDays;
    private String features;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
