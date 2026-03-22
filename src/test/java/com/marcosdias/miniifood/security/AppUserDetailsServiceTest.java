package com.marcosdias.miniifood.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.marcosdias.miniifood.user.domain.User;
import com.marcosdias.miniifood.user.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@ExtendWith(MockitoExtension.class)
class AppUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AppUserDetailsService appUserDetailsService;

    @Test
    void shouldLoadUserByEmail() {
        User user = new User();
        user.setEmail("marcos@email.com");
        user.setPassword("encoded-password");

        when(userRepository.findByEmail("marcos@email.com")).thenReturn(Optional.of(user));

        UserDetails details = appUserDetailsService.loadUserByUsername("marcos@email.com");

        assertEquals("marcos@email.com", details.getUsername());
        assertEquals("encoded-password", details.getPassword());
    }

    @Test
    void shouldThrowWhenUserIsNotFound() {
        when(userRepository.findByEmail("missing@email.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
            () -> appUserDetailsService.loadUserByUsername("missing@email.com"));
    }
}

