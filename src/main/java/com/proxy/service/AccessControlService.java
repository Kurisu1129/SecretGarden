package com.proxy.service;

import com.proxy.db.DatabaseConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AccessControlService {
    private static final Logger logger = LogManager.getLogger(AccessControlService.class);

    public boolean addRule(Long userId, String targetHost, boolean allowed) {
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "INSERT INTO access_rules (user_id, target_host, allowed) VALUES (?, ?, ?)"
             )) {
            stmt.setLong(1, userId);
            stmt.setString(2, targetHost);
            stmt.setBoolean(3, allowed);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            logger.error("Failed to add access rule for user: " + userId, e);
            return false;
        }
    }

    public boolean isAllowed(Long userId, String targetHost) {
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT allowed FROM access_rules WHERE user_id = ? AND target_host = ? " +
                 "UNION " +
                 "SELECT allowed FROM access_rules WHERE user_id = ? AND target_host = '*' " +
                 "ORDER BY target_host DESC LIMIT 1"
             )) {
            stmt.setLong(1, userId);
            stmt.setString(2, targetHost);
            stmt.setLong(3, userId);
            ResultSet rs = stmt.executeQuery();
            
            // 如果没有特定规则，默认允许
            if (!rs.next()) {
                return true;
            }
            
            return rs.getBoolean("allowed");
        } catch (SQLException e) {
            logger.error("Failed to check access rule for user: " + userId, e);
            return false;
        }
    }

    public boolean updateRule(Long userId, String targetHost, boolean allowed) {
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "UPDATE access_rules SET allowed = ? WHERE user_id = ? AND target_host = ?"
             )) {
            stmt.setBoolean(1, allowed);
            stmt.setLong(2, userId);
            stmt.setString(3, targetHost);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Failed to update access rule for user: " + userId, e);
            return false;
        }
    }

    public boolean deleteRule(Long userId, String targetHost) {
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "DELETE FROM access_rules WHERE user_id = ? AND target_host = ?"
             )) {
            stmt.setLong(1, userId);
            stmt.setString(2, targetHost);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Failed to delete access rule for user: " + userId, e);
            return false;
        }
    }
} 