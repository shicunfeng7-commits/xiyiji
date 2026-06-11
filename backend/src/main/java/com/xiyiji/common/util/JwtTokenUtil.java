package com.xiyiji.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public class JwtTokenUtil {

    private static final String SECRET_KEY = "WashProSecretKey2024!@#$%^&*()_+QWERTYUIOP";
    private static final long EXPIRATION = 604800000L; // 7天

    private static SecretKey getKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
    }

    public static String generateToken(Long userId, String phone) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + EXPIRATION);

        return Jwts.builder()
                .claim("userId", userId)
                .claim("phone", phone)
                .issuedAt(now)
                .expiration(expirationDate)
                .signWith(getKey())
                .compact();
    }

    public static Long parseUserId(String token) {
        Claims claims = parseClaims(token);
        if (claims != null) {
            return claims.get("userId", Long.class);
        }
        return null;
    }

    public static String parsePhone(String token) {
        Claims claims = parseClaims(token);
        if (claims != null) {
            return claims.get("phone", String.class);
        }
        return null;
    }

    public static boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private static Claims parseClaims(String token) {
        Jws<Claims> jws = Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token);
        return jws.getPayload();
    }
}