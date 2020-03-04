--    部门工资最高的员工
--
--    @link https://leetcode-cn.com/problems/department-highest-salary/
--
--    Employee 表包含所有员工信息，每个员工有其对应的 Id, salary 和 department Id。
--
--    +----+-------+--------+--------------+
--    | Id | Name  | Salary | DepartmentId |
--    +----+-------+--------+--------------+
--    | 1  | Joe   | 70000  | 1            |
--    | 2  | Henry | 80000  | 2            |
--    | 3  | Sam   | 60000  | 2            |
--    | 4  | Max   | 90000  | 1            |
--    +----+-------+--------+--------------+
--    Department 表包含公司所有部门的信息。
--
--    +----+----------+
--    | Id | Name     |
--    +----+----------+
--    | 1  | IT       |
--    | 2  | Sales    |
--    +----+----------+
--    编写一个 SQL 查询，找出每个部门工资最高的员工。例如，根据上述给定的表格，Max 在 IT 部门有最高工资，
--    Henry 在 Sales 部门有最高工资。
--
--    +------------+----------+--------+
--    | Department | Employee | Salary |
--    +------------+----------+--------+
--    | IT         | Max      | 90000  |
--    | Sales      | Henry    | 80000  |
--    +------------+----------+--------+

USE db_tutorial;
CREATE TABLE IF NOT EXISTS employee (
    id           INT PRIMARY KEY AUTO_INCREMENT,
    name         VARCHAR(10),
    salary       INT,
    departmentid INT
);

INSERT INTO employee (name, salary, departmentid)
VALUES ('Joe', 70000, 1);
INSERT INTO employee (name, salary, departmentid)
VALUES ('Henry', 80000, 2);
INSERT INTO employee (name, salary, departmentid)
VALUES ('Sam', 60000, 2);
INSERT INTO employee (name, salary, departmentid)
VALUES ('Max', 90000, 1);

CREATE TABLE IF NOT EXISTS department (
    id   INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(10)
);

INSERT INTO department (name)
VALUES ('IT');
INSERT INTO department (name)
VALUES ('Sale');

SELECT *
FROM employee
WHERE departmentid, salary IN
(SELECT departmentid, MAX(salary)
FROM employee
GROUP BY departmentid);


-- 第 1 种解法
SELECT d.name AS department, e.name AS employee, e.salary
FROM employee e,
    (SELECT departmentid, MAX(salary) AS max
     FROM employee
     GROUP BY departmentid) t,
    department d
WHERE e.departmentid = t.departmentid AND e.salary = t.max AND e.departmentid = d.id;


-- 第 2 种解法
SELECT d.name AS department, e.name AS employee, e.salary
FROM employee e,
    department d
WHERE e.departmentid = d.id AND
        (departmentid, salary) IN
        (SELECT departmentid, MAX(salary) AS max
         FROM employee
         GROUP BY departmentid);
