--    上升的温度
--
--    @link https://leetcode-cn.com/problems/rising-temperature/
--
--    给定一个 Weather 表，编写一个 SQL 查询，来查找与之前（昨天的）日期相比温度更高的所有日期的 Id。
--
--    +---------+------------------+------------------+
--    | Id(INT) | RecordDate(DATE) | Temperature(INT) |
--    +---------+------------------+------------------+
--    |       1 |       2015-01-01 |               10 |
--    |       2 |       2015-01-02 |               25 |
--    |       3 |       2015-01-03 |               20 |
--    |       4 |       2015-01-04 |               30 |
--    +---------+------------------+------------------+
--    例如，根据上述给定的 Weather 表格，返回如下 Id:
--
--    +----+
--    | Id |
--    +----+
--    |  2 |
--    |  4 |
--    +----+

CREATE TABLE weather (
    id          INT PRIMARY KEY AUTO_INCREMENT,
    recorddate  TIMESTAMP,
    temperature INT
);

INSERT INTO weather (recorddate, temperature)
VALUES (TIMESTAMP('2015-01-01'), 10);
INSERT INTO weather (recorddate, temperature)
VALUES (TIMESTAMP('2015-01-02'), 25);
INSERT INTO weather (recorddate, temperature)
VALUES (TIMESTAMP('2015-01-03'), 20);
INSERT INTO weather (recorddate, temperature)
VALUES (TIMESTAMP('2015-01-04'), 30);

-- 解题
SELECT w1.id
FROM weather w1, weather w2
WHERE w1.recorddate = DATE_ADD(w2.recorddate, INTERVAL 1 DAY) AND w1.temperature > w2.temperature;

