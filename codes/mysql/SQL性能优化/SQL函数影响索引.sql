-- --------------------------------------------------------------------------------------
-- 函数操作影响索引效率示例
-- @author <a href="mailto:forbreak@163.com">Zhang Peng</a>
-- ----------------------------------------------------------------------------------------

-- 步骤 1、建表
CREATE TABLE tradelog (
    id         INT(11) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Id',
    tradeid    VARCHAR(32) DEFAULT NULL,
    operator   INT(11)     DEFAULT NULL,
    t_modified DATETIME    DEFAULT NULL,
    PRIMARY KEY (id),
    KEY tradeid(tradeid),
    KEY t_modified(t_modified)
)
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4;

CREATE TABLE trade_detail (
    id         INT(11) NOT NULL,
    tradeid    VARCHAR(32) DEFAULT NULL,
    trade_step INT(11)     DEFAULT NULL, /* 操作步骤 */
    step_info  VARCHAR(32) DEFAULT NULL, /* 步骤信息 */
    PRIMARY KEY (id),
    KEY tradeid(tradeid)
)
    ENGINE = InnoDB
    DEFAULT CHARSET = utf8;

-- 步骤 2、存储过程初始化数据

INSERT INTO trade_detail
VALUES (1, 'aaaaaaaa', 1, 'add');
INSERT INTO trade_detail
VALUES (2, 'aaaaaaaa', 2, 'update');
INSERT INTO trade_detail
VALUES (3, 'aaaaaaaa', 3, 'commit');
INSERT INTO trade_detail
VALUES (4, 'aaaaaaab', 1, 'add');
INSERT INTO trade_detail
VALUES (5, 'aaaaaaab', 2, 'update');
INSERT INTO trade_detail
VALUES (6, 'aaaaaaab', 3, 'update again');
INSERT INTO trade_detail
VALUES (7, 'aaaaaaab', 4, 'commit');
INSERT INTO trade_detail
VALUES (8, 'aaaaaaac', 1, 'add');
INSERT INTO trade_detail
VALUES (9, 'aaaaaaac', 2, 'update');
INSERT INTO trade_detail
VALUES (10, 'aaaaaaac', 3, 'update again');
INSERT INTO trade_detail
VALUES (11, 'aaaaaaac', 4, 'commit');

INSERT INTO tradelog
VALUES (1, 'aaaaaaaa', 1000, now());
INSERT INTO tradelog
VALUES (2, 'aaaaaaab', 1000, now());
INSERT INTO tradelog
VALUES (3, 'aaaaaaac', 1000, now());

DELIMITER ;;
DROP PROCEDURE IF EXISTS init;
CREATE PROCEDURE init()
BEGIN
    DECLARE i INT;
    SET i = 3;
    WHILE i < 10000
        DO
            INSERT INTO tradelog(tradeid, operator, t_modified)
            VALUES (concat(char(97 + (i DIV 1000)), char(97 + (i % 1000 DIV 100)), char(97 + (i % 100 DIV 10)),
                           char(97 + (i % 10))), i, now());
            SET i = i + 1;
        END WHILE;
END;;
DELIMITER ;
CALL init();

-- 步骤 3、执行计划查看SQL效率
-- 3.1.1 此 SQL 对索引字段做函数操作，优化器会放弃走树搜索功能，改为全表扫描
EXPLAIN
SELECT count(*)
FROM tradelog
WHERE month(t_modified) = 7;

-- 3.1.2 SQL 优化
EXPLAIN
SELECT count(*)
FROM tradelog
WHERE (t_modified >= '2016-7-1' AND t_modified < '2016-8-1') OR
    (t_modified >= '2017-7-1' AND t_modified < '2017-8-1') OR
    (t_modified >= '2018-7-1' AND t_modified < '2018-8-1');

-- 3.2.1 此 SQL 对索引字段隐式的使用了转换函数操作，优化器会放弃走树搜索功能，改为全表扫描
-- 相当于 select * from tradelog where  CAST(tradid AS signed int) = 110717;
EXPLAIN
SELECT *
FROM tradelog
WHERE tradeid = 110717;

-- 3.3.1 下面两条 SQL 的扫描行数不同
-- 原因是：字符集 utf8mb4 是 utf8 的超集，所以当这两个类型的字符串在做比较的时候，
-- MySQL 内部的操作是，先把 utf8 字符串转成 utf8mb4 字符集，再做比较。
# 需要做字符编码转换
EXPLAIN
SELECT d.*
FROM tradelog l, trade_detail d
WHERE d.tradeid = l.tradeid AND l.id = 2;

# 上面的 SQL 等价于这条注掉的 SQL
# SELECT *
# FROM trade_detail
# WHERE CONVERT(traideid USING utf8mb4) = $l2.tradeid.value;

# 不需要做字符编码转换
EXPLAIN
SELECT l.operator
FROM tradelog l, trade_detail d
WHERE d.tradeid = l.tradeid AND d.id = 2;
