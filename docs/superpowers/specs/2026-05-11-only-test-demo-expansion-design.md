# only-test 沙箱测试 Demo 扩展设计

## 概述

在 `only-test` 模块中新增 9 个方向的测试 Demo，基于方案 C（单一模块按能力分包），用于功能验证与学习测试。

## 组织方式

```
only-test/src/main/java/com/wuyou/onlytest/
├── config/                          # 已有配置
├── controller/demo/                 # 已有 Demo
├── service/demo/                    # 已有关联
├── seata/                           # Seata 分布式事务
│   ├── controller/SeataAtController.java
│   ├── controller/SeataTccController.java
│   ├── controller/SeataSagaController.java
│   ├── controller/SeataXaController.java
│   ├── service/SeataAtService.java
│   ├── service/SeataTccService.java
│   ├── service/SeataSagaService.java
│   ├── service/SeataXaService.java
│   └── entity/Account.java
├── mq/                              # RocketMQ 消息
│   ├── controller/RocketMqController.java
│   ├── service/RocketMqProducerService.java
│   └── consumer/
│       ├── OrderStatusConsumer.java
│       └── TransactionConsumer.java
├── idempotent/                      # 幂等设计
│   ├── annotation/Idempotent.java
│   ├── aspect/IdempotentAspect.java
│   ├── controller/IdempotentController.java
│   └── service/
│       ├── IdempotentRedisService.java
│       └── IdempotentDbService.java
├── datasource/                      # 多数据源
│   ├── controller/DatasourceController.java
│   └── service/DatasourceService.java
├── idgen/                           # 分布式 ID
│   ├── controller/IdGeneratorController.java
│   └── service/
│       ├── SnowflakeService.java
│       └── RedisIdGenService.java
├── ratelimit/                       # 限流
│   ├── annotation/LocalRateLimit.java
│   ├── annotation/DistributedRateLimit.java
│   ├── aspect/RateLimitAspect.java
│   ├── controller/RateLimitController.java
│   └── service/RateLimitService.java
├── retry/                           # Spring Retry
│   ├── controller/RetryController.java
│   └── service/RetryDemoService.java
├── lock/                            # Redisson 锁扩展
│   ├── controller/LockController.java
│   └── service/LockDemoService.java
└── async/                           # CompletableFuture
    ├── controller/CompletableFutureController.java
    └── service/CompletableFutureService.java
```

## 依赖变更

`only-test/pom.xml` 新增依赖：

| 依赖 | 用途 |
|------|------|
| `com.alibaba.cloud:spring-cloud-starter-alibaba-seata` | Seata 客户端 |
| `org.apache.rocketmq:rocketmq-spring-boot-starter` | RocketMQ |
| `com.baomidou:dynamic-datasource-spring-boot-starter` | 多数据源 |
| `com.google.guava:guava` | RateLimiter |
| `org.springframework.retry:spring-retry` | Spring Retry |
| `org.aspectj:aspectjweaver` | AOP（已有，确认 scope） |

## 各模块详细设计

### 1. Seata 分布式事务

**新增表 `seata_account`：**
```sql
CREATE TABLE seata_account (
    id       BIGINT       AUTO_INCREMENT PRIMARY KEY,
    user_id  BIGINT       NOT NULL,
    balance  DECIMAL(10,2) NOT NULL DEFAULT 0,
    frozen   DECIMAL(10,2) NOT NULL DEFAULT 0,
    UNIQUE KEY uk_user_id (user_id)
);
```

| 模式 | Service 核心逻辑 | 说明 |
|------|-----------------|------|
| **AT** | `@GlobalTransactional` 包裹下单扣库存 | 复用 `demo_order`、`demo_product`，默认模式 |
| **TCC** | Try（冻结）→ Confirm（扣减）→ Cancel（回滚） | 实现 Seata `@TwoPhaseBusinessAction` 接口 |
| **Saga** | 创建订单 → 扣库存 → 扣余额，每步有补偿 | 使用 Seata Saga 注解 `@SagaStart` + 补偿方法 |
| **XA** | 同 AT 场景，数据源改为 XA 模式 | 配置 xa 数据源连接池 |

**API：**
- `POST /api/v1/seata/at/order` — AT 模式创建订单
- `POST /api/v1/seata/tcc/transfer` — TCC 账户转账
- `POST /api/v1/seata/saga/order` — Saga 订单全流程
- `POST /api/v1/seata/xa/order` — XA 模式创建订单

### 2. RocketMQ 消息

**配置：** `rocketmq.name-server=localhost:9876`

**Topic：** `demo-order`、`demo-stock`、`demo-notification`

**API：**
- `POST /api/v1/mq/send` — 同步/异步/单向三种方式
- `POST /api/v1/mq/send-orderly?orderId=X` — 顺序消息
- `POST /api/v1/mq/send-transaction` — 事务消息

**消费者：**
- `OrderStatusConsumer` — 顺序消费，`MessageModel.CLUSTERING`
- `TransactionConsumer` — 消费事务消息

### 3. 幂等设计（注解 + AOP）

**注解：**
```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Idempotent {
    String key();                     // SpEL
    String bizType();                 // 业务类型
    int ttlSeconds() default 86400;   // key 过期时间
}
```

**新增表 `demo_idempotent_record`：**
```sql
CREATE TABLE demo_idempotent_record (
    id         BIGINT       AUTO_INCREMENT PRIMARY KEY,
    biz_type   VARCHAR(50)  NOT NULL,
    biz_id     VARCHAR(100) NOT NULL,
    status     TINYINT      NOT NULL DEFAULT 0 COMMENT '0-处理中 1-已完成',
    result     TEXT,
    create_time datetime    DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_biz (biz_type, biz_id)
);
```

**AOP 流程：** Redis SET NX → 执行 → 标记完成/异常回滚
**边界处理：** 重复请求提示、完成请求返回缓存结果、业务异常释放 key

**API：**
- `POST /api/v1/idempotent/pay?orderId=X&userId=Y` — Redis 幂等
- `POST /api/v1/idempotent/pay-db?orderId=X&userId=Y` — DB 幂等

### 4. 多数据源

**配置两个数据源：**
- `master` — 写库 `jdbc:mysql://localhost:3306/wuyou_test`
- `slave` — 读库（指向同一个或不同的实例）

**API：**
- `GET /api/v1/datasource/users` — 从库查询
- `POST /api/v1/datasource/user` — 主库写入
- `GET /api/v1/datasource/users/{id}/fresh` — 强制走主库

### 5. 分布式 ID

**技术选型：** Hutool Snowflake、Redis INCR

**API：**
- `GET /api/v1/idgen/snowflake` — 雪花 ID
- `GET /api/v1/idgen/redis/{bizType}` — Redis 自增
- `GET /api/v1/idgen/compare?count=1000` — 性能对比

### 6. 限流

**两种实现：** Guava RateLimiter（本地）、Redis Lua 滑动窗口（分布式）

**注解：** `@LocalRateLimit`、`@DistributedRateLimit` + AOP

**API：**
- `GET /api/v1/ratelimit/local` — 本地限流
- `GET /api/v1/ratelimit/distributed` — 分布式限流
- `GET /api/v1/ratelimit/stress` — 压力测试

### 7. Spring Retry

**注解：** `@Retryable` + `@Recover`

**API：**
- `POST /api/v1/retry/unstable` — 重试 3 次，间隔递增
- `POST /api/v1/retry/fallback` — 重试耗尽后回退

### 8. Redisson 锁扩展

**类型：** RReadWriteLock、RSemaphore

**API：**
- `POST /api/v1/locks/product/{id}/read` — 读锁
- `POST /api/v1/locks/product/{id}/write` — 写锁
- `POST /api/v1/locks/semaphore/acquire` — 获取信号量
- `POST /api/v1/locks/semaphore/release` — 释放信号量

### 9. CompletableFuture

**场景：** 并行查询、串行编排、竞速、异常兜底

**使用自定义线程池：** 复用 `AsyncConfig` 配置

**API：**
- `GET /api/v1/async/parallel` — 并行查询
- `POST /api/v1/async/pipeline` — 串行编排
- `POST /api/v1/async/race` — 竞速模式
- `GET /api/v1/async/error-handling` — 异常兜底

---

## 实现顺序

建议按依赖关系分 4 个阶段：

| 阶段 | 模块 | 理由 |
|------|------|------|
| Phase 1 | 幂等注解、限流注解、Redisson 锁、CompletableFuture | 纯代码/已有中间件，无新增外部依赖 |
| Phase 2 | 分布式 ID、多数据源、Spring Retry | 需新增依赖，但无额外中间件进程 |
| Phase 3 | RocketMQ 消息 | 需本地启动 RocketMQ |
| Phase 4 | Seata 分布式事务 | 需搭建 Seata Server，复杂度最高 |
