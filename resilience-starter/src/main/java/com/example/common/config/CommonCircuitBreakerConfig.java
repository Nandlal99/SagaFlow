package com.example.common.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class CommonCircuitBreakerConfig {

    @Bean
    public CircuitBreakerConfig standardCircuitBreakerConfig(){
        return CircuitBreakerConfig.custom()
                .failureRateThreshold(50) // Trip if 50% of calls fail
                .waitDurationInOpenState(Duration.ofSeconds(30)) // Wait 30s before trying again
                .slidingWindowSize(10) // Look at the last 10 calls
                .permittedNumberOfCallsInHalfOpenState(3)
                .build();
    }
}
