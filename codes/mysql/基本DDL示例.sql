-- --------------------------------------------------------------------------------------
-- Mysql 基本 DDL 语句示例
-- @author <a href="mailto:forbreak@163.com">Zhang Peng</a>
-- @date 2018/4/28
-- ----------------------------------------------------------------------------------------


-- --------------------------------------------------------------------- 数据库定义

-- 删除数据库 db_tutorial
DROP DATABASE IF EXISTS db_tutorial;

-- 创建数据库 db_tutorial
CREATE DATABASE IF NOT EXISTS db_tutorial;

-- 选择数据库 db_tutorial
USE db_tutorial;

-- --------------------------------------------------------------------- 数据表定义

-- 删除数据表 user
DROP TABLE IF EXISTS user;
DROP TABLE IF EXISTS vip_user;

-- 创建数据表 user
CREATE TABLE user (
    id       INT(10) UNSIGNED NOT NULL COMMENT 'Id',
    username VARCHAR(64)      NOT NULL DEFAULT 'default' COMMENT '用户名',
    password VARCHAR(64)      NOT NULL DEFAULT 'default' COMMENT '密码',
    email    VARCHAR(64)      NOT NULL DEFAULT 'default' COMMENT '邮箱'
) COMMENT ='用户表';

-- 创建新表 vip_user 并复制表 user 的内容
CREATE TABLE vip_user AS
SELECT *
FROM user;

-- 添加列 age
ALTER TABLE user
ADD age INT(3);

-- 修改列 age 的类型为 tinyint
ALTER TABLE user
MODIFY COLUMN age TINYINT;

-- 删除列 age
ALTER TABLE user
DROP COLUMN age;

-- --------------------------------------------------------------------- 索引定义

-- 创建表的索引
CREATE INDEX idx_email
    ON user(email);

-- 创建表的唯一索引
CREATE UNIQUE INDEX uniq_username
    ON user(username);

-- 删除表 user 的索引
ALTER TABLE user
DROP INDEX idx_email;

ALTER TABLE user
DROP INDEX uniq_username;

-- --------------------------------------------------------------------- 视图定义

-- 创建表 user 的视图 top_10_user_view
CREATE VIEW top_10_user_view AS
SELECT id, username
FROM user
WHERE id < 10;

-- 删除表 user 的视图 top_10_user_view
DROP VIEW top_10_user_view;
