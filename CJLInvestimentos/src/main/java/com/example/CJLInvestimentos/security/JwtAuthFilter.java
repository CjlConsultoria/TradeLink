package com.example.CJLInvestimentos.security;

import com.example.CJLInvestimentos.entities.User;
import com.example.CJLInvestimentos.entities.enums.Role;
import com.example.CJLInvestimentos.repositories.UserRepository;
import com.example.CJLInvestimentos.services.FaturaService;
import com.example.CJLInvestimentos.services.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String[] ALLOWED_WHEN_BLOCKED = {
            "/api/me",
            "/api/consultor/faturas",
            "/api/consultor/checkout-pix",
            "/api/consultor/checkout-cartao-boleto",
            "/api/consultor/checkout-embedded",
            "/api/cliente/faturas",
            "/api/cliente/checkout-pix",
            "/api/cliente/checkout-cartao-boleto",
            "/api/cliente/checkout-embedded"
    };

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final FaturaService faturaService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getServletPath();

        // 🔓 ignora rotas públicas e error
        if (path.startsWith("/api/auth") || path.startsWith("/api/webhooks/") || path.equals("/error")) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");

        // sem token → segue fluxo
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        String userEmail;

        try {
            // ⚠️ deve extrair do SUBJECT do token
            userEmail = jwtService.extractEmail(token);
        } catch (ExpiredJwtException e) {
            logger.warn("JWT expirado: {}");
            filterChain.doFilter(request, response);
            return;
        } catch (Exception e) {
            logger.warn("Erro ao validar JWT: {}");
            filterChain.doFilter(request, response);
            return;
        }

        // autentica no contexto
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            User user = userRepository.findByEmail(userEmail).orElse(null);

            if (user != null) {
                if (!Boolean.TRUE.equals(user.getAtivo())) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write(objectMapper.writeValueAsString(Map.of(
                            "mensagem", "Conta inativa. Entre em contato com o administrador."
                    )));
                    return;
                }
                UserDetailsImpl userDetails = new UserDetailsImpl(user);
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );
                SecurityContextHolder.getContext().setAuthentication(authToken);

                // Bloqueio: por admin (qualquer acesso) ou por pagamento vencido (só faturas/checkout liberados)
                if (user.getRole() != Role.AdminMax && user.getEmpresa() != null) {
                    if (!faturaService.acessoPermitidoPorUsuarioId(user.getId())) {
                        boolean allowedPath;
                        if (faturaService.isBloqueadoPorAdmin(user.getId())) {
                            // Bloqueio por admin: só pode acessar /api/me para carregar dados e exibir a mensagem
                            allowedPath = path.startsWith("/api/me");
                        } else {
                            // Bloqueio por pagamento: permite faturas e checkout para regularizar
                            allowedPath = false;
                            for (String prefix : ALLOWED_WHEN_BLOCKED) {
                                if (path.startsWith(prefix)) {
                                    allowedPath = true;
                                    break;
                                }
                            }
                        }
                        if (!allowedPath) {
                            String motivo = faturaService.getMotivoBloqueioPorUsuarioId(user.getId());
                            if (motivo == null) motivo = "Acesso bloqueado.";
                            boolean bloqueadoPorAdmin = faturaService.isBloqueadoPorAdmin(user.getId());
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write(objectMapper.writeValueAsString(Map.of(
                                    "bloqueado", true,
                                    "bloqueadoPorAdmin", bloqueadoPorAdmin,
                                    "motivo", motivo
                            )));
                            return;
                        }
                    }
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
