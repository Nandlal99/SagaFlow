package com.example.inventory.service;

import com.example.inventory.entity.Inventory;
import com.example.inventory.entity.InventoryEvent;
import com.example.inventory.entity.OrderEvent;
import com.example.inventory.entity.Reservation;
import com.example.inventory.producer.InventoryProducer;
import com.example.inventory.repository.InventoryRepository;
import com.example.inventory.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryProducer inventoryProducer;
    private final ReservationRepository reservationRepository;

    @Transactional
    public void processInventory(OrderEvent orderEvent){
        // 1. Business Logic: Check and Update Stock
        Inventory item = inventoryRepository.findById(orderEvent.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (item.getAvailableQuantity() >= orderEvent.getQuantity()) {
            // Success path
            item.setAvailableQuantity(item.getAvailableQuantity() - orderEvent.getQuantity());
            inventoryRepository.save(item);

            // 2. ADD RECORD TO RESERVATION TABLE
            // This is the record your Rollback method will look for later!
            Reservation reservation = new Reservation();
            reservation.setOrderId(orderEvent.getOrderId());
            reservation.setProductId(orderEvent.getProductId());
            reservation.setQuantity(orderEvent.getQuantity());
            reservation.setStatus("RESERVED");
            reservationRepository.save(reservation);

            // 3. Publish Success Event
            inventoryProducer.publishEvent("INVENTORY_RESERVED", orderEvent);
        } else {
            // Failure path
            log.warn("Stock insufficient for Product: {}", orderEvent.getProductId());
            inventoryProducer.publishEvent("INVENTORY_FAILED", orderEvent);
        }
    }
}
