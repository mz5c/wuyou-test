package com.wuyou.onlytest.controller.demo;

import com.wuyou.common.result.Result;
import com.wuyou.onlytest.dto.demo.UserDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Tag(name = "参数校验", description = "参数校验测试接口")
@RestController
@RequestMapping("/api/v1/validation")
public class ValidationController {

    @Operation(summary = "验证用户信息", description = "测试参数校验注解")
    @PostMapping("/user")
    public Result<Void> validateUser(@Parameter(description = "用户信息", required = true) @Valid @RequestBody UserDTO dto) {
        return Result.success(null);
    }
}
