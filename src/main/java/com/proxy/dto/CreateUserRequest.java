package com.proxy.dto;

import lombok.Data;

import java.util.List;

@Data
public class CreateUserRequest {
    private String username;
    private String password;
    private List<AccessRuleRequest> accessRules;

    @Data
    public static class AccessRuleRequest {
        private String targetHost;
        private Boolean allowed;
    }
}

