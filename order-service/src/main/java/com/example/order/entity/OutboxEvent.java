package com.example.order.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "outbox")
@Data
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String aggregatedId;  // The Order ID
    private String eventType;    // e.g., "ORDER_CREATED"

    @Column(columnDefinition = "TEXT")
    private String payload;     // The JSON content of the event

    private String status;      // PENDING, PROCESSED
    private LocalDateTime createdAt;
}
