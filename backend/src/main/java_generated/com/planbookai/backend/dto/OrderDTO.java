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
public class OrderDTO {
    private Long id;
    private Long userId;
    private String userEmail;
    private String userFullName;
    private Integer packageId;
    private String packageName;
    private String status;
    private BigDecimal amountPaid;
    private String paymentMethod;
    private LocalDateTime startedAt;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
}
