package com.example.order.consumer;

import com.example.order.entity.Order;
import com.example.order.entity.PaymentEvent;
import com.example.order.repository.OrderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class OrderPaymentConsumer {

    private final ObjectMapper objectMapper;
    private final OrderRepository orderRepository;

    public OrderPaymentConsumer(ObjectMapper objectMapper, OrderRepository orderRepository) {
        this.objectMapper = objectMapper;
        this.orderRepository = orderRepository;
    }

    @KafkaListener(topics = "${spring.kafka.consume.topic.name}", groupId = "${spring.kafka.consumer.group-id}")
    @Transactional
    public void consumePaymentResult(String message)  {
        try{

            PaymentEvent paymentEvent = objectMapper.readValue(message, PaymentEvent.class);
            Order order = orderRepository.findById(paymentEvent.getOrderId())
                    .orElseThrow(() -> new RuntimeException("Order not found"));

            if("PAYMENT_SUCCESS".equals(paymentEvent.getStatus())){
                order.setStatus("CONFIRMED");
                log.info("Order {} is now CONFIRMED ✅", paymentEvent.getOrderId());
            }else if ("PAYMENT_FAILED".equals(paymentEvent.getStatus())){
                order.setStatus("CANCELLED");
                log.info("Order {} is now CANCELLED ❌ due to payment failure", paymentEvent.getOrderId());
            }
            orderRepository.save(order);

        }catch (Exception e){
            log.error("Error updating order status for message: {}", message, e);
        }
    }
}
