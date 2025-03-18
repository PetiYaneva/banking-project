package com.example.banking_project.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.function.Function;

import static io.jsonwebtoken.SignatureAlgorithm.HS256;

@Service
public class JwtService {
    private final String SECRET_KEY = "209b55fb78302d4d0f264b2254e6b94f5fcea5a7bfc96b350b7771671739b0d12bf659b29fd8168340e0349f7f6bedeac27427d7fb0deeaf06cc9a8c23ca25695e12829b3930d95760f33ce0b6931cfa6f17f385e03ab67368155c4578d6a4167d8b9743b81540570f22b5cb5b06e408c8f16bc320bf59f991a63584d2ee92e3ba8d41f8557336646feaa64952c870c807a7600beef9a3cab2212e5c97ba64cbe2efa7feee53250cb2464c6f8564cbbe1154d3a03a9548a1eb4ccfc9494358c384710969d0ac3487394879982e26014c74d6c603e1fa6711670dceb7dadf59bbeb35553a8f58ab9562a3c87860731c5f2bf46b29e2fee3ab1e8957ed4073564b";

    private final long EXPIRATION_TIME = 60000; // 1 ден в милисекунди

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver){
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }


    public String generateToken(UserDetails userDetails
    ) {
        return Jwts
                .builder()
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSigningKey(), HS256)
                .compact();
    }

    public String extractEmail(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts
                    .parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
