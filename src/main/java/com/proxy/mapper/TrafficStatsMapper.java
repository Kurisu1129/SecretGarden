package com.proxy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.proxy.entity.TrafficStats;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface TrafficStatsMapper extends BaseMapper<TrafficStats> {
    @Update("UPDATE traffic_stats SET bytes_up = bytes_up + #{bytesUp}, " +
            "bytes_down = bytes_down + #{bytesDown}, request_count = request_count + 1, " +
            "last_access = NOW() " +
            "WHERE user_id = #{userId} AND target_host = #{targetHost}")
    int updateTrafficStats(Long userId, String targetHost, long bytesUp, long bytesDown);

    @Select("SELECT COALESCE(SUM(bytes_up), 0) as bytes_up, " +
            "COALESCE(SUM(bytes_down), 0) as bytes_down, " +
            "COALESCE(SUM(request_count), 0) as request_count " +
            "FROM traffic_stats WHERE user_id = #{userId}")
    TrafficStats getUserTotalStats(Long userId);

    @Select("SELECT * FROM traffic_stats WHERE user_id = #{userId} " +
            "ORDER BY bytes_up + bytes_down DESC LIMIT #{limit}")
    List<TrafficStats> getTopHosts(Long userId, int limit);
} 