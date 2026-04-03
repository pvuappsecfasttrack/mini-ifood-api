package com.marcosdias.miniifood.auth;

import static org.assertj.core.api.Assertions.assertThat;

import com.marcosdias.miniifood.auth.dto.AuthResponse;
import com.marcosdias.miniifood.auth.dto.LoginRequest;
import com.marcosdias.miniifood.auth.dto.RegisterRequest;
import com.marcosdias.miniifood.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AuthServiceIntegrationTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void shouldRegisterUserAndPersistEncodedPassword() {
        RegisterRequest request = new RegisterRequest("Marcos", "marcos@register.com", "123456");

        authService.register(request);

        var saved = userRepository.findByEmail("marcos@register.com").orElseThrow();
        assertThat(saved.getName()).isEqualTo("Marcos");
        assertThat(saved.getPassword()).isNotEqualTo("123456");
        assertThat(passwordEncoder.matches("123456", saved.getPassword())).isTrue();
    }

    @Test
    void shouldLoginSuccessfullyAndReturnBearerToken() {
        RegisterRequest request = new RegisterRequest("Marcos", "marcos@login.com", "123456");
        authService.register(request);

        AuthResponse response = authService.login(new LoginRequest("marcos@login.com", "123456"));

        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.accessToken()).isNotBlank();
    }
}

