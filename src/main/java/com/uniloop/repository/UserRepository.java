package com.uniloop.repository;
import com.uniloop.DatabaseConnection;
import com.uniloop.model.AdminUser;
import com.uniloop.model.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class UserRepository {
    public User findByEmail(String email) throws SQLException {
        try (Connection c = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM users WHERE LOWER(email) = ? LIMIT 1";
            try (PreparedStatement s = c.prepareStatement(sql)) {
                s.setString(1, email);
                try (ResultSet r = s.executeQuery()) {
                    if (!r.next()) return null;
                    return readUser(r);
                }
            }
        }
    }

    public User findByUserId(int userId) throws SQLException {
        try (Connection c = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM users WHERE user_id = ? LIMIT 1";
            try (PreparedStatement s = c.prepareStatement(sql)) {
                s.setInt(1, userId);
                try (ResultSet r = s.executeQuery()) {
                    if (!r.next()) return null;
                    return readUser(r);
                }
            }
        }
    }

    public int countUsers() throws SQLException {
        return count("SELECT COUNT(*) FROM users");
    }

    public int countActiveUsers() throws SQLException {
        return count("SELECT COUNT(*) FROM users WHERE status = 'ACTIVE'");
    }

    public List<AdminUser> findAllForAdmin() throws SQLException {
        String sql = """
                SELECT user_id, name, email, role, status, created_at
                FROM users
                ORDER BY user_id DESC
                """;
        List<AdminUser> users = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement s = c.prepareStatement(sql);
             ResultSet r = s.executeQuery()) {
            while (r.next()) {
                users.add(new AdminUser(
                        r.getInt("user_id"),
                        r.getString("name"),
                        r.getString("email"),
                        r.getString("role"),
                        r.getString("status"),
                        r.getString("created_at")));
            }
        }
        return users;
    }

    private int count(String sql) throws SQLException {
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement s = c.prepareStatement(sql);
             ResultSet r = s.executeQuery()) {
            r.next();
            return r.getInt(1);
        }
    }

    public boolean existsByEmail(String email) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE LOWER(email) = ? LIMIT 1";
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement s = c.prepareStatement(sql)) {
            s.setString(1, email);
            try (ResultSet r = s.executeQuery()) { return r.next(); }
        }
    }

    public void create(User user) throws SQLException {
        String sql = "INSERT INTO users (name, email, password) VALUES (?, ?, ?)";
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement s = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            s.setString(1, user.getName());
            s.setString(2, user.getEmail());
            s.setString(3, user.getPasswordHash());
            s.executeUpdate();
            try (ResultSet keys = s.getGeneratedKeys()) { if (keys.next()) user.setUserId(keys.getInt(1)); }
        }
    }

    /**
     * authenticatedUserId must come from the server-side authenticated session,
     * not from a profile form field.
     */
    public boolean updateProfile(int authenticatedUserId, String phone, String department,
                                 Integer admissionYear, String courseProgram) throws SQLException {
        try (Connection c = DatabaseConnection.getConnection()) {
            Set<String> columns = getUserColumns(c);
            String[] requiredColumns = {"phone", "department", "admission_year", "course_program"};
            StringBuilder missingColumns = new StringBuilder();
            for (String column : requiredColumns) {
                if (!columns.contains(column)) {
                    if (missingColumns.length() > 0) missingColumns.append(", ");
                    missingColumns.append(column);
                }
            }
            if (missingColumns.length() > 0) {
                throw new SQLException("Cannot update profile; users table is missing columns: "
                        + missingColumns);
            }

            String sql = "UPDATE users SET phone = ?, department = ?, admission_year = ?, course_program = ? WHERE user_id = ?";
            try (PreparedStatement s = c.prepareStatement(sql)) {
                s.setString(1, phone);
                s.setString(2, department);
                if (admissionYear == null) {
                    s.setNull(3, Types.INTEGER);
                } else {
                    s.setInt(3, admissionYear);
                }
                s.setString(4, courseProgram);
                s.setInt(5, authenticatedUserId);
                return s.executeUpdate() > 0;
            }
        }
    }

    private User readUser(ResultSet r) throws SQLException {
        User user = new User(r.getString("name"), r.getString("email"), r.getString("password"));
        user.setUserId(r.getInt("user_id"));
        user.setRole(r.getString("role"));
        user.setStatus(r.getString("status"));
        Set<String> columns = getResultColumns(r);
        if (columns.contains("phone")) user.setPhone(r.getString("phone"));
        if (columns.contains("department")) user.setDepartment(r.getString("department"));
        if (columns.contains("admission_year")) {
            int admissionYear = r.getInt("admission_year");
            if (!r.wasNull()) user.setAdmissionYear(admissionYear);
        }
        if (columns.contains("course_program")) user.setCourseProgram(r.getString("course_program"));
        return user;
    }

    private Set<String> getUserColumns(Connection c) throws SQLException {
        try (PreparedStatement s = c.prepareStatement("SELECT * FROM users WHERE 1 = 0");
             ResultSet r = s.executeQuery()) {
            return getResultColumns(r);
        }
    }

    private Set<String> getResultColumns(ResultSet r) throws SQLException {
        Set<String> columns = new HashSet<>();
        ResultSetMetaData metadata = r.getMetaData();
        for (int i = 1; i <= metadata.getColumnCount(); i++) {
            columns.add(metadata.getColumnLabel(i).toLowerCase(Locale.ROOT));
        }
        return columns;
    }
}
