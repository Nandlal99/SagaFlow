package com.example.order.controller;

import com.example.order.dto.OrderRequest;
import com.example.order.entity.Order;
import com.example.order.service.OrderService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody OrderRequest request){
        try {
            // We call the service that handles the Order + Outbox transaction
            Order createdOrder = orderService.placedOrder(request);

            // Return 201 Created
            return new ResponseEntity<>(createdOrder, HttpStatus.CREATED);

        }catch (JsonProcessingException e){
            // Log the error and return a 500 Internal Server Error
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
