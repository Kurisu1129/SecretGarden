package com.proxy.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.proxy.entity.TrafficStats;
import com.proxy.mapper.TrafficStatsMapper;
import com.proxy.service.ITrafficStatsService;

import org.springframework.stereotype.Service;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("trafficStatsService")
public class TrafficStatsServiceImpl extends ServiceImpl<TrafficStatsMapper, TrafficStats> implements ITrafficStatsService{
    private static final Logger logger = LogManager.getLogger(TrafficStatsServiceImpl.class);

    public void recordTraffic(Long userId, String targetHost, long bytesUp, long bytesDown) {
        try {
            // 尝试更新现有记录
            int updated = baseMapper.updateTrafficStats(userId, targetHost, bytesUp, bytesDown);
            
            // 如果没有更新任何记录，说明需要创建新记录
            if (updated == 0) {
                TrafficStats stats = new TrafficStats();
                stats.setUserId(userId);
                stats.setTargetHost(targetHost);
                stats.setBytesUp(bytesUp);
                stats.setBytesDown(bytesDown);
                stats.setRequestCount(1);
                save(stats);
            }
        } catch (Exception e) {
            logger.error("Failed to record traffic for user: " + userId, e);
        }
    }

    public Map<String, Object> getUserStats(Long userId) {
        Map<String, Object> stats = new HashMap<>();
        try {
            TrafficStats totalStats = baseMapper.getUserTotalStats(userId);
            if (totalStats != null) {
                stats.put("totalBytesUp", totalStats.getBytesUp());
                stats.put("totalBytesDown", totalStats.getBytesDown());
                stats.put("totalRequests", totalStats.getRequestCount());
            }
        } catch (Exception e) {
            logger.error("Failed to get traffic stats for user: " + userId, e);
        }
        return stats;
    }

    public Map<String, Object> getHostStats(Long userId, String targetHost) {
        Map<String, Object> stats = new HashMap<>();
        try {
            TrafficStats hostStats = lambdaQuery()
                .eq(TrafficStats::getUserId, userId)
                .eq(TrafficStats::getTargetHost, targetHost)
                .one();
            
            if (hostStats != null) {
                stats.put("bytesUp", hostStats.getBytesUp());
                stats.put("bytesDown", hostStats.getBytesDown());
                stats.put("requestCount", hostStats.getRequestCount());
                stats.put("lastAccess", hostStats.getLastAccess());
            }
        } catch (Exception e) {
            logger.error("Failed to get host stats for user: " + userId, e);
        }
        return stats;
    }

    public Map<String, Map<String, Object>> getTopHosts(Long userId, int limit) {
        Map<String, Map<String, Object>> topHosts = new HashMap<>();
        try {
            List<TrafficStats> stats = baseMapper.getTopHosts(userId, limit);
            for (TrafficStats stat : stats) {
                Map<String, Object> hostStats = new HashMap<>();
                hostStats.put("bytesUp", stat.getBytesUp());
                hostStats.put("bytesDown", stat.getBytesDown());
                hostStats.put("requestCount", stat.getRequestCount());
                topHosts.put(stat.getTargetHost(), hostStats);
            }
        } catch (Exception e) {
            logger.error("Failed to get top hosts for user: " + userId, e);
        }
        return topHosts;
    }

    public Map<String, Long> getTotalTraffic() {
        Map<String, Long> totalTraffic = new HashMap<>();
        try {
            List<TrafficStats> allStats = list();
            long totalBytesUp = allStats.stream().mapToLong(TrafficStats::getBytesUp).sum();
            long totalBytesDown = allStats.stream().mapToLong(TrafficStats::getBytesDown).sum();
            totalTraffic.put("bytesUp", totalBytesUp);
            totalTraffic.put("bytesDown", totalBytesDown);
            totalTraffic.put("totalBytes", totalBytesUp + totalBytesDown);
        } catch (Exception e) {
            logger.error("Failed to get total traffic stats", e);
        }
        return totalTraffic;
    }
} 