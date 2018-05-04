/**
 * Mysql DDL 语句示例
 * @author Zhang Peng
 * @date 2018/4/28
 */

#############################################################
# 数据库定义
#############################################################

-- 撤销数据库 test
DROP DATABASE IF EXISTS test;

-- 创建数据库 test
CREATE DATABASE test;

-- 选择数据库 test
use test;

#############################################################
# 数据表定义
#############################################################

-- 撤销表 user
DROP TABLE IF EXISTS user;
DROP TABLE IF EXISTS vip_user;

-- 创建表 user
CREATE TABLE user (
  id int(10) unsigned NOT NULL COMMENT 'Id',
  username varchar(64) NOT NULL DEFAULT 'default' COMMENT '用户名',
  password varchar(64) NOT NULL DEFAULT 'default' COMMENT '密码',
  email varchar(64) NOT NULL DEFAULT 'default' COMMENT '邮箱'
) COMMENT='用户表';

-- 创建新表 vip_user 并复制表 user 的内容
CREATE TABLE vip_user AS
SELECT * FROM user;

-- 添加列 age
ALTER TABLE user
ADD age int(3);

-- 修改列 age 的类型为 tinyint
ALTER TABLE user
MODIFY COLUMN age tinyint;

-- 撤销列 age
ALTER TABLE user
DROP COLUMN age;

#############################################################
# 索引定义
#############################################################

-- 创建表 user 的索引 user_index
CREATE INDEX user_index
ON user (id);

-- 创建表 user 的唯一索引 user_index2
CREATE UNIQUE INDEX user_index2
ON user (id);

-- 撤销表 user 的索引
ALTER TABLE user
DROP INDEX user_index;
ALTER TABLE user
DROP INDEX user_index2;

#############################################################
# 视图定义
#############################################################

-- 创建表 user 的视图 top_10_user_view
CREATE VIEW top_10_user_view AS
SELECT id, username
FROM user
WHERE id < 10;

-- 撤销表 user 的视图 top_10_user_view
DROP VIEW top_10_user_view;
