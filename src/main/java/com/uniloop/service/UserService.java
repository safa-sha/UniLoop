package com.uniloop.service;
import com.uniloop.model.User;
import com.uniloop.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
public class UserService {
    private static final Logger LOGGER = Logger.getLogger(UserService.class.getName());
    private static final String COLLEGE_DOMAIN = "saintgits.org";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Z0-9!#$%&'*+/=?^_{|}~-]+(?:\\.[A-Z0-9!#$%&'*+/=?^_{|}~-]+)*@[A-Z0-9](?:[A-Z0-9-]{0,61}[A-Z0-9])?(?:\\.[A-Z0-9](?:[A-Z0-9-]{0,61}[A-Z0-9])?)+$",
        Pattern.CASE_INSENSITIVE);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    public UserService(UserRepository userRepository) { this.userRepository = userRepository; }
    public RegistrationResult register(String nameInput, String emailInput, String password) {
        String name = nameInput == null ? "" : nameInput.trim();
        String email = emailInput == null ? "" : emailInput.trim().toLowerCase(Locale.ROOT);
        if (name.isEmpty() || name.length() > 100) return RegistrationResult.INVALID_NAME;
        if (!isValidEmailSyntax(email)) return RegistrationResult.INVALID_EMAIL_SYNTAX;
        String domain = email.substring(email.lastIndexOf('@') + 1);
        if (!COLLEGE_DOMAIN.equalsIgnoreCase(domain)) return RegistrationResult.INVALID_EMAIL_DOMAIN;
        if (email.length() > 100) return RegistrationResult.INVALID_EMAIL_SYNTAX;
        if (password == null || password.codePointCount(0, password.length()) < 8
                || password.getBytes(StandardCharsets.UTF_8).length > 72) return RegistrationResult.INVALID_PASSWORD;
        try {
            if (userRepository.existsByEmail(email)) return RegistrationResult.DUPLICATE_EMAIL;
            userRepository.create(new User(name, email, passwordEncoder.encode(password)));
            return RegistrationResult.SUCCESS;
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) return RegistrationResult.DUPLICATE_EMAIL;
            LOGGER.log(Level.SEVERE, "Could not complete user registration", e);
            return RegistrationResult.FAILED;
        }
    }
    private boolean isValidEmailSyntax(String email) {
        if (email.isEmpty() || email.length() > 254) return false;
        int at = email.indexOf('@');
        return at > 0 && at <= 64 && at == email.lastIndexOf('@') && EMAIL_PATTERN.matcher(email).matches();
    }
    public enum RegistrationResult { SUCCESS, INVALID_NAME, INVALID_EMAIL_SYNTAX, INVALID_EMAIL_DOMAIN, INVALID_PASSWORD, DUPLICATE_EMAIL, FAILED }
}