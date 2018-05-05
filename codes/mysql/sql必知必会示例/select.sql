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
