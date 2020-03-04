--    连续出现的数字
--
--    编写一个 SQL 查询，查找所有至少连续出现三次的数字。
--
--    +----+-----+
--    | Id | Num |
--    +----+-----+
--    | 1  |  1  |
--    | 2  |  1  |
--    | 3  |  1  |
--    | 4  |  2  |
--    | 5  |  1  |
--    | 6  |  2  |
--    | 7  |  2  |
--    +----+-----+
--    例如，给定上面的 Logs 表， 1 是唯一连续出现至少三次的数字。
--
--    +-----------------+
--    | ConsecutiveNums |
--    +-----------------+
--    | 1               |
--    +-----------------+

USE db_tutorial;

CREATE TABLE IF NOT EXISTS logs (
    id  INT PRIMARY KEY AUTO_INCREMENT,
    num INT
);

INSERT INTO logs(num)
VALUES (1);
INSERT INTO logs(num)
VALUES (1);
INSERT INTO logs(num)
VALUES (1);
INSERT INTO logs(num)
VALUES (2);
INSERT INTO logs(num)
VALUES (1);
INSERT INTO logs(num)
VALUES (2);
INSERT INTO logs(num)
VALUES (2);

-- 解题
SELECT DISTINCT (l1.num) AS consecutivenums
FROM logs l1, logs l2, logs l3
WHERE l1.id = l2.id + 1 AND l2.id = l3.id + 1 AND l1.num = l2.num AND l2.num = l3.num;

