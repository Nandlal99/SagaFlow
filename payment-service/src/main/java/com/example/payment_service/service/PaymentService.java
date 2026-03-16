package com.example.payment_service.service;

import com.example.payment_service.entity.InventoryEvent;
import com.example.payment_service.entity.Payment;
import com.example.payment_service.producer.PaymentProducer;
import com.example.payment_service.repository.PaymentRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);
    private final PaymentProducer paymentProducer;
    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentProducer paymentProducer, PaymentRepository paymentRepository) {
        this.paymentProducer = paymentProducer;
        this.paymentRepository = paymentRepository;
    }

    @Transactional
    @CircuitBreaker(name = "paymentapi", fallbackMethod = "handlePaymentFailure")
    public void processUserPayment(InventoryEvent event){
        // Mocking a payment gateway call
        boolean isSuccess = Math.random() > 0.2; // 80% success rate
        Payment payment = new Payment();
        payment.setOrderId(event.getOrderId());
        payment.setAmount(event.getPrice());
        if (isSuccess){
            payment.setPaymentStatus("SUCCESS");
            payment.setTransactionId(UUID.randomUUID().toString());
            // publish success message to kafka
            paymentProducer.produceEventToKafka("PAYMENT_SUCCESS", event);
        }else {
            payment.setPaymentStatus("FAILED");
            payment.setFailureReason("INSUFFICIENT_FUNDS");
            // publish failed message to kafka
            paymentProducer.produceEventToKafka("PAYMENT_FAILED", event);
        }
        paymentRepository.save(payment);
    }

    public void handlePaymentFailure(InventoryEvent event, Throwable t){
        log.error("Circuit Open! Falling back to local queue for amount: {}", event.getPrice());
    }
}
