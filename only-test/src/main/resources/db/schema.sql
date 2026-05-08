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
    INDEX idx_username (username),
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
