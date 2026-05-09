-- ============================================================
-- 为 demo_user.username 添加唯一索引，防止重复用户
-- ============================================================

USE wuyou_test;

-- 先清理可能存在的重复数据，保留 id 最小的记录
DELETE t1 FROM demo_user t1
    INNER JOIN demo_user t2
WHERE t1.username = t2.username
  AND t1.id > t2.id;

ALTER TABLE demo_user
    ADD UNIQUE KEY uk_username (username);
