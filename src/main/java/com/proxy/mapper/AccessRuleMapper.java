package com.proxy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.proxy.entity.AccessRule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AccessRuleMapper extends BaseMapper<AccessRule> {
    @Select("SELECT * FROM access_rules WHERE user_id = #{userId} AND target_host IN (#{targetHost}, '*') ORDER BY target_host DESC LIMIT 1")
    AccessRule findEffectiveRule(Long userId, String targetHost);
} 