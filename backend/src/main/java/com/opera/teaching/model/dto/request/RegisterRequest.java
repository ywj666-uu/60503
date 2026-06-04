package com.opera.teaching.model.dto.request;

import com.opera.teaching.model.enums.UserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    private String nickname;

    @NotNull(message = "角色不能为空")
    private UserRole role;

    private String email;
    private String phone;
}
