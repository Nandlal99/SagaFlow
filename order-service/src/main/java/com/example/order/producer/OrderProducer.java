package com.example.order.producer;



import com.example.order.entity.OutboxEvent;
import com.example.order.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderProducer {

    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${spring.kafka.topic.name}")
    private String topic;

    @Scheduled(fixedRate = 5000)    // Poll every 5 second
    @Transactional
    public void publishEvent(){
        List<OutboxEvent> events = outboxRepository.findByStatus("PENDING");
        for (OutboxEvent event: events){
            try {
                kafkaTemplate.send(topic, event.getAggregatedId(), event.getPayload()).get();
                event.setStatus("PROCESSED");
                outboxRepository.save(event);
                log.info("Successfully published event: {}", event.getAggregatedId());
            }catch (Exception e){
                log.error("Failed to publish event: {}", event.getId(), e);
            }
        }
    }
}
