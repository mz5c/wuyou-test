package com.wuyou.onlytest.controller.demo;

import com.wuyou.common.page.PageResult;
import com.wuyou.common.result.Result;
import com.wuyou.onlytest.entity.demo.User;
import java.util.List;
import com.wuyou.onlytest.dto.demo.UserDTO;
import com.wuyou.onlytest.service.demo.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public Result<PageResult<User>> page(@RequestParam(defaultValue = "1") int page,
                                         @RequestParam(defaultValue = "10") int size,
                                         @RequestParam(required = false) String keyword) {
        return Result.success(userService.pageUsers(page, size, keyword));
    }

    @GetMapping("/{id}")
    public Result<User> getById(@PathVariable Long id) {
        return Result.success(userService.getById(id));
    }

    @PostMapping
    public Result<Void> create(@Valid @RequestBody UserDTO dto) {
        User user = new User();
        BeanUtils.copyProperties(dto, user);
        userService.save(user);
        return Result.success(null);
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody UserDTO dto) {
        User user = new User();
        BeanUtils.copyProperties(dto, user);
        user.setId(id);
        userService.updateById(user);
        return Result.success(null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        userService.removeById(id);
        return Result.success(null);
    }

    /** 查询包含已逻辑删除的记录 */
    @GetMapping("/deleted-list")
    public Result<List<User>> listDeleted() {
        return Result.success(userService.list());
    }

    /** 恢复已删除的用户 */
    @PostMapping("/{id}/restore")
    public Result<Void> restore(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user != null) {
            user.setDeleted(0);
            userService.updateById(user);
        }
        return Result.success(null);
    }
}
