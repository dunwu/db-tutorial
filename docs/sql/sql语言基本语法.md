# sql 语言

## DDL

DDL 全称 Data Definition Language，即数据定义语言。

### DATABASE

#### 创建数据库

`CREATE DATABASE` 语句用于创建数据库。

```sql
CREATE DATABASE database_name;
```

#### 撤销数据库

`DROP DATABASE` 语句用于撤销数据库。

```sql
DROP DATABASE database_name;
```

### TABLE

#### 创建表

`CREATE TABLE` 语句用于创建数据库中的表。

```sql
CREATE TABLE table_name
(
	column_name1 data_type(size),
	column_name2 data_type(size),
	column_name3 data_type(size),
	....
);
```

#### 撤销表

`DROP TABLE` 语句用于撤销数据库中的表。

```sql
DROP TABLE table_name;
```

#### 修改表

`ALTER TABLE` 语句用于在已有的表中添加、删除或修改列。

- **添加列**

```sql
ALTER TABLE table_name
ADD column_name datatype;
```

- **删除列**

```sql
ALTER TABLE table_name
DROP COLUMN column_name;
```

- **修改列**

```sql
ALTER TABLE table_name
MODIFY COLUMN column_name datatype;
```

### INDEX

#### 创建索引

`CREATE INDEX` 语句用于在表中创建索引。

```sql
CREATE INDEX index_name
ON table_name (column_name)
```

#### 创建唯一索引

`CREATE UNIQUE INDEX` 语句用于在表中创建唯一索引。

在表上创建一个唯一的索引。不允许使用重复的值：唯一的索引意味着两个行不能拥有相同的索引值。

```sql
CREATE UNIQUE INDEX index_name
ON table_name (column_name)
```

#### 撤销索引

Oracle 方法：

```sql
DROP INDEX index_name
```

Mysql 方法：

```sql
ALTER TABLE table_name DROP INDEX index_name
```

### VIEW

视图是基于 SQL 语句的结果集的可视化的表。

视图包含行和列，就像一个真实的表。视图中的字段就是来自一个或多个数据库中的真实的表中的字段。

> **注：**视图总是显示最新的数据！每当用户查询视图时，数据库引擎通过使用视图的 SQL 语句重建数据。

#### 创建视图

`CREATE VIEW` 语句用于创建视图。

```sql
CREATE VIEW view_name AS
SELECT column_name(s)
FROM table_name
WHERE condition;
```

#### 撤销视图

`DROP VIEW` 语句用于撤销视图。

```sql
DROP VIEW view_name;
```

#### 修改视图

`CREATE OR REPLACE VIEW` 语句用于修改视图。

```sql
CREATE OR REPLACE VIEW view_name AS
SELECT column_name(s)
FROM table_name
WHERE condition;
```

## DML

DML 全称 Data Manipulation Language，即数据操纵语言。

### INSERT

`INSERT INTO` 语句用于向表中插入新记录。

第一种形式无需指定要插入数据的列名，只需提供被插入的值即可：

```sql
INSERT INTO table_name
VALUES (value1,value2,value3,...);
```

第二种形式需要指定列名及被插入的值：

```sql
INSERT INTO table_name (column1,column2,column3,...)
VALUES (value1,value2,value3,...);
```

### UPDATE

`UPDATE` 语句用于更新表中的记录。

```sql
UPDATE table_name
SET column1=value1,column2=value2,...
WHERE some_column=some_value;
```

### DELETE

`DELETE` 语句用于删除表中的记录。

```sql
DELETE FROM table_name
WHERE some_column=some_value;
```

## DQL

DQL 全称 Data Query Language，即数据查询语言。

### SELECT

```sql
SELECT column_name(s)
FROM table_name
[ WHERE search_condition ]
[ GROUP BY group_by_expression ]
[ HAVING search_condition ]
[ ORDER BY order_expression [ ASC | DESC ] ]
```

DISTINCT

### WHERE

`WHERE` 子句用于过滤查询条件。

下面的运算符可以在 WHERE 子句中使用：

| 运算符     | 描述                              |
| ------- | ------------------------------- |
| =       | 等于                              |
| <>      | 不等于。注释：在 SQL 的一些版本中，该操作符可被写成 != |
| >       | 大于                              |
| <       | 小于                              |
| >=      | 大于等于                            |
| <=      | 小于等于                            |
| AND     | 前后两个条件都成立                       |
| OR      | 前后两个条件有一个成立                     |
| BETWEEN | 在某个范围内                          |
| IN      | 指定针对某个列的多个可能值                   |
| LIKE    | 搜索某种模式                          |

#### AND & OR

AND & OR 运算符用于基于一个以上的条件对记录进行过滤。

```sql
SELECT column_name(s)
FROM table_name
WHERE condition1 AND condition2;

SELECT column_name(s)
FROM table_name
WHERE condition1 OR condition2;
```

#### BETWEEN

BETWEEN 操作符用于选取介于两个值之间的数据范围内的值。

```sql
SELECT column_name(s)
FROM table_name
WHERE column_name BETWEEN value1 AND value2;
```

#### IN

IN 操作符允许您在 WHERE 子句中规定多个值。

```sql
SELECT column_name(s)
FROM table_name
WHERE column_name IN (value1,value2,...);
```

#### LIKE

LIKE 操作符用于在 WHERE 子句中搜索列中的指定模式。

```sql
SELECT column_name(s)
FROM table_name
WHERE column_name LIKE pattern;
```

#### 通配符

通配符可用于替代字符串中的任何其他字符。在 SQL 中，通配符与 SQL LIKE 操作符一起使用。

在 SQL 中，可使用以下通配符：

| 通配符                         | 描述            |
| --------------------------- | ------------- |
| %                           | 替代 0 个或多个字符   |
| _                           | 替代一个字符        |
| [*charlist*]                | 字符列中的任何单一字符   |
| [^*charlist*]或[!*charlist*] | 不在字符列中的任何单一字符 |

### ORDER BY

`ORDER BY` 关键字用于对结果集进行排序。

```sql
SELECT column_name,column_name
FROM table_name
ORDER BY column_name,column_name ASC|DESC;
```

> 注：`ASC` 表示升序；`DESC` 表示降序。

### GROUP BY

`GROUP BY` 语句用于结合聚合函数，根据一个或多个列对结果集进行分组。

```sql
SELECT column_name, aggregate_function(column_name)
FROM table_name
WHERE column_name operator value
GROUP BY column_name;
```

例：

```sql
SELECT site_id, SUM(access_log.count) AS nums
FROM access_log GROUP BY site_id;
```

#### HAVING

在 SQL 中增加 HAVING 子句原因是，WHERE 关键字无法与聚合函数一起使用。

HAVING 子句可以让我们筛选分组后的各组数据。

```sql
SELECT column_name, aggregate_function(column_name)
FROM table_name
WHERE column_name operator value
GROUP BY column_name
HAVING aggregate_function(column_name) operator value;
```

例：

```sql
SELECT Websites.name, SUM(access_log.count) AS nums FROM Websites
INNER JOIN access_log
ON Websites.id=access_log.site_id
WHERE Websites.alexa < 200 
GROUP BY Websites.name
HAVING SUM(access_log.count) > 200;
```

### JOIN

`JOIN` 子句用于把来自两个或多个表的行结合起来，基于这些表之间的共同字段。

在我们继续讲解实例之前，我们先列出您可以使用的不同的 SQL JOIN 类型：

- **INNER JOIN**：如果表中有至少一个匹配，则返回行

```sql
SELECT column_name(s)
FROM table1
INNER JOIN table2
ON table1.column_name=table2.column_name;
```

- **LEFT JOIN**：即使右表中没有匹配，也从左表返回所有的行

```sql
SELECT column_name(s)
FROM table1
LEFT JOIN table2
ON table1.column_name=table2.column_name;
```

- **RIGHT JOIN**：即使左表中没有匹配，也从右表返回所有的行

```sql
SELECT column_name(s)
FROM table1
RIGHT JOIN table2
ON table1.column_name=table2.column_name;
```

- **FULL JOIN**：只要其中一个表中存在匹配，则返回行

```sql
SELECT column_name(s)
FROM table1
FULL OUTER JOIN table2
ON table1.column_name=table2.column_name;
```

### UNION

UNION 操作符用于合并两个或多个 SELECT 语句的结果集。

请注意，UNION 内部的每个 SELECT 语句必须拥有相同数量的列。列也必须拥有相似的数据类型。同时，每个 SELECT 语句中的列的顺序必须相同。

```sql
SELECT column_name(s) FROM table1
UNION
SELECT column_name(s) FROM table2;

--如果允许重复
SELECT column_name(s) FROM table1
UNION ALL
SELECT column_name(s) FROM table2;
```

# FAQ

CRUD：是指Create、Retrieve、Update 和 Delete 的首字母，合指增删改查。