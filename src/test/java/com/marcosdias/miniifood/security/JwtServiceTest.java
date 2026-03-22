package com.marcosdias.miniifood.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

class JwtServiceTest {

    private final JwtService jwtService = new JwtService(
        "test-secret-key-with-at-least-32-characters",
        30
    );

    @Test
    void shouldGenerateTokenAndExtractUsername() {
        UserDetails userDetails = User.withUsername("marcos@email.com")
            .password("encoded-password")
            .authorities("ROLE_USER")
            .build();

        String token = jwtService.generateToken(userDetails);

        assertEquals("marcos@email.com", jwtService.extractUsername(token));
        assertTrue(jwtService.isTokenValid(token, userDetails));
    }
}

