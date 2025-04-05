package com.proxy.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.proxy.entity.AccessRule;
import com.proxy.mapper.AccessRuleMapper;
import com.proxy.service.IAccessControlService;

import org.springframework.stereotype.Service;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Service("accessControlService")
public class AccessControlServiceImpl extends ServiceImpl<AccessRuleMapper, AccessRule> implements IAccessControlService{
    private static final Logger logger = LogManager.getLogger(AccessControlServiceImpl.class);

    public boolean addRule(Long userId, String targetHost, boolean allowed) {
        try {
            AccessRule rule = new AccessRule();
            rule.setUserId(userId);
            rule.setTargetHost(targetHost);
            rule.setAllowed(allowed);
            return save(rule);
        } catch (Exception e) {
            logger.error("Failed to add access rule for user: " + userId, e);
            return false;
        }
    }

    public boolean isAllowed(Long userId, String targetHost) {
        try {
            AccessRule rule = baseMapper.findEffectiveRule(userId, targetHost);
            return rule == null || rule.getAllowed();
        } catch (Exception e) {
            logger.error("Failed to check access rule for user: " + userId, e);
            return false;
        }
    }

    public boolean updateRule(Long userId, String targetHost, boolean allowed) {
        try {
            AccessRule rule = lambdaQuery()
                .eq(AccessRule::getUserId, userId)
                .eq(AccessRule::getTargetHost, targetHost)
                .one();
            
            if (rule != null) {
                rule.setAllowed(allowed);
                return updateById(rule);
            }
            return false;
        } catch (Exception e) {
            logger.error("Failed to update access rule for user: " + userId, e);
            return false;
        }
    }

    public boolean deleteRule(Long userId, String targetHost) {
        try {
            return lambdaUpdate()
                .eq(AccessRule::getUserId, userId)
                .eq(AccessRule::getTargetHost, targetHost)
                .remove();
        } catch (Exception e) {
            logger.error("Failed to delete access rule for user: " + userId, e);
            return false;
        }
    }
} 