package com.uniloop.service;

import com.uniloop.model.User;
import com.uniloop.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.sql.SQLException;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoginService {

    private static final Logger LOGGER = Logger.getLogger(LoginService.class.getName());
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public LoginService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User authenticate(String emailInput, String password) {
        if (emailInput == null || password == null) {
            return null;
        }

        String email = emailInput.trim().toLowerCase(Locale.ROOT);
        if (email.isEmpty()) {
            return null;
        }

        try {
            User user = userRepository.findByEmail(email);
            if (user == null || !"ACTIVE".equalsIgnoreCase(user.getStatus())) {
                return null;
            }

            return passwordEncoder.matches(password, user.getPasswordHash()) ? user : null;
        } catch (SQLException | IllegalArgumentException exception) {
            LOGGER.log(Level.WARNING, "Login lookup or password verification failed", exception);
            return null;
        }
    }
}
