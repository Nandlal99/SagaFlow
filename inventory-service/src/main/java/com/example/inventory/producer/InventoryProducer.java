package com.example.inventory.producer;

import com.example.inventory.entity.InventoryEvent;
import com.example.inventory.entity.OrderEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InventoryProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    @Value("${spring.kafka.topic.name}")
    private final String topicName;

    public void publishEvent(String type, OrderEvent event){
        InventoryEvent inventoryEvent = new InventoryEvent();
        inventoryEvent.setOrderId(event.getOrderId());
        inventoryEvent.setPrice(event.getTotalAmount());
        inventoryEvent.setStatus(type);
        kafkaTemplate.send(topicName, String.valueOf(event.getOrderId()), inventoryEvent.toString());
    }
}
