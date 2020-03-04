--    从不订购的客户
--
--    @link https://leetcode-cn.com/problems/customers-who-never-order/
--
--    某网站包含两个表，Customers 表和 Orders 表。编写一个 SQL 查询，找出所有从不订购任何东西的客户。
--
--    Customers 表：
--
--    +----+-------+
--    | Id | Name  |
--    +----+-------+
--    | 1  | Joe   |
--    | 2  | Henry |
--    | 3  | Sam   |
--    | 4  | Max   |
--    +----+-------+
--    Orders 表：
--
--    +----+------------+
--    | Id | CustomerId |
--    +----+------------+
--    | 1  | 3          |
--    | 2  | 1          |
--    +----+------------+
--    例如给定上述表格，你的查询应返回：
--
--    +-----------+
--    | Customers |
--    +-----------+
--    | Henry     |
--    | Max       |
--    +-----------+

CREATE TABLE IF NOT EXISTS customers (
    id   INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(20)
);
INSERT INTO customers(name)
VALUES ('Joe');
INSERT INTO customers(name)
VALUES ('Henry');
INSERT INTO customers(name)
VALUES ('Sam');
INSERT INTO customers(name)
VALUES ('Max');

CREATE TABLE IF NOT EXISTS orders (
    id         INT PRIMARY KEY AUTO_INCREMENT,
    customerid INT
);
INSERT INTO orders(customerid)
VALUES (3);
INSERT INTO orders(customerid)
VALUES (1);

-- 方法一
SELECT name AS customers
FROM customers c
WHERE c.id NOT IN (SELECT DISTINCT customerid
                   FROM orders);
