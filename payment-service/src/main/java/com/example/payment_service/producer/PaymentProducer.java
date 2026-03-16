package com.example.payment_service.producer;

import com.example.payment_service.entity.InventoryEvent;
import com.example.payment_service.entity.PaymentEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${spring.kafka.topic.name}")
    private String topicName;

    public void produceEventToKafka(String type, InventoryEvent event){
        PaymentEvent paymentEvent = new PaymentEvent();
        paymentEvent.setOrderId(event.getOrderId());
        paymentEvent.setAmount(event.getPrice());
        paymentEvent.setStatus(type);
        // Send back to payment-events topic
        kafkaTemplate.send(topicName, String.valueOf(event.getOrderId()), paymentEvent.toString());
    }
}
