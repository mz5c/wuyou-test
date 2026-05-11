package com.wuyou.onlytest.datasource.controller;

import com.wuyou.common.result.Result;
import com.wuyou.onlytest.datasource.service.DatasourceService;
import com.wuyou.onlytest.entity.demo.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "多数据源测试")
@RestController
@RequestMapping("/api/v1/datasource")
@RequiredArgsConstructor
public class DatasourceController {

    private final DatasourceService datasourceService;

    @Operation(summary = "从库查询用户列表")
    @GetMapping("/users")
    public Result<List<User>> users() {
        return Result.success(datasourceService.listUsersFromSlave());
    }

    @Operation(summary = "主库写入用户")
    @PostMapping("/user")
    public Result<Void> createUser(@RequestParam String username, @RequestParam String nickname) {
        datasourceService.createUserOnMaster(username, nickname);
        return Result.success(null);
    }

    @Operation(summary = "强制走主库读取用户")
    @GetMapping("/users/{id}/fresh")
    public Result<User> freshRead(@PathVariable Long id) {
        return Result.success(datasourceService.getUserFromMaster(id));
    }
}
