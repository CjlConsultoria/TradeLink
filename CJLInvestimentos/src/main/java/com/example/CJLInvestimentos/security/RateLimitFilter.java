package com.example.CJLInvestimentos.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

/**
 * Filtro que aplica rate limiting nos endpoints de login e verify-otp.
 * Extrai o email/userId do corpo da requisição e verifica se está bloqueado.
 */
@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final LoginRateLimiter rateLimiter;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // Só aplica rate limit em POST /api/auth/login e /api/auth/verify-otp
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        boolean isLogin = path.endsWith("/api/auth/login");
        boolean isVerifyOtp = path.endsWith("/api/auth/verify-otp");

        if (!isLogin && !isVerifyOtp) {
            filterChain.doFilter(request, response);
            return;
        }

        // Precisamos ler o body para extrair o email — usamos um wrapper
        CachedBodyHttpServletRequest cachedRequest = new CachedBodyHttpServletRequest(request);
        String body = cachedRequest.getCachedBody();

        String key = extractRateLimitKey(body, isLogin);
        if (key != null && !key.isBlank() && rateLimiter.isBlocked(key)) {
            long seconds = rateLimiter.getSecondsUntilUnblock(key);
            response.setStatus(429);
            response.setContentType("application/json;charset=UTF-8");
            String json = objectMapper.writeValueAsString(Map.of(
                    "mensagem", "Muitas tentativas. Tente novamente em " + seconds + " segundos.",
                    "retryAfter", seconds
            ));
            response.getWriter().write(json);
            return;
        }

        filterChain.doFilter(cachedRequest, response);
    }

    private String extractRateLimitKey(String body, boolean isLogin) {
        if (body == null || body.isBlank()) return null;
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = objectMapper.readValue(body, Map.class);
            if (isLogin) {
                Object email = map.get("email");
                return email != null ? email.toString().toLowerCase().trim() : null;
            } else {
                // verify-otp: usa o userId como chave
                Object userId = map.get("userId");
                return userId != null ? "otp:" + userId.toString() : null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !(path.endsWith("/api/auth/login") || path.endsWith("/api/auth/verify-otp"));
    }
}
