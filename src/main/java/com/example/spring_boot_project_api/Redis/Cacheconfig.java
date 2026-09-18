package com.example.spring_boot_project_api.Redis;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Configuration
@EnableCaching
public class Cacheconfig {

    private static final Duration DEFAULT_TTL = Duration.ofMinutes(15);

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        RedisCacheConfiguration base = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(RedisSerializer.string()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(jsonValueSerializer()))
                .entryTtl(DEFAULT_TTL);

        RedisCacheManager.RedisCacheManagerBuilder builder =
                RedisCacheManager.builder(redisConnectionFactory)
                        .cacheDefaults(base);

        cacheTtls().forEach((cacheName, ttl) ->
                builder.withCacheConfiguration(cacheName, base.entryTtl(ttl)));

        return builder.build();
    }

    private static Map<String, Duration> cacheTtls() {
        Map<String, Duration> m = new LinkedHashMap<>();
        m.put("locations", Duration.ofHours(24));
        m.put("placeCategories", Duration.ofHours(24));
        m.put("foodCategories", Duration.ofHours(24));
        m.put("roomTypes", Duration.ofHours(24));
        m.put("hotels", Duration.ofMinutes(30));
        m.put("hotelRooms", Duration.ofMinutes(30));
        m.put("rooms", Duration.ofMinutes(30));
        m.put("restaurants", Duration.ofMinutes(30));
        m.put("foods", Duration.ofMinutes(30));
        m.put("tickets", Duration.ofMinutes(30));
        m.put("tourPlaces", Duration.ofMinutes(30));
        return m;
    }

    private RedisSerializer<Object> jsonValueSerializer() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.registerModule(new JavaTimeModule());
        mapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
        return new GenericJackson2JsonRedisSerializer(mapper);
    }
}
