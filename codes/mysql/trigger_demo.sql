/**
 * Mysql 触发器（TRIGGER）创建、使用示例
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

-- 新建数据表 user_history
DROP TABLE IF EXISTS user_history;
CREATE TABLE user_history (
  id INT(10) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'ID',
  user_id INT(10) UNSIGNED NOT NULL DEFAULT 0 COMMENT '用户ID',
  operate_type VARCHAR(64) NOT NULL DEFAULT 'default' COMMENT '操作类型',
  operate_time VARCHAR(64) NOT NULL DEFAULT 'default' COMMENT '操作时间',
  PRIMARY KEY (id)
) COMMENT='用户记录表';

#############################################################
# 创建触发器
#############################################################

-- 删除触发器
DROP TRIGGER IF EXISTS trigger_insert_user;

-- 创建触发器
DELIMITER $
CREATE TRIGGER trigger_insert_user
AFTER INSERT ON user
FOR EACH ROW
BEGIN
    INSERT INTO user_history(user_id, operate_type, operate_time)
    VALUES (NEW.id, 'add a user',  now());
END $
DELIMITER ;

-- 查看触发器
SHOW TRIGGERS;

#############################################################
# 测试
#############################################################

INSERT INTO user(username, password, email)
VALUES ('admin', 'admin', 'xxxx@163.com');
SELECT * FROM user_history;
