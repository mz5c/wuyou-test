# cURL 测试用例文档

本文档汇总了 `only-test` 模块中所有 Web 接口的 cURL 测试用例。

## 基础信息

- **基础 URL**: `http://localhost:8080`
- **API 版本**: `v1`
- **内容类型**: `application/json`
- **统一响应格式**:
  ```json
  {
    "code": 200,
    "message": "success",
    "data": { ... },
    "success": true
  }
  ```

---

## 用户接口 (User API)

### 1. 分页查询用户列表

```bash
# 基本分页查询
curl -X GET "http://localhost:8080/api/v1/users?page=1&size=10"

# 带关键字搜索
curl -X GET "http://localhost:8080/api/v1/users?page=1&size=10&keyword=admin"
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 100,
    "page": 1,
    "size": 10,
    "list": [...]
  },
  "success": true
}
```

---

### 2. 根据 ID 查询用户

```bash
curl -X GET "http://localhost:8080/api/v1/users/2"
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 2,
    "username": "zhangsan",
    "nickname": "张三",
    "phone": "13912345678",
    "email": "zhangsan@test.com",
    "deleted": 0,
    "version": 0,
    "createTime": "2026-05-09T10:10:10",
    "updateTime": "2026-05-09T10:10:10"
  },
  "success": true
}
```

---

### 3. 创建用户

```bash
curl -X POST "http://localhost:8080/api/v1/users" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "zhangsan",
    "nickname": "张三",
    "phone": "13800138001",
    "email": "zhangsan@example.com"
  }'
```

**验证规则**:
- `username`: 必填，不能为空，且唯一（UK）
- `phone`: 可选，格式必须为 11 位手机号
- `email`: 可选，格式必须为有效邮箱

**重复用户名将返回**:
```json
{
  "code": 500,
  "message": "internal server error",
  "data": null,
  "success": false
}
```

---

### 4. 更新用户

```bash
curl -X PUT "http://localhost:8080/api/v1/users/2" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "zhangsan_v2",
    "nickname": "张三v2",
    "phone": "13800138111",
    "email": "v2@test.com"
  }'
```

---

### 5. 删除用户（逻辑删除）

```bash
curl -X DELETE "http://localhost:8080/api/v1/users/3"
```

**说明**: 此接口执行逻辑删除，将用户的 `deleted` 字段设置为 1

---

### 6. 查询已删除的用户列表

```bash
curl -X GET "http://localhost:8080/api/v1/users/deleted-list"
```

**说明**: 查询所有用户记录（包含逻辑删除的），绕过 `@TableLogic` 自动过滤

---

### 7. 恢复已删除的用户

```bash
curl -X POST "http://localhost:8080/api/v1/users/3/restore"
```

**说明**: 将用户的 `deleted` 字段恢复为 0

---

## 商品接口 (Product API)

### 8. 根据 ID 查询商品

```bash
curl -X GET "http://localhost:8080/api/v1/products/1"
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "name": "iPhone 15 Pro",
    "price": 8999.00,
    "stock": 100,
    "version": 0,
    "createTime": "2026-05-09T10:10:10",
    "updateTime": "2026-05-09T10:10:10"
  },
  "success": true
}
```

---

### 9. 查询商品（不读取缓存）

```bash
curl -X GET "http://localhost:8080/api/v1/products/1/nocache"
```

**说明**: 强制从数据库查询，使用手动 RedisTemplate 操作，不经过 `@Cacheable` 注解

---

### 10. 扣减商品库存

```bash
curl -X POST "http://localhost:8080/api/v1/products/1/deduct?quantity=10"
```

**说明**: 使用 Redisson 分布式锁 + `@Transactional` 保证扣减安全，扣减后自动清除缓存

---

### 11. 乐观锁扣减库存

```bash
curl -X POST "http://localhost:8080/api/v1/products/1/deduct-optimistic?quantity=10"
```

**说明**: 使用 MyBatis-Plus `@Version` 乐观锁机制扣减库存，更新时自动校验版本号，冲突时需要重试

---

## 订单接口 (Order API)

### 12. 查询订单及订单明细

```bash
curl -X GET "http://localhost:8080/api/v1/orders/1/with-items"
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "orderNo": "ORD202401010001",
    "userId": 1,
    "totalAmount": 199.98,
    "status": 0,
    "items": [
      {
        "id": 1,
        "orderId": 1,
        "productName": "iPhone 15 Pro",
        "quantity": 2,
        "price": 99.99
      }
    ]
  },
  "success": true
}
```

---

### 13. 创建订单

```bash
curl -X POST "http://localhost:8080/api/v1/orders?userId=2" \
  -H "Content-Type: application/json" \
  -d '[
    {
      "productName": "iPhone 15 Pro",
      "quantity": 2,
      "price": 8999.00
    },
    {
      "productName": "MacBook Air M3",
      "quantity": 1,
      "price": 10999.00
    }
  ]'
```

**说明**: 创建包含多个商品明细的订单，自动计算总金额

---

### 14. 支付订单（演示事务回滚）

```bash
curl -X POST "http://localhost:8080/api/v1/orders/1/pay"
```

**说明**: 先将订单状态更新为已支付，然后尝试扣减库存（故意抛出异常演示 `@Transactional` 回滚），最终事务全部回滚

**响应**:
```json
{
  "code": 500,
  "message": "deduct stock failed, rollback",
  "data": null,
  "success": false
}
```

> 注意：此接口故意设计为失败，用于验证 `@Transactional(rollbackFor = Exception.class)` 的事务回滚行为

---

## 异步任务接口 (Async API)

### 15. 发送异步通知

```bash
# 中文消息需 URL 编码
curl -X POST "http://localhost:8080/api/v1/async/notification?userId=1&message=%E6%82%A8%E7%9A%84%E8%AE%A2%E5%8D%95%E5%B7%B2%E5%8F%91%E8%B4%A7"
```

**说明**: 异步发送用户通知，不会阻塞主线程。`message` 参数中的中文必须进行 URL 编码

**相同请求的 curl 明文版本**:
```bash
curl -X POST "http://localhost:8080/api/v1/async/notification?userId=1&message=hello"
```

---

## 文件上传接口 (File Upload API)

### 16. 上传文件

```bash
curl -X POST "http://localhost:8080/api/v1/files/upload" \
  -F "file=@/path/to/your/file.txt"
```

**示例**:
```bash
# 上传文本文件
curl -X POST "http://localhost:8080/api/v1/files/upload" \
  -F "file=@/Users/wucheng/projects/wuyou-test/upload/test.txt"

# 上传图片
curl -X POST "http://localhost:8080/api/v1/files/upload" \
  -F "file=@/path/to/image.jpg"
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": "a4c75807-6911-4abd-8769-3559c88b90ca_test-upload.txt",
  "success": true
}
```

**说明**: 文件上传后以 UUID 重命名存储，防止文件名冲突

---

## 数据验证接口 (Validation API)

### 17. 验证用户数据（有效数据）

```bash
curl -X POST "http://localhost:8080/api/v1/validation/user" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "valid_user",
    "phone": "13800138000",
    "email": "valid@example.com"
  }'
```

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": null,
  "success": true
}
```

---

### 18. 验证用户数据（无效用户名）

```bash
curl -X POST "http://localhost:8080/api/v1/validation/user" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "",
    "phone": "13800138000"
  }'
```

**响应**:
```json
{
  "code": 1001,
  "message": "username: username cannot be blank",
  "data": null,
  "success": false
}
```

---

### 19. 验证用户数据（无效手机号）

```bash
curl -X POST "http://localhost:8080/api/v1/validation/user" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "test_user",
    "phone": "123456"
  }'
```

**响应**:
```json
{
  "code": 1001,
  "message": "phone: invalid phone number",
  "data": null,
  "success": false
}
```

---

### 20. 验证用户数据（无效邮箱）

```bash
curl -X POST "http://localhost:8080/api/v1/validation/user" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "test_user",
    "email": "invalid-email"
  }'
```

**响应**:
```json
{
  "code": 1001,
  "message": "email: invalid email",
  "data": null,
  "success": false
}
```

---

## 数据库约束说明

| 表名 | 约束类型 | 字段 | 约束名 |
|------|----------|------|--------|
| `demo_user` | UNIQUE KEY | `username` | `uk_username` |
| `demo_order` | UNIQUE KEY | `order_no` | `uk_order_no` |

---

## 测试脚本

为了方便测试，可以创建一个 shell 脚本：

```bash
#!/bin/bash

BASE_URL="http://localhost:8080"

echo "=== 用户接口 ==="
echo "1. 查询用户列表"
curl -s "$BASE_URL/api/v1/users?page=1&size=10" | python3 -m json.tool

echo -e "\n2. 查询用户 ID=2"
curl -s "$BASE_URL/api/v1/users/2" | python3 -m json.tool

echo -e "\n3. 创建用户"
curl -s -X POST "$BASE_URL/api/v1/users" \
  -H "Content-Type: application/json" \
  -d '{"username":"test_user","phone":"13800138000","email":"test@example.com"}' | python3 -m json.tool

echo -e "\n=== 商品接口 ==="
echo "4. 查询商品 ID=1"
curl -s "$BASE_URL/api/v1/products/1" | python3 -m json.tool

echo -e "\n5. 扣减库存"
curl -s -X POST "$BASE_URL/api/v1/products/1/deduct?quantity=5" | python3 -m json.tool

echo -e "\n=== 订单接口 ==="
echo "6. 查询订单 ID=1"
curl -s "$BASE_URL/api/v1/orders/1/with-items" | python3 -m json.tool

echo -e "\n=== 测试完成 ==="
```

保存为 `test-api.sh`，然后执行：

```bash
chmod +x test-api.sh
./test-api.sh
```

---

## 注意事项

1. **服务启动**: 确保 `only-test` 应用已启动并运行在 `8080` 端口
2. **数据库**: 确保 MySQL 数据库已创建并初始化了测试数据
3. **Redis**: 如果使用了缓存功能，确保 Redis 服务已启动。如果因序列化问题导致 `ClassCastException`，执行 `redis-cli FLUSHALL` 清除缓存
4. **JSON 格式化**: 示例中使用 `python3 -m json.tool` 美化 JSON 输出
5. **文件上传路径**: 上传文件的路径需要根据实际情况调整
6. **URL 编码**: 中文参数需进行 URL 编码（如异步通知接口的 `message` 参数）

---

## 常见错误码

| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 1001 | 参数校验失败（`@Valid` 校验不通过） |
| 500 | 业务异常或服务器内部错误 |

---

## 测试中发现的 Bug 及修复

| Bug | 文件 | 修复方式 |
|-----|------|----------|
| `demo_user` 表 `username` 缺少唯一索引 | `schema.sql` | 新增 `UNIQUE KEY uk_username (username)` |
| Redis 缓存无法序列化 `LocalDateTime` | `RedisConfig.java` | 注册 `JavaTimeModule`，禁用时间戳输出 |
| `@Version` 乐观锁拦截器未注册 | `MybatisPlusConfig.java` | 添加 `OptimisticLockerInnerInterceptor` |

---

**文档生成时间**: 2026 年 5 月 9 日
