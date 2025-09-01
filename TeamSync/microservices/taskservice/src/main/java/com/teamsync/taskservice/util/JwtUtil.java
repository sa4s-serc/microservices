package com.teamsync.taskservice.util;

import io.jsonwebtoken.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Date;
import java.util.function.Function;
import java.util.Base64;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {

    private SecretKey Key;

    public JwtUtil() {
        String secreteString = "843567893696976453275974432697R634976R738467TR678T34865R6834R8763T478378637664538745673865783678548735687R3";
        byte[] keyBytes = Base64.getDecoder().decode(secreteString.getBytes(StandardCharsets.UTF_8));
        this.Key = new SecretKeySpec(keyBytes, "HmacSHA256");
    }

    private <T> T extractClaims(String token, Function<Claims, T> claimsResolver) {
        return claimsResolver.apply(Jwts.parser().verifyWith(Key).build().parseSignedClaims(token).getPayload());
    }

    public String extractUsername(String token){
        return extractClaims(token, Claims::getSubject);
    }

    public String extractRole(String token){
        return extractClaims(token, claims -> (String) claims.get("role"));
    }

    public boolean isTokenValid(String token){
        try {
            Date expiration = extractClaims(token, Claims::getExpiration);
            System.out.println("Expiration date: " + expiration);
            return expiration.after(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public int getUserId(HttpServletRequest request) {
        HttpServletRequest httpReq = (HttpServletRequest) request;
        String authHeader = httpReq.getHeader("Authorization");
        String username = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            // System.out.println("Token: " + token);

            if (isTokenValid(token)) {
                // System.out.println("Token is valid");
                username = extractUsername(token);
            }
        }

        int studentId = Integer.parseInt(username);
        return studentId;
    }
}