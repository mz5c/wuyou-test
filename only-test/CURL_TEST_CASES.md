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
curl -X POST "http://localhost:8080/api/v1/datasource/user?username=fromMaster&nickname=主库写入"
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

**文档生成时间**: 2026 年 5 月 11 日
