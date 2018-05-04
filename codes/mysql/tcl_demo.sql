/**
 * Mysql TCL 语句示例
 * @author Zhang Peng
 * @date 2018/5/2
 */

#############################################################
# 运行本例的预置操作
#############################################################

-- 新建数据表 user
DROP TABLE IF EXISTS user;
CREATE TABLE user (
  id int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT 'Id',
  username varchar(64) NOT NULL DEFAULT 'default' COMMENT '用户名',
  password varchar(64) NOT NULL DEFAULT 'default' COMMENT '密码',
  email varchar(64) NOT NULL DEFAULT 'default' COMMENT '邮箱',
  PRIMARY KEY (id)
) COMMENT='用户表';

#############################################################
# 事务操作
#############################################################

-- 开始事务
START TRANSACTION;

-- 插入操作A
INSERT INTO user
VALUES (1, 'root1', 'root1', 'xxxx@163.com');

-- 创建保留点 updateA
SAVEPOINT updateA;

-- 插入操作B
INSERT INTO user
VALUES (2, 'root2', 'root2', 'xxxx@163.com');

-- 回滚到保留点 updateA
ROLLBACK TO updateA;

-- 提交事务，只有操作A生效
COMMIT;
