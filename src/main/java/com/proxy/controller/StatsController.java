package com.proxy.controller;

import com.proxy.service.impl.TrafficStatsServiceImpl;
import com.proxy.service.impl.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

    @Autowired
    private TrafficStatsServiceImpl trafficStatsService;

    @Autowired
    private UserServiceImpl userService;

    @GetMapping("/users")
    public List<Map<String, Object>> getAllUsersStats() {
        return userService.list().stream()
                .map(user -> {
                    Map<String, Object> stats = trafficStatsService.getUserStats(user.getId());
                    stats.put("username", user.getUsername());
                    return stats;
                })
                .collect(Collectors.toList());
    }

    @GetMapping("/user/{userId}/top-hosts")
    public Map<String, Map<String, Object>> getUserTopHosts(@PathVariable Long userId) {
        return trafficStatsService.getTopHosts(userId, 10);
    }

    @GetMapping("/system")
    public Map<String, Object> getSystemStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userService.count());
        stats.put("activeUsers", userService.count()); // TODO: Implement active users count
        stats.put("totalTraffic", trafficStatsService.getTotalTraffic());
        return stats;
    }
} 