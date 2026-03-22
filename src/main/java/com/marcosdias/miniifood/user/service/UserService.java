package com.marcosdias.miniifood.user.service;

import com.marcosdias.miniifood.user.domain.User;
import com.marcosdias.miniifood.user.repository.UserRepository;
import com.marcosdias.miniifood.user.service.exception.EmailAlreadyInUseException;
import com.marcosdias.miniifood.user.service.exception.ResourceNotFoundException;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public User findById(Long id) {
        return getExistingUser(id);
    }

    @Transactional
    public User create(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new EmailAlreadyInUseException("Email already in use: " + user.getEmail());
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @Transactional
    public User update(Long id, User userData) {
        User user = getExistingUser(id);

        if (!user.getEmail().equals(userData.getEmail()) && userRepository.existsByEmail(userData.getEmail())) {
            throw new EmailAlreadyInUseException("Email already in use: " + userData.getEmail());
        }

        user.setName(userData.getName());
        user.setEmail(userData.getEmail());
        user.setPassword(passwordEncoder.encode(userData.getPassword()));

        return userRepository.save(user);
    }

    @Transactional
    public void delete(Long id) {
        User user = getExistingUser(id);
        userRepository.delete(user);
    }

    private User getExistingUser(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }
}

