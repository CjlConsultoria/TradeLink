package com.example.CJLInvestimentos.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate limiter em memória para proteção contra brute force no login.
 * Máximo de 5 tentativas por email em janela de 15 minutos.
 */
@Slf4j
@Component
public class LoginRateLimiter {

    private static final int MAX_TENTATIVAS = 5;
    private static final long JANELA_MINUTOS = 15;

    private final ConcurrentHashMap<String, AttemptInfo> tentativas = new ConcurrentHashMap<>();

    public boolean isBlocked(String email) {
        if (email == null || email.isBlank()) return false;
        String key = email.toLowerCase().trim();
        AttemptInfo info = tentativas.get(key);
        if (info == null) return false;
        if (info.isExpired()) {
            tentativas.remove(key);
            return false;
        }
        return info.count >= MAX_TENTATIVAS;
    }

    public void registerAttempt(String email) {
        if (email == null || email.isBlank()) return;
        String key = email.toLowerCase().trim();
        tentativas.compute(key, (k, existing) -> {
            if (existing == null || existing.isExpired()) {
                return new AttemptInfo(1, Instant.now());
            }
            existing.count++;
            return existing;
        });
    }

    public void resetAttempts(String email) {
        if (email == null || email.isBlank()) return;
        tentativas.remove(email.toLowerCase().trim());
    }

    public long getSecondsUntilUnblock(String email) {
        if (email == null || email.isBlank()) return 0;
        AttemptInfo info = tentativas.get(email.toLowerCase().trim());
        if (info == null || info.isExpired()) return 0;
        long elapsed = Instant.now().getEpochSecond() - info.firstAttempt.getEpochSecond();
        long window = JANELA_MINUTOS * 60;
        return Math.max(0, window - elapsed);
    }

    private static class AttemptInfo {
        int count;
        Instant firstAttempt;

        AttemptInfo(int count, Instant firstAttempt) {
            this.count = count;
            this.firstAttempt = firstAttempt;
        }

        boolean isExpired() {
            return Instant.now().isAfter(firstAttempt.plusSeconds(JANELA_MINUTOS * 60));
        }
    }
}
