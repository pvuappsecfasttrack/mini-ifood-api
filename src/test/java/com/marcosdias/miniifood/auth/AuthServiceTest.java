package com.marcosdias.miniifood.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.marcosdias.miniifood.auth.dto.AuthResponse;
import com.marcosdias.miniifood.auth.dto.LoginRequest;
import com.marcosdias.miniifood.auth.dto.RegisterRequest;
import com.marcosdias.miniifood.security.JwtService;
import com.marcosdias.miniifood.user.domain.User;
import com.marcosdias.miniifood.user.service.UserService;
import com.marcosdias.miniifood.user.service.exception.EmailAlreadyInUseException;
import com.marcosdias.miniifood.user.web.dto.UserResponse;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void shouldRegisterUserSuccessfully() {
        RegisterRequest request = new RegisterRequest("Marcos", "marcos@email.com", "123456");
        User created = new User();
        created.setId(10L);
        created.setName("Marcos");
        created.setEmail("marcos@email.com");
        created.setCreatedAt(OffsetDateTime.now());
        created.setUpdatedAt(OffsetDateTime.now());

        when(userService.create(any(User.class))).thenReturn(created);

        UserResponse response = authService.register(request);

        assertEquals(10L, response.id());
        assertEquals("marcos@email.com", response.email());
        verify(userService).create(any(User.class));
    }

    @Test
    void shouldAuthenticateAndReturnJwtToken() {
        LoginRequest request = new LoginRequest("marcos@email.com", "123456");
        UserDetails userDetails = org.springframework.security.core.userdetails.User
            .withUsername("marcos@email.com")
            .password("encoded")
            .authorities("ROLE_USER")
            .build();

        Authentication authentication =
            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);
        when(jwtService.generateToken(userDetails)).thenReturn("jwt-token");

        AuthResponse response = authService.login(request);

        assertEquals("jwt-token", response.accessToken());
        assertEquals("Bearer", response.tokenType());
    }

    @Test
    void shouldPropagateAuthenticationErrorOnInvalidCredentials() {
        LoginRequest request = new LoginRequest("marcos@email.com", "wrong-password");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(BadCredentialsException.class, () -> authService.login(request));
    }

    @Test
    void shouldPropagateBusinessValidationErrorOnRegister() {
        RegisterRequest request = new RegisterRequest("Marcos", "marcos@email.com", "123456");

        when(userService.create(any(User.class)))
            .thenThrow(new EmailAlreadyInUseException("Email already in use: marcos@email.com"));

        assertThrows(EmailAlreadyInUseException.class, () -> authService.register(request));
    }
}

