package com.example.order.service;


import com.example.order.dto.OrderRequest;
import com.example.order.entity.Order;
import com.example.order.entity.OutboxEvent;
import com.example.order.repository.OrderRepository;
import com.example.order.repository.OutboxRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public Order placedOrder(OrderRequest request) throws JsonProcessingException {
        // 1. Save the actual order
        Order order = new Order();
        order.setProductId(request.getProductId());
        order.setPrice(request.getPrice());
        order.setQuantity(request.getQuantity());
        order.setStatus("PENDING");
        Order savedOrder = orderRepository.save(order);

        // 2. Prepare the Outbox record
        OutboxEvent outboxEvent = new OutboxEvent();
        outboxEvent.setAggregatedId(savedOrder.getId().toString());
        outboxEvent.setEventType("ORDER_CREATED");
        outboxEvent.setPayload(objectMapper.writeValueAsString(savedOrder));
        outboxEvent.setStatus("PENDING");
        outboxEvent.setCreatedAt(LocalDateTime.now());

        // 3. Save to Outbox(In the SAME transaction)
        outboxRepository.save(outboxEvent);
        return savedOrder;
    }
}
