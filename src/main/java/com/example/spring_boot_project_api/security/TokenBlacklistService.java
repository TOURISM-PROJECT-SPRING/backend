package com.example.spring_boot_project_api.security;

import java.time.Duration;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TokenBlacklistService {

    private static final String PREFIX = "blacklisted:token:";

    private final StringRedisTemplate redisTemplate;

    public void blacklist(String token, Duration ttl) {
        if (token == null || ttl.isNegative() || ttl.isZero()) {
            return;
        }
        try {
            redisTemplate.opsForValue().set(PREFIX + token, "1", ttl);
        } catch (Exception ignored) {
            // If Redis is unavailable, fall back to stateless JWT expiry only.
        }
    }

    public boolean isBlacklisted(String token) {
        if (token == null) {
            return false;
        }
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(PREFIX + token));
        } catch (Exception e) {
            return false;
        }
    }
}