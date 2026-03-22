package com.marcosdias.miniifood.user.repository;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.marcosdias.miniifood.user.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;

@SpringBootTest
class UserRepositoryIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldPersistAndFindUserByEmail() {
        User user = new User();
        user.setName("Marcos");
        user.setEmail("marcos@email.com");
        user.setPassword("123456");

        User saved = userRepository.save(user);

        assertNotNull(saved.getId());
        assertTrue(userRepository.findByEmail("marcos@email.com").isPresent());
    }

    @Test
    void shouldRejectDuplicatedEmail() {
        User firstUser = new User();
        firstUser.setName("Marcos");
        firstUser.setEmail("duplicated@email.com");
        firstUser.setPassword("123456");

        User secondUser = new User();
        secondUser.setName("Maria");
        secondUser.setEmail("duplicated@email.com");
        secondUser.setPassword("abcdef");

        userRepository.saveAndFlush(firstUser);

        assertThrows(DataIntegrityViolationException.class, () -> {
            userRepository.saveAndFlush(secondUser);
        });
    }
}

