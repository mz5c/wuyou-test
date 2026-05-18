-- MySQL dump 10.13  Distrib 9.6.0, for macos14.8 (arm64)
--
-- Host: 127.0.0.1    Database: wuyou_test_slave
-- ------------------------------------------------------
-- Server version	8.0.45

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Current Database: `wuyou_test_slave`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `wuyou_test_slave` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `wuyou_test_slave`;

--
-- Table structure for table `demo_idempotent_record`
--

DROP TABLE IF EXISTS `demo_idempotent_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `demo_idempotent_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `biz_type` varchar(50) NOT NULL COMMENT '业务类型',
  `biz_id` varchar(100) NOT NULL COMMENT '业务唯一ID',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '0-处理中 1-已完成',
  `result` text COMMENT '处理结果',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_biz` (`biz_type`,`biz_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='幂等记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `demo_idempotent_record`
--

LOCK TABLES `demo_idempotent_record` WRITE;
/*!40000 ALTER TABLE `demo_idempotent_record` DISABLE KEYS */;
/*!40000 ALTER TABLE `demo_idempotent_record` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `demo_order`
--

DROP TABLE IF EXISTS `demo_order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `demo_order` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `order_no` varchar(50) NOT NULL COMMENT '订单号',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `total_amount` decimal(10,2) NOT NULL COMMENT '总金额',
  `status` tinyint DEFAULT '0' COMMENT '状态 0-待支付 1-已支付 2-已取消',
  `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `demo_order`
--

LOCK TABLES `demo_order` WRITE;
/*!40000 ALTER TABLE `demo_order` DISABLE KEYS */;
INSERT INTO `demo_order` VALUES (1,'ORD1778298705779',1,17998.00,0,0,NULL,NULL),(2,'ORD1778299107160',2,10999.00,0,0,NULL,NULL),(3,'ORD1778424505819',2,28997.00,0,0,'2026-05-10 22:49:28','2026-05-10 22:49:28'),(4,'ORD1778424631762',2,28997.00,0,0,'2026-05-10 22:50:40','2026-05-10 22:50:40'),(5,'ORD1778425060377',2,28997.00,0,0,'2026-05-10 22:57:42','2026-05-10 22:57:42'),(6,'ORD1778425097020',2,28997.00,0,0,'2026-05-10 22:58:21','2026-05-10 22:58:21'),(7,'ORD1778425127338',2,28997.00,0,0,'2026-05-10 22:58:49','2026-05-10 22:58:49'),(8,'ORD1778425639360',2,28997.00,0,0,'2026-05-10 23:07:19','2026-05-10 23:07:19'),(9,'ORD1778425640943',2,28997.00,0,0,'2026-05-10 23:07:21','2026-05-10 23:07:21');
/*!40000 ALTER TABLE `demo_order` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `demo_order_item`
--

DROP TABLE IF EXISTS `demo_order_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `demo_order_item` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `product_name` varchar(100) NOT NULL COMMENT '产品名称',
  `quantity` int NOT NULL COMMENT '数量',
  `price` decimal(10,2) NOT NULL COMMENT '单价',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单明细表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `demo_order_item`
--

LOCK TABLES `demo_order_item` WRITE;
/*!40000 ALTER TABLE `demo_order_item` DISABLE KEYS */;
INSERT INTO `demo_order_item` VALUES (1,1,'iPhone 15 Pro',2,8999.00,NULL),(2,2,'MacBook Air M3',1,10999.00,NULL),(3,3,'iPhone 15 Pro',2,8999.00,'2026-05-10 22:49:28'),(4,3,'MacBook Air M3',1,10999.00,'2026-05-10 22:49:28'),(5,4,'iPhone 15 Pro',2,8999.00,'2026-05-10 22:50:40'),(6,4,'MacBook Air M3',1,10999.00,'2026-05-10 22:50:40'),(7,5,'iPhone 15 Pro',2,8999.00,'2026-05-10 22:57:42'),(8,5,'MacBook Air M3',1,10999.00,'2026-05-10 22:57:42'),(9,6,'iPhone 15 Pro',2,8999.00,'2026-05-10 22:58:21'),(10,6,'MacBook Air M3',1,10999.00,'2026-05-10 22:58:21'),(11,7,'iPhone 15 Pro',2,8999.00,'2026-05-10 22:58:48'),(12,7,'MacBook Air M3',1,10999.00,'2026-05-10 22:58:48'),(13,8,'iPhone 15 Pro',2,8999.00,'2026-05-10 23:07:19'),(14,8,'MacBook Air M3',1,10999.00,'2026-05-10 23:07:19'),(15,9,'iPhone 15 Pro',2,8999.00,'2026-05-10 23:07:20'),(16,9,'MacBook Air M3',1,10999.00,'2026-05-10 23:07:20');
/*!40000 ALTER TABLE `demo_order_item` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `demo_product`
--

DROP TABLE IF EXISTS `demo_product`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `demo_product` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(100) NOT NULL COMMENT '产品名称',
  `price` decimal(10,2) NOT NULL COMMENT '价格',
  `stock` int NOT NULL DEFAULT '0' COMMENT '库存',
  `version` int DEFAULT '0' COMMENT '乐观锁版本号',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='产品表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `demo_product`
--

LOCK TABLES `demo_product` WRITE;
/*!40000 ALTER TABLE `demo_product` DISABLE KEYS */;
INSERT INTO `demo_product` VALUES (1,'iPhone 15 Pro',8999.00,9,24,'2026-05-09 10:10:10','2026-05-10 22:22:27'),(2,'MacBook Air M3',10999.00,50,0,'2026-05-09 10:10:10','2026-05-09 10:10:10'),(3,'AirPods Pro 2',1899.00,200,0,'2026-05-09 10:10:10','2026-05-09 10:10:10');
/*!40000 ALTER TABLE `demo_product` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `demo_user`
--

DROP TABLE IF EXISTS `demo_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `demo_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `nickname` varchar(50) DEFAULT NULL COMMENT '昵称',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除 0-未删 1-已删',
  `version` int DEFAULT '0' COMMENT '乐观锁版本号',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_username` (`username`),
  KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `demo_user`
--

LOCK TABLES `demo_user` WRITE;
/*!40000 ALTER TABLE `demo_user` DISABLE KEYS */;
INSERT INTO `demo_user` VALUES (1,'admin_updated','管理员更新','13800138099','admin_new@example.com',0,0,'2026-05-09 10:10:10','2026-05-10 16:47:45'),(2,'ww_v2','wwv2','13800138222','v2333@test.com',0,0,'2026-05-09 10:10:10','2026-05-10 16:47:45'),(3,'lisi','李四','13687654321','lisi@test.com',0,0,'2026-05-09 10:10:10','2026-05-10 16:57:32'),(17,'zhangsan_v2','张三v2','13800138111','v2@test.com',0,0,'2026-05-10 17:25:03','2026-05-10 21:34:23');
/*!40000 ALTER TABLE `demo_user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `seata_account`
--

DROP TABLE IF EXISTS `seata_account`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `seata_account` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `balance` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '余额',
  `frozen` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '冻结金额(TCC)',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Seata 账户表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `seata_account`
--

LOCK TABLES `seata_account` WRITE;
/*!40000 ALTER TABLE `seata_account` DISABLE KEYS */;
/*!40000 ALTER TABLE `seata_account` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `seata_state_inst`
--

DROP TABLE IF EXISTS `seata_state_inst`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `seata_state_inst` (
  `id` varchar(128) NOT NULL,
  `machine_inst_id` varchar(128) NOT NULL,
  `name` varchar(128) DEFAULT NULL,
  `type` varchar(32) DEFAULT NULL,
  `gmt_started` datetime(3) DEFAULT NULL,
  `gmt_end` datetime(3) DEFAULT NULL,
  `service_name` varchar(128) DEFAULT NULL,
  `service_method` varchar(128) DEFAULT NULL,
  `service_type` varchar(32) DEFAULT NULL,
  `is_for_update` tinyint(1) DEFAULT NULL,
  `input_params` text,
  `status` varchar(32) DEFAULT NULL,
  `output_params` text,
  `business_key` varchar(128) DEFAULT NULL,
  `state_id_compensated_for` varchar(128) DEFAULT NULL,
  `state_id_retried_for` varchar(128) DEFAULT NULL,
  `gmt_updated` datetime(3) DEFAULT NULL,
  `excep` text,
  `start_time` datetime DEFAULT NULL,
  `end_time` datetime DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_machine_inst_id` (`machine_inst_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Saga state instance';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `seata_state_inst`
--

LOCK TABLES `seata_state_inst` WRITE;
/*!40000 ALTER TABLE `seata_state_inst` DISABLE KEYS */;
/*!40000 ALTER TABLE `seata_state_inst` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `seata_state_machine_def`
--

DROP TABLE IF EXISTS `seata_state_machine_def`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `seata_state_machine_def` (
  `id` varchar(32) NOT NULL,
  `name` varchar(128) NOT NULL,
  `tenant_id` varchar(32) NOT NULL DEFAULT '',
  `app_name` varchar(128) NOT NULL DEFAULT 'only-test',
  `status` varchar(32) DEFAULT NULL,
  `gmt_create` datetime(3) DEFAULT NULL,
  `ver` varchar(32) DEFAULT NULL,
  `type` varchar(32) DEFAULT NULL,
  `recover_strategy` varchar(32) DEFAULT NULL,
  `comment_` varchar(255) DEFAULT NULL,
  `gmt_modified` datetime(3) DEFAULT NULL,
  `content` text,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name_tenant` (`name`,`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Saga state machine definition';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `seata_state_machine_def`
--

LOCK TABLES `seata_state_machine_def` WRITE;
/*!40000 ALTER TABLE `seata_state_machine_def` DISABLE KEYS */;
INSERT INTO `seata_state_machine_def` VALUES ('cdb72d7abfd485bdab1f9a04769c3589','order-fulfillment-saga','','SEATA','AC','2026-05-11 14:06:16.255','0.0.1','STATE_LANG',NULL,'Order fulfillment saga demo',NULL,'{\n  \"Name\": \"order-fulfillment-saga\",\n  \"Comment\": \"Order fulfillment saga demo\",\n  \"StartState\": \"CreateOrder\",\n  \"Version\": \"0.0.1\",\n  \"Timeout\": 60000,\n  \"States\": {\n    \"CreateOrder\": {\n      \"Type\": \"ServiceTask\",\n      \"ServiceName\": \"sagaActionService\",\n      \"ServiceMethod\": \"createOrder\",\n      \"CompensateState\": \"CancelOrder\",\n      \"Next\": \"DeductStock\",\n      \"Input\": [\n        \"$initialContext.userId\",\n        \"$initialContext.productId\",\n        \"$initialContext.quantity\"\n      ],\n      \"Output\": {\n        \"orderId\": \"$.#.orderId\",\n        \"out\": \"$.#\"\n      }\n    },\n    \"CancelOrder\": {\n      \"Type\": \"ServiceTask\",\n      \"ServiceName\": \"sagaActionService\",\n      \"ServiceMethod\": \"cancelOrder\",\n      \"Input\": [\n        \"$compensationContext.orderId\"\n      ],\n      \"Output\": {\n        \"out\": \"$.#\"\n      }\n    },\n    \"DeductStock\": {\n      \"Type\": \"ServiceTask\",\n      \"ServiceName\": \"sagaActionService\",\n      \"ServiceMethod\": \"deductStock\",\n      \"CompensateState\": \"AddStock\",\n      \"Next\": \"DeductBalance\",\n      \"Input\": [\n        \"$initialContext.productId\",\n        \"$initialContext.quantity\"\n      ],\n      \"Output\": {\n        \"out\": \"$.#\"\n      }\n    },\n    \"AddStock\": {\n      \"Type\": \"ServiceTask\",\n      \"ServiceName\": \"sagaActionService\",\n      \"ServiceMethod\": \"addStock\",\n      \"Input\": [\n        \"$compensationContext.productId\",\n        \"$compensationContext.quantity\"\n      ],\n      \"Output\": {\n        \"out\": \"$.#\"\n      }\n    },\n    \"DeductBalance\": {\n      \"Type\": \"ServiceTask\",\n      \"ServiceName\": \"sagaActionService\",\n      \"ServiceMethod\": \"deductBalance\",\n      \"CompensateState\": \"AddBalance\",\n      \"Next\": \"Notify\",\n      \"Input\": [\n        \"$initialContext.userId\",\n        \"$initialContext.productId\",\n        \"$initialContext.quantity\"\n      ],\n      \"Output\": {\n        \"out\": \"$.#\"\n      }\n    },\n    \"AddBalance\": {\n      \"Type\": \"ServiceTask\",\n      \"ServiceName\": \"sagaActionService\",\n      \"ServiceMethod\": \"addBalance\",\n      \"Input\": [\n        \"$compensationContext.userId\",\n        \"$compensationContext.productId\",\n        \"$compensationContext.quantity\"\n      ],\n      \"Output\": {\n        \"out\": \"$.#\"\n      }\n    },\n    \"Notify\": {\n      \"Type\": \"ServiceTask\",\n      \"ServiceName\": \"sagaActionService\",\n      \"ServiceMethod\": \"notify\",\n      \"Next\": \"End\",\n      \"Input\": [\n        \"$initialContext.userId\",\n        \"$initialContext.orderId\"\n      ],\n      \"Output\": {\n        \"out\": \"$.#\"\n      }\n    }\n  }\n}\n','2026-05-11 14:06:16','2026-05-11 14:06:16');
/*!40000 ALTER TABLE `seata_state_machine_def` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `seata_state_machine_inst`
--

DROP TABLE IF EXISTS `seata_state_machine_inst`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `seata_state_machine_inst` (
  `id` varchar(128) NOT NULL,
  `machine_id` varchar(32) NOT NULL,
  `tenant_id` varchar(32) NOT NULL DEFAULT '',
  `parent_id` varchar(128) DEFAULT NULL,
  `gmt_started` datetime(3) DEFAULT NULL,
  `gmt_end` datetime(3) DEFAULT NULL,
  `business_key` varchar(128) DEFAULT NULL,
  `start_params` text,
  `end_params` text,
  `is_running` tinyint(1) DEFAULT NULL,
  `gmt_updated` datetime(3) DEFAULT NULL,
  `excep` text,
  `compensation_status` varchar(32) DEFAULT NULL,
  `status` varchar(32) DEFAULT NULL,
  `start_time` datetime DEFAULT NULL,
  `end_time` datetime DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_machine_id` (`machine_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Saga state machine instance';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `seata_state_machine_inst`
--

LOCK TABLES `seata_state_machine_inst` WRITE;
/*!40000 ALTER TABLE `seata_state_machine_inst` DISABLE KEYS */;
/*!40000 ALTER TABLE `seata_state_machine_inst` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tcc_record`
--

DROP TABLE IF EXISTS `tcc_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tcc_record` (
  `xid` varchar(64) NOT NULL COMMENT '全局事务ID',
  `phase` varchar(16) NOT NULL COMMENT 'COMMITTED / ROLLBACKED',
  PRIMARY KEY (`xid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='TCC 事务记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tcc_record`
--

LOCK TABLES `tcc_record` WRITE;
/*!40000 ALTER TABLE `tcc_record` DISABLE KEYS */;
/*!40000 ALTER TABLE `tcc_record` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `undo_log`
--

DROP TABLE IF EXISTS `undo_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `undo_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `branch_id` bigint NOT NULL,
  `xid` varchar(100) NOT NULL,
  `context` varchar(128) NOT NULL,
  `rollback_info` longblob NOT NULL,
  `log_status` int NOT NULL,
  `log_created` datetime NOT NULL,
  `log_modified` datetime NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_undo_log` (`xid`,`branch_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AT transaction undo log';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `undo_log`
--

LOCK TABLES `undo_log` WRITE;
/*!40000 ALTER TABLE `undo_log` DISABLE KEYS */;
/*!40000 ALTER TABLE `undo_log` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-05-11 15:00:42
