package com.teamsync.userservice.util;

import io.jsonwebtoken.*;
import java.util.Date;
import java.util.HashMap;
import java.util.function.Function;
import java.util.Base64;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import java.nio.charset.StandardCharsets;

import org.springframework.stereotype.Component;

@Component
public class JwtUtil {

    private SecretKey Key;
    private static final long EXPIRATION_TIME = 86400000;  //24 hours

    public JwtUtil() {
        String secreteString = "843567893696976453275974432697R634976R738467TR678T34865R6834R8763T478378637664538745673865783678548735687R3";
        byte[] keyBytes = Base64.getDecoder().decode(secreteString.getBytes(StandardCharsets.UTF_8));
        this.Key = new SecretKeySpec(keyBytes, "HmacSHA256");
    }

    public String generateToken(String username, String role){
        HashMap<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(Key)
                .compact();
    }

    // public Claims extractClaims(String token) {
    //     return Jwts.parser()
    //             .verifyWith(Key)
    //             .build()
    //             .parseSignedClaims(token)
    //             .getPayload();
    // }

    private <T> T extractClaims(String token, Function<Claims, T> claimsTFunction){
        return claimsTFunction.apply(Jwts.parser().verifyWith(Key).build().parseSignedClaims(token).getPayload());
    }

    // public String extractUsername(String token) {
    //     return extractClaims(token).getSubject();
    // }
    public  String extractUsername(String token){
        return  extractClaims(token, Claims::getSubject);
    }

    public String extractRole(String token) {
        // return (String) extractClaims(token).get("role");
        return extractClaims(token, claims -> (String) claims.get("role"));
    }

    public boolean isTokenValid(String token) {
        // return extractClaims(token, Claims::getExpiration).before(new Date());
        try {
            Date expiration = extractClaims(token, Claims::getExpiration);
            return expiration.after(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}