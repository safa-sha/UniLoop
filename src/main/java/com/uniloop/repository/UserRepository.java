package com.uniloop.repository;
import com.uniloop.DatabaseConnection;
import com.uniloop.model.User;
import java.sql.*;
public class UserRepository {
    public User findByEmail(String email) throws SQLException {
        String sql = "SELECT user_id, name, email, password, status FROM users WHERE LOWER(email) = ? LIMIT 1";
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement s = c.prepareStatement(sql)) {
            s.setString(1, email);
            try (ResultSet r = s.executeQuery()) {
                if (!r.next()) return null;
                User user = new User(r.getString("name"), r.getString("email"), r.getString("password"));
                user.setUserId(r.getInt("user_id"));
                user.setStatus(r.getString("status"));
                return user;
            }
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
}
