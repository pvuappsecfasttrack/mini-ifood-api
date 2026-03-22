package com.marcosdias.miniifood.auth;

import com.marcosdias.miniifood.auth.dto.AuthResponse;
import com.marcosdias.miniifood.auth.dto.LoginRequest;
import com.marcosdias.miniifood.auth.dto.RegisterRequest;
import com.marcosdias.miniifood.security.JwtService;
import com.marcosdias.miniifood.user.domain.User;
import com.marcosdias.miniifood.user.service.UserService;
import com.marcosdias.miniifood.user.web.dto.UserResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(UserService userService, AuthenticationManager authenticationManager, JwtService jwtService) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPassword(request.password());

        User created = userService.create(user);
        return new UserResponse(
            created.getId(),
            created.getName(),
            created.getEmail(),
            created.getCreatedAt(),
            created.getUpdatedAt()
        );
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtService.generateToken(userDetails);

        return new AuthResponse(token, "Bearer");
    }
}

