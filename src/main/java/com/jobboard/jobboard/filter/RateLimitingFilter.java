package com.jobboard.jobboard.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobboard.jobboard.config.RateLimitingConfig;
import com.jobboard.jobboard.dto.ApiResponse;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitingFilter extends OncePerRequestFilter {

    private final RateLimitingConfig rateLimitingConfig;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        
        // Only apply rate limiting to the search API
        if (request.getRequestURI().startsWith("/jobs") && 
            request.getMethod().equals("GET") && 
            request.getRequestURI().equals("/jobs")) {
            
            // Get the client's IP
            String clientIp = getClientIP(request);
            
            // Get the bucket for this IP
            Bucket bucket = rateLimitingConfig.resolveBucket(clientIp);
            
            // Try to consume a token
            ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
            
            if (probe.isConsumed()) {
                // Add rate limit headers
                response.addHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
                filterChain.doFilter(request, response);
            } else {
                // Too many requests
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                
                long waitTimeSeconds = probe.getNanosToWaitForRefill() / 1_000_000_000;
                ApiResponse<Object> errorResponse = ApiResponse.error(
                    "Rate limit exceeded. Try again in " + waitTimeSeconds + " seconds.", 
                    null
                );
                
                response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
                log.warn("Rate limit exceeded for IP: {}", clientIp);
            }
        } else {
            filterChain.doFilter(request, response);
        }
    }
    
    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
} 