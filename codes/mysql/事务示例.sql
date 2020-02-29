-- --------------------------------------------------------------------------------------
-- Mysql 事务示例
-- @author Zhang Peng
-- @date 2020/02/29
-- ----------------------------------------------------------------------------------------

-- --------------------------------------------------------------------- 数据定义

CREATE DATABASE IF NOT EXISTS db_tutorial;
USE db_tutorial;

-- 撤销表 user
DROP TABLE IF EXISTS user;

-- 创建表 user
CREATE TABLE user (
    id       INT(10) UNSIGNED NOT NULL COMMENT 'Id',
    username VARCHAR(64)      NOT NULL DEFAULT 'default' COMMENT '用户名',
    password VARCHAR(64)      NOT NULL DEFAULT 'default' COMMENT '密码',
    email    VARCHAR(64)      NOT NULL DEFAULT 'default' COMMENT '邮箱'
) COMMENT ='用户表';

-- --------------------------------------------------------------------- 事务示例

-- 开始事务
START TRANSACTION;

-- 插入操作 A
INSERT INTO user
VALUES (1, 'root1', 'root1', 'xxxx@163.com');

-- 创建保留点 updateA
SAVEPOINT updateA;

-- 插入操作 B
INSERT INTO user
VALUES (2, 'root2', 'root2', 'xxxx@163.com');

-- 回滚到保留点 updateA
ROLLBACK TO updateA;

-- 提交事务，只有操作 A 生效
COMMIT;

-- --------------------------------------------------------------------- 检验结果

SELECT *
FROM user;
