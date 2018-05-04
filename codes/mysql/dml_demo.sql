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
) COMMENT='用户表';

#############################################################
# 向表中插入新记录
#############################################################

-- 不指定列名方式插入记录
INSERT INTO `user`
VALUES (1, 'root', 'root', 'xxxx@163.com');

-- 指定列名方式插入记录
INSERT INTO `user`(`username`, `password`, `email`)
VALUES ('admin', 'admin', 'xxxx@163.com');

#############################################################
# 更新表中的记录
#############################################################

-- 更新记录
UPDATE `user`
SET `username`='robot', `password`='robot'
WHERE `username` = 'root';

#############################################################
# 查询表中的记录
#############################################################

-- 查询表中的记录
SELECT `username`, `password` FROM `user`
WHERE `id` = 1;

-- 查询表中的所有记录
SELECT * FROM `user`;

-- 查询表中的不重复记录
SELECT DISTINCT `username`
FROM `user`;

#############################################################
# 删除表中的记录
#############################################################

-- 删除符合条件的记录
DELETE FROM `user`
WHERE `username` = 'robot';

-- 清空数据表
TRUNCATE TABLE `user`;
