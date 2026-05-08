package com.wuyou.onlytest.controller.demo;

import com.wuyou.common.result.Result;
import com.wuyou.onlytest.dto.demo.UserDTO;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/validation")
public class ValidationController {

    @PostMapping("/user")
    public Result<Void> validateUser(@Valid @RequestBody UserDTO dto) {
        return Result.success(null);
    }
}
