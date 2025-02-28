package com.proxy.service;

import com.proxy.db.DatabaseConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class TrafficStatsService {
    private static final Logger logger = LogManager.getLogger(TrafficStatsService.class);

    public void recordTraffic(Long userId, String targetHost, long bytesUp, long bytesDown) {
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "INSERT INTO traffic_stats (user_id, target_host, bytes_up, bytes_down, request_count) " +
                 "VALUES (?, ?, ?, ?, 1) " +
                 "ON DUPLICATE KEY UPDATE " +
                 "bytes_up = bytes_up + ?, " +
                 "bytes_down = bytes_down + ?, " +
                 "request_count = request_count + 1, " +
                 "last_access = CURRENT_TIMESTAMP"
             )) {
            stmt.setLong(1, userId);
            stmt.setString(2, targetHost);
            stmt.setLong(3, bytesUp);
            stmt.setLong(4, bytesDown);
            stmt.setLong(5, bytesUp);
            stmt.setLong(6, bytesDown);
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.error("Failed to record traffic for user: " + userId, e);
        }
    }

    public Map<String, Object> getUserStats(Long userId) {
        Map<String, Object> stats = new HashMap<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT SUM(bytes_up) as total_up, " +
                 "SUM(bytes_down) as total_down, " +
                 "SUM(request_count) as total_requests " +
                 "FROM traffic_stats WHERE user_id = ?"
             )) {
            stmt.setLong(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                stats.put("totalBytesUp", rs.getLong("total_up"));
                stats.put("totalBytesDown", rs.getLong("total_down"));
                stats.put("totalRequests", rs.getLong("total_requests"));
            }
        } catch (SQLException e) {
            logger.error("Failed to get traffic stats for user: " + userId, e);
        }
        
        return stats;
    }

    public Map<String, Object> getHostStats(Long userId, String targetHost) {
        Map<String, Object> stats = new HashMap<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT bytes_up, bytes_down, request_count, last_access " +
                 "FROM traffic_stats WHERE user_id = ? AND target_host = ?"
             )) {
            stmt.setLong(1, userId);
            stmt.setString(2, targetHost);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                stats.put("bytesUp", rs.getLong("bytes_up"));
                stats.put("bytesDown", rs.getLong("bytes_down"));
                stats.put("requestCount", rs.getInt("request_count"));
                stats.put("lastAccess", rs.getTimestamp("last_access"));
            }
        } catch (SQLException e) {
            logger.error("Failed to get host stats for user: " + userId, e);
        }
        
        return stats;
    }

    public Map<String, Map<String, Object>> getTopHosts(Long userId, int limit) {
        Map<String, Map<String, Object>> topHosts = new HashMap<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT target_host, bytes_up, bytes_down, request_count " +
                 "FROM traffic_stats WHERE user_id = ? " +
                 "ORDER BY (bytes_up + bytes_down) DESC LIMIT ?"
             )) {
            stmt.setLong(1, userId);
            stmt.setInt(2, limit);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Map<String, Object> hostStats = new HashMap<>();
                String host = rs.getString("target_host");
                hostStats.put("bytesUp", rs.getLong("bytes_up"));
                hostStats.put("bytesDown", rs.getLong("bytes_down"));
                hostStats.put("requestCount", rs.getInt("request_count"));
                topHosts.put(host, hostStats);
            }
        } catch (SQLException e) {
            logger.error("Failed to get top hosts for user: " + userId, e);
        }
        
        return topHosts;
    }
} 