package com.example.inventory.consumer;

import com.example.inventory.entity.OrderEvent;
import com.example.inventory.entity.ProcessedOrder;
import com.example.inventory.repository.ProcessedOrderRepository;
import com.example.inventory.service.InventoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

@Controller
@RequiredArgsConstructor
@Slf4j
public class InventoryConsumer {

    private final InventoryService inventoryService;
    private final ObjectMapper objectMapper;
    private final ProcessedOrderRepository processedMessageRepository;

    @KafkaListener(topics = "${spring.kafka.consume_order.topic.name}", groupId = "${spring.kafka.consumer.group-id}")
    @Transactional
    public void consumeOrderEvent(String message)  {
        try {
            // 1. Parse the message from Order Service
            OrderEvent orderEvent = objectMapper.readValue(message, OrderEvent.class);
            log.info("Received order event for Product ID: {}", orderEvent.getProductId());

            // Idempotency case: Check if already processed
            if(processedMessageRepository.existsById(orderEvent.getOrderId())){
                log.warn("Message for Order {} already processed. Skipping...", orderEvent.getOrderId());
                return;
            }
            // 2. Process Business Logic (Deduct stock / Create Reservation)
            inventoryService.processInventory(orderEvent);

            // 3. Mark as Processed (IMPORTANT: Do this in the same transaction!)
            // You can create a small entity called 'ProcessedOrder'
            processedMessageRepository.save(new ProcessedOrder(orderEvent.getOrderId()));

            log.info("Successfully processed Order {}", orderEvent.getOrderId());
        }catch (Exception e){
            log.error("Error processing inventory for message: {}", message, e);
        }
    }
}
