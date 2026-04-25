package com.example.order.controller;

import com.example.order.dto.OrderRequest;
import com.example.order.entity.Order;
import com.example.order.service.OrderService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/create")
    public ResponseEntity<Order> createOrder(@RequestBody OrderRequest request){
        try {
            // We can extract the user email directly from the SecurityContext, without ever hitting the User database in this service
            var auth = SecurityContextHolder.getContext().getAuthentication();
            if(auth == null || !auth.isAuthenticated()) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

            // We call the service that handles the Order + Outbox transaction
            Order createdOrder = orderService.placedOrder(request, auth.getName());

            // Return 201 Created
            return new ResponseEntity<>(createdOrder, HttpStatus.CREATED);

        }catch (JsonProcessingException e){
            // Log the error and return a 500 Internal Server Error
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
