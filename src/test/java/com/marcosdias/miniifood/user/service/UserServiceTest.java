package com.marcosdias.miniifood.user.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.marcosdias.miniifood.user.domain.User;
import com.marcosdias.miniifood.user.repository.UserRepository;
import com.marcosdias.miniifood.user.service.exception.EmailAlreadyInUseException;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void shouldCreateUserWhenEmailIsAvailable() {
        User input = new User();
        input.setName("Marcos");
        input.setEmail("marcos@email.com");
        input.setPassword("123456");

        when(userRepository.existsByEmail(input.getEmail())).thenReturn(false);
        when(passwordEncoder.encode("123456")).thenReturn("encoded-123456");
        when(userRepository.save(any(User.class))).thenReturn(input);

        User created = userService.create(input);

        assertEquals("marcos@email.com", created.getEmail());
        verify(passwordEncoder).encode("123456");
        verify(userRepository).save(input);
    }

    @Test
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        User input = new User();
        input.setEmail("marcos@email.com");

        when(userRepository.existsByEmail(input.getEmail())).thenReturn(true);

        assertThrows(EmailAlreadyInUseException.class, () -> userService.create(input));
    }

    @Test
    void shouldUpdateUserWhenEmailRemainsSame() {
        User existing = new User();
        existing.setId(1L);
        existing.setName("Old");
        existing.setEmail("old@email.com");
        existing.setPassword("123456");

        User input = new User();
        input.setName("New");
        input.setEmail("old@email.com");
        input.setPassword("abcdef");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(passwordEncoder.encode(eq("abcdef"))).thenReturn("encoded-abcdef");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User updated = userService.update(1L, input);

        assertEquals("New", updated.getName());
        assertEquals("old@email.com", updated.getEmail());
        assertEquals("encoded-abcdef", updated.getPassword());
    }
}

