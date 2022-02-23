-- -------------------------------------------------------------------
-- 运行本项目的初始化 DDL 脚本
-- Mysql 知识点可以参考：
-- https://dunwu.github.io/db-tutorial/#/sql/mysql/README
-- -------------------------------------------------------------------

-- 创建用户表
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id`      BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `name`    VARCHAR(255)        NOT NULL DEFAULT '' COMMENT '用户名',
  `age`     INT(3)              NOT NULL DEFAULT 0 COMMENT '年龄',
  `address` VARCHAR(255)        NOT NULL DEFAULT '' COMMENT '地址',
  `email`   VARCHAR(255)        NOT NULL DEFAULT '' COMMENT '邮件',
  PRIMARY KEY (`id`),
  UNIQUE (`name`)
);

