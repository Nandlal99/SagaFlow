package com.example.order.dto;

import lombok.Data;

@Data
public class OrderRequest {
    private Long productId;
    private Integer quantity;
    private Double price;
    private String userId;
}
