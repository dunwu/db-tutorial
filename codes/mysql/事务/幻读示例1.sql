-- --------------------------------------------------------------------------------------
-- 幻读示例
-- 实验说明：以下 SQL 脚本必须严格按照顺序执行，并且事务 A 和事务 B 必须在不同的 Client 中执行。
-- @author <a href="mailto:forbreak@163.com">Zhang Peng</a>
-- @date 2023/10/25
-- ----------------------------------------------------------------------------------------

-- --------------------------------------------------------------------- （1）数据初始化

-- 创建表 test
CREATE TABLE `test` (
	`id` INT(10) UNSIGNED PRIMARY KEY AUTO_INCREMENT,
	`value` INT(10) NOT NULL
);

-- 数据初始化
INSERT INTO `test` (`id`, `value`) VALUES (1, 1);
INSERT INTO `test` (`id`, `value`) VALUES (2, 2);
INSERT INTO `test` (`id`, `value`) VALUES (3, 3);

-- --------------------------------------------------------------------- （2）事务 A

BEGIN;

-- 查询 id = 4 的记录
SELECT * FROM `test` WHERE `id` = 4;
-- 结果为空

-- --------------------------------------------------------------------- （3）事务 B

BEGIN;

INSERT INTO `test` (`id`, `value`) VALUES (4, 4);

COMMIT;

-- --------------------------------------------------------------------- （4）事务 A

-- 查询 id = 4 的记录
SELECT * FROM `test` WHERE `id` = 4;
-- 结果依然为空

-- 成功更新本应看不到的记录 id = 4
UPDATE `test` SET `value` = 0 WHERE `id` = 4;

-- 再一次查询 id = 4 的记录
SELECT * FROM `test` WHERE `id` = 4;
-- 结果为：
-- +----+-------+
-- | id | value |
-- +----+-------+
-- |  4 |     0 |
-- +----+-------+

COMMIT;