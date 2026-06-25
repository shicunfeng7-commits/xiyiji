package com.xiyiji.common.util;

import com.xiyiji.common.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenUtil {

    @Resource
    private JwtProperties jwtProperties;

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(Long userId, String phone) {
        return generateToken(userId, phone, "user");
    }

    public String generateToken(Long userId, String phone, String role) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + jwtProperties.getExpiration());

        return Jwts.builder()
                .claim("userId", userId)
                .claim("phone", phone)
                .claim("role", role)
                .issuedAt(now)
                .expiration(expirationDate)
                .signWith(getKey())
                .compact();
    }

    public Long parseUserId(String token) {
        Claims claims = parseClaims(token);
        if (claims != null) {
            Number userId = claims.get("userId", Number.class);
            return userId != null ? userId.longValue() : null;
        }
        return null;
    }

    public String parsePhone(String token) {
        Claims claims = parseClaims(token);
        if (claims != null) {
            return claims.get("phone", String.class);
        }
        return null;
    }

    public String parseRole(String token) {
        Claims claims = parseClaims(token);
        if (claims != null) {
            return claims.get("role", String.class);
        }
        return null;
    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        Jws<Claims> jws = Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token);
        return jws.getPayload();
    }
}
