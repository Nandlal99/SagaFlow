package com.example.inventory.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderEvent {
    private Long productId;
    private Integer quantity;
    private Long orderId;
    private Double totalAmount;
    private String status;      // e.g., ORDER_CREATED
}
