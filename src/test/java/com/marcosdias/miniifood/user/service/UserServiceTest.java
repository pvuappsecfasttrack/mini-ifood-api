package com.marcosdias.miniifood.user.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void shouldCreateUserWhenEmailIsAvailable() {
        User input = new User();
        input.setName("Marcos");
        input.setEmail("marcos@email.com");
        input.setPassword("123456");

        when(userRepository.existsByEmail(input.getEmail())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(input);

        User created = userService.create(input);

        assertEquals("marcos@email.com", created.getEmail());
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
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User updated = userService.update(1L, input);

        assertEquals("New", updated.getName());
        assertEquals("old@email.com", updated.getEmail());
    }
}

