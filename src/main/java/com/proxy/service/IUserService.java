package com.proxy.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.proxy.entity.User;

public interface IUserService extends IService<User> {
    boolean createUser(String username, String password);
    boolean authenticate(String username, String password);
    Long getUserId(String username);
    boolean isEnabled(String username);
    boolean updateUserStatus(String username, boolean enabled);
}