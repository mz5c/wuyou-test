package com.wuyou.onlytest.dto.demo;

import lombok.Data;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
public class UserDTO {

    private Long id;

    @NotBlank(message = "username cannot be blank")
    private String username;

    private String nickname;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "invalid phone number")
    private String phone;

    @Email(message = "invalid email")
    private String email;
}
