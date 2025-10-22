package com.example.CJLInvestimentos.services;

import com.example.CJLInvestimentos.entities.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class JwtService {

    private static final String SECRET = System.getenv("JWT_SECRET") != null ? System.getenv("JWT_SECRET") : "chave-secreta";

    public String generateToken(User user) {
        if (user == null || user.getEmail() == null) {
            throw new IllegalArgumentException("Usuário ou e-mail inválido");
        }
        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim("role", user.getRole() != null ? user.getRole().name() : "ROLE_USER")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000))
                .signWith(SignatureAlgorithm.HS256, SECRET)
                .compact();
    }

    public String extractEmail(String token) {
        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("Token inválido");
        }
        try {
            Claims claims = Jwts.parser().setSigningKey(SECRET).parseClaimsJws(token).getBody();
            if (claims.getExpiration().before(new Date())) {
                throw new ExpiredJwtException(null, claims, "Token expirado");
            }
            return claims.getSubject();
        } catch (SignatureException | MalformedJwtException | ExpiredJwtException e) {
            throw new IllegalArgumentException("Token inválido ou expirado", e);
        }
    }

    public boolean isTokenValid(String token, User user) {
        try {
            String email = extractEmail(token);
            return email.equals(user.getEmail()) && !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        try {
            return Jwts.parser().setSigningKey(SECRET).parseClaimsJws(token).getBody().getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }
}