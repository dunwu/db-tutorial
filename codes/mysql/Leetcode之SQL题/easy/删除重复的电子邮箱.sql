--     删除重复的电子邮箱
--
--     @link https://leetcode-cn.com/problems/delete-duplicate-emails/
--
--     编写一个 SQL 查询，来删除 Person 表中所有重复的电子邮箱，重复的邮箱里只保留 Id 最小 的那个。
--
--     +----+------------------+
--     | Id | Email            |
--     +----+------------------+
--     | 1  | john@example.com |
--     | 2  | bob@example.com  |
--     | 3  | john@example.com |
--     +----+------------------+
--     Id 是这个表的主键。
--     例如，在运行你的查询语句之后，上面的 Person 表应返回以下几行:
--
--     +----+------------------+
--     | Id | Email            |
--     +----+------------------+
--     | 1  | john@example.com |
--     | 2  | bob@example.com  |
--     +----+------------------+
--      
--
--     提示：
--
--     执行 SQL 之后，输出是整个 Person 表。
--     使用 delete 语句。

USE db_tutorial;

CREATE TABLE IF NOT EXISTS person (
    id    INT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(32)
);

INSERT INTO person (email)
VALUES ('john@example.com');
INSERT INTO person (email)
VALUES ('bob@example.com');
INSERT INTO person (email)
VALUES ('john@example.com');

-- 解题
DELETE p1
FROM person p1, person p2
WHERE p1.id > p2.id AND p1.email = p2.email;
