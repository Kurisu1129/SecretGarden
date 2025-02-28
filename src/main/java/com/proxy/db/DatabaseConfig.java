package com.proxy.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseConfig {
    private static final Logger logger = LogManager.getLogger(DatabaseConfig.class);
    private static HikariDataSource dataSource;

    public static void initialize(String url, String username, String password) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(5);
        config.setIdleTimeout(300000);
        config.setConnectionTimeout(20000);

        dataSource = new HikariDataSource(config);
        
        // 初始化数据库表
        initializeTables();
    }

    public static Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("Database not initialized");
        }
        return dataSource.getConnection();
    }

    private static void initializeTables() {
        try (Connection conn = getConnection()) {
            // 创建用户表
            conn.createStatement().execute(
                "CREATE TABLE IF NOT EXISTS users (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                "username VARCHAR(50) UNIQUE NOT NULL," +
                "password VARCHAR(60) NOT NULL," +
                "enabled BOOLEAN DEFAULT true," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")"
            );

            // 创建访问控制表
            conn.createStatement().execute(
                "CREATE TABLE IF NOT EXISTS access_rules (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                "user_id BIGINT," +
                "target_host VARCHAR(255)," +
                "allowed BOOLEAN DEFAULT true," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "FOREIGN KEY (user_id) REFERENCES users(id)" +
                ")"
            );

            // 创建流量统计表
            conn.createStatement().execute(
                "CREATE TABLE IF NOT EXISTS traffic_stats (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                "user_id BIGINT," +
                "target_host VARCHAR(255)," +
                "bytes_up BIGINT DEFAULT 0," +
                "bytes_down BIGINT DEFAULT 0," +
                "request_count INT DEFAULT 0," +
                "last_access TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "FOREIGN KEY (user_id) REFERENCES users(id)" +
                ")"
            );

            logger.info("Database tables initialized successfully");
        } catch (SQLException e) {
            logger.error("Failed to initialize database tables", e);
        }
    }

    public static void shutdown() {
        if (dataSource != null) {
            dataSource.close();
        }
    }
} 