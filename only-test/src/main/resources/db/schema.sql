-- ============================================================
-- wuyou-test 数据库建表脚本
-- 数据库：wuyou_test (utf8mb4)
-- 引擎：InnoDB
-- ============================================================

CREATE DATABASE IF NOT EXISTS wuyou_test
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE wuyou_test;

-- -----------------------------------------------------------
-- 用户表
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS demo_user (
    id          BIGINT       AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    username    VARCHAR(50)  NOT NULL                        COMMENT '用户名',
    nickname    VARCHAR(50)  DEFAULT NULL                    COMMENT '昵称',
    phone       VARCHAR(20)  DEFAULT NULL                    COMMENT '手机号',
    email       VARCHAR(100) DEFAULT NULL                    COMMENT '邮箱',
    deleted     TINYINT      DEFAULT 0                       COMMENT '逻辑删除 0-未删 1-已删',
    version     INT          DEFAULT 0                       COMMENT '乐观锁版本号',
    create_time datetime     DEFAULT CURRENT_TIMESTAMP       COMMENT '创建时间',
    update_time datetime     DEFAULT CURRENT_TIMESTAMP
                                  ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_username (username),
    INDEX idx_deleted  (deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- -----------------------------------------------------------
-- 产品表
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS demo_product (
    id          BIGINT        AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    name        VARCHAR(100)  NOT NULL                   COMMENT '产品名称',
    price       DECIMAL(10,2) NOT NULL                   COMMENT '价格',
    stock       INT           NOT NULL DEFAULT 0         COMMENT '库存',
    version     INT           DEFAULT 0                  COMMENT '乐观锁版本号',
    create_time datetime      DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
    update_time datetime      DEFAULT CURRENT_TIMESTAMP
                                  ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='产品表';

-- -----------------------------------------------------------
-- 订单表
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS demo_order (
    id           BIGINT        AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    order_no     VARCHAR(50)   NOT NULL                   COMMENT '订单号',
    user_id      BIGINT        NOT NULL                   COMMENT '用户ID',
    total_amount DECIMAL(10,2) NOT NULL                   COMMENT '总金额',
    status       TINYINT       DEFAULT 0                  COMMENT '状态 0-待支付 1-已支付 2-已取消',
    deleted      TINYINT       DEFAULT 0                  COMMENT '逻辑删除',
    create_time  datetime      DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
    update_time  datetime      DEFAULT CURRENT_TIMESTAMP
                                   ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_order_no (order_no),
    INDEX idx_user_id (user_id),
    INDEX idx_status  (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- -----------------------------------------------------------
-- 订单明细表
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS demo_order_item (
    id            BIGINT        AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    order_id      BIGINT        NOT NULL                   COMMENT '订单ID',
    product_name  VARCHAR(100)  NOT NULL                   COMMENT '产品名称',
    quantity      INT           NOT NULL                   COMMENT '数量',
    price         DECIMAL(10,2) NOT NULL                   COMMENT '单价',
    create_time   datetime      DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
    INDEX idx_order_id (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细表';

-- -----------------------------------------------------------
-- 幂等记录表
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS demo_idempotent_record (
    id         BIGINT       AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    biz_type   VARCHAR(50)  NOT NULL                      COMMENT '业务类型',
    biz_id     VARCHAR(100) NOT NULL                      COMMENT '业务唯一ID',
    status     TINYINT      NOT NULL DEFAULT 0            COMMENT '0-处理中 1-已完成',
    result     TEXT                                        COMMENT '处理结果',
    create_time datetime     DEFAULT CURRENT_TIMESTAMP    COMMENT '创建时间',
    UNIQUE KEY uk_biz (biz_type, biz_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='幂等记录表';

-- -----------------------------------------------------------
-- Seata 账户表
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS seata_account (
    id       BIGINT        AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    user_id  BIGINT        NOT NULL                      COMMENT '用户ID',
    balance  DECIMAL(10,2) NOT NULL DEFAULT 0            COMMENT '余额',
    frozen   DECIMAL(10,2) NOT NULL DEFAULT 0            COMMENT '冻结金额(TCC)',
    UNIQUE KEY uk_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Seata 账户表';

-- TCC 事务记录表（用于 TCC 二阶段幂等控制）
CREATE TABLE IF NOT EXISTS tcc_record (
    xid   VARCHAR(64) NOT NULL COMMENT '全局事务ID',
    phase VARCHAR(16) NOT NULL COMMENT 'COMMITTED / ROLLBACKED',
    PRIMARY KEY (xid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='TCC 事务记录表';

-- Seata AT 模式需要 undo_log 表
CREATE TABLE IF NOT EXISTS undo_log (
    id            BIGINT       AUTO_INCREMENT PRIMARY KEY,
    branch_id     BIGINT       NOT NULL,
    xid           VARCHAR(100) NOT NULL,
    context       VARCHAR(128) NOT NULL,
    rollback_info LONGBLOB     NOT NULL,
    log_status    INT          NOT NULL,
    log_created   DATETIME     NOT NULL,
    log_modified  DATETIME     NOT NULL,
    UNIQUE KEY uk_undo_log (xid, branch_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AT transaction undo log';

-- -----------------------------------------------------------
-- Seata Saga 状态机存储表（Seata 1.6.1 标准 DDL）
-- -----------------------------------------------------------
CREATE TABLE IF NOT EXISTS seata_state_machine_def (
    id               VARCHAR(32)   NOT NULL PRIMARY KEY,
    name             VARCHAR(128)  NOT NULL,
    tenant_id        VARCHAR(32)   NOT NULL DEFAULT '',
    app_name         VARCHAR(128)  NOT NULL,
    status           VARCHAR(32)   DEFAULT NULL,
    gmt_create       DATETIME(3)   DEFAULT NULL,
    ver              VARCHAR(32)   DEFAULT NULL,
    type             VARCHAR(32)   DEFAULT NULL,
    content          TEXT,
    recover_strategy VARCHAR(32)   DEFAULT NULL,
    comment_         VARCHAR(255)  DEFAULT NULL,
    gmt_modified     DATETIME(3)   DEFAULT NULL,
    create_time      DATETIME      DEFAULT CURRENT_TIMESTAMP,
    update_time      DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_name_tenant (name, tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Saga state machine definition';

CREATE TABLE IF NOT EXISTS seata_state_machine_inst (
    id                  VARCHAR(128)  NOT NULL,
    machine_id          VARCHAR(32)   NOT NULL,
    tenant_id           VARCHAR(32)   NOT NULL DEFAULT '',
    parent_id           VARCHAR(128)  DEFAULT NULL,
    gmt_started         DATETIME(3)   DEFAULT NULL,
    gmt_end             DATETIME(3)   DEFAULT NULL,
    business_key        VARCHAR(128)  DEFAULT NULL,
    start_params        TEXT          DEFAULT NULL,
    end_params          TEXT          DEFAULT NULL,
    is_running          TINYINT(1)    DEFAULT NULL,
    status              VARCHAR(32)   DEFAULT NULL,
    gmt_updated         DATETIME(3)   DEFAULT NULL,
    excep               TEXT          DEFAULT NULL,
    compensation_status VARCHAR(32)   DEFAULT NULL,
    create_time         DATETIME      DEFAULT CURRENT_TIMESTAMP,
    update_time         DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_machine_id (machine_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Saga state machine instance';

CREATE TABLE IF NOT EXISTS seata_state_inst (
    id                       VARCHAR(128)  NOT NULL,
    machine_inst_id          VARCHAR(128)  NOT NULL,
    name                     VARCHAR(128)  DEFAULT NULL,
    type                     VARCHAR(32)   DEFAULT NULL,
    gmt_started              DATETIME(3)   DEFAULT NULL,
    gmt_end                  DATETIME(3)   DEFAULT NULL,
    service_name             VARCHAR(128)  DEFAULT NULL,
    service_method           VARCHAR(128)  DEFAULT NULL,
    service_type             VARCHAR(32)   DEFAULT NULL,
    is_for_update            TINYINT(1)    DEFAULT NULL,
    input_params             TEXT          DEFAULT NULL,
    status                   VARCHAR(32)   DEFAULT NULL,
    output_params            TEXT          DEFAULT NULL,
    business_key             VARCHAR(128)  DEFAULT NULL,
    state_id_compensated_for VARCHAR(128)  DEFAULT NULL,
    state_id_retried_for     VARCHAR(128)  DEFAULT NULL,
    gmt_updated              DATETIME(3)   DEFAULT NULL,
    excep                    TEXT          DEFAULT NULL,
    create_time              DATETIME      DEFAULT CURRENT_TIMESTAMP,
    update_time              DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_machine_inst_id (machine_inst_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Saga state instance';
