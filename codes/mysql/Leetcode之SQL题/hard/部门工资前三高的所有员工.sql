--     部门工资前三高的所有员工
--
--     Employee 表包含所有员工信息，每个员工有其对应的工号 Id，姓名 Name，工资 Salary 和部门编号 DepartmentId 。
--
--     +----+-------+--------+--------------+
--     | Id | Name  | Salary | DepartmentId |
--     +----+-------+--------+--------------+
--     | 1  | Joe   | 85000  | 1            |
--     | 2  | Henry | 80000  | 2            |
--     | 3  | Sam   | 60000  | 2            |
--     | 4  | Max   | 90000  | 1            |
--     | 5  | Janet | 69000  | 1            |
--     | 6  | Randy | 85000  | 1            |
--     | 7  | Will  | 70000  | 1            |
--     +----+-------+--------+--------------+
--     Department 表包含公司所有部门的信息。
--
--     +----+----------+
--     | Id | Name     |
--     +----+----------+
--     | 1  | IT       |
--     | 2  | Sales    |
--     +----+----------+
--     编写一个 SQL 查询，找出每个部门获得前三高工资的所有员工。例如，根据上述给定的表，查询结果应返回：
--
--     +------------+----------+--------+
--     | Department | Employee | Salary |
--     +------------+----------+--------+
--     | IT         | Max      | 90000  |
--     | IT         | Randy    | 85000  |
--     | IT         | Joe      | 85000  |
--     | IT         | Will     | 70000  |
--     | Sales      | Henry    | 80000  |
--     | Sales      | Sam      | 60000  |
--     +------------+----------+--------+
--     解释：
--
--     IT 部门中，Max 获得了最高的工资，Randy 和 Joe 都拿到了第二高的工资，Will 的工资排第三。销售部门（Sales）只有两名员工，Henry 的工资最高，Sam 的工资排第二。

USE db_tutorial;
CREATE TABLE IF NOT EXISTS employee (
    id           INT PRIMARY KEY AUTO_INCREMENT,
    name         VARCHAR(10),
    salary       INT,
    departmentid INT
);

INSERT INTO employee (name, salary, departmentid)
VALUES ('Joe', 85000, 1);
INSERT INTO employee (name, salary, departmentid)
VALUES ('Henry', 80000, 2);
INSERT INTO employee (name, salary, departmentid)
VALUES ('Sam', 60000, 2);
INSERT INTO employee (name, salary, departmentid)
VALUES ('Max', 90000, 1);
INSERT INTO employee (name, salary, departmentid)
VALUES ('Janet', 69000, 1);
INSERT INTO employee (name, salary, departmentid)
VALUES ('Randy', 85000, 1);
INSERT INTO employee (name, salary, departmentid)
VALUES ('Will', 70000, 1);

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
WHERE departmentid = 1 LIMIT ;
