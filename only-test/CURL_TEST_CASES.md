# cURL 测试用例文档

本文档汇总了 `only-test` 模块中所有 Web 接口的 cURL 测试用例。

## 基础信息

- **基础 URL**: `http://localhost:8080`
- **API 版本**: `v1`
- **内容类型**: `application/json`

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
  "data": {
    "records": [...],
    "total": 100,
    "size": 10,
    "current": 1
  },
  "msg": "success"
}
```

---

### 2. 根据 ID 查询用户

```bash
curl -X GET "http://localhost:8080/api/v1/users/1"
```

**响应示例**:
```json
{
  "code": 200,
  "data": {
    "id": 1,
    "username": "admin",
    "nickname": "管理员",
    "phone": "13800138000",
    "email": "admin@example.com",
    "deleted": 0
  },
  "msg": "success"
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
- `username`: 必填，不能为空
- `phone`: 可选，格式必须为 11 位手机号
- `email`: 可选，格式必须为有效邮箱

---

### 4. 更新用户

```bash
curl -X PUT "http://localhost:8080/api/v1/users/1" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin_updated",
    "nickname": "管理员更新",
    "phone": "13800138099",
    "email": "admin_new@example.com"
  }'
```

---

### 5. 删除用户（逻辑删除）

```bash
curl -X DELETE "http://localhost:8080/api/v1/users/1"
```

**说明**: 此接口执行逻辑删除，将用户的 `deleted` 字段设置为 1

---

### 6. 查询已删除的用户列表

```bash
curl -X GET "http://localhost:8080/api/v1/users/deleted-list"
```

**说明**: 查询包含已逻辑删除的记录

---

### 7. 恢复已删除的用户

```bash
curl -X POST "http://localhost:8080/api/v1/users/1/restore"
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
  "data": {
    "id": 1,
    "name": "测试商品",
    "price": 99.99,
    "stock": 100,
    "version": 1
  },
  "msg": "success"
}
```

---

### 9. 查询商品（不读取缓存）

```bash
curl -X GET "http://localhost:8080/api/v1/products/1/nocache"
```

**说明**: 强制从数据库查询，不读取 Redis 缓存

---

### 10. 扣减商品库存

```bash
curl -X POST "http://localhost:8080/api/v1/products/1/deduct?quantity=10"
```

**说明**: 扣减指定数量的库存

---

### 11. 乐观锁扣减库存

```bash
curl -X POST "http://localhost:8080/api/v1/products/1/deduct-optimistic?quantity=10"
```

**说明**: 使用 `@Version` 乐观锁机制扣减库存，冲突时需要重试

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
  "data": {
    "id": 1,
    "userId": 1,
    "orderNo": "ORDER202401010001",
    "totalAmount": 199.98,
    "status": 0,
    "orderItems": [
      {
        "id": 1,
        "orderId": 1,
        "productId": 1,
        "quantity": 2,
        "price": 99.99
      }
    ]
  },
  "msg": "success"
}
```

---

### 13. 创建订单

```bash
curl -X POST "http://localhost:8080/api/v1/orders?userId=1" \
  -H "Content-Type: application/json" \
  -d '[
    {
      "productId": 1,
      "quantity": 2,
      "price": 99.99
    },
    {
      "productId": 2,
      "quantity": 1,
      "price": 49.99
    }
  ]'
```

**说明**: 创建包含多个商品明细的订单

---

### 14. 支付订单

```bash
curl -X POST "http://localhost:8080/api/v1/orders/1/pay"
```

**说明**: 将订单状态更新为已支付

---

## 异步任务接口 (Async API)

### 15. 发送异步通知

```bash
curl -X POST "http://localhost:8080/api/v1/async/notification?userId=1&message=您的订单已发货"
```

**说明**: 异步发送用户通知，不会阻塞主线程

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
  "data": "20240101/test.txt",
  "msg": "success"
}
```

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
  "data": null,
  "msg": "success"
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
  "code": 500,
  "data": null,
  "msg": "username cannot be blank"
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
  "code": 500,
  "data": null,
  "msg": "invalid phone number"
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
  "code": 500,
  "data": null,
  "msg": "invalid email"
}
```

---

## 测试脚本

为了方便测试，可以创建一个 shell 脚本：

```bash
#!/bin/bash

# base URL
BASE_URL="http://localhost:8080"

echo "=== 测试用户接口 ==="
echo "1. 查询用户列表"
curl -s "$BASE_URL/api/v1/users?page=1&size=10" | jq .

echo -e "\n2. 查询用户 ID=1"
curl -s "$BASE_URL/api/v1/users/1" | jq .

echo -e "\n3. 创建用户"
curl -s -X POST "$BASE_URL/api/v1/users" \
  -H "Content-Type: application/json" \
  -d '{"username":"test_user","phone":"13800138000","email":"test@example.com"}' | jq .

echo -e "\n=== 测试商品接口 ==="
echo "4. 查询商品 ID=1"
curl -s "$BASE_URL/api/v1/products/1" | jq .

echo -e "\n5. 扣减库存"
curl -s -X POST "$BASE_URL/api/v1/products/1/deduct?quantity=5" | jq .

echo -e "\n=== 测试订单接口 ==="
echo "6. 查询订单 ID=1"
curl -s "$BASE_URL/api/v1/orders/1/with-items" | jq .

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
3. **Redis**: 如果使用了缓存功能，确保 Redis 服务已启动
4. **JSON 格式化**: 示例中使用 `jq` 工具美化 JSON 输出，如未安装可使用 `brew install jq` 安装
5. **文件上传路径**: 上传文件的路径需要根据实际情况调整

---

## 常见错误响应

| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 404 | 资源未找到 |
| 500 | 业务异常或验证失败 |

---

**文档生成时间**: 2026 年 5 月 9 日
