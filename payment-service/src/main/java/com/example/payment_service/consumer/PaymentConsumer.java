package com.example.payment_service.consumer;

import com.example.payment_service.entity.InventoryEvent;
import com.example.payment_service.service.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
@Slf4j
public class PaymentConsumer {

    private final ObjectMapper objectMapper;
    private final PaymentService paymentService;

    public PaymentConsumer(ObjectMapper objectMapper, PaymentService paymentService) {
        this.objectMapper = objectMapper;
        this.paymentService = paymentService;
    }

    @KafkaListener(topics = "${spring.kafka.topic.name}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeInventoryEvent(String message){
        try{
            // 1. Parse the event coming from Inventory
            InventoryEvent inventoryEvent = objectMapper.readValue(message, InventoryEvent.class);
            // 2. Only process if Inventory was successfully reserved
            if("INVENTORY_RESERVED".equals(inventoryEvent.getStatus())){
                paymentService.processUserPayment(inventoryEvent);
            }
        }catch (Exception e){
            log.error("Error in Payment Listener", e);
        }
    }
}
