--    【超过5名学生的课】
--
--    有一个courses 表 ，有: student (学生) 和 class (课程)。
--
--    请列出所有超过或等于5名学生的课。
--
--    例如,表:
--
--    +---------+------------+
--    | student | class      |
--    +---------+------------+
--    | A       | Math       |
--    | B       | English    |
--    | C       | Math       |
--    | D       | Biology    |
--    | E       | Math       |
--    | F       | Computer   |
--    | G       | Math       |
--    | H       | Math       |
--    | I       | Math       |
--    +---------+------------+
--    应该输出:
--
--    +---------+
--    | class   |
--    +---------+
--    | Math    |
--    +---------+
--    Note:
--    学生在每个课中不应被重复计算。

USE db_tutorial;

CREATE TABLE courses (
    student VARCHAR(10) PRIMARY KEY,
    class   VARCHAR(10)
);

INSERT INTO courses
VALUES ('A', 'Math');
INSERT INTO courses
VALUES ('B', 'English');
INSERT INTO courses
VALUES ('C', 'Math');
INSERT INTO courses
VALUES ('D', 'Biology');
INSERT INTO courses
VALUES ('E', 'Math');
INSERT INTO courses
VALUES ('F', 'Computer');
INSERT INTO courses
VALUES ('G', 'Math');
INSERT INTO courses
VALUES ('H', 'Math');
INSERT INTO courses
VALUES ('I', 'Math');

-- 解题
SELECT class
FROM courses
GROUP BY class
HAVING COUNT(DISTINCT student) >= 5;
