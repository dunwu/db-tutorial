-- --------------------------------------------------------------------------------------
-- Mysql 基本 TCL 语句示例
-- @author <a href="mailto:forbreak@163.com">Zhang Peng</a>
-- @date 2018/4/28
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
SAVEPOINT updatea;

-- 插入操作 B
INSERT INTO user
VALUES (2, 'root2', 'root2', 'xxxx@163.com');

-- 回滚到保留点 updateA
ROLLBACK TO updatea;

-- 提交事务，只有操作 A 生效
COMMIT;

-- --------------------------------------------------------------------- 检验结果

SELECT *
FROM user;


-- --------------------------------------------------------------------- 开启/关闭 AUTOCOMMIT

-- 查看 AUTOCOMMIT
SHOW VARIABLES LIKE 'AUTOCOMMIT';

-- 关闭 AUTOCOMMIT
SET autocommit = 0;


-- 开启 AUTOCOMMIT
SET autocommit = 1;


-- --------------------------------------------------------------------- 事务隔离级别

-- 查看事务隔离级别
SHOW VARIABLES LIKE 'transaction_isolation';

-- 设置事务隔离级别为 READ UNCOMMITTED
SET SESSION TRANSACTION ISOLATION LEVEL READ UNCOMMITTED;

-- 设置事务隔离级别为 READ COMMITTED
SET SESSION TRANSACTION ISOLATION LEVEL READ COMMITTED;

-- 设置事务隔离级别为 REPEATABLE READ
SET SESSION TRANSACTION ISOLATION LEVEL REPEATABLE READ;

-- 设置事务隔离级别为 SERIALIZABLE
SET SESSION TRANSACTION ISOLATION LEVEL SERIALIZABLE;


