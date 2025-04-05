package com.proxy.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.proxy.entity.TrafficStats;
import java.util.Map;

public interface ITrafficStatsService extends IService<TrafficStats> {
    void recordTraffic(Long userId, String targetHost, long bytesUp, long bytesDown);
    Map<String, Object> getUserStats(Long userId);
    Map<String, Object> getHostStats(Long userId, String targetHost);
    Map<String, Map<String, Object>> getTopHosts(Long userId, int limit);
}