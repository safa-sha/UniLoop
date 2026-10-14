package com.uniloop.service;
import com.uniloop.DatabaseConnection;
import com.uniloop.model.User;
import com.uniloop.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Year;
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
        if (!isValidPassword(password)) return RegistrationResult.INVALID_PASSWORD;
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

    public User getUserById(int userId) throws SQLException {
        if (userId <= 0) return null;
        return userRepository.findByUserId(userId);
    }

    public ProfileUpdateResult updateProfile(int authenticatedUserId, String phoneInput,
                                             String departmentInput, Integer admissionYear,
                                             String courseProgramInput) {
        if (authenticatedUserId <= 0) return ProfileUpdateResult.INVALID_USER_ID;

        String department = departmentInput == null ? "" : departmentInput.trim();
        if (department.isEmpty()) return ProfileUpdateResult.INVALID_DEPARTMENT;
        if (department.length() > 100) return ProfileUpdateResult.INVALID_DEPARTMENT;

        int currentYear = Year.now().getValue();
        if (admissionYear == null || admissionYear < 1900 || admissionYear > currentYear + 1) {
            return ProfileUpdateResult.INVALID_ADMISSION_YEAR;
        }

        String courseProgram = courseProgramInput == null ? "" : courseProgramInput.trim();
        if (courseProgram.isEmpty() || courseProgram.length() > 150) {
            return ProfileUpdateResult.INVALID_COURSE_PROGRAM;
        }

        String phone = phoneInput == null ? null : phoneInput.trim();
        if (phone != null && phone.isEmpty()) phone = null;
        if (phone != null && phone.length() > 20) return ProfileUpdateResult.INVALID_PHONE;

        try {
            return userRepository.updateProfile(authenticatedUserId, phone, department,
                    admissionYear, courseProgram)
                    ? ProfileUpdateResult.SUCCESS : ProfileUpdateResult.USER_NOT_FOUND;
        } catch (SQLException exception) {
            LOGGER.log(Level.WARNING, "Could not update user profile", exception);
            return ProfileUpdateResult.FAILED;
        }
    }

    public PasswordChangeResult changePassword(int authenticatedUserId, String currentPassword,
                                               String newPassword) {
        if (authenticatedUserId <= 0) return PasswordChangeResult.INVALID_USER_ID;
        if (currentPassword == null || currentPassword.isEmpty()) {
            return PasswordChangeResult.INVALID_CURRENT_PASSWORD;
        }
        if (!isValidPassword(newPassword)) return PasswordChangeResult.INVALID_NEW_PASSWORD;

        try {
            User user = userRepository.findByUserId(authenticatedUserId);
            if (user == null) return PasswordChangeResult.USER_NOT_FOUND;
            if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
                return PasswordChangeResult.INVALID_CURRENT_PASSWORD;
            }

            String sql = "UPDATE users SET password = ? WHERE user_id = ?";
            try (Connection connection = DatabaseConnection.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, passwordEncoder.encode(newPassword));
                statement.setInt(2, authenticatedUserId);
                return statement.executeUpdate() > 0
                        ? PasswordChangeResult.SUCCESS : PasswordChangeResult.USER_NOT_FOUND;
            }
        } catch (SQLException | IllegalArgumentException exception) {
            LOGGER.log(Level.WARNING, "Could not change user password", exception);
            return PasswordChangeResult.FAILED;
        }
    }

    private boolean isValidPassword(String password) {
        return password != null && password.codePointCount(0, password.length()) >= 8
                && password.getBytes(StandardCharsets.UTF_8).length <= 72;
    }

    private boolean isValidEmailSyntax(String email) {
        if (email.isEmpty() || email.length() > 254) return false;
        int at = email.indexOf('@');
        return at > 0 && at <= 64 && at == email.lastIndexOf('@') && EMAIL_PATTERN.matcher(email).matches();
    }

    public enum RegistrationResult { SUCCESS, INVALID_NAME, INVALID_EMAIL_SYNTAX, INVALID_EMAIL_DOMAIN, INVALID_PASSWORD, DUPLICATE_EMAIL, FAILED }

    public enum ProfileUpdateResult {
        SUCCESS, INVALID_USER_ID, INVALID_DEPARTMENT, INVALID_ADMISSION_YEAR,
        INVALID_COURSE_PROGRAM, INVALID_PHONE, USER_NOT_FOUND, FAILED
    }

    public enum PasswordChangeResult {
        SUCCESS, INVALID_USER_ID, USER_NOT_FOUND, INVALID_CURRENT_PASSWORD,
        INVALID_NEW_PASSWORD, FAILED
    }
}