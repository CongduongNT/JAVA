package com.planbookai.backend.Security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final String SECRET_KEY = "planbookai-secret-key-planbookai-secret-key";

    private final long ACCESS_EXPIRATION = 86400000; // 1 day for demo, usually shorter (e.g. 15 mins)
    private final long REFRESH_EXPIRATION = 604800000; // 7 days

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    public String generateAccessToken(String email) {
        return buildToken(email, ACCESS_EXPIRATION);
    }

    public String generateRefreshToken(String email) {
        return buildToken(email, REFRESH_EXPIRATION);
    }

    private String buildToken(String email, long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String getEmailFromToken(String token) {

        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }

    public boolean validateToken(String token) {

        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);

            return true;

        } catch (JwtException e) {

            return false;
        }
    }
}