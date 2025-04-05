package com.proxy.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.proxy.entity.AccessRule;

public interface IAccessControlService extends IService<AccessRule> {
    boolean addRule(Long userId, String targetHost, boolean allowed);
    boolean isAllowed(Long userId, String targetHost);
    boolean updateRule(Long userId, String targetHost, boolean allowed);
    boolean deleteRule(Long userId, String targetHost);
}