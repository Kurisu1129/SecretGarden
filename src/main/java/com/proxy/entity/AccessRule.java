package com.proxy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("access_rules")
public class AccessRule {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String targetHost;
    private Boolean allowed;
    private LocalDateTime createdAt;
} 