package com.wuyou.onlytest.controller.demo;

import com.wuyou.common.page.PageResult;
import com.wuyou.common.result.Result;
import com.wuyou.onlytest.entity.demo.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import com.wuyou.onlytest.dto.demo.UserDTO;
import com.wuyou.onlytest.service.demo.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Tag(name = "用户管理", description = "用户 CRUD 接口")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "分页查询用户", description = "支持按关键字搜索")
    @GetMapping
    public Result<PageResult<User>> page(
            @Parameter(description = "页码", example = "1") 
            @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页大小", example = "10") 
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "搜索关键字") 
            @RequestParam(required = false) String keyword) {
        return Result.success(userService.pageUsers(page, size, keyword));
    }

    @Operation(summary = "根据 ID 查询用户")
    @GetMapping("/{id}")
    public Result<User> getById(@Parameter(description = "用户 ID") @PathVariable Long id) {
        return Result.success(userService.getById(id));
    }

    @Operation(summary = "创建用户")
    @PostMapping
    public Result<Void> create(@Parameter(description = "用户信息", required = true) @Valid @RequestBody UserDTO dto) {
        User user = new User();
        BeanUtils.copyProperties(dto, user);
        userService.save(user);
        return Result.success(null);
    }

    @Operation(summary = "更新用户")
    @PutMapping("/{id}")
    public Result<Void> update(@Parameter(description = "用户 ID") @PathVariable Long id, 
                               @Parameter(description = "用户信息", required = true) @Valid @RequestBody UserDTO dto) {
        User user = new User();
        BeanUtils.copyProperties(dto, user);
        user.setId(id);
        userService.updateById(user);
        return Result.success(null);
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@Parameter(description = "用户 ID") @PathVariable Long id) {
        userService.removeById(id);
        return Result.success(null);
    }

    @Operation(summary = "查询包含已逻辑删除的记录")
    @GetMapping("/deleted-list")
    public Result<List<User>> listDeleted() {
        return Result.success(userService.listAllIncludeDeleted());
    }

    @Operation(summary = "恢复已删除的用户")
    @PostMapping("/{id}/restore")
    public Result<Void> restore(@Parameter(description = "用户 ID") @PathVariable Long id) {
        User user = userService.getById(id);
        if (user != null) {
            user.setDeleted(0);
            userService.updateById(user);
        }
        return Result.success(null);
    }
}
