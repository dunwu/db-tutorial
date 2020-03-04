-- --------------------------------------------------------------------------------------
-- 查找重复的电子邮箱
-- @link https://leetcode-cn.com/problems/duplicate-emails/
-- @author Zhang Peng
-- @date 2020/02/29
-- ----------------------------------------------------------------------------------------

USE db_tutorial;

CREATE TABLE IF NOT EXISTS person (
    id    INT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(32)
);

INSERT INTO person (email)
VALUES ('a@b.com');
INSERT INTO person (email)
VALUES ('c@d.com');
INSERT INTO person (email)
VALUES ('a@b.com');

-- 方法一
SELECT email
FROM (
    SELECT email, COUNT(email) AS num
    FROM person
    GROUP BY email
) AS statistic
WHERE num > 1;

-- 方法二
SELECT email
FROM person
GROUP BY email
HAVING count(email) > 1;
