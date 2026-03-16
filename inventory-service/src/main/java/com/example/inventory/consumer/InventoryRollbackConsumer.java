package com.example.inventory.consumer;

import com.example.inventory.entity.Inventory;
import com.example.inventory.entity.InventoryEvent;
import com.example.inventory.entity.Reservation;
import com.example.inventory.repository.InventoryRepository;
import com.example.inventory.repository.ReservationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryRollbackConsumer {

    private final ObjectMapper objectMapper;
    private final InventoryRepository inventoryRepository;
    private final ReservationRepository reservationRepository;

    @KafkaListener(topics = "${spring.kafka.consume_payment.topic.name}", groupId = "${spring.kafka.consumer.group-id}")
    @Transactional
    public void handleRollback(String message){
        try{
            InventoryEvent event = objectMapper.readValue(message, InventoryEvent.class);
            Long orderId = event.getOrderId();

            // Use findById and handle the case where it might already be deleted (Idempotency)
            var reservedOrderOpt = reservationRepository.findById(event.getOrderId());

            if(reservedOrderOpt.isEmpty()){
                log.warn("Reservation for Order {} not found. Possibly already processed.", orderId);
                return;
            }
            Reservation reservedOrder = reservedOrderOpt.get();

            // We act both if payment failed and success
            if("PAYMENT_FAILED".equals(event.getStatus())){
                Long productId = reservedOrder.getProductId();
                Integer quantity = reservedOrder.getQuantity();
                log.info("Rolling back stock for Order {}: Adding {} back to Product {}", orderId, quantity, productId);

                Inventory item = inventoryRepository.findById(productId)
                        .orElseThrow(() -> new RuntimeException("Product not found"));

                // Add the stock back!
                item.setAvailableQuantity(item.getAvailableQuantity() + quantity);
                inventoryRepository.save(item);
                reservationRepository.delete(reservedOrder);

            }else if ("PAYMENT_SUCCESS".equals(event.getStatus())){
                reservationRepository.delete(reservedOrder);
                // Payment success: The stock is already deducted, just clean up the reservation record
                log.info("Payment successful for Order {}: Reservation finalized.", orderId);
            }
        }catch (Exception e){
            log.error("Failed to rollback inventory for message: {}", message, e);
        }
    }
}
