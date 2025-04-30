package com.jobboard.jobboard.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.PageRequest;
import java.util.List;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.time.Duration;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public RedisCacheConfiguration cacheConfiguration() {
        // Create ObjectMapper and configure it for default typing
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules(); // Register standard modules
        
        // Add custom deserializers
        SimpleModule module = new SimpleModule();
        module.addDeserializer(Sort.class, new SortDeserializer());
        objectMapper.registerModule(module);
        
        objectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY); // Removed the explicit "@class" argument

        // tell Jackson how to recreate PageImpl<T>
        objectMapper.addMixIn(PageImpl.class, PageImplMixin.class);

        // tell Jackson how to recreate PageRequest
        objectMapper.addMixIn(PageRequest.class, PageRequestMixin.class);

        // Create the serializer *with* the configured ObjectMapper
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(objectMapper);

        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .disableCachingNullValues()
                .serializeKeysWith(SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(SerializationPair.fromSerializer(serializer));
    }

    /**
     * Mixin to wire up PageImpl's @JsonCreator constructor.
     */
    private static abstract class PageImplMixin {
        @JsonCreator
        PageImplMixin(
            @JsonProperty("content") List<?> content,
            @JsonProperty("pageable") Pageable pageable,
            @JsonProperty("totalElements") long totalElements
        ) {}
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static abstract class SortMixin {
        @JsonCreator
        SortMixin(@JsonProperty("orders") List<Sort.Order> orders) {}
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static abstract class PageRequestMixin {
        @JsonCreator
        PageRequestMixin(
          @JsonProperty("pageNumber") int page,
          @JsonProperty("pageSize")   int size,
          @JsonProperty("sort")       Sort sort) {}
    }
} 