package com.wuyou.onlytest.dto.demo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Schema(name = "UserDTO", description = "用户信息传输对象")
@Data
public class UserDTO {

    @Schema(description = "用户 ID", example = "1")
    private Long id;

    @Schema(description = "用户名", required = true, example = "zhangsan")
    @NotBlank(message = "username cannot be blank")
    private String username;

    @Schema(description = "昵称", example = "张三")
    private String nickname;

    @Schema(description = "手机号", pattern = "^1[3-9]\\d{9}$", example = "13800138000")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "invalid phone number")
    private String phone;

    @Schema(description = "邮箱", example = "zhangsan@example.com")
    @Email(message = "invalid email")
    private String email;
}
