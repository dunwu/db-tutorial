/**
 * Mysql DML 语句示例
 * @author Zhang Peng
 * @date 2018/4/28
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='用户表';


-- 添加测试数据
INSERT INTO user(username, password, email) VALUES ('张三', '123456', 'xxxx@163.com');
INSERT INTO user(username, password, email) VALUES ('李四', '123456', 'xxxx@163.com');
INSERT INTO user(username, password, email) VALUES ('王五', '123456', 'xxxx@163.com');
INSERT INTO user(username, password, email) VALUES ('赵六', '123456', 'xxxx@163.com');
INSERT INTO user(username, password, email) VALUES ('柳七', '123456', 'xxxx@163.com');
INSERT INTO user(username, password, email) VALUES ('英八', '123456', 'xxxx@163.com');
INSERT INTO user(username, password, email) VALUES ('雷九', '123456', 'xxxx@163.com');

#############################################################
# 插入数据
#############################################################

-- 插入完整的行
INSERT INTO user
VALUES (10, 'root', 'root', 'xxxx@163.com');

-- 插入行的一部分
-- 注意自增ID数值，由于当前最大的ID值为10，所以插入本条记录时自增ID为11
INSERT INTO user(username, password, email)
VALUES ('admin', 'admin', 'xxxx@163.com');

#############################################################
# 更新数据
#############################################################

-- 更新记录
UPDATE user
SET username='robot', password='robot'
WHERE username = 'root';

#############################################################
# 查询数据
#############################################################

-- 查询单列
SELECT username FROM user;

-- 查询多列
SELECT username, password FROM user;

-- 查询所有列
SELECT * FROM user;

-- 查询不同的值
SELECT DISTINCT password FROM user;
SELECT DISTINCT username, password FROM user;

-- 限制结果
-- 返回前 5 行
SELECT * FROM user LIMIT 5;
-- 返回前 5 行
SELECT * FROM user LIMIT 0, 5;
-- 返回第 3 ~ 5 行
SELECT * FROM user LIMIT 2, 3;

-- 简单的过滤查询
SELECT username, password FROM user
WHERE id = 1;

#############################################################
# 删除数据
#############################################################

-- 删除符合条件的记录
DELETE FROM user
WHERE username = 'robot';

-- 清空数据表
TRUNCATE TABLE user;
