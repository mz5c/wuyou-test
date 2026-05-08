# wuyou-test

基于 Spring Boot 2.7 + Java 11 的多模块测试与工具项目。

## 技术栈

| 项目 | 选型 |
|------|------|
| 框架 | Spring Boot 2.7.18 |
| Java | 11 |
| 构建 | Maven 多模块 |
| ORM | MyBatis-Plus 3.5.3 |
| 数据库 | MySQL 8.0 |
| 缓存 | Redis 7.x + Redisson |
| LLM 客户端 | RestTemplate (OpenAI 兼容接口) |

## 模块说明

### wuyou-common
公共基础模块，提供统一返回结果、业务异常、全局异常处理、JSON 工具、分页结果封装等。

### only-test
集成测试沙箱，包含 12 类 Demo 示例，覆盖 Spring Boot 常用组件。

| # | 类别 | 说明 |
|---|------|------|
| 1 | CRUD 基础 | 用户表单表增删改查 |
| 2 | 分页查询 | MyBatis-Plus Page + PageResult |
| 3 | 多表关联 | 订单主表 + 明细表 LEFT JOIN |
| 4 | 事务 | @Transactional 支付回滚 |
| 5 | Redis 缓存 | @Cacheable + 旁路缓存 |
| 6 | 分布式锁 | Redisson 库存扣减 |
| 7 | 异步任务 | @Async + 自定义线程池 |
| 8 | 参数校验 | @Valid + 分组校验 |
| 9 | 逻辑删除 | MyBatis-Plus @TableLogic |
| 10 | 乐观锁 | @Version 并发控制 |
| 11 | 数据脱敏 | Jackson 序列化配置 |
| 12 | 文件上传 | MultipartFile + 类型/大小校验 |

### llm-utils
LLM API Spring Boot Starter，封装 OpenAI 兼容接口的同步和流式调用。

**配置：**
```yaml
llm:
  api:
    base-url: https://api.openai.com
    api-key: ${LLM_API_KEY}
    model: gpt-4o
```

**使用：**
```java
@Autowired
private LlmClient llmClient;

// 同步调用
ChatResponse resp = llmClient.chat(ChatRequest.builder()
    .messages(List.of(new ChatMessage("user", "Hello")))
    .build());

// 流式调用
llmClient.chatStream(request, new StreamCallback() {
    @Override public void onToken(String token) { System.out.print(token); }
    @Override public void onComplete(ChatResponse r) { System.out.println("[Done]"); }
    @Override public void onError(Throwable e) { log.error("LLM error", e); }
});
```

## 快速开始

### 前置条件
- JDK 11+
- MySQL 8.0+
- Redis 7.x+

### 1. 初始化数据库
```sql
mysql -u root -p < only-test/src/main/resources/db/schema.sql
mysql -u root -p < only-test/src/main/resources/db/data.sql
```

### 2. 修改配置
编辑 `only-test/src/main/resources/application.yml`，修改数据库和 Redis 连接信息。

### 3. 启动
```bash
mvn spring-boot:run -pl only-test
```

### 4. 构建
```bash
mvn clean install -DskipTests    # 打包所有模块
mvn clean verify                 # 完整构建（含测试）
```

## 项目结构
```
wuyou-test/
├── pom.xml
├── wuyou-common/
│   └── src/main/java/com/wuyou/common/
│       ├── result/       # Result, ResultCode
│       ├── exception/    # BizException, GlobalExceptionHandler
│       ├── constant/     # CommonConstant
│       ├── util/         # JsonUtils, SpringContextHolder
│       └── page/         # PageResult
├── only-test/
│   └── src/main/java/com/wuyou/onlytest/
│       ├── config/       # MyBatis-Plus, Redis, Async 配置
│       ├── controller/   # REST 接口
│       ├── service/      # 业务逻辑
│       ├── mapper/       # MyBatis-Plus Mapper
│       ├── entity/       # 数据实体
│       └── dto/          # 传输对象
└── llm-utils/
    └── src/main/java/com/wuyou/llmutils/
        ├── model/        # ChatMessage, ChatRequest, ChatResponse
        ├── client/       # LlmClient
        ├── streaming/    # StreamCallback
        └── properties/   # LlmProperties
```
