package com.jobboard.jobboard.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class RateLimitingConfig {

    @Bean
    public Map<String, Bucket> buckets() {
        return new ConcurrentHashMap<>();
    }
    
    public Bucket resolveBucket(String key) {
        return buckets().computeIfAbsent(key, k -> createNewBucket());
    }
    
    private Bucket createNewBucket() {
        // Allow 30 requests per minute
        Bandwidth limit = Bandwidth.classic(30, Refill.greedy(30, Duration.ofMinutes(1)));
        return Bucket4j.builder().addLimit(limit).build();
    }
} 