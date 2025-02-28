package com.proxy.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;

public class ProxyConfig {
    private int port = 7890;
    private String bindAddress = "127.0.0.1";
    private boolean enableHttp = true;
    private boolean enableSocks = true;
    private DatabaseConfig databaseConfig = new DatabaseConfig();

    // Database configuration inner class
    public static class DatabaseConfig {
        private String url = "jdbc:mysql://localhost:3306/proxy_db";
        private String username = "root";
        private String password = "";

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    // Getters and setters
    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getBindAddress() {
        return bindAddress;
    }

    public void setBindAddress(String bindAddress) {
        this.bindAddress = bindAddress;
    }

    public boolean isEnableHttp() {
        return enableHttp;
    }

    public void setEnableHttp(boolean enableHttp) {
        this.enableHttp = enableHttp;
    }

    public boolean isEnableSocks() {
        return enableSocks;
    }

    public void setEnableSocks(boolean enableSocks) {
        this.enableSocks = enableSocks;
    }

    public DatabaseConfig getDatabaseConfig() {
        return databaseConfig;
    }

    public void setDatabaseConfig(DatabaseConfig databaseConfig) {
        this.databaseConfig = databaseConfig;
    }

    // Load configuration from file
    public static ProxyConfig loadFromFile(String configPath) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(new File(configPath), ProxyConfig.class);
    }

    // Save configuration to file
    public void saveToFile(String configPath) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(new File(configPath), this);
    }
} 