package com.example.movieticketing.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User; // Spring's UserDetails User
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    // It's better to externalize these properties
    @Value("${app.jwt.secret:yourSuperSecretKeyWhichShouldBeLongAndComplexAndAtLeast256BitsLongForHS256}")
    private String jwtSecretString;

    @Value("${app.jwt.expiration-in-ms:3600000}") // 1 hour
    private int jwtExpirationInMs;

    private Key jwtSecretKey;
    private static final String AUTHORITIES_KEY = "auth";


    @PostConstruct
    public void init() {
        // Ensure the secret key is strong enough for HS256
        if (jwtSecretString == null || jwtSecretString.getBytes().length < 32) {
            logger.warn("JWT Secret key is not configured or too short. Using a default secure key. PLEASE CONFIGURE a strong app.jwt.secret property.");
            // Using a dynamically generated key if not configured or too short, for safety in dev.
            // In production, this MUST be a configured, stable key.
            this.jwtSecretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        } else {
            this.jwtSecretKey = Keys.hmacShaKeyFor(jwtSecretString.getBytes());
        }
    }

    public String generateToken(Authentication authentication) {
        String username = authentication.getName();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        String authoritiesString = authorities.stream()
                                            .map(GrantedAuthority::getAuthority)
                                            .collect(Collectors.joining(","));

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        return Jwts.builder()
                .setSubject(username)
                .claim(AUTHORITIES_KEY, authoritiesString)
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(jwtSecretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String getUsernameFromJWT(String token) {
        Claims claims = Jwts.parserBuilder()
                            .setSigningKey(jwtSecretKey)
                            .build()
                            .parseClaimsJws(token)
                            .getBody();
        return claims.getSubject();
    }

    public boolean validateToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(jwtSecretKey).build().parseClaimsJws(authToken);
            return true;
        } catch (SignatureException ex) {
            logger.error("Invalid JWT signature");
        } catch (MalformedJwtException ex) {
            logger.error("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            logger.error("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            logger.error("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            logger.error("JWT claims string is empty.");
        }
        return false;
    }

    public Authentication getAuthentication(String token) {
        Claims claims = Jwts.parserBuilder()
                            .setSigningKey(jwtSecretKey)
                            .build()
                            .parseClaimsJws(token)
                            .getBody();

        String username = claims.getSubject();
        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get(AUTHORITIES_KEY).toString().split(","))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

        User principal = new User(username, "", authorities); // password field is not needed here
        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }
}
