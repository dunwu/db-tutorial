/**
 * Mysql 查询示例
 * @author Zhang Peng
 * @date 2018/5/5
 */

-- -------------------------------------------
-- 查询数据
-- -------------------------------------------

-- 查询单列
SELECT prod_name
FROM products;

-- 查询多列
SELECT prod_id, prod_name, prod_price
FROM products;

-- 查询所有列
SELECT *
FROM products;

-- 查询不同的值
SELECT DISTINCT vend_id
FROM products;

-- 限制结果
-- 返回前 5 行（1）
SELECT *
FROM products
LIMIT 5;
-- 返回前 5 行（2）
SELECT *
FROM products
LIMIT 0, 5;
-- 返回第 3 ~ 5 行
SELECT *
FROM products
LIMIT 2, 3;

-- -------------------------------------------
-- 查询数据排序
-- -------------------------------------------

-- 默认升序
SELECT prod_price FROM products
ORDER BY prod_price;

-- 指定多个列的排序方向
SELECT * FROM products
ORDER BY prod_price DESC, prod_name ASC;


-- -------------------------------------------
-- IN、BETWEEN
-- -------------------------------------------

-- IN 示例
SELECT *
FROM products
WHERE vend_id IN ('DLL01', 'BRS01');

-- BETWEEN 示例
SELECT *
FROM products
WHERE prod_price BETWEEN 3 AND 5;

-- -------------------------------------------
-- AND、OR、NOT
-- -------------------------------------------

-- AND 示例
SELECT prod_id, prod_name, prod_price
FROM products
WHERE vend_id = 'DLL01' AND prod_price <= 4;

-- OR 示例
SELECT prod_id, prod_name, prod_price
FROM products
WHERE vend_id = 'DLL01' OR vend_id = 'BRS01';

-- OR 示例
SELECT prod_id, prod_name, prod_price
FROM products
WHERE vend_id = 'DLL01' OR vend_id = 'BRS01';

-- AND 优先级高于 OR 示例（比较两条 sql 结果的差异）
SELECT *
FROM products
WHERE vend_id = 'DLL01' OR vend_id = 'BRS01' AND prod_price >= 10;
SELECT *
FROM products
WHERE (vend_id = 'DLL01' OR vend_id = 'BRS01') AND prod_price >= 10;

-- NOT 示例
SELECT *
FROM products
WHERE prod_price NOT BETWEEN 3 AND 5;

-- -------------------------------------------
-- 通配符
-- -------------------------------------------
SELECT prod_id, prod_name, prod_price
FROM products
WHERE prod_name LIKE 'Fish%';

SELECT prod_id, prod_name, prod_price
FROM products
WHERE prod_name LIKE '%bean bag%';

SELECT prod_id, prod_name, prod_price
FROM products
WHERE prod_name LIKE 'F%y';

SELECT prod_id, prod_name, prod_price
FROM products
WHERE prod_name LIKE '__ inch teddy bear';

SELECT prod_id, prod_name, prod_price
FROM products
WHERE prod_name LIKE '% inch teddy bear';

-- -------------------------------------------
-- 排序和分组
-- -------------------------------------------

SELECT vend_id, COUNT(*) AS num
FROM products
GROUP BY vend_id;

SELECT cust_id, COUNT(*) AS orders
FROM orders
GROUP BY cust_id
HAVING COUNT(*) >= 2;

SELECT order_num, COUNT(*) AS items
FROM orderitems
GROUP BY order_num
HAVING COUNT(*) >= 3
ORDER BY items, order_num;

-- -------------------------------------------
-- 子查询
-- -------------------------------------------

SELECT cust_id
FROM orders
WHERE order_num IN (SELECT order_num
                    FROM orderitems
                    WHERE prod_id = 'RGAN01');

SELECT cust_name, cust_contact
FROM customers
WHERE cust_id IN (SELECT cust_id
                  FROM orders
                  WHERE order_num IN (SELECT order_num
                                      FROM orderitems
                                      WHERE prod_id = 'RGAN01'));

-- -------------------------------------------
-- 连接
-- -------------------------------------------

-- 内连接
SELECT vend_name, prod_name, prod_price
FROM vendors INNER JOIN products
ON vendors.vend_id = products.vend_id;

-- 自连接
SELECT c1.cust_id, c1.cust_name, c1.cust_contact
FROM customers c1, customers c2
WHERE c1.cust_name = c2.cust_name
AND c2.cust_contact = 'Jim Jones';

-- 自连接
SELECT c.*, o.order_num, o.order_date,
       oi.prod_id, oi.quantity, oi.item_price
FROM customers c, orders o, orderitems oi
WHERE c.cust_id = o.cust_id
AND oi.order_num = o.order_num
AND prod_id = 'RGAN01';

-- 左连接
SELECT customers.cust_id, orders.order_num
FROM customers LEFT JOIN orders
ON customers.cust_id = orders.cust_id;

-- 右连接
SELECT customers.cust_id, orders.order_num
FROM customers RIGHT JOIN orders
ON customers.cust_id = orders.cust_id;

-- 组合
SELECT cust_name, cust_contact, cust_email
FROM customers
WHERE cust_state IN ('IL', 'IN', 'MI')
UNION
SELECT cust_name, cust_contact, cust_email
FROM customers
WHERE cust_name = 'Fun4All';