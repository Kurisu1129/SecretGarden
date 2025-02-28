package com.proxy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("traffic_stats")
public class TrafficStats {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String targetHost;
    private Long bytesUp;
    private Long bytesDown;
    private Integer requestCount;
    private LocalDateTime lastAccess;
} 