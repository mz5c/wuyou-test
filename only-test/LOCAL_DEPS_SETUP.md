# 本地中间件部署指南

only-test 模块依赖的中间件：RocketMQ、Seata Server。本文档说明如何快速在本地部署这些服务。

---

## RocketMQ 5.3.2

> RocketMQ 5.x 引入 Proxy 组件（gRPC 协议）、POP 消费模式等新特性，同时**完全兼容 4.x 客户端**（Remoting 协议）。本地开发若不使用 gRPC 客户端，无需部署 Proxy。

### 方案一：Docker Compose（推荐）

> **关键**：Broker 在容器内向 NameServer 注册的默认地址是 Docker 内部 IP（如 `172.x.x.x`），宿主机应用无法直接访问。必须通过 `brokerIP1` 指定为宿主机可达的 IP。

首先创建 `broker.conf`，与 `docker-compose.yml` 放在同一目录：

```bash
# broker.conf
brokerIP1 = 127.0.0.1
autoCreateTopicEnable = true
```

然后创建 `docker-compose.yml`：

```yaml
version: '3.8'

services:
  rocketmq-namesrv:
    image: apache/rocketmq:5.3.2
    container_name: rocketmq-namesrv
    ports:
      - "9876:9876"
    environment:
      JAVA_OPT_EXT: "-server -Xms256m -Xmx256m -Xmn128m"
    command: sh mqnamesrv
    volumes:
      - ./rocketmq/namesrv/logs:/home/rocketmq/logs

  rocketmq-broker:
    image: apache/rocketmq:5.3.2
    container_name: rocketmq-broker
    depends_on:
      - rocketmq-namesrv
    ports:
      - "10909:10909"
      - "10911:10911"
      - "10912:10912"
    environment:
      JAVA_OPT_EXT: "-server -Xms256m -Xmx256m -Xmn128m"
    command: sh mqbroker -n rocketmq-namesrv:9876 -c /home/rocketmq/conf/broker.conf
    volumes:
      - ./rocketmq/broker/logs:/home/rocketmq/logs
      - ./rocketmq/broker/store:/home/rocketmq/store
      - ./broker.conf:/home/rocketmq/conf/broker.conf

  rocketmq-dashboard:
    image: apacherocketmq/rocketmq-dashboard:2.1.0
    container_name: rocketmq-dashboard
    depends_on:
      - rocketmq-namesrv
    ports:
      - "8082:8082"
    environment:
      JAVA_OPTS: "-Drocketmq.namesrv.addr=rocketmq-namesrv:9876 -Dcom.rocketmq.sendMessageWithVIPChannel=false"
```

> **注意**：Dashboard 在容器内访问 Broker 时，NameServer 返回 `127.0.0.1`（即 Dashboard 容器自身），因此 Dashboard 的 Producer/Consumer 连接查询会报错（如 `producer group not exist`），可忽略，不影响宿主机应用的消息收发。

启动与停止：

```bash
# 启动
docker compose up -d

# 查看日志
docker compose logs -f rocketmq-broker

# 停止
docker compose down

# 停止并清除数据
docker compose down -v
```

> **关于 Dashboard 报错**：Dashboard 中查询 Producer/Consumer 连接信息时报 `producer group not exist` 可忽略，不影响实际消息收发。Dashboard 主要用于查看 Topic、消息轨迹等，这些功能正常。

### 方案二：手动部署

#### 1. 下载

```bash
wget https://archive.apache.org/dist/rocketmq/5.3.2/rocketmq-all-5.3.2-bin-release.zip
unzip rocketmq-all-5.3.2-bin-release.zip
cd rocketmq-all-5.3.2-bin-release
```

> 注意：RocketMQ 默认启动需要 8G+ 内存，本地开发建议调小 JVM 参数：
```bash
# 编辑 bin/runserver.sh，修改 JAVA_OPT 中的 -Xms -Xmx -Xmn 为 256m
# 编辑 bin/runbroker.sh，修改 JAVA_OPT 中的 -Xms -Xmx -Xmn 为 256m
```

#### 2. 启动 NameServer

```bash
nohup sh bin/mqnamesrv &
```

默认端口 **9876**。查看日志：

```bash
tail -f ~/logs/rocketmqlogs/namesrv.log
```

#### 3. 启动 Broker

```bash
# 允许自动创建 Topic
echo "autoCreateTopicEnable=true" >> conf/broker.conf

nohup sh bin/mqbroker -n 127.0.0.1:9876 -c conf/broker.conf &
```

查看日志：

```bash
tail -f ~/logs/rocketmqlogs/broker.log
```

#### 4. 验证

```bash
# 查看集群状态
sh bin/mqadmin clusterList -n 127.0.0.1:9876
```

#### 5. 停止

```bash
sh bin/mqshutdown broker
sh bin/mqshutdown namesrv
```

#### 6. Web 管理页面（rocketmq-dashboard）

```bash
docker run -d \
  --name rocketmq-dashboard \
  --network host \
  -e "rocketmq.config.namesrvAddr=127.0.0.1:9876" \
  apacherocketmq/rocketmq-dashboard:2.1.0
```

访问：http://localhost:8082

#### 7. 应用配置

```yaml
rocketmq:
  name-server: 127.0.0.1:9876
  producer:
    group: demo-producer-group
    send-message-timeout: 3000

mq:
  consumer:
    enabled: true   # 启用消费者（需先启动 RocketMQ）
```

---

## Seata Server 1.6.1

### 1. 下载

```bash
wget https://github.com/seata/seata/releases/download/v1.6.1/seata-server-1.6.1.tar.gz
tar -xzf seata-server-1.6.1.tar.gz
cd seata
```

### 2. 修改配置

Seata Server 默认使用 file 注册中心和配置中心，无需额外配置即可启动。

如果需要使用数据库存储事务日志，编辑 `conf/application.yml`，配置数据库连接。

### 3. 启动

```bash
# 默认端口 8091
bash bin/seata-server.sh
```

查看日志：

```bash
tail -f logs/start.out
```

### 4. 停止

```bash
# Ctrl+C 或 kill 进程
```

### 5. 应用配置

```yaml
seata:
  enabled: true
  application-id: only-test
  tx-service-group: default_tx_group
  data-source-proxy-mode: AT
  service:
    vgroup-mapping:
      default_tx_group: default
    grouplist:
      default: 127.0.0.1:8091
  registry:
    type: file
```

### 6. Web 管理页面

访问 http://localhost:7091

默认登录：`seata` / `seata`

---

## 快速启动检查清单

### 启动顺序

1. MySQL → 2. Redis → 3. RocketMQ（NameServer → Broker） → 4. Seata Server → 5. 应用

### RocketMQ

| 组件 | 端口 | 验证方式 |
|------|------|---------|
| NameServer | 9876 | `lsof -i :9876` |
| Broker | 10911 | `lsof -i :10911` |
| Dashboard | 8082 | http://localhost:8082 |

### Seata

| 组件 | 端口 | 验证方式 |
|------|------|---------|
| Server | 8091 | `lsof -i :8091` |
| Console | 7091 | http://localhost:7091 |

### 启用消费端

确认 RocketMQ 已启动后，修改 `application.yml`：

```yaml
mq:
  consumer:
    enabled: true   # false = 禁用消费者，应用启动不依赖 RocketMQ
```
