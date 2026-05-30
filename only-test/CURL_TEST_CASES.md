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

## LLM 聊天接口 (LLM Chat API)

### 16. 非流式聊天

```bash
curl -X POST "http://localhost:8080/api/v1/llm/chat" \
  -H "Content-Type: application/json" \
  -d '{
    "prompt": "你好，请用中文回答"
  }'
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": "你好！有什么可以帮助你的吗？",
  "success": true
}
```

**指定模型**:
```bash
curl -X POST "http://localhost:8080/api/v1/llm/chat" \
  -H "Content-Type: application/json" \
  -d '{
    "prompt": "讲个笑话",
    "model": "gpt-4o"
  }'
```

---

### 17. 流式聊天 (SSE)

```bash
curl -N -X POST "http://localhost:8080/api/v1/llm/chat/stream" \
  -H "Content-Type: application/json" \
  -d '{
    "prompt": "请用中文介绍你自己"
  }'
```

**SSE 事件流示例**:
```
event:token
data:{"content":"我会"}

event:token
data:{"content":"用"}

event:token
data:{"content":"最适合"}

event:token
data:{"content":"你的"}

event:token
data:{"content":"方式"}

event:token
data:{"content":"提供帮助"}

event:token
data:{"content":"！"}

event:token
data:{"content":"😊"}

event:token
data:{"content":""}

event:done
data:[DONE]
```

**说明**: 流式接口使用 SSE (Server-Sent Events) 协议，逐个 token 返回 `event: token` 事件，全部完成后发送 `event: done` 事件。`-N` 参数禁用 curl 的缓冲，确保实时输出。

---

## 文件上传接口 (File Upload API)

### 18.1 上传文件

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

### 18.2 下载文件

```bash
curl -X GET "http://localhost:8080/api/v1/files/download/{filename}" -O
```

**示例**:
```bash
# 下载上传返回的文件名
curl -X GET "http://localhost:8080/api/v1/files/download/a4c75807-6911-4abd-8769-3559c88b90ca_test-upload.txt" -O

# 直接输出到终端
curl -X GET "http://localhost:8080/api/v1/files/download/a4c75807-6911-4abd-8769-3559c88b90ca_test-upload.txt"
```

**说明**: 以附件流形式下载已上传的文件，`-O` 参数保持原文件名保存，不带 `-O` 则输出到终端

**响应**:
```
# 二进制文件流（Content-Type: application/octet-stream）
# Content-Disposition: attachment; filename="xxx"
```

---

## 数据验证接口 (Validation API)

### 19. 验证用户数据（有效数据）

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

### 20. 验证用户数据（无效用户名）

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

### 21. 验证用户数据（无效手机号）

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

### 22. 验证用户数据（无效邮箱）

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

## 幂等接口 (Idempotent API)

### 23. Redis 幂等支付

```bash
curl -X POST "http://localhost:8080/api/v1/idempotent/pay?orderId=10001&userId=1"
```

**首次请求成功响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": "支付成功",
  "success": true
}
```

**重复请求（处理中）响应**:
```json
{
  "code": 500,
  "message": "请求正在处理中，请勿重复提交",
  "data": null,
  "success": false
}
```

**说明**: 基于 Redis SET NX 实现幂等，相同 `orderId` 在 `ttlSeconds` 内重复提交会被拒绝。首次完成后标记为 `DONE`，后续重复请求会返回缓存结果。

---

### 24. 数据库幂等支付

```bash
# 首次请求
curl -X POST "http://localhost:8080/api/v1/idempotent/pay-db?bizType=order.pay&bizId=10001"

# 重复请求（相同 bizType + bizId）
curl -X POST "http://localhost:8080/api/v1/idempotent/pay-db?bizType=order.pay&bizId=10001"
```

**说明**: 基于数据库唯一键约束实现幂等，重复请求会抛出唯一键冲突异常。`bizType` 和 `bizId` 联合构成业务幂等键。

---

## 限流接口 (Rate Limit API)

### 25. 本地限流（单机 QPS 5）

```bash
# 正常请求
curl -X GET "http://localhost:8080/api/v1/ratelimit/local"

# 快速连续请求（触发限流）
for i in $(seq 1 10); do
  curl -s -X GET "http://localhost:8080/api/v1/ratelimit/local" &
done
wait
```

**正常响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": "local ok",
  "success": true
}
```

**被限流响应**:
```json
{
  "code": 500,
  "message": "触发本地限流，请稍后重试",
  "data": null,
  "success": false
}
```

**说明**: 使用 Guava `RateLimiter`，每秒 5 个令牌，超出等待 `timeoutMs` 后抛出限流异常。仅单机有效。

---

### 26. 分布式限流（Redis 滑动窗口）

```bash
# 正常请求
curl -X GET "http://localhost:8080/api/v1/ratelimit/distributed"

# 快速连续请求（触发限流）
for i in $(seq 1 10); do
  curl -s -X GET "http://localhost:8080/api/v1/ratelimit/distributed" &
done
wait
```

**被限流响应**:
```json
{
  "code": 500,
  "message": "触发分布式限流，请稍后重试",
  "data": null,
  "success": false
}
```

**说明**: 基于 Redis Lua 脚本实现滑动窗口算法，每秒最多 3 次请求。窗口精确到毫秒级，所有实例共享同一计数器。

---

### 27. 限流压力测试

```bash
curl -X GET "http://localhost:8080/api/v1/ratelimit/stress"
```

**说明**: 同时触发本地限流和分布式限流两种场景，用于验证限流逻辑在多并发下的表现。

---

## 分布式锁接口 (Distributed Lock API)

### 28. 获取读锁

```bash
curl -X GET "http://localhost:8080/api/v1/locks/product/1/read"
```

**说明**: 使用 Redisson `RReadWriteLock` 获取读锁。读锁之间不互斥，可多个线程同时持有。

---

### 29. 获取写锁

```bash
curl -X POST "http://localhost:8080/api/v1/locks/product/1/write?stock=50"
```

**说明**: 使用 Redisson `RReadWriteLock` 获取写锁。写锁与读锁互斥，与写锁互斥。写锁持有期间模拟 3 秒业务处理。

---

### 30. 信号量获取

```bash
curl -X POST "http://localhost:8080/api/v1/locks/semaphore/acquire?name=mySemaphore&permits=1"
```

**说明**: 使用 Redisson `RSemaphore` 尝试获取信号量。信号量初始值为 3，获取后可用许可数减少。

---

### 31. 信号量释放

```bash
curl -X POST "http://localhost:8080/api/v1/locks/semaphore/release?name=mySemaphore&permits=1"
```

**说明**: 释放之前获取的信号量许可，释放后可用许可数增加。

**信号量完整测试流程**:
```bash
# 1. 获取 2 个许可
curl -s -X POST "http://localhost:8080/api/v1/locks/semaphore/acquire?name=mySemaphore&permits=2"

# 2. 获取 1 个许可（此时只剩 0 个可用）
curl -s -X POST "http://localhost:8080/api/v1/locks/semaphore/acquire?name=mySemaphore&permits=1"

# 3. 再次获取（阻塞，因为可用许可为 0）
curl -s -X POST "http://localhost:8080/api/v1/locks/semaphore/acquire?name=mySemaphore&permits=1"

# 4. 释放 1 个许可
curl -s -X POST "http://localhost:8080/api/v1/locks/semaphore/release?name=mySemaphore&permits=1"
```

---

## 异步编排接口 (CompletableFuture API)

### 32. 并行查询（allOf）

```bash
curl -X GET "http://localhost:8080/api/v1/async/parallel?userId=1"
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "userInfo": {"userId": 1, "name": "用户1"},
    "orders": [{"orderId": 1, "amount": 100}],
    "recommendations": ["商品推荐A", "商品推荐B"],
    "elapsedMs": 2010
  },
  "success": true
}
```

**说明**: 使用 `CompletableFuture.allOf` 并行执行 3 个任务（用户信息 1.5s、订单列表 2s、商品推荐 1s），总耗时约 2s（最慢任务），大幅缩短串行执行的 4.5s。

---

### 33. 竞速查询（anyOf）

```bash
curl -X POST "http://localhost:8080/api/v1/async/race?userId=1"
```

**说明**: 使用 `CompletableFuture.anyOf` 多个数据源同时查询，取最先返回的结果。模拟多数据源竞速查询场景。

---

### 34. 异常处理

```bash
# 正常请求
curl -X GET "http://localhost:8080/api/v1/async/error-handling?shouldFail=false"

# 触发异常
curl -X GET "http://localhost:8080/api/v1/async/error-handling?shouldFail=true"
```

**正常响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "step1Result": "步骤1完成",
    "step2Result": "步骤2完成",
    "step3Result": null,
    "exceptionMessage": null
  },
  "success": true
}
```

**异常响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "step1Result": "步骤1完成",
    "step2Result": null,
    "step3Result": "异常恢复：使用了默认值",
    "exceptionMessage": "步骤2模拟异常"
  },
  "success": true
}
```

**说明**: 演示 `CompletableFuture.exceptionally` 异常恢复机制。当 `shouldFail=true` 时，步骤 2 抛出异常，步骤 3 通过 `exceptionally` 提供默认值兜底。

---

## 多数据源接口 (Dynamic Datasource API)

### 35. 从库查询用户列表

```bash
curl -X GET "http://localhost:8080/api/v1/datasource/users"
```

**说明**: 使用 `@DS("slave")` 强制路由到从库查询。适用于读写分离场景的读操作。

---

### 36. 主库写入用户

```bash
curl -X POST "http://localhost:8080/api/v1/datasource/user" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "test_user",
    "nickname": "test_user"
  }'
```

**说明**: 使用 `@DS("master")` 强制路由到主库执行写操作。

---

### 37. 主库读取（强一致性读）

```bash
curl -X GET "http://localhost:8080/api/v1/datasource/users/1/fresh"
```

**说明**: 写入后立即从主库读取，避免从库同步延迟导致的脏读问题。

---

## 分布式 ID 接口 (Distributed ID API)

### 38. 雪花算法 ID

```bash
curl -X GET "http://localhost:8080/api/v1/idgen/snowflake"
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 185632947712000000,
    "type": "snowflake"
  },
  "success": true
}
```

**说明**: 基于 Hutool `Snowflake`，使用 `datacenterId=1, workerId=1`。生成的 ID 全局唯一、趋势递增。

---

### 39. Redis 自增 ID

```bash
# 订单类型 ID
curl -X GET "http://localhost:8080/api/v1/idgen/redis/order"

# 用户类型 ID
curl -X GET "http://localhost:8080/api/v1/idgen/redis/user"
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "type": "redis:order"
  },
  "success": true
}
```

**说明**: 基于 Redis `INCR` 命令，按业务类型（`bizType`）分别自增。每次调用 +1，适合对趋势递增和业务前缀有要求的场景。

---

### 40. 批量性能对比

```bash
# 生成 1000 个 ID 做性能对比
curl -X GET "http://localhost:8080/api/v1/idgen/compare?count=1000"
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "snowflakeIds": [...],
    "redisIds": [...],
    "snowflakeElapsedMs": 12,
    "redisElapsedMs": 150
  },
  "success": true
}
```

**说明**: 同时生成指定数量的雪花算法 ID 和 Redis ID，返回各自耗时。通常雪花算法比 Redis 自增快 10-20 倍。

---

## Spring Retry 接口 (Retry API)

### 41. 不可靠服务调用

```bash
# 默认失败（模拟服务不可用）
curl -X POST "http://localhost:8080/api/v1/retry/unstable?shouldFail=true"

# 模拟成功
curl -X POST "http://localhost:8080/api/v1/retry/unstable?shouldFail=false"
```

**失败后重试成功响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": "第3次重试后终于成功",
  "success": true
}
```

**彻底失败响应**:
```json
{
  "code": 500,
  "message": "业务异常: 服务调用失败",
  "data": null,
  "success": false
}
```

**说明**: `@Retryable(maxAttempts=3, backoff=@Backoff(delay=1000, multiplier=2))`，重试间隔依次为 1s、2s。第 1-2 次失败继续重试，第 3 次失败抛出异常。

---

### 42. 回退方法测试

```bash
curl -X POST "http://localhost:8080/api/v1/retry/fallback"
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": "服务暂时不可用，请稍后重试",
  "success": true
}
```

**说明**: 触发 `@Recover` 回退方法。当 `@Retryable` 重试耗尽后，由 `@Recover` 方法返回降级结果，不抛出异常。

---

## RocketMQ 消息接口 (RocketMQ API)

### 43. 同步发送消息

```bash
curl -X POST "http://localhost:8080/api/v1/mq/send?topic=demo-order&body=order_10001&mode=sync"
```

**说明**: 同步发送，等待 Broker 返回确认结果。可靠性最高，适合关键业务消息。

---

### 44. 异步发送消息

```bash
curl -X POST "http://localhost:8080/api/v1/mq/send?topic=demo-order&body=order_10002&mode=async"
```

**说明**: 异步发送，通过回调接收发送结果。不阻塞主线程，适合对 TPS 要求高的场景。

---

### 45. 单向发送消息

```bash
curl -X POST "http://localhost:8080/api/v1/mq/send?topic=demo-order&body=order_10003&mode=oneway"
```

**说明**: 单向发送，不等待任何响应。吞吐量最高，适合日志等可丢失场景。

---

### 46. 顺序消息

```bash
curl -X POST "http://localhost:8080/api/v1/mq/send-orderly?body=order_10004&orderKey=order_10004"
```

**说明**: 使用 `RocketMQTemplate.syncSendOrderly` 发送顺序消息。相同 `orderKey` 的消息落在同一队列，消费端按序处理。服务端 `OrderStatusConsumer` 配置 `consumeMode=ORDERLY`。

---

### 47. 事务消息（半消息）

```bash
curl -X POST "http://localhost:8080/api/v1/mq/send-transaction?body=order_10005"
```

**说明**: 发送事务消息（半消息），RocketMQ 先持久化半消息，执行本地事务后提交或回滚。服务端 `TransactionConsumer` 消费已提交的事务消息。

---

## Seata 分布式事务接口 (Seata Distributed Transaction API)

### 48. AT 模式下单

```bash
curl -X POST "http://localhost:8080/api/v1/seata/at/order?userId=1&productId=1&quantity=1"
```

**说明**: AT 模式（自动补偿），使用 `@GlobalTransactional`。框架自动生成 UNDO LOG，业务无需感知。当分布式事务中任意一步失败时，自动回滚所有分支事务。

**前置条件**: 启动 Seata Server (`seata-server.sh -p 8091`)，数据库已创建 `undo_log` 表。

---

### 49. TCC 模式转账

```bash
curl -X POST "http://localhost:8080/api/v1/seata/tcc/transfer?fromUserId=1&toUserId=2&amount=100"
```

**说明**: TCC 模式（手动补偿），使用 `@LocalTCC` + `@TwoPhaseBusinessAction`。分为三个阶段：
- **Try**: 冻结转出方资金
- **Confirm**: 扣减冻结资金，增加转入方余额
- **Cancel**: 解冻转出方资金

**前置条件**: 启动 Seata Server。

---

### 50. Saga 模式下单

```bash
curl -X POST "http://localhost:8080/api/v1/seata/saga/order?userId=1&productId=1&quantity=1"
```

**说明**: Saga 模式（状态机编排），使用 Seata `StateMachineEngine`。通过 `order-fulfillment-saga.json` 状态机定义四步流程：CreateOrder → DeductStock → DeductBalance → Notify。每步有对应的补偿操作，异常时按反向顺序执行补偿。

**前置条件**: 启动 Seata Server。

---

### 51. XA 模式下单

```bash
curl -X POST "http://localhost:8080/api/v1/seata/xa/order?userId=1&productId=1&quantity=1"
```

**说明**: XA 模式（数据库原生事务），使用 `data-source-proxy-mode: XA`。基于数据库的 XA 协议实现，事务隔离性最强，但性能相对较低。

**前置条件**: 启动 Seata Server，配置文件中已设置 `seata.enableAutoDataSourceProxy=true`。

---

## 数据库约束说明（补充）

| 表名 | 约束类型 | 字段 | 约束名 |
|------|----------|------|--------|
| `idempotent_record` | UNIQUE KEY | `biz_type`, `biz_id` | `uk_biz_type_biz_id` |
| `seata_account` | INDEX | `user_id` | `idx_user_id` |

---

## 各模块前置依赖

| 模块 | 前置依赖 | 备注 |
|------|----------|------|
| 幂等 | Redis | Redis SET NX 实现 |
| 限流 | Redis（分布式限流） | Guava（本地限流）无需额外依赖 |
| 分布式锁 | Redis | Redisson 锁 |
| CompletableFuture | 无 | JDK 内置 |
| 多数据源 | MySQL | 主从指向同一实例，用于功能验证 |
| 分布式ID | Redis（自增 ID） | 雪花算法无需额外依赖 |
| Spring Retry | 无 | Spring AOP 内置 |
| RocketMQ | RocketMQ Broker（127.0.0.1:9876） | 需启动 NameServer 和 Broker |
| Seata | Seata Server（127.0.0.1:8091） | 四种模式均需 Seata Server |

---

## 本地缓存接口 (Local Cache API)

### 52. Guava 自动加载缓存

```bash
curl -X GET "http://localhost:8080/api/v1/local-cache/guava/auto-load?key=hello"
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": "loaded:hello",
  "success": true
}
```

**说明**: 同一 key 第二次请求直接命中缓存，不触发 `CacheLoader` 加载。

---

### 53. Guava 写入过期

```bash
# 首次请求
curl -X GET "http://localhost:8080/api/v1/local-cache/guava/write-expire?key=test"

# 5s 后再次请求（已过期，重新加载）
curl -X GET "http://localhost:8080/api/v1/local-cache/guava/write-expire?key=test"
```

**说明**: `expireAfterWrite=5s`，写入 5 秒后过期，过期后首次访问重新加载。观察返回值的时间戳是否变化。

---

### 54. Guava 访问过期

```bash
curl -X GET "http://localhost:8080/api/v1/local-cache/guava/access-expire?key=test"
```

**说明**: `expireAfterAccess=5s`，5 秒内无任何读写则过期。持续访问不会过期（每次访问重置计时器）。

---

### 55. Guava 缓存统计

```bash
curl -X GET "http://localhost:8080/api/v1/local-cache/guava/stats"
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "hitCount": 2,
    "missCount": 2,
    "hitRate": 0.5,
    "evictionCount": 0,
    "loadCount": 2,
    "totalLoadTime": 0.0
  },
  "success": true
}
```

---

### 56. Guava 淘汰监听

```bash
curl -X POST "http://localhost:8080/api/v1/local-cache/guava/removal-test"
```

**说明**: 缓存 `maximumSize=5`，插入 10 个 key，触发 5 次淘汰。观察服务端日志中的 `RemovalListener` 输出。

---

### 57. Caffeine 基本缓存

```bash
curl -X GET "http://localhost:8080/api/v1/local-cache/caffeine/basic?key=hello"
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": "basic:hello",
  "success": true
}
```

---

### 58. Caffeine LoadingCache

```bash
curl -X GET "http://localhost:8080/api/v1/local-cache/caffeine/loading?key=key1"
```

**说明**: 使用 `CacheLoader` 自动加载缓存，第二次请求命中。

---

### 59. Caffeine 定时刷新

```bash
# 首次请求
curl -X GET "http://localhost:8080/api/v1/local-cache/caffeine/refresh?key=rk"

# 等待 3s 后再次请求（触发异步刷新）
sleep 3 && curl -X GET "http://localhost:8080/api/v1/local-cache/caffeine/refresh?key=rk"
```

**说明**: `refreshAfterWrite=3s`，写入 3 秒后访问触发异步刷新，刷新期间返回旧值。观察服务端日志中的"异步刷新"输出。

---

### 60. Caffeine 异步加载

```bash
curl -X GET "http://localhost:8080/api/v1/local-cache/caffeine/async?key=hello"
```

**说明**: 使用 `AsyncLoadingCache`，异步线程加载缓存，不阻塞主请求。

---

### 61. Caffeine 自定义过期

```bash
# short 前缀的 key 2s 过期
curl -X GET "http://localhost:8080/api/v1/local-cache/caffeine/custom-expire?key=short-x"
sleep 3 && curl -X GET "http://localhost:8080/api/v1/local-cache/caffeine/custom-expire?key=short-x"

# 普通 key 10s 过期
curl -X GET "http://localhost:8080/api/v1/local-cache/caffeine/custom-expire?key=normal-y"
```

**说明**: `Expiry` 接口实现不同 key 不同 TTL。`short-` 前缀的 key 2 秒过期，其余 key 10 秒过期。

---

### 62. Caffeine 批量获取

```bash
curl -X GET "http://localhost:8080/api/v1/local-cache/caffeine/batch?keys=a,b,c"
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "a": "loaded:a",
    "b": "loaded:b",
    "c": "loaded:c"
  },
  "success": true
}
```

---

### 63. Caffeine 淘汰监听

```bash
curl -X POST "http://localhost:8080/api/v1/local-cache/caffeine/eviction-test"
```

**说明**: 缓存 `maximumSize=3`，插入 10 个 key，触发 7 次淘汰。观察服务端日志中的 `evictionListener` 输出。

---

### 64. Caffeine 详细统计

```bash
curl -X GET "http://localhost:8080/api/v1/local-cache/caffeine/stats"
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "hitCount": 0,
    "missCount": 1,
    "hitRate": 0.0,
    "missRate": 1.0,
    "evictionCount": 0,
    "evictionWeight": 0,
    "loadCount": 1,
    "loadFailureCount": 0,
    "totalLoadTime": 0,
    "averageLoadPenalty": 0.0,
    "requestCount": 1
  },
  "success": true
}
```

---

### 65. 失效缓存

```bash
curl -X POST "http://localhost:8080/api/v1/local-cache/invalidate?key=hello"
```

**说明**: 同时失效 Guava 和 Caffeine 中指定 key 的缓存。

---

## 各模块前置依赖（更新）

| 模块 | 前置依赖 | 备注 |
|------|----------|------|
| 本地缓存 | 无 | Guava Cache 和 Caffeine 均为进程内缓存，无需外部中间件 |

---

---

## ES 操作接口 (Elasticsearch API)

### HTTP API 方式（无 ES 依赖）

> 基础路径: `/api/v1/es/http`

#### 66. 创建索引

```bash
# 创建简单索引（不带 mapping）
curl -X POST "http://localhost:8080/api/v1/es/http/index/my-index" \
  -H "Content-Type: application/json" \
  -d '{}'

# 创建索引并指定 settings 和 mappings
curl -X POST "http://localhost:8080/api/v1/es/http/index/my-index" \
  -H "Content-Type: application/json" \
  -d '{
    "settings": {
      "number_of_shards": 1,
      "number_of_replicas": 0
    },
    "mappings": {
      "properties": {
        "title": {"type": "text"},
        "price": {"type": "double"},
        "createTime": {"type": "date"}
      }
    }
  }'
```

---

#### 67. 检查索引是否存在

```bash
curl -X GET "http://localhost:8080/api/v1/es/http/index/my-index/exists"
```

**响应**: `true` 或 `false`

---

#### 68. 获取索引信息

```bash
curl -X GET "http://localhost:8080/api/v1/es/http/index/my-index"
```

---

#### 69. 列出所有索引

```bash
curl -X GET "http://localhost:8080/api/v1/es/http/indices"
```

---

#### 70. 创建文档（自动生成 ID）

```bash
curl -X POST "http://localhost:8080/api/v1/es/http/doc/my-index" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "iPhone 15 Pro",
    "price": 8999.00,
    "tags": ["手机", "苹果"],
    "createTime": "2026-05-17"
  }'
```

---

#### 71. 创建文档（指定 ID）

```bash
curl -X PUT "http://localhost:8080/api/v1/es/http/doc/my-index/1" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "MacBook Air M3",
    "price": 10999.00,
    "tags": ["笔记本", "苹果"],
    "createTime": "2026-05-17"
  }'
```

---

#### 72. 获取文档

```bash
curl -X GET "http://localhost:8080/api/v1/es/http/doc/my-index/1"
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "_index": "my-index",
    "_id": "1",
    "_version": 1,
    "_seq_no": 0,
    "_primary_term": 1,
    "found": true,
    "_source": {
      "title": "MacBook Air M3",
      "price": 10999.0,
      "tags": ["笔记本", "苹果"],
      "createTime": "2026-05-17"
    }
  },
  "success": true
}
```

---

#### 73. 更新文档（部分更新）

```bash
curl -X POST "http://localhost:8080/api/v1/es/http/doc/my-index/1/update" \
  -H "Content-Type: application/json" \
  -d '{
    "price": 9999.00
  }'
```

---

#### 74. 搜索文档

```bash
# matchAll 查询
curl -X POST "http://localhost:8080/api/v1/es/http/search/my-index" \
  -H "Content-Type: application/json" \
  -d '{
    "query": {
      "match_all": {}
    }
  }'

# 按条件搜索
curl -X POST "http://localhost:8080/api/v1/es/http/search/my-index" \
  -H "Content-Type: application/json" \
  -d '{
    "query": {
      "match": {
        "title": "iPhone"
      }
    }
  }'
```

---

#### 75. 删除文档

```bash
curl -X DELETE "http://localhost:8080/api/v1/es/http/doc/my-index/1"
```

---

#### 76. 删除索引

```bash
curl -X DELETE "http://localhost:8080/api/v1/es/http/index/my-index"
```

---

### Spring Data 方式（引入 ES 依赖）

> 基础路径: `/api/v1/es/sd`

#### 77. 创建索引

```bash
curl -X POST "http://localhost:8080/api/v1/es/sd/index/my-index-sd" \
  -H "Content-Type: application/json" \
  -d '{
    "settings": {
      "number_of_shards": 1,
      "number_of_replicas": 0
    },
    "mappings": {
      "properties": {
        "name": {"type": "text"},
        "price": {"type": "double"},
        "date": {"type": "date"}
      }
    }
  }'
```

---

#### 78. 检查索引是否存在

```bash
curl -X GET "http://localhost:8080/api/v1/es/sd/index/my-index-sd/exists"
```

---

#### 79. 获取索引信息

```bash
curl -X GET "http://localhost:8080/api/v1/es/sd/index/my-index-sd"
```

---

#### 80. 创建文档（自动生成 ID）

```bash
curl -X POST "http://localhost:8080/api/v1/es/sd/doc/my-index-sd" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Galaxy S25",
    "price": 6999.00,
    "date": "2026-05-17"
  }'
```

---

#### 81. 创建文档（指定 ID）

```bash
curl -X PUT "http://localhost:8080/api/v1/es/sd/doc/my-index-sd/1" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Galaxy S25 Ultra",
    "price": 9999.00,
    "date": "2026-05-17"
  }'
```

---

#### 82. 获取文档

```bash
curl -X GET "http://localhost:8080/api/v1/es/sd/doc/my-index-sd/1"
```

---

#### 83. 更新文档

```bash
curl -X POST "http://localhost:8080/api/v1/es/sd/doc/my-index-sd/1/update" \
  -H "Content-Type: application/json" \
  -d '{
    "price": 8999.00
  }'
```

---

#### 84. 搜索文档

```bash
# 全量搜索
curl -X GET "http://localhost:8080/api/v1/es/sd/search/my-index-sd?size=10"

# 按字段搜索
curl -X GET "http://localhost:8080/api/v1/es/sd/search/my-index-sd/field?field=name&value=Galaxy&size=10"

# 原始 JSON 查询搜索
curl -X POST "http://localhost:8080/api/v1/es/sd/search/my-index-sd/raw?size=10" \
  -H "Content-Type: application/json" \
  -d '{
    "match": { "name": "Galaxy" }
  }'
```

---

#### 85. 删除文档

```bash
curl -X DELETE "http://localhost:8080/api/v1/es/sd/doc/my-index-sd/1"
```

---

#### 86. 删除索引

```bash
curl -X DELETE "http://localhost:8080/api/v1/es/sd/index/my-index-sd"
```

---

# 金额交易测试沙箱 (tx)

## 账户管理

#### 87. 开户

```bash
curl -s -X POST "http://localhost:8080/api/v1/tx/accounts?userId=1001"
```

#### 88. 查询账户

```bash
curl -s -X GET "http://localhost:8080/api/v1/tx/accounts/1"
```

#### 89. 充值

```bash
curl -s -X POST "http://localhost:8080/api/v1/tx/accounts/1/recharge?amount=10000.00"
```

开通第二个账户用于转账测试：

```bash
curl -s -X POST "http://localhost:8080/api/v1/tx/accounts?userId=1002"
```

充值 5000：

```bash
curl -s -X POST "http://localhost:8080/api/v1/tx/accounts/2/recharge?amount=5000.00"
```

## 转账

#### 90. 基础转账 (REQUIRED + 悲观锁)

```bash
curl -s -X POST "http://localhost:8080/api/v1/tx/transfer" \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccountNo": "ACC2056735586217189376",
    "toAccountNo": "ACC2056735893416402944",
    "amount": 1000.00
  }'
```

#### 91. 悲观锁转账

```bash
curl -s -X POST "http://localhost:8080/api/v1/tx/transfer/pessimistic" \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccountNo": "ACC2056735586217189376",
    "toAccountNo": "ACC2056735893416402944",
    "amount": 500.00
  }'
```

#### 92. 乐观锁转账

```bash
curl -s -X POST "http://localhost:8080/api/v1/tx/transfer/optimistic" \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccountNo": "ACC2056735586217189376",
    "toAccountNo": "ACC2056735893416402944",
    "amount": 200.00
  }'
```

#### 93. 分布式锁转账 (Redisson)

```bash
curl -s -X POST "http://localhost:8080/api/v1/tx/transfer/distributed-lock" \
  -H "Content-Type: application/json" \
  -d '{
    "fromAccountNo": "ACC2056735586217189376",
    "toAccountNo": "ACC2056735893416402944",
    "amount": 300.00
  }'
```

## 分页查询转账记录

#### 94. 查询转账记录

```bash
curl -s -X GET "http://localhost:8080/api/v1/tx/records?page=1&size=10"
```

按账户查询：

```bash
curl -s -X GET "http://localhost:8080/api/v1/tx/records?page=1&size=10&accountNo=ACNO1001"
```

## 金额分摊计算

#### 95. 按比例分摊

```bash
curl -s -X POST "http://localhost:8080/api/v1/tx/calc/split" \
  -H "Content-Type: application/json" \
  -d '{
    "totalAmount": 1000.00,
    "ratios": [30, 30, 30, 10]
  }'
```

余数兜底分摊（尾差测试）：

```bash
curl -s -X POST "http://localhost:8080/api/v1/tx/calc/split" \
  -H "Content-Type: application/json" \
  -d '{
    "totalAmount": 100.00,
    "ratios": [33, 33, 33]
  }'
```

自定义精度与舍入模式：

```bash
curl -s -X POST "http://localhost:8080/api/v1/tx/calc/split" \
  -H "Content-Type: application/json" \
  -d '{
    "totalAmount": 200.00,
    "ratios": [1, 1, 1],
    "scale": 4,
    "roundingMode": "HALF_UP"
  }'
```

---

# Redis 数据结构测试

## List 列表

> List 是基于链表的有序结构，支持左右两端推入/弹出，适合消息队列、最新消息列表等场景。

#### 96. List - 右侧推入 (RPUSH)

```bash
# 依次从右侧推入元素 A、B、C，列表变为 [A, B, C]
curl -s -X POST "http://localhost:8080/api/v1/redis/ds/list/rpush?key=mylist&value=A"
curl -s -X POST "http://localhost:8080/api/v1/redis/ds/list/rpush?key=mylist&value=B"
curl -s -X POST "http://localhost:8080/api/v1/redis/ds/list/rpush?key=mylist&value=C"
```

#### 97. List - 左侧推入 (LPUSH)

```bash
# 从左侧推入 X，列表变为 [X, A, B, C]
curl -s -X POST "http://localhost:8080/api/v1/redis/ds/list/lpush?key=mylist&value=X"
```

#### 98. List - 获取范围 (LRANGE)

```bash
# 获取全部元素
curl -s -X GET "http://localhost:8080/api/v1/redis/ds/list/range?key=mylist"
```

**响应示例**:
```json
{
  "success": true,
  "data": ["X", "A", "B", "C"]
}
```

#### 99. List - 获取列表长度 (LLEN)

```bash
curl -s -X GET "http://localhost:8080/api/v1/redis/ds/list/size?key=mylist"
```

**响应**: `{"success": true, "data": 4}`

#### 100. List - 设置指定索引 (LSET)

```bash
# 将索引 1 的值改为 "Z"，列表变为 [X, Z, B, C]
curl -s -X POST "http://localhost:8080/api/v1/redis/ds/list/set?key=mylist&index=1&value=Z"
```

#### 101. List - 右侧弹出 (RPOP)

```bash
curl -s -X POST "http://localhost:8080/api/v1/redis/ds/list/rpop?key=mylist"
```

**响应**: `{"success": true, "data": "C"}`

#### 102. List - 左侧弹出 (LPOP)

```bash
curl -s -X POST "http://localhost:8080/api/v1/redis/ds/list/lpop?key=mylist"
```

**响应**: `{"success": true, "data": "X"}`

#### 103. List - 删除指定元素 (LREM)

```bash
# 删除列表中所有值为 "A" 的元素
curl -s -X POST "http://localhost:8080/api/v1/redis/ds/list/remove?key=mylist&count=0&value=A"
```

**说明**: `count=0` 删除所有匹配的元素，`count>0` 从头部删指定数量，`count<0` 从尾部删指定数量。

## Hash 散列

> Hash 是 field-value 映射表，适合存储对象、配置信息等结构化数据。

#### 104. Hash - 设置字段值 (HSET)

```bash
curl -s -X POST "http://localhost:8080/api/v1/redis/ds/hash/put?key=user:1001&field=name&value=%E5%BC%A0%E4%B8%89"
curl -s -X POST "http://localhost:8080/api/v1/redis/ds/hash/put?key=user:1001&field=age&value=28"
curl -s -X POST "http://localhost:8080/api/v1/redis/ds/hash/put?key=user:1001&field=city&value=%E5%8C%97%E4%BA%AC"
```

#### 105. Hash - 批量设置 (HMSET)

```bash
curl -s -X POST "http://localhost:8080/api/v1/redis/ds/hash/put-all?key=user:1002" \
  -H "Content-Type: application/json" \
  -d '{"name":"李四","age":32,"city":"上海","dept":"技术部"}'
```

#### 106. Hash - 获取字段值 (HGET)

```bash
curl -s -X GET "http://localhost:8080/api/v1/redis/ds/hash/get?key=user:1001&field=name"
```

**响应**: `{"success": true, "data": "张三"}`

#### 107. Hash - 获取所有字段和值 (HGETALL)

```bash
curl -s -X GET "http://localhost:8080/api/v1/redis/ds/hash/entries?key=user:1001"
```

**响应示例**:
```json
{
  "success": true,
  "data": {
    "name": "张三",
    "age": "28",
    "city": "北京"
  }
}
```

#### 108. Hash - 获取所有字段名 (HKEYS)

```bash
curl -s -X GET "http://localhost:8080/api/v1/redis/ds/hash/keys?key=user:1001"
```

#### 109. Hash - 获取所有字段值 (HVALS)

```bash
curl -s -X GET "http://localhost:8080/api/v1/redis/ds/hash/values?key=user:1001"
```

#### 110. Hash - 判断字段是否存在 (HEXISTS)

```bash
curl -s -X GET "http://localhost:8080/api/v1/redis/ds/hash/exists?key=user:1001&field=name"
curl -s -X GET "http://localhost:8080/api/v1/redis/ds/hash/exists?key=user:1001&field=salary"
```

#### 111. Hash - 获取字段数量 (HLEN)

```bash
curl -s -X GET "http://localhost:8080/api/v1/redis/ds/hash/size?key=user:1001"
```

#### 112. Hash - 删除字段 (HDEL)

```bash
# 删除 city 字段，可同时删除多个字段
curl -s -X POST "http://localhost:8080/api/v1/redis/ds/hash/delete?key=user:1001&fields=city"
```

## Set 集合

> Set 是无序、不可重复的集合，支持交集/并集/差集运算，适合标签、关注关系等场景。

#### 113. Set - 添加元素 (SADD)

```bash
# 创建集合 set:dev，包含 3 个元素
curl -s -X POST "http://localhost:8080/api/v1/redis/ds/set/add?key=set:dev&values=Java&values=Python&values=Go"

# 创建集合 set:ops，包含 3 个元素
curl -s -X POST "http://localhost:8080/api/v1/redis/ds/set/add?key=set:ops&values=Python&values=Go&values=Docker"

# 重复添加不会生效
curl -s -X POST "http://localhost:8080/api/v1/redis/ds/set/add?key=set:dev&values=Java"
```

#### 114. Set - 获取所有元素 (SMEMBERS)

```bash
curl -s -X GET "http://localhost:8080/api/v1/redis/ds/set/members?key=set:dev"
```

**响应示例**:
```json
{
  "success": true,
  "data": ["Java", "Python", "Go"]
}
```

#### 115. Set - 判断元素是否存在 (SISMEMBER)

```bash
curl -s -X GET "http://localhost:8080/api/v1/redis/ds/set/contains?key=set:dev&value=Go"
curl -s -X GET "http://localhost:8080/api/v1/redis/ds/set/contains?key=set:dev&value=Rust"
```

**响应**: `true` / `false`

#### 116. Set - 获取元素个数 (SCARD)

```bash
curl -s -X GET "http://localhost:8080/api/v1/redis/ds/set/size?key=set:dev"
```

#### 117. Set - 交集 (SINTER)

```bash
# dev 和 ops 都掌握的技能
curl -s -X GET "http://localhost:8080/api/v1/redis/ds/set/intersect?key1=set:dev&key2=set:ops"
```

**响应**: `["Python", "Go"]`

#### 118. Set - 并集 (SUNION)

```bash
# dev 和 ops 的所有技能（去重）
curl -s -X GET "http://localhost:8080/api/v1/redis/ds/set/union?key1=set:dev&key2=set:ops"
```

**响应**: `["Java", "Python", "Go", "Docker"]`

#### 119. Set - 差集 (SDIFF)

```bash
# dev 有但 ops 没有的技能
curl -s -X GET "http://localhost:8080/api/v1/redis/ds/set/diff?key1=set:dev&key2=set:ops"
```

**响应**: `["Java"]`

#### 120. Set - 随机获取元素 (SRANDMEMBER)

```bash
curl -s -X GET "http://localhost:8080/api/v1/redis/ds/set/random-member?key=set:dev"
```

#### 121. Set - 随机弹出元素 (SPOP)

```bash
curl -s -X POST "http://localhost:8080/api/v1/redis/ds/set/pop?key=set:dev"
```

#### 122. Set - 删除元素 (SREM)

```bash
curl -s -X POST "http://localhost:8080/api/v1/redis/ds/set/remove?key=set:ops&values=Docker"
```

## ZSet 有序集合

> ZSet 是有序、不可重复的集合，每个元素关联一个 score（分数），按分数排序。适合排行榜、延时队列等场景。

#### 123. ZSet - 添加元素 (ZADD)

```bash
# 创建排行榜，添加商品销量
curl -s -X POST "http://localhost:8080/api/v1/redis/ds/zset/add?key=rank:sales&value=iPhone15&score=850"
curl -s -X POST "http://localhost:8080/api/v1/redis/ds/zset/add?key=rank:sales&value=MacBookAir&score=620"
curl -s -X POST "http://localhost:8080/api/v1/redis/ds/zset/add?key=rank:sales&value=%E5%8D%8E%E4%B8%BAP70&score=980"
```

#### 124. ZSet - 批量添加

```bash
curl -s -X POST "http://localhost:8080/api/v1/redis/ds/zset/add-batch?key=rank:sales" \
  -H "Content-Type: application/json" \
  -d '{"GalaxyS25": 750, "小米14": 890, "OPPOFindX8": 530}'
```

#### 125. ZSet - 获取分数 (ZSCORE)

```bash
curl -s -X GET "http://localhost:8080/api/v1/redis/ds/zset/score?key=rank:sales&value=iPhone15"
```

**响应**: `{"success": true, "data": 850.0}`

#### 126. ZSet - 获取正序排名 (ZRANK)

```bash
# 从低到高排名，0 为最低
curl -s -X GET "http://localhost:8080/api/v1/redis/ds/zset/rank?key=rank:sales&value=MacBookAir"
```

#### 127. ZSet - 获取倒序排名 (ZREVRANK)

```bash
# 从高到低排名，0 为最高（适合排行榜）
curl -s -X GET "http://localhost:8080/api/v1/redis/ds/zset/reverse-rank?key=rank:sales&value=iPhone15"
```

#### 128. ZSet - 按索引范围获取 (ZRANGE)

```bash
# 正序获取全部元素
curl -s -X GET "http://localhost:8080/api/v1/redis/ds/zset/range?key=rank:sales"
```

**响应示例**:
```json
{
  "success": true,
  "data": ["OPPOFindX8", "MacBookAir", "GalaxyS25", "iPhone15", "小米14", "华为P70"]
}
```

#### 129. ZSet - 按索引范围倒序获取 (ZREVRANGE)

```bash
# 销量排行榜（从高到低）
curl -s -X GET "http://localhost:8080/api/v1/redis/ds/zset/reverse-range?key=rank:sales"
```

**响应示例**:
```json
{
  "success": true,
  "data": ["华为P70", "小米14", "iPhone15", "GalaxyS25", "MacBookAir", "OPPOFindX8"]
}
```

#### 130. ZSet - 按分数范围获取 (ZRANGEBYSCORE)

```bash
# 获取销量 600-900 的商品
curl -s -X GET "http://localhost:8080/api/v1/redis/ds/zset/range-by-score?key=rank:sales&min=600&max=900"
```

**响应**: `["MacBookAir", "GalaxyS25", "iPhone15", "小米14"]`

#### 131. ZSet - 获取元素个数 (ZCARD)

```bash
curl -s -X GET "http://localhost:8080/api/v1/redis/ds/zset/size?key=rank:sales"
```

#### 132. ZSet - 统计分数范围内元素个数 (ZCOUNT)

```bash
# 销量 >= 800 的商品个数
curl -s -X GET "http://localhost:8080/api/v1/redis/ds/zset/count?key=rank:sales&min=800&max=99999"
```

**响应**: `{"success": true, "data": 3}`

#### 133. ZSet - 增加分数 (ZINCRBY)

```bash
# iPhone15 销量 +50，变为 900
curl -s -X POST "http://localhost:8080/api/v1/redis/ds/zset/increment-score?key=rank:sales&value=iPhone15&delta=50"
```

**响应**: `{"success": true, "data": 900.0}`

#### 134. ZSet - 删除元素 (ZREM)

```bash
# 删除 OPPOFindX8
curl -s -X POST "http://localhost:8080/api/v1/redis/ds/zset/remove?key=rank:sales&values=OPPOFindX8"
```

---

# Netty 网络编程测试

## 服务器管理

#### 135. 查看所有 Netty 服务器状态

```bash
curl -s -X GET "http://localhost:8080/api/v1/netty/servers"
```

**响应示例**:
```json
{
  "success": true,
  "data": {
    "echo": false,
    "protocol": false,
    "http": false,
    "websocket": false,
    "heartbeat": false
  }
}
```

## Echo TCP 回显

> Echo 是最基础的 TCP 演示：Client 发送消息，Server 原样返回。演示 `ServerBootstrap`、`ChannelInitializer`、`ChannelHandler` 的核心管道模型。

#### 136. Echo - 启动服务器

```bash
curl -s -X POST "http://localhost:8080/api/v1/netty/echo/start"
```

#### 137. Echo - 发送消息并接收回显

```bash
curl -s -X POST "http://localhost:8080/api/v1/netty/echo/send?msg=HelloNetty"
```

**响应示例**: `{"success": true, "data": "ECHO: HelloNetty"}`

```bash
# 多条消息测试
curl -s -X POST "http://localhost:8080/api/v1/netty/echo/send?msg=%E4%BD%A0%E5%A5%BD%EF%BC%8C%E4%B8%96%E7%95%8C"
```

#### 138. Echo - 停止服务器

```bash
curl -s -X POST "http://localhost:8080/api/v1/netty/echo/stop"
```

## 自定义协议（粘包拆包）

> 演示 Netty 解决 TCP 粘包/拆包问题。定义 `4字节长度 + JSON Body` 的协议，使用 `LengthFieldBasedFrameDecoder` 自动解码。适合面试/实战高频考点。

#### 139. Protocol - 启动服务器

```bash
curl -s -X POST "http://localhost:8080/api/v1/netty/protocol/start"
```

#### 140. Protocol - 发送编码消息

```bash
# 发送 greeting 消息
curl -s -X POST "http://localhost:8080/api/v1/netty/protocol/send?type=greeting&content=Hello%20Netty%20Protocol"

# 发送 order 消息
curl -s -X POST "http://localhost:8080/api/v1/netty/protocol/send?type=order&content=ORDER20260530001"
```

**说明**: 消息经过 `CustomProtocolEncoder`（写入4字节长度 + JSON字节）→ TCP 发送 → 服务端 `LengthFieldBasedFrameDecoder` 解码 → `CustomProtocolDecoder` 反序列化为 Java 对象。

#### 141. Protocol - 停止服务器

```bash
curl -s -X POST "http://localhost:8080/api/v1/netty/protocol/stop"
```

## HTTP 服务器

> 演示 Netty 作为 HTTP 容器的能力。使用 `HttpServerCodec` + `HttpObjectAggregator` 处理 HTTP 请求。

#### 142. HTTP - 启动服务器

```bash
curl -s -X POST "http://localhost:8080/api/v1/netty/http/start"
```

#### 143. HTTP - 直接访问 Netty HTTP 服务器

```bash
# 启动后直接用 curl 访问 9001 端口
curl -s -X GET "http://localhost:9001/api/hello"
curl -s -X POST "http://localhost:9001/api/data" -H "Content-Type: application/json" -d '{"key":"value"}'
```

**响应示例**:
```json
{"method":"GET","uri":"/api/hello","body":"","from":"netty-http"}
```

#### 144. HTTP - 停止服务器

```bash
curl -s -X POST "http://localhost:8080/api/v1/netty/http/stop"
```

## WebSocket 全双工通信

> 演示 WebSocket 握手、全双工通信和广播推送。使用 `WebSocketServerProtocolHandler` 处理 WebSocket 升级握手。

#### 145. WebSocket - 启动服务器

```bash
curl -s -X POST "http://localhost:8080/api/v1/netty/ws/start"
```

#### 146. WebSocket - 客户端连接测试

```bash
# 安装 wscat（如未安装）
# npm install -g wscat

# 连接 WebSocket 服务器（新开终端执行）
wscat -c ws://127.0.0.1:9002/ws

# 连接后发送消息
# > hello
# 服务端回复: < SERVER: hello
```

**说明**: WebSocket 路径为 `/ws`，连接后服务端自动回复，也可通过 REST 接口广播消息。

#### 147. WebSocket - 广播消息

```bash
# 向所有 WebSocket 客户端广播消息
curl -s -X POST "http://localhost:8080/api/v1/netty/ws/broadcast?message=%E5%A4%A7%E5%AE%B6%E5%A5%BD%EF%BC%8C%E8%BF%99%E6%98%AF%E4%B8%80%E6%9D%A1%E5%B9%BF%E6%92%AD%E6%B6%88%E6%81%AF"
```

#### 148. WebSocket - 查看在线客户端数

```bash
curl -s -X GET "http://localhost:8080/api/v1/netty/ws/clients"
```

#### 149. WebSocket - 停止服务器

```bash
curl -s -X POST "http://localhost:8080/api/v1/netty/ws/stop"
```

## 心跳检测

> 演示 Netty 空闲连接检测和自动清理。使用 `IdleStateHandler` 检测 5 秒无读事件则主动关闭连接，避免僵尸连接占用资源。

#### 150. Heartbeat - 启动心跳服务器

```bash
curl -s -X POST "http://localhost:8080/api/v1/netty/heartbeat/start"
```

#### 151. Heartbeat - 测试空闲超时

```bash
# 用 TCP 连接模拟客户端（新开终端），5 秒不发送数据会被服务端主动关闭
# nc 127.0.0.1 9003
# 观察应用日志: "Heartbeat timeout, closing client: /127.0.0.1:xxxxx"
```

**说明**: 服务端配置 `IdleStateHandler(5, 0, 0)`，5 秒无读事件触发 `IdleStateEvent`，`ChannelDuplexHandler` 捕获后主动关闭连接。可观察到服务端日志输出心跳超时信息。

#### 152. Heartbeat - 停止服务器

```bash
curl -s -X POST "http://localhost:8080/api/v1/netty/heartbeat/stop"
```

---

**文档生成时间**: 2026 年 5 月 25 日
