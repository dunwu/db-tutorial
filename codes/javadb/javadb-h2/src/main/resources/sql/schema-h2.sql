-- -------------------------------------------------------------------
-- 运行本项目的初始化 DDL 脚本
-- H2 知识点可以参考：
-- https://dunwu.github.io/db-tutorial/#/sql/h2
-- -------------------------------------------------------------------
CREATE TABLE user (
  id      INT NOT NULL AUTO_INCREMENT,
  name    VARCHAR(100),
  age     INT,
  address VARCHAR(50),
  email   VARCHAR(50),
  PRIMARY KEY (id)
);
