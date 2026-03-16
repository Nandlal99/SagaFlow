package com.example.common.config;

import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.ExponentialBackOff;
import org.springframework.util.backoff.FixedBackOff;

import java.util.logging.Logger;


@Configuration
@EnableKafka
public class CommonKafkaResilienceConfig {

    Logger logger = Logger.getLogger(getClass().getName());

    @Bean
    public DefaultErrorHandler commonErrorHandler(KafkaTemplate<Object, Object> template){
        // 1. DLQ Destination: logic to route failed messages to {original-topic}.DLQ
        var recover = new DeadLetterPublishingRecoverer(template, (consumerRecord, ex) ->
                new TopicPartition(consumerRecord.topic() + ".DLQ", consumerRecord.partition()));

        // 2. Exponential Backoff: Wait 1s, 2s, 4s... (prevents thundering herd)
        var backOff = new ExponentialBackOff();
        backOff.setInitialInterval(1000L);
        backOff.setMultiplier(2.0);
        backOff.setMaxInterval(10000L);

        // 3. Create Error Handler with 3 delivery attempts
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recover, new FixedBackOff(1000L, 3));

        // Log failures specifically for observability
        errorHandler.setRetryListeners((consumerRecord, ex, deliveryAttempt) ->
                logger.info("Retry attempt " + deliveryAttempt +" for message in "+ consumerRecord.topic()));

        return errorHandler;
    }

}
