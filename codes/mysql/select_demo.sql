/**
 * Mysql 触发器（TRIGGER）创建、使用示例
 * @author Zhang Peng
 * @date 2018/5/4
 */

#############################################################
# 运行本例的预置操作
#############################################################

-- 新建数据表 user
DROP TABLE IF EXISTS user;
CREATE TABLE user (
  id INT(10) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Id',
  username VARCHAR(64) NOT NULL DEFAULT '' COMMENT '用户名',
  password VARCHAR(64) NOT NULL DEFAULT '' COMMENT '密码',
  email VARCHAR(64) DEFAULT NULL COMMENT '邮箱',
  date TIMESTAMP NOT NULL DEFAULT NOW() COMMENT '日期',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='用户表';

-- 添加测试数据
INSERT INTO user(username, email) VALUES ('叶开', 'xxxx@163.com');
INSERT INTO user(username, password, email) VALUES ('傅红雪', '444444', 'xxxx@163.com');
INSERT INTO user(username, password, email) VALUES ('张三丰', '333333', 'xxxx@163.com');
INSERT INTO user(username, password, email) VALUES ('陆小凤', '777777', 'xxxx@163.com');
INSERT INTO user(username, password, email) VALUES ('王小虎', '555555', 'xxxx@163.com');
INSERT INTO user(username, password, email) VALUES ('张飞', '222222', '');
INSERT INTO user(username, password, email) VALUES ('李寻欢', '444444', 'xxxx@163.com');
INSERT INTO user(username, password, email) VALUES ('楚留香', '999999', 'xxxx@163.com');
INSERT INTO user(username, password, email) VALUES ('段 誉', '888888', 'xxxx@163.com');
INSERT INTO user(username, password) VALUES ('萧 峰', '444444');
INSERT INTO user(username, password, email) VALUES ('李逍遥', '666666', 'xxxx@163.com');
INSERT INTO user(username, password, email) VALUES ('sb', '444444', 'xxxx@163.com');
INSERT INTO user(username, password, email) VALUES ('Joe', '666666', 'xxxx@163.com');

#############################################################
# 查询排序
#############################################################

-- 查询结果排序
SELECT * FROM user
ORDER BY date DESC, username ASC;

#############################################################
# 过滤查询
#############################################################

-- 查询 email 为 NULL 的记录
SELECT * FROM user
WHERE email IS NULL;

-- 查询 email 为 '' 的记录
SELECT * FROM user
WHERE email='';

#############################################################
# 过滤查询中使用通配符
#############################################################

-- 以张开头的任意文本
SELECT * FROM user
WHERE username LIKE '张%';

-- 以张开头的两字文本
SELECT * FROM user
WHERE username LIKE '张_';

-- 不以张开头的任意文本
SELECT * FROM user
WHERE username NOT LIKE '张%';

-- 查询2个字姓名的记录
SELECT * FROM user
WHERE username LIKE '__' ;

-- 查询3个字姓名的记录
SELECT * FROM user
WHERE username LIKE '___' ;

#############################################################
# 查询中使用计算字段
#############################################################

-- 查询3个字姓名的记录
SELECT CONCAT(TRIM(username), ' (', password, ')') AS '用户名密码' FROM user;

#############################################################
# 查询分组
#############################################################

-- 分组就是把具有相同的数据值的行放在同一组中
-- 指定的分组字段除了能按该字段进行分组，也会自动按按该字段进行排序
SELECT password, COUNT(*) AS num
FROM user
GROUP BY password;

-- GROUP BY 按分组字段进行排序，ORDER BY 也可以以汇总字段来进行排序
SELECT password, COUNT(*) AS num
FROM user
GROUP BY password
ORDER BY num DESC;

-- WHERE 过滤行，HAVING 过滤分组，行过滤应当先于分组过滤
SELECT password, COUNT(*) AS num
FROM user
WHERE password != ''
GROUP BY password
HAVING num >= 2;
