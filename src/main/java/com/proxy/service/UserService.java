package com.proxy.service;

import com.proxy.db.DatabaseConfig;
import org.mindrot.jbcrypt.BCrypt;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserService {
    private static final Logger logger = LogManager.getLogger(UserService.class);

    public boolean createUser(String username, String password) {
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "INSERT INTO users (username, password) VALUES (?, ?)"
             )) {
            stmt.setString(1, username);
            stmt.setString(2, hashedPassword);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            logger.error("Failed to create user: " + username, e);
            return false;
        }
    }

    public boolean authenticate(String username, String password) {
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT password, enabled FROM users WHERE username = ?"
             )) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next() && rs.getBoolean("enabled")) {
                String hashedPassword = rs.getString("password");
                return BCrypt.checkpw(password, hashedPassword);
            }
            return false;
        } catch (SQLException e) {
            logger.error("Failed to authenticate user: " + username, e);
            return false;
        }
    }

    public Long getUserId(String username) {
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT id FROM users WHERE username = ?"
             )) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getLong("id");
            }
            return null;
        } catch (SQLException e) {
            logger.error("Failed to get user ID for: " + username, e);
            return null;
        }
    }

    public boolean isEnabled(String username) {
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT enabled FROM users WHERE username = ?"
             )) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            
            return rs.next() && rs.getBoolean("enabled");
        } catch (SQLException e) {
            logger.error("Failed to check user status: " + username, e);
            return false;
        }
    }

    public boolean updateUserStatus(String username, boolean enabled) {
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "UPDATE users SET enabled = ? WHERE username = ?"
             )) {
            stmt.setBoolean(1, enabled);
            stmt.setString(2, username);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Failed to update user status: " + username, e);
            return false;
        }
    }
} 