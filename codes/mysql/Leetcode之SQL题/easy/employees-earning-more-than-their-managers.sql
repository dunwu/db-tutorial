--    超过经理收入的员工
--
--    Employee 表包含所有员工，他们的经理也属于员工。每个员工都有一个 Id，此外还有一列对应员工的经理的 Id。
--
--    +----+-------+--------+-----------+
--    | Id | Name  | Salary | ManagerId |
--    +----+-------+--------+-----------+
--    | 1  | Joe   | 70000  | 3         |
--    | 2  | Henry | 80000  | 4         |
--    | 3  | Sam   | 60000  | NULL      |
--    | 4  | Max   | 90000  | NULL      |
--    +----+-------+--------+-----------+
--    给定 Employee 表，编写一个 SQL 查询，该查询可以获取收入超过他们经理的员工的姓名。
--    在上面的表格中，Joe 是唯一一个收入超过他的经理的员工。
--
--    +----------+
--    | Employee |
--    +----------+
--    | Joe      |
--    +----------+

-- 以下 3 种解法，由上至下，处理速度越来越慢：
-- 第 1 种查询
SELECT e1.Name AS Employee
FROM Employee e1
    INNER JOIN Employee e2
    ON e1.ManagerId = e2.Id AND e1.Salary > e2.Salary;

-- 第 2 种解法
SELECT e1.Name AS Employee
FROM Employee e1, Employee  e2
WHERE e1.ManagerId = e2.Id AND e1.Salary > e2.Salary;

-- 第 3 种查询
SELECT e1.Name AS Employee
FROM Employee e1
WHERE
e1.Salary > (SELECT e2.Salary
FROM Employee e2
WHERE e1.ManagerId = e2.Id);
