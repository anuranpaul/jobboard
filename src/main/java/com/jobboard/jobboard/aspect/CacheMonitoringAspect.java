package com.jobboard.jobboard.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Aspect
@Component
@Slf4j
public class CacheMonitoringAspect {
    
    private final ConcurrentHashMap<String, AtomicLong> cacheHits = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicLong> cacheMisses = new ConcurrentHashMap<>();
    
    @Around("@annotation(org.springframework.cache.annotation.Cacheable) && execution(* com.jobboard.jobboard.service.JobCacheService.*(..))")
    public Object monitorCachePerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String cacheKey = methodName;
        
        // Track before method execution
        long startTime = System.currentTimeMillis();
        
        // Execute method
        Object result = joinPoint.proceed();
        
        // Check logs to determine if this was a cache hit or miss
        if (log.isInfoEnabled()) {
            // If the method logged "Cache miss", it's a miss
            if (joinPoint.getTarget().toString().contains("Cache miss")) {
                cacheMisses.computeIfAbsent(cacheKey, k -> new AtomicLong(0)).incrementAndGet();
                log.info("CACHE MISS: {} - misses: {}", cacheKey, cacheMisses.get(cacheKey).get());
            } else {
                cacheHits.computeIfAbsent(cacheKey, k -> new AtomicLong(0)).incrementAndGet();
                log.info("CACHE HIT: {} - hits: {}", cacheKey, cacheHits.get(cacheKey).get());
            }
        }
        
        long executionTime = System.currentTimeMillis() - startTime;
        log.info("CACHE PERFORMANCE: {} completed in {} ms", cacheKey, executionTime);
        
        return result;
    }
    
    // Add a method to expose metrics via an endpoint if needed
    public ConcurrentHashMap<String, AtomicLong> getCacheHits() {
        return cacheHits;
    }
    
    public ConcurrentHashMap<String, AtomicLong> getCacheMisses() {
        return cacheMisses;
    }
} 