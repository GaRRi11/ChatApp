package com.gary.ChatApp.web.dto;

import lombok.Data;

@Data
public class AuthRequest {
    private String name;
    private String password;
}
