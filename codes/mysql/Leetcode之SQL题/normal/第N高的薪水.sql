--    第N高的薪水
--
--    编写一个 SQL 查询，获取 Employee 表中第 n 高的薪水（Salary）。
--
--    +----+--------+
--    | Id | Salary |
--    +----+--------+
--    | 1  | 100    |
--    | 2  | 200    |
--    | 3  | 300    |
--    +----+--------+
--    例如上述 Employee 表，n = 2 时，应返回第二高的薪水 200。如果不存在第 n 高的薪水，那么查询应返回 null。
--
--    +------------------------+
--    | getNthHighestSalary(2) |
--    +------------------------+
--    | 200                    |
--    +------------------------+

USE db_tutorial;
CREATE TABLE IF NOT EXISTS employee (
    id     INT PRIMARY KEY AUTO_INCREMENT,
    salary INT
);

INSERT INTO employee(salary)
VALUES (100);
INSERT INTO employee(salary)
VALUES (200);
INSERT INTO employee(salary)
VALUES (300);

SELECT DISTINCT salary
FROM employee e
WHERE 1 = (SELECT COUNT(DISTINCT salary)
           FROM employee
           WHERE salary >= e.salary);

CREATE FUNCTION getNthHighestSalary(n INT) RETURNS INT
BEGIN
    RETURN (
        SELECT DISTINCT salary
        FROM employee e
        WHERE n = (SELECT COUNT(DISTINCT salary)
                   FROM employee
                   WHERE salary >= e.salary)
    );
END
