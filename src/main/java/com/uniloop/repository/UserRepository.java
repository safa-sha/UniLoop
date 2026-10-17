package com.uniloop.repository;
import com.uniloop.DatabaseConnection;
import com.uniloop.model.AdminChangeResult;
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

    public boolean isActiveAdmin(int userId) throws SQLException {
        String sql = "SELECT role, status FROM users WHERE user_id = ? LIMIT 1";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement s = c.prepareStatement(sql)) {
            s.setInt(1, userId);
            try (ResultSet r = s.executeQuery()) {
                return r.next()
                        && "ADMIN".equalsIgnoreCase(r.getString("role"))
                        && "ACTIVE".equalsIgnoreCase(r.getString("status"));
            }
        }
    }

    public int countUsers() throws SQLException {
        return count("SELECT COUNT(*) FROM users");
    }

    public int countActiveUsers() throws SQLException {
        return count("SELECT COUNT(*) FROM users WHERE status = 'ACTIVE'");
    }

    public List<AdminUser> findAllForAdmin(String search, String role, String status) throws SQLException {
        String sql = """
                SELECT user_id, name, email, role, status, created_at
                FROM users
                WHERE (? = '' OR LOWER(name) LIKE ? OR LOWER(email) LIKE ?)
                  AND (? = '' OR role = ?)
                  AND (? = '' OR status = ?)
                ORDER BY user_id DESC
                """;
        List<AdminUser> users = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement s = c.prepareStatement(sql)) {
            String normalizedSearch = search == null ? "" : search.trim().toLowerCase(Locale.ROOT);
            String normalizedRole = role == null ? "" : role;
            String normalizedStatus = status == null ? "" : status;
            s.setString(1, normalizedSearch);
            s.setString(2, "%" + normalizedSearch + "%");
            s.setString(3, "%" + normalizedSearch + "%");
            s.setString(4, normalizedRole);
            s.setString(5, normalizedRole);
            s.setString(6, normalizedStatus);
            s.setString(7, normalizedStatus);
            try (ResultSet r = s.executeQuery()) {
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
        }
        return users;
    }

    public AdminChangeResult changeAccountStatus(int userId, String expectedStatus,
                                                  String newStatus) throws SQLException {
        if (userId <= 0 || !(("ACTIVE".equals(expectedStatus) && "INACTIVE".equals(newStatus))
                || ("INACTIVE".equals(expectedStatus) && "ACTIVE".equals(newStatus)))) {
            return AdminChangeResult.INVALID_STATE;
        }
        try (Connection c = DatabaseConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                int activeAdminCount = lockAndCountActiveAdmins(c);
                String currentRole;
                String currentStatus;
                try (PreparedStatement s = c.prepareStatement(
                        "SELECT role, status FROM users WHERE user_id = ? FOR UPDATE")) {
                    s.setInt(1, userId);
                    try (ResultSet r = s.executeQuery()) {
                        if (!r.next()) {
                            c.rollback();
                            return AdminChangeResult.NOT_FOUND;
                        }
                        currentRole = r.getString("role");
                        currentStatus = r.getString("status");
                    }
                }

                if (!expectedStatus.equalsIgnoreCase(currentStatus)) {
                    c.rollback();
                    return AdminChangeResult.INVALID_STATE;
                }
                if ("ADMIN".equalsIgnoreCase(currentRole)
                        && "ACTIVE".equalsIgnoreCase(currentStatus)
                        && "INACTIVE".equals(newStatus)
                        && activeAdminCount <= 1) {
                    c.rollback();
                    return AdminChangeResult.LAST_ACTIVE_ADMIN;
                }

                try (PreparedStatement s = c.prepareStatement(
                        "UPDATE users SET status = ? WHERE user_id = ? AND status = ?")) {
                    s.setString(1, newStatus);
                    s.setInt(2, userId);
                    s.setString(3, expectedStatus);
                    if (s.executeUpdate() != 1) {
                        c.rollback();
                        return AdminChangeResult.INVALID_STATE;
                    }
                }
                c.commit();
                return AdminChangeResult.SUCCESS;
            } catch (SQLException exception) {
                c.rollback();
                throw exception;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }

    public AdminChangeResult changeRole(int userId, String expectedRole,
                                        String newRole) throws SQLException {
        if (userId <= 0 || !(("STUDENT".equals(expectedRole) && "ADMIN".equals(newRole))
                || ("ADMIN".equals(expectedRole) && "STUDENT".equals(newRole)))) {
            return AdminChangeResult.INVALID_STATE;
        }
        try (Connection c = DatabaseConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                int activeAdminCount = lockAndCountActiveAdmins(c);
                String currentRole;
                String currentStatus;
                try (PreparedStatement s = c.prepareStatement(
                        "SELECT role, status FROM users WHERE user_id = ? FOR UPDATE")) {
                    s.setInt(1, userId);
                    try (ResultSet r = s.executeQuery()) {
                        if (!r.next()) {
                            c.rollback();
                            return AdminChangeResult.NOT_FOUND;
                        }
                        currentRole = r.getString("role");
                        currentStatus = r.getString("status");
                    }
                }

                if (!expectedRole.equalsIgnoreCase(currentRole)) {
                    c.rollback();
                    return AdminChangeResult.INVALID_STATE;
                }
                if ("ADMIN".equalsIgnoreCase(currentRole)
                        && "ACTIVE".equalsIgnoreCase(currentStatus)
                        && "STUDENT".equals(newRole)
                        && activeAdminCount <= 1) {
                    c.rollback();
                    return AdminChangeResult.LAST_ACTIVE_ADMIN;
                }
                if ("STUDENT".equalsIgnoreCase(currentRole)
                        && "ADMIN".equals(newRole)
                        && !"ACTIVE".equalsIgnoreCase(currentStatus)) {
                    c.rollback();
                    return AdminChangeResult.INVALID_STATE;
                }

                try (PreparedStatement s = c.prepareStatement(
                        "UPDATE users SET role = ? WHERE user_id = ? AND role = ?")) {
                    s.setString(1, newRole);
                    s.setInt(2, userId);
                    s.setString(3, expectedRole);
                    if (s.executeUpdate() != 1) {
                        c.rollback();
                        return AdminChangeResult.INVALID_STATE;
                    }
                }
                c.commit();
                return AdminChangeResult.SUCCESS;
            } catch (SQLException exception) {
                c.rollback();
                throw exception;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }

    private int lockAndCountActiveAdmins(Connection connection) throws SQLException {
        int count = 0;
        String sql = """
                SELECT user_id
                FROM users
                WHERE role = 'ADMIN' AND status = 'ACTIVE'
                ORDER BY user_id
                FOR UPDATE
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) count++;
        }
        return count;
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
    public boolean updateProfile(int authenticatedUserId, String name, String phone,
                                 String department, Integer admissionYear,
                                 String courseProgram) throws SQLException {
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

            String sql = "UPDATE users SET name = ?, phone = ?, department = ?, admission_year = ?, course_program = ? WHERE user_id = ?";
            try (PreparedStatement s = c.prepareStatement(sql)) {
                s.setString(1, name);
                s.setString(2, phone);
                s.setString(3, department);
                if (admissionYear == null) {
                    s.setNull(4, Types.INTEGER);
                } else {
                    s.setInt(4, admissionYear);
                }
                s.setString(5, courseProgram);
                s.setInt(6, authenticatedUserId);
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
