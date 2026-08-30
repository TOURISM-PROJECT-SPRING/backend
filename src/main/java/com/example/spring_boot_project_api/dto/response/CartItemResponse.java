package com.example.spring_boot_project_api.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CartItemResponse {

    private Long id;
    private Long foodId;
    private String foodName;
    private String foodImage;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subTotal;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
