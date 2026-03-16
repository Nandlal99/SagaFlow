package com.example.inventory.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "processed_orders")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProcessedOrder {
    @Id
    private Long orderId; // Using the Order ID as the Primary Key ensures uniqueness

    private LocalDateTime processedAt;

    public ProcessedOrder(Long orderId) {
        this.orderId = orderId;
        this.processedAt = LocalDateTime.now();
    }
}
