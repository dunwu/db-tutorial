# sql

<!-- TOC depthFrom:2 depthTo:2 -->

- [概念](#概念)
- [定义表](#定义表)
- [插入数据](#插入数据)
- [更新数据](#更新数据)
- [删除数据](#删除数据)
- [查询数据](#查询数据)
- [排序](#排序)
- [过滤](#过滤)
- [通配符](#通配符)
- [计算字段](#计算字段)
- [函数](#函数)
- [分组](#分组)
- [子查询](#子查询)
- [连接](#连接)
- [组合查询](#组合查询)
- [视图](#视图)
- [存储过程](#存储过程)
- [游标](#游标)
- [触发器](#触发器)
- [事务处理](#事务处理)
- [字符集](#字符集)
- [权限管理](#权限管理)
- [参考资料](#参考资料)

<!-- /TOC -->

## 概念

* 数据库（database）：保存有组织的数据的容器（通常是一个文件或一组文件）。
* 数据表（table）：某种特定类型数据的结构化清单。
* 模式（schema）：关于数据库和表的布局及特性的信息。模式定义了数据在表中如何存储，包含存储什么样的数据，数据如何分解，各部分信息如何命名等信息。数据库和表都有模式。
* 列（column）：表中的一个字段。所有表都是由一个或多个列组成的。
* 行（row）：表中的一个记录。
* 主键（primary key）：一列（或一组列），其值能够唯一识别表中每一行。
  * 任意两行都不具有相同的主键值；
  * 每一行都必须具有一个主键值（主键列不允许 NULL 值）；
  * 主键列中的值不允许修改或更新；
  * 主键值不能重用（如果某行从表中删除，它的主键不能赋给以后的新行）。
* SQL（Structured Query Language)，标准 SQL 由 ANSI 标准委员会管理，从而称为 ANSI SQL。各个 DBMS 都有自己的实现，如 PL/SQL、Transact-SQL 等。
  * SQL 语句不区分大小写，但是数据库表名、列名和值是否区分依赖于具体的 DBMS 以及配置。
  * SQL 支持三种注释：
  ```sql
  ## 注释1
  -- 注释2
  /* 注释3 */
  ```
* 数据控制语言 (Data Control Language) 在 SQL 语言中，是一种可对数据访问权进行控制的指令，它可以控制特定用户账户对数据表、查看表、预存程序、用户自定义函数等数据库对象的控制权。由 GRANT 和 REVOKE 两个指令组成。

## 定义表

### 创建数据表

#### 普通创建

语法：

```sql
CREATE TABLE 数据表名 (
  列名1 数据类型,
  列名2 数据类型,
  ...
);
```

示例：

```sql
-- 创建表 user
CREATE TABLE `user` (
  `id` int(10) unsigned NOT NULL COMMENT 'Id',
  `username` varchar(64) NOT NULL DEFAULT 'default' COMMENT '用户名',
  `password` varchar(64) NOT NULL DEFAULT 'default' COMMENT '密码',
  `email` varchar(64) NOT NULL DEFAULT 'default' COMMENT '邮箱'
) COMMENT='用户表';
```

#### 根据已有的表创建新表

语法：

```sql
CREATE TABLE 数据表名 AS
SELECT * FROM 数据表名;
```

示例：

```sql
-- 创建新表 vip_user 并复制表 user 的内容
CREATE TABLE `vip_user` AS
SELECT * FROM `user`;
```

### 撤销数据表

语法：

```sql
DROP TABLE 数据表名;
```

示例：

```sql
-- 删除表 user
DROP TABLE `user`;
```

### 修改数据表

#### 添加列

语法：

```sql
ALTER TABLE 数据表名
ADD 列名 数据类型;
```

示例：

```sql
-- 添加列 age
ALTER TABLE `user`
ADD age int(3);
```

#### 删除列

语法：

```sql
ALTER TABLE 数据表名
DROP COLUMN 列名;
```

示例：

```sql
-- 删除列 age
ALTER TABLE `user`
DROP COLUMN age;
```

#### 修改列

语法：

```sql
ALTER TABLE 数据表名
ADD 列名 数据类型;
```

示例：

```sql
-- 修改列 age 的类型为 tinyint
ALTER TABLE `user`
MODIFY COLUMN age tinyint;
```

#### 添加主键

语法：

```sql
ALTER TABLE 数据表名
ADD PRIMARY KEY (列名);
```

示例：

```sql
-- 给表 user 添加主键 id
ALTER TABLE `user`
ADD PRIMARY KEY (id);
```

#### 删除主键

语法：

```sql
ALTER TABLE 数据表名
DROP PRIMARY KEY;
```

示例：

```sql
-- 表 user 删除主键
ALTER TABLE `user`
DROP PRIMARY KEY;
```

## 插入数据

### 插入完整的行

语法：

```sql
INSERT INTO 数据表名
VALUES (value1, value2, value3, ...);
```

示例：

```sql
-- 插入完整的行
INSERT INTO `user`
VALUES (1, 'root', 'root', 'xxxx@163.com');
```

### 插入行的一部分

语法：

```sql
INSERT INTO 数据表名 (列名1, 列名2, 列名3, ...)
VALUES (value1,value2,value3,...);
```

示例：

```sql
-- 插入行的一部分
INSERT INTO `user`(`username`, `password`, `email`)
VALUES ('admin', 'admin', 'xxxx@163.com');
```

### 插入检索出来的数据

语法：

```sql
INSERT INTO 数据表名 (列名1, 列名2, 列名3, ...)
SELECT 列名a, 列名b, 列名c, ...
FROM 数据表名2;
```

示例：

```sql
-- 插入检索出来的数据
INSERT INTO `user`(`username`)
SELECT `name`
FROM `account`;
```

## 更新数据

语法：

```sql
UPDATE 表名
SET 列名=值, 列名=值, ...
WHERE 条件;
```

示例：

```sql
-- 更新记录
UPDATE `user`
SET `username`='robot', `password`='robot'
WHERE `username` = 'root';
```

## 删除数据

语法：

```sql
DELETE FROM 数据表名
WHERE 条件;
```

示例：

```sql
-- 删除符合条件的记录
DELETE FROM `user`
WHERE `username` = 'admin';
```

**TRUNCATE TABLE** 可以清空表，也就是删除所有行。

使用更新和删除操作时一定要用 WHERE 子句，不然会把整张表的数据都破坏。可以先用 SELECT 语句进行测试，防止错误删除。

## 查询数据

### DISTINCT

相同值只会出现一次。它作用于所有列，也就是说所有列的值都相同才算相同。

```sql
SELECT DISTINCT col1, col2
FROM mytable;
```

### LIMIT

限制返回的行数。可以有两个参数，第一个参数为起始行，从 0 开始；第二个参数为返回的总行数。

返回前 5 行：

```sql
SELECT *
FROM mytable
LIMIT 5;
```

```sql
SELECT *
FROM mytable
LIMIT 0, 5;
```

返回第 3 \~ 5 行：

```sql
SELECT *
FROM mytable
LIMIT 2, 3;
```

## 排序

* **ASC** ：升序（默认）
* **DESC** ：降序

可以按多个列进行排序，并且为每个列指定不同的排序方式：

```sql
SELECT *
FROM mytable
ORDER BY col1 DESC, col2 ASC;
```

## 过滤

不进行过滤的数据非常大，导致通过网络传输了多余的数据，从而浪费了网络带宽。因此尽量使用 SQL 语句来过滤不必要的数据，而不是传输所有的数据到客户端中然后由客户端进行过滤。

```sql
SELECT *
FROM mytable
WHERE col IS NULL;
```

下表显示了 WHERE 子句可用的操作符

| 操作符   | 说明           |
| -------- | -------------- |
| = < >    | 等于 小于 大于 |
| <> !=    | 不等于         |
| <= !>    | 小于等于       |
| &gt;= !< | 大于等于       |
| BETWEEN  | 在两个值之间   |
| IS NULL  | 为 NULL 值     |

应该注意到，NULL 与 0 、空字符串都不同。

**AND OR** 用于连接多个过滤条件，优先处理 AND，当一个过滤表达式涉及到多个 AND 和 OR 时，可以使用 () 来决定优先级，使得优先级关系更清晰。

**IN** 操作符用于匹配一组值，其后也可以接一个 SELECT 子句，从而匹配子查询得到的一组值。

**NOT** 操作符用于否定一个条件。

## 通配符

通配符也是用在过滤语句中，但它只能用于文本字段。

* **%** 匹配 >=0 个任意字符；

* **\_** 匹配 ==1 个任意字符；

* **[ ]** 可以匹配集合内的字符，例如 [ab] 将匹配字符 a 或者 b。用脱字符 ^ 可以对其进行否定，也就是不匹配集合内的字符。

使用 Like 来进行通配符匹配。

```sql
SELECT *
FROM mytable
WHERE col LIKE '[^AB]%' -- 不以 A 和 B 开头的任意文本
```

不要滥用通配符，通配符位于开头处匹配会非常慢。

## 计算字段

在数据库服务器上完成数据的转换和格式化的工作往往比客户端上快得多，并且转换和格式化后的数据量更少的话可以减少网络通信量。

计算字段通常需要使用 **AS** 来取别名，否则输出的时候字段名为计算表达式。

```sql
SELECT col1*col2 AS alias
FROM mytable
```

**CONCAT()** 用于连接两个字段。许多数据库会使用空格把一个值填充为列宽，因此连接的结果会出现一些不必要的空格，使用 **TRIM()** 可以去除首尾空格。

```sql
SELECT CONCAT(TRIM(col1), ' (', TRIM(col2), ')')
FROM mytable
```

## 函数

各个 DBMS 的函数都是不相同的，因此不可移植。

### 文本处理

|      函数       |          说明          |
| :-------------: | :--------------------: |
| LEFT() RIGHT()  |   左边或者右边的字符   |
| LOWER() UPPER() |   转换为小写或者大写   |
| LTRIM() RTIM()  | 去除左边或者右边的空格 |
|    LENGTH()     |          长度          |
|    SOUNDEX()    |      转换为语音值      |

其中， **SOUNDEX()** 可以将一个字符串转换为描述其语音表示的字母数字模式。

```sql
SELECT *
FROM mytable
WHERE SOUNDEX(col1) = SOUNDEX('apple')
```

### 日期和时间处理

* 日期格式：YYYY-MM-DD
* 时间格式：HH:MM:SS

|     函 数     |             说 明              |
| :-----------: | :----------------------------: |
|   AddDate()   |    增加一个日期（天、周等）    |
|   AddTime()   |    增加一个时间（时、分等）    |
|   CurDate()   |          返回当前日期          |
|   CurTime()   |          返回当前时间          |
|    Date()     |     返回日期时间的日期部分     |
|  DateDiff()   |        计算两个日期之差        |
|  Date_Add()   |     高度灵活的日期运算函数     |
| Date_Format() |  返回一个格式化的日期或时间串  |
|     Day()     |     返回一个日期的天数部分     |
|  DayOfWeek()  | 对于一个日期，返回对应的星期几 |
|    Hour()     |     返回一个时间的小时部分     |
|   Minute()    |     返回一个时间的分钟部分     |
|    Month()    |     返回一个日期的月份部分     |
|     Now()     |       返回当前日期和时间       |
|   Second()    |      返回一个时间的秒部分      |
|    Time()     |   返回一个日期时间的时间部分   |
|    Year()     |     返回一个日期的年份部分     |

```sql
mysql> SELECT NOW();
```

```
2018-4-14 20:25:11
```

### 数值处理

|  函数  |  说明  |
| :----: | :----: |
| SIN()  |  正弦  |
| COS()  |  余弦  |
| TAN()  |  正切  |
| ABS()  | 绝对值 |
| SQRT() | 平方根 |
| MOD()  |  余数  |
| EXP()  |  指数  |
|  PI()  | 圆周率 |
| RAND() | 随机数 |

### 汇总

|  函 数  |      说 明       |
| :-----: | :--------------: |
|  AVG()  | 返回某列的平均值 |
| COUNT() |  返回某列的行数  |
|  MAX()  | 返回某列的最大值 |
|  MIN()  | 返回某列的最小值 |
|  SUM()  |  返回某列值之和  |

AVG() 会忽略 NULL 行。

使用 DISTINCT 可以让汇总函数值汇总不同的值。

```sql
SELECT AVG(DISTINCT col1) AS avg_col
FROM mytable
```

## 分组

分组就是把具有相同的数据值的行放在同一组中。

可以对同一分组数据使用汇总函数进行处理，例如求分组数据的平均值等。

指定的分组字段除了能按该字段进行分组，也会自动按按该字段进行排序。

```sql
SELECT col, COUNT(*) AS num
FROM mytable
GROUP BY col;
```

GROUP BY 按分组字段进行排序，ORDER BY 也可以以汇总字段来进行排序。

```sql
SELECT col, COUNT(*) AS num
FROM mytable
GROUP BY col
ORDER BY num;
```

WHERE 过滤行，HAVING 过滤分组，行过滤应当先于分组过滤。

```sql
SELECT col, COUNT(*) AS num
FROM mytable
WHERE col > 2
GROUP BY col
HAVING num >= 2;
```

分组规定：

1.  GROUP BY 子句出现在 WHERE 子句之后，ORDER BY 子句之前；
2.  除了汇总字段外，SELECT 语句中的每一字段都必须在 GROUP BY 子句中给出；
3.  NULL 的行会单独分为一组；
4.  大多数 SQL 实现不支持 GROUP BY 列具有可变长度的数据类型。

## 子查询

子查询中只能返回一个字段的数据。

可以将子查询的结果作为 WHRER 语句的过滤条件：

```sql
SELECT *
FROM mytable1
WHERE col1 IN (SELECT col2
               FROM mytable2);
```

下面的语句可以检索出客户的订单数量，子查询语句会对第一个查询检索出的每个客户执行一次：

```sql
SELECT cust_name, (SELECT COUNT(*)
                   FROM Orders
                   WHERE Orders.cust_id = Customers.cust_id)
                   AS orders_num
FROM Customers
ORDER BY cust_name;
```

## 连接

连接用于连接多个表，使用 JOIN 关键字，并且条件语句使用 ON 而不是 WHERE。

连接可以替换子查询，并且比子查询的效率一般会更快。

可以用 AS 给列名、计算字段和表名取别名，给表名取别名是为了简化 SQL 语句以及连接相同表。

### 内连接

内连接又称等值连接，使用 INNER JOIN 关键字。

```sql
select a, b, c
from A inner join B
on A.key = B.key
```

可以不明确使用 INNER JOIN，而使用普通查询并在 WHERE 中将两个表中要连接的列用等值方法连接起来。

```sql
select a, b, c
from A, B
where A.key = B.key
```

在没有条件语句的情况下返回笛卡尔积。

### 自连接

自连接可以看成内连接的一种，只是连接的表是自身而已。

一张员工表，包含员工姓名和员工所属部门，要找出与 Jim 处在同一部门的所有员工姓名。

子查询版本

```sql
select name
from employee
where department = (
      select department
      from employee
      where name = "Jim");
```

自连接版本

```sql
select e1.name
from employee as e1, employee as e2
where e1.department = e2.department
      and e2.name = "Jim";
```

连接一般比子查询的效率高。

### 自然连接

自然连接是把同名列通过等值测试连接起来的，同名列可以有多个。

内连接和自然连接的区别：内连接提供连接的列，而自然连接自动连接所有同名列。

```sql
select *
from employee natural join department;
```

### 外连接

外连接保留了没有关联的那些行。分为左外连接，右外连接以及全外连接，左外连接就是保留左表没有关联的行。

检索所有顾客的订单信息，包括还没有订单信息的顾客。

```sql
select Customers.cust_id, Orders.order_num
from Customers left outer join Orders
on Customers.cust_id = Orders.cust_id;
```

如果需要统计顾客的订单数，使用聚集函数。

```sql
select Customers.cust_id,
       COUNT(Orders.order_num) as num_ord
from Customers left outer join Orders
on Customers.cust_id = Orders.cust_id
group by Customers.cust_id;
```

## 组合查询

使用 **UNION** 来组合两个查询，如果第一个查询返回 M 行，第二个查询返回 N 行，那么组合查询的结果一般为 M+N 行。

每个查询必须包含相同的列、表达式和聚集函数。

默认会去除相同行，如果需要保留相同行，使用 UNION ALL。

只能包含一个 ORDER BY 子句，并且必须位于语句的最后。

```sql
SELECT col
FROM mytable
WHERE col = 1
UNION
SELECT col
FROM mytable
WHERE col =2;
```

## 视图

视图是虚拟的表，本身不包含数据，也就不能对其进行索引操作。对视图的操作和对普通表的操作一样。

视图具有如下好处：

1.  简化复杂的 SQL 操作，比如复杂的联结；
2.  只使用实际表的一部分数据；
3.  通过只给用户访问视图的权限，保证数据的安全性；
4.  更改数据格式和表示。

```sql
CREATE VIEW myview AS
SELECT Concat(col1, col2) AS concat_col, col3*col4 AS count_col
FROM mytable
WHERE col5 = val;
```

## 存储过程

存储过程可以看成是对一系列 SQL 操作的批处理；

### 使用存储过程的好处

1.  代码封装，保证了一定的安全性；
2.  代码复用；
3.  由于是预先编译，因此具有很高的性能。

### 创建存储过程

命令行中创建存储过程需要自定义分隔符，因为命令行是以 ; 为结束符，而存储过程中也包含了分号，因此会错误把这部分分号当成是结束符，造成语法错误。

包含 in、out 和 inout 三种参数。

给变量赋值都需要用 select into 语句。

每次只能给一个变量赋值，不支持集合的操作。

```sql
delimiter //

create procedure myprocedure( out ret int )
    begin
        declare y int;
        select sum(col1)
        from mytable
        into y;
        select y*y into ret;
    end //
delimiter ;
```

```sql
call myprocedure(@ret);
select @ret;
```

## 游标

在存储过程中使用游标可以对一个结果集进行移动遍历。

游标主要用于交互式应用，其中用户需要对数据集中的任意行进行浏览和修改。

使用游标的四个步骤：

1.  声明游标，这个过程没有实际检索出数据；
2.  打开游标；
3.  取出数据；
4.  关闭游标；

```sql
delimiter //
create procedure myprocedure(out ret int)
    begin
        declare done boolean default 0;

        declare mycursor cursor for
        select col1 from mytable;
        ## 定义了一个continue handler，当 sqlstate '02000' 这个条件出现时，会执行 set done = 1
        declare continue handler for sqlstate '02000' set done = 1;

        open mycursor;

        repeat
            fetch mycursor into ret;
            select ret;
        until done end repeat;

        close mycursor;
    end //
 delimiter ;
```

## 触发器

### 指令

#### 创建触发器

> 提示：为了理解触发器的要点，有必要先了解一下创建触发器的指令。

`CREATE TRIGGER` 指令用于创建触发器。

语法：

```sql
CREATE TRIGGER trigger_name
trigger_time
trigger_event
ON table_name
FOR EACH ROW
BEGIN
  trigger_statements
END;
```

说明：

* trigger_name：触发器名
* trigger_time: 触发器的触发时机。取值为 `BEFORE` 或 `AFTER`。
* trigger_event: 触发器的监听事件。取值为 `INSERT`、`UPDATE` 或 `DELETE`。
* table_name: 触发器的监听目标。指定在哪张表上建立触发器。
* FOR EACH ROW: 行级监视，Mysql 固定写法，其他 DBMS 不同。
* trigger_statements: 触发器执行动作。是一条或多条 SQL 语句的列表，列表内的每条语句都必须用分号 `;` 来结尾。

示例：

```sql
DELIMITER $
CREATE TRIGGER `trigger_insert_user`
AFTER INSERT ON `user`
FOR EACH ROW
BEGIN
    INSERT INTO `user_history`(user_id, operate_type, operate_time)
    VALUES (NEW.id, 'add a user',  now());
END $
DELIMITER ;
```

#### 查看触发器

```sql
SHOW TRIGGERS [FROM schema_name];
```

#### 删除触发器

```sql
DROP TRIGGER [IF EXISTS] [schema_name.]trigger_name
```

### 要点

触发器是一种与表操作有关的数据库对象，当触发器所在表上出现指定事件时，将调用该对象，即表的操作事件触发表上的触发器的执行。

可以使用触发器来进行审计跟踪，把修改记录到另外一张表中。

MySQL 不允许在触发器中使用 CALL 语句 ，也就是不能调用存储过程。

#### `BEGIN` 和 `END`

当触发器的触发条件满足时，将会执行 BEGIN 和 END 之间的触发器执行动作。

> 注意：在 MySQL 中，分号 `;` 是语句结束的标识符，遇到分号表示该段语句已经结束，MySQL 可以开始执行了。因此，解释器遇到触发器执行动作中的分号后就开始执行，然后会报错，因为没有找到和 BEGIN 匹配的 END。
>
> 这时就会用到 `DELIMITER` 命令（DELIMITER 是定界符，分隔符的意思）。它是一条命令，不需要语句结束标识，语法为：`DELIMITER new_delemiter`。`new_delemiter` 可以设为 1 个或多个长度的符号，默认的是分号 `;`，我们可以把它修改为其他符号，如 `$`：`DELIMITER $` 。在这之后的语句，以分号结束，解释器不会有什么反应，只有遇到了 `$`，才认为是语句结束。注意，使用完之后，我们还应该记得把它给修改回来。

#### `NEW` 和 `OLD`

在指令一节的示例中，使用了 `NEW` 关键字。

MySQL 中定义了 `NEW` 和 `OLD` 关键字，用来表示触发器的所在表中，触发了触发器的那一行数据。

具体地：

* 在 `INSERT` 型触发器中，`NEW` 用来表示将要（`BEFORE`）或已经（`AFTER`）插入的新数据；
* 在 `UPDATE` 型触发器中，`OLD` 用来表示将要或已经被修改的原数据，`NEW` 用来表示将要或已经修改为的新数据；
* 在 `DELETE` 型触发器中，`OLD` 用来表示将要或已经被删除的原数据；

使用方法： `NEW.columnName` （columnName 为相应数据表某一列名）

## 事务处理

### 要点

不能回退 SELECT 语句，回退 SELECT 语句也没意义；也不能回退 CREATE 和 DROP 语句。

**MySQL 默认是隐式提交**，每执行一条语句就把这条语句当成一个事务然后进行提交。当出现 `START TRANSACTION` 语句时，会关闭隐式提交；当 `COMMIT` 或 `ROLLBACK` 语句执行后，事务会自动关闭，重新恢复隐式提交。

通过 `set autocommit=0` 可以取消自动提交，直到 `set autocommit=1` 才会提交；autocommit 标记是针对每个连接而不是针对服务器的。

### 指令

* `START TRANSACTION`：指令用于标记事务的起始点。
* `SAVEPOINT`：指令用于创建保留点。
* `ROLLBACK TO`：指令用于回滚到指定的保留点；如果没有设置保留点，则回退到 `START TRANSACTION` 语句处。
* `COMMIT`：提交事务。

完整示例：

在下面的示例中，只有第一条 `INSERT INTO` 语句生效。

```sql
-- 开始事务
START TRANSACTION;

-- 插入操作 A
INSERT INTO `user`
VALUES (1, 'root1', 'root1', 'xxxx@163.com');

-- 创建保留点 updateA
SAVEPOINT updateA;

-- 插入操作 B
INSERT INTO `user`
VALUES (2, 'root2', 'root2', 'xxxx@163.com');

-- 回滚到保留点 updateA
ROLLBACK TO updateA;

-- 提交事务，只有操作 A 生效
COMMIT;
```

## 字符集

基本术语：

1.  字符集为字母和符号的集合；
2.  编码为某个字符集成员的内部表示；
3.  校对字符指定如何比较，主要用于排序和分组。

除了给表指定字符集和校对外，也可以给列指定：

```sql
CREATE TABLE mytable
(col VARCHAR(10) CHARACTER SET latin COLLATE latin1_general_ci )
DEFAULT CHARACTER SET hebrew COLLATE hebrew_general_ci;
```

可以在排序、分组时指定校对：

```sql
SELECT *
FROM mytable
ORDER BY col COLLATE latin1_general_ci;
```

## 权限管理

MySQL 的账户信息保存在 mysql 这个数据库中。

```sql
USE mysql;
SELECT user FROM user;
```

### 创建账户

```sql
CREATE USER myuser IDENTIFIED BY 'mypassword';
```

新创建的账户没有任何权限。

### 修改账户名

```sql
RENAME myuser TO newuser;
```

### 删除账户

```sql
DROP USER myuser;
```

### 查看权限

```sql
SHOW GRANTS FOR myuser;
```

### 授予权限

```sql
GRANT SELECT, INSERT ON mydatabase.* TO myuser;
```

账户用 username@host 的形式定义，username@% 使用的是默认主机名。

### 删除权限

```sql
REVOKE SELECT, INSERT ON mydatabase.* FROM myuser;
```

GRANT 和 REVOKE 可在几个层次上控制访问权限：

* 整个服务器，使用 GRANT ALL 和 REVOKE ALL；
* 整个数据库，使用 ON database.\*；
* 特定的表，使用 ON database.table；
* 特定的列；
* 特定的存储过程。

### 更改密码

必须使用 Password() 函数

```sql
SET PASSWROD FOR myuser = Password('newpassword');
```

## 参考资料

* BenForta. SQL 必知必会 [M]. 人民邮电出版社, 2013.
* [『浅入深出』MySQL 中事务的实现](https://draveness.me/mysql-transaction)
* [MySQL 的学习--触发器](https://www.cnblogs.com/CraryPrimitiveMan/p/4206942.html)
