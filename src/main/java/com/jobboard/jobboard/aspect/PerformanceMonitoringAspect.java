package com.jobboard.jobboard.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class PerformanceMonitoringAspect {

    @Around("execution(* com.jobboard.jobboard.service.JobService.*(..)) || execution(* com.jobboard.jobboard.service.JobCacheService.*(..))")
    public Object measureMethodExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        
        try {
            return joinPoint.proceed();
        } finally {
            long executionTime = System.currentTimeMillis() - start;
            log.info("PERFORMANCE: {}.{} executed in {} ms", 
                     joinPoint.getSignature().getDeclaringTypeName(),
                     joinPoint.getSignature().getName(), 
                     executionTime);
        }
    }
} 