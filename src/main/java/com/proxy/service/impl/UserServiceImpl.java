package com.proxy.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.proxy.entity.User;
import com.proxy.mapper.UserMapper;
import com.proxy.service.IUserService;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Service("userService")
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService{
    private static final Logger logger = LogManager.getLogger(UserServiceImpl.class);

    public boolean createUser(String username, String password) {
        try {
            User user = new User();
            user.setUsername(username);
            user.setPassword(BCrypt.hashpw(password, BCrypt.gensalt()));
            user.setEnabled(true);
            return save(user);
        } catch (Exception e) {
            logger.error("Failed to create user: " + username, e);
            return false;
        }
    }

    public boolean authenticate(String username, String password) {
        try {
            User user = baseMapper.findByUsername(username);
            return user != null && user.getEnabled() && BCrypt.checkpw(password, user.getPassword());
        } catch (Exception e) {
            logger.error("Failed to authenticate user: " + username, e);
            return false;
        }
    }

    public Long getUserId(String username) {
        try {
            User user = baseMapper.findByUsername(username);
            return user != null ? user.getId() : null;
        } catch (Exception e) {
            logger.error("Failed to get user ID for: " + username, e);
            return null;
        }
    }

    public boolean isEnabled(String username) {
        try {
            User user = baseMapper.findByUsername(username);
            return user != null && user.getEnabled();
        } catch (Exception e) {
            logger.error("Failed to check user status: " + username, e);
            return false;
        }
    }

    public boolean updateUserStatus(String username, boolean enabled) {
        try {
            User user = baseMapper.findByUsername(username);
            if (user != null) {
                user.setEnabled(enabled);
                return updateById(user);
            }
            return false;
        } catch (Exception e) {
            logger.error("Failed to update user status: " + username, e);
            return false;
        }
    }
} 