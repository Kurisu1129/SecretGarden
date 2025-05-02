package com.proxy.controller;

import com.proxy.dto.CreateUserRequest;
import com.proxy.dto.CreateUserRequest.AccessRuleRequest;
import com.proxy.service.IUserService;
import com.proxy.service.IAccessControlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private static final Logger logger = LogManager.getLogger(UserController.class);

    @Autowired
    private IUserService userService;

    @Autowired
    private IAccessControlService accessControlService;

    /**
     * 创建新用户并设置访问规则
     * @param request 包含用户信息和访问规则的请求对象
     * @return 创建结果
     */
    @PostMapping("/create")
    public ResponseEntity<?> createUser(@RequestBody CreateUserRequest request) {
        try {
            // 创建用户
            boolean userCreated = userService.createUser(request.getUsername(), request.getPassword());
            
            if (!userCreated) {
                return ResponseEntity.badRequest().body("Failed to create user");
            }

            // 获取新创建的用户ID
            Long userId = userService.getUserId(request.getUsername());
            
            // 创建访问规则
            if (request.getAccessRules() != null) {
                for (AccessRuleRequest rule : request.getAccessRules()) {
                    accessControlService.addRule(userId, rule.getTargetHost(), rule.getAllowed());
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("message", "User created successfully");
            response.put("username", request.getUsername());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error creating user: " + request.getUsername(), e);
            return ResponseEntity.internalServerError().body("Error creating user");
        }
    }
}