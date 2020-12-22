# Mysql 性能优化

<!-- TOC depthFrom:2 depthTo:3 -->

- [1. 数据结构优化](#1-数据结构优化)
  - [1.1. 数据类型优化](#11-数据类型优化)
  - [1.2. 表设计](#12-表设计)
  - [1.3. 范式和反范式](#13-范式和反范式)
  - [1.4. 索引优化](#14-索引优化)
- [2. SQL 优化](#2-sql-优化)
  - [2.1. 优化 `COUNT()` 查询](#21-优化-count-查询)
  - [2.2. 优化关联查询](#22-优化关联查询)
  - [2.3. 优化 `GROUP BY` 和 `DISTINCT`](#23-优化-group-by-和-distinct)
  - [2.4. 优化 `LIMIT`](#24-优化-limit)
  - [2.5. 优化 UNION](#25-优化-union)
  - [2.6. 优化查询方式](#26-优化查询方式)
- [3. 执行计划（`EXPLAIN`）](#3-执行计划explain)
- [4. optimizer trace](#4-optimizer-trace)
- [5. 数据模型和业务](#5-数据模型和业务)
- [6. 参考资料](#6-参考资料)

<!-- /TOC -->

## 1. 数据结构优化

良好的逻辑设计和物理设计是高性能的基石。

### 1.1. 数据类型优化

#### 数据类型优化基本原则

- **更小的通常更好** - 越小的数据类型通常会更快，占用更少的磁盘、内存，处理时需要的 CPU 周期也更少。
  - 例如：整型比字符类型操作代价低，因而会使用整型来存储 IP 地址，使用 `DATETIME` 来存储时间，而不是使用字符串。
- **简单就好** - 如整型比字符型操作代价低。
  - 例如：很多软件会用整型来存储 IP 地址。
  - 例如：**`UNSIGNED` 表示不允许负值，大致可以使正数的上限提高一倍**。
- **尽量避免 NULL** - 可为 NULL 的列会使得索引、索引统计和值比较都更复杂。

#### 类型的选择

- 整数类型通常是标识列最好的选择，因为它们很快并且可以使用 `AUTO_INCREMENT`。

- `ENUM` 和 `SET` 类型通常是一个糟糕的选择，应尽量避免。
- 应该尽量避免用字符串类型作为标识列，因为它们很消耗空间，并且通常比数字类型慢。对于 `MD5`、`SHA`、`UUID` 这类随机字符串，由于比较随机，所以可能分布在很大的空间内，导致 `INSERT` 以及一些 `SELECT` 语句变得很慢。
  - 如果存储 UUID ，应该移除 `-` 符号；更好的做法是，用 `UNHEX()` 函数转换 UUID 值为 16 字节的数字，并存储在一个 `BINARY(16)` 的列中，检索时，可以通过 `HEX()` 函数来格式化为 16 进制格式。

### 1.2. 表设计

应该避免的设计问题：

- **太多的列** - 设计者为了图方便，将大量冗余列加入表中，实际查询中，表中很多列是用不到的。这种宽表模式设计，会造成不小的性能代价，尤其是 `ALTER TABLE` 非常耗时。
- **太多的关联** - 所谓的实体 - 属性 - 值（EVA）设计模式是一个常见的糟糕设计模式。Mysql 限制了每个关联操作最多只能有 61 张表，但 EVA 模式需要许多自关联。
- **枚举** - 尽量不要用枚举，因为添加和删除字符串（枚举选项）必须使用 `ALTER TABLE`。
- 尽量避免 `NULL`

### 1.3. 范式和反范式

**范式化目标是尽量减少冗余，而反范式化则相反**。

范式化的优点：

- 比反范式更节省空间
- 更新操作比反范式快
- 更少需要 `DISTINCT` 或 `GROUP BY` 语句

范式化的缺点：

- 通常需要关联查询。而关联查询代价较高，如果是分表的关联查询，代价更是高昂。

在真实世界中，很少会极端地使用范式化或反范式化。实际上，应该权衡范式和反范式的利弊，混合使用。

### 1.4. 索引优化

> 索引优化应该是查询性能优化的最有效手段。
>
> 如果想详细了解索引特性请参考：[Mysql 索引](https://github.com/dunwu/db-tutorial/blob/master/docs/sql/mysql/mysql-index.md)

#### 何时使用索引

- 对于非常小的表，大部分情况下简单的全表扫描更高效。
- 对于中、大型表，索引非常有效。
- 对于特大型表，建立和使用索引的代价将随之增长。可以考虑使用分区技术。
- 如果表的数量特别多，可以建立一个元数据信息表，用来查询需要用到的某些特性。

#### 索引优化策略

- **索引基本原则**
  - 索引不是越多越好，不要为所有列都创建索引。
  - 要尽量避免冗余和重复索引。
  - 要考虑删除未使用的索引。
  - 尽量的扩展索引，不要新建索引。
  - 频繁作为 `WHERE` 过滤条件的列应该考虑添加索引。
- **独立的列** - “独立的列” 是指索引列不能是表达式的一部分，也不能是函数的参数。
- **前缀索引** - 索引很长的字符列，可以索引开始的部分字符，这样可以大大节约索引空间。
- **最左匹配原则** - 将选择性高的列或基数大的列优先排在多列索引最前列。
- **使用索引来排序** - 索引最好既满足排序，又用于查找行。这样，就可以使用索引来对结果排序。
- `=`、`IN` 可以乱序 - 不需要考虑 `=`、`IN` 等的顺序
- **覆盖索引**
- **自增字段作主键**

## 2. SQL 优化

使用 `EXPLAIN` 命令查看当前 SQL 是否使用了索引，优化后，再通过执行计划（`EXPLAIN`）来查看优化效果。

SQL 优化基本思路：

- **只返回必要的列** - 最好不要使用 `SELECT *` 语句。

- **只返回必要的行** - 使用 `WHERE` 子查询语句进行过滤查询，有时候也需要使用 `LIMIT` 语句来限制返回的数据。

- **缓存重复查询的数据** - 应该考虑在客户端使用缓存，尽量不要使用 Mysql 服务器缓存（存在较多问题和限制）。

- **使用索引来覆盖查询**

### 2.1. 优化 `COUNT()` 查询

`COUNT()` 有两种作用：

- 统计某个列值的数量。统计列值时，要求列值是非 `NULL` 的，它不会统计 `NULL`。
- 统计行数。

**统计列值时，要求列值是非空的，它不会统计 NULL**。如果确认括号中的表达式不可能为空时，实际上就是在统计行数。最简单的就是当使用 `COUNT(*)` 时，并不是我们所想象的那样扩展成所有的列，实际上，它会忽略所有的列而直接统计行数。

我们最常见的误解也就在这儿，在括号内指定了一列却希望统计结果是行数，而且还常常误以为前者的性能会更好。但实际并非这样，如果要统计行数，直接使用 `COUNT(*)`，意义清晰，且性能更好。

（1）简单优化

```sql
SELECT count(*) FROM world.city WHERE id > 5;

SELECT (SELECT count(*) FROM world.city) - count(*)
FROM world.city WHERE id <= 5;
```

（2）使用近似值

有时候某些业务场景并不需要完全精确的统计值，可以用近似值来代替，`EXPLAIN` 出来的行数就是一个不错的近似值，而且执行 `EXPLAIN` 并不需要真正地去执行查询，所以成本非常低。通常来说，执行 `COUNT()` 都需要扫描大量的行才能获取到精确的数据，因此很难优化，MySQL 层面还能做得也就只有覆盖索引了。如果不还能解决问题，只有从架构层面解决了，比如添加汇总表，或者使用 Redis 这样的外部缓存系统。

### 2.2. 优化关联查询

在大数据场景下，表与表之间通过一个冗余字段来关联，要比直接使用 `JOIN` 有更好的性能。

如果确实需要使用关联查询的情况下，需要特别注意的是：

- **确保 `ON` 和 `USING` 字句中的列上有索引**。在创建索引的时候就要考虑到关联的顺序。当表 A 和表 B 用某列 column 关联的时候，如果优化器关联的顺序是 A、B，那么就不需要在 A 表的对应列上创建索引。没有用到的索引会带来额外的负担，一般来说，除非有其他理由，只需要在关联顺序中的第二张表的相应列上创建索引（具体原因下文分析）。
- **确保任何的 `GROUP BY` 和 `ORDER BY` 中的表达式只涉及到一个表中的列**，这样 MySQL 才有可能使用索引来优化。

要理解优化关联查询的第一个技巧，就需要理解 MySQL 是如何执行关联查询的。当前 MySQL 关联执行的策略非常简单，它对任何的关联都执行**嵌套循环关联**操作，即先在一个表中循环取出单条数据，然后在嵌套循环到下一个表中寻找匹配的行，依次下去，直到找到所有表中匹配的行为为止。然后根据各个表匹配的行，返回查询中需要的各个列。

太抽象了？以上面的示例来说明，比如有这样的一个查询：

```css
SELECT A.xx,B.yy
FROM A INNER JOIN B USING(c)
WHERE A.xx IN (5,6)
```

假设 MySQL 按照查询中的关联顺序 A、B 来进行关联操作，那么可以用下面的伪代码表示 MySQL 如何完成这个查询：

```ruby
outer_iterator = SELECT A.xx,A.c FROM A WHERE A.xx IN (5,6);
outer_row = outer_iterator.next;
while(outer_row) {
    inner_iterator = SELECT B.yy FROM B WHERE B.c = outer_row.c;
    inner_row = inner_iterator.next;
    while(inner_row) {
        output[inner_row.yy,outer_row.xx];
        inner_row = inner_iterator.next;
    }
    outer_row = outer_iterator.next;
}
```

可以看到，最外层的查询是根据`A.xx`列来查询的，`A.c`上如果有索引的话，整个关联查询也不会使用。再看内层的查询，很明显`B.c`上如果有索引的话，能够加速查询，因此只需要在关联顺序中的第二张表的相应列上创建索引即可。

### 2.3. 优化 `GROUP BY` 和 `DISTINCT`

Mysql 优化器会在内部处理的时候相互转化这两类查询。它们都**可以使用索引来优化，这也是最有效的优化方法**。

### 2.4. 优化 `LIMIT`

当需要分页操作时，通常会使用 `LIMIT` 加上偏移量的办法实现，同时加上合适的 `ORDER BY` 字句。**如果有对应的索引，通常效率会不错，否则，MySQL 需要做大量的文件排序操作**。

一个常见的问题是当偏移量非常大的时候，比如：`LIMIT 10000 20`这样的查询，MySQL 需要查询 10020 条记录然后只返回 20 条记录，前面的 10000 条都将被抛弃，这样的代价非常高。

优化这种查询一个最简单的办法就是尽可能的使用覆盖索引扫描，而不是查询所有的列。然后根据需要做一次关联查询再返回所有的列。对于偏移量很大时，这样做的效率会提升非常大。考虑下面的查询：

```sql
SELECT film_id,description FROM film ORDER BY title LIMIT 50,5;
```

如果这张表非常大，那么这个查询最好改成下面的样子：

```sql
SELECT film.film_id,film.description
FROM film INNER JOIN (
    SELECT film_id FROM film ORDER BY title LIMIT 50,5
) AS tmp USING(film_id);
```

这里的延迟关联将大大提升查询效率，让 MySQL 扫描尽可能少的页面，获取需要访问的记录后在根据关联列回原表查询所需要的列。

有时候如果可以使用书签记录上次取数据的位置，那么下次就可以直接从该书签记录的位置开始扫描，这样就可以避免使用`OFFSET`，比如下面的查询：

```objectivec
SELECT id FROM t LIMIT 10000, 10;
改为：
SELECT id FROM t WHERE id > 10000 LIMIT 10;
```

其他优化的办法还包括使用预先计算的汇总表，或者关联到一个冗余表，冗余表中只包含主键列和需要做排序的列。

### 2.5. 优化 UNION

MySQL 总是通过创建并填充临时表的方式来执行 `UNION` 查询。因此很多优化策略在`UNION`查询中都没有办法很好的时候。经常需要手动将`WHERE`、`LIMIT`、`ORDER BY`等字句“下推”到各个子查询中，以便优化器可以充分利用这些条件先优化。

除非确实需要服务器去重，否则就一定要使用`UNION ALL`，如果没有`ALL`关键字，MySQL 会给临时表加上`DISTINCT`选项，这会导致整个临时表的数据做唯一性检查，这样做的代价非常高。当然即使使用 ALL 关键字，MySQL 总是将结果放入临时表，然后再读出，再返回给客户端。虽然很多时候没有这个必要，比如有时候可以直接把每个子查询的结果返回给客户端。

### 2.6. 优化查询方式

#### 切分大查询

一个大查询如果一次性执行的话，可能一次锁住很多数据、占满整个事务日志、耗尽系统资源、阻塞很多小的但重要的查询。

```sql
DELEFT FROM messages WHERE create < DATE_SUB(NOW(), INTERVAL 3 MONTH);
```

```sql
rows_affected = 0
do {
    rows_affected = do_query(
    "DELETE FROM messages WHERE create  < DATE_SUB(NOW(), INTERVAL 3 MONTH) LIMIT 10000")
} while rows_affected > 0
```

#### 分解大连接查询

将一个大连接查询（JOIN）分解成对每一个表进行一次单表查询，然后将结果在应用程序中进行关联，这样做的好处有：

- 让缓存更高效。对于连接查询，如果其中一个表发生变化，那么整个查询缓存就无法使用。而分解后的多个查询，即使其中一个表发生变化，对其它表的查询缓存依然可以使用。
- 分解成多个单表查询，这些单表查询的缓存结果更可能被其它查询使用到，从而减少冗余记录的查询。
- 减少锁竞争；
- 在应用层进行连接，可以更容易对数据库进行拆分，从而更容易做到高性能和可扩展。
- 查询本身效率也可能会有所提升。例如下面的例子中，使用 IN() 代替连接查询，可以让 MySQL 按照 ID 顺序进行查询，这可能比随机的连接要更高效。

```sql
SELECT * FROM tag
JOIN tag_post ON tag_post.tag_id=tag.id
JOIN post ON tag_post.post_id=post.id
WHERE tag.tag='mysql';
```

```sql
SELECT * FROM tag WHERE tag='mysql';
SELECT * FROM tag_post WHERE tag_id=1234;
SELECT * FROM post WHERE post.id IN (123,456,567,9098,8904);
```

## 3. 执行计划（`EXPLAIN`）

如何判断当前 SQL 是否使用了索引？如何检验修改后的 SQL 确实有优化效果？

在 SQL 中，可以通过执行计划（`EXPLAIN`）分析 `SELECT` 查询效率。

```sql
mysql> explain select * from user_info where id = 2\G
*************************** 1. row ***************************
           id: 1
  select_type: SIMPLE
        table: user_info
   partitions: NULL
         type: const
possible_keys: PRIMARY
          key: PRIMARY
      key_len: 8
          ref: const
         rows: 1
     filtered: 100.00
        Extra: NULL
1 row in set, 1 warning (0.00 sec)
```

`EXPLAIN` 参数说明：

- `id`: SELECT 查询的标识符. 每个 SELECT 都会自动分配一个唯一的标识符.
- `select_type` ⭐ ：SELECT 查询的类型.
  - `SIMPLE`：表示此查询不包含 UNION 查询或子查询
  - `PRIMARY`：表示此查询是最外层的查询
  - `UNION`：表示此查询是 UNION 的第二或随后的查询
  - `DEPENDENT UNION`：UNION 中的第二个或后面的查询语句, 取决于外面的查询
  - `UNION RESULT`：UNION 的结果
  - `SUBQUERY`：子查询中的第一个 SELECT
  - `DEPENDENT SUBQUERY`: 子查询中的第一个 SELECT, 取决于外面的查询. 即子查询依赖于外层查询的结果.
- `table`: 查询的是哪个表，如果给表起别名了，则显示别名。
- `partitions`：匹配的分区
- `type` ⭐：表示从表中查询到行所执行的方式，查询方式是 SQL 优化中一个很重要的指标，结果值从好到差依次是：system > const > eq_ref > ref > range > index > ALL。
  - `system`/`const`：表中只有一行数据匹配，此时根据索引查询一次就能找到对应的数据。如果是 B + 树索引，我们知道此时索引构造成了多个层级的树，当查询的索引在树的底层时，查询效率就越低。const 表示此时索引在第一层，只需访问一层便能得到数据。
  - `eq_ref`：使用唯一索引扫描，常见于多表连接中使用主键和唯一索引作为关联条件。
  - `ref`：非唯一索引扫描，还可见于唯一索引最左原则匹配扫描。
  - `range`：索引范围扫描，比如，<，>，between 等操作。
  - `index`：索引全表扫描，此时遍历整个索引树。
  - `ALL`：表示全表扫描，需要遍历全表来找到对应的行。
- `possible_keys`：此次查询中可能选用的索引。
- `key` ⭐：此次查询中实际使用的索引。
- `ref`：哪个字段或常数与 key 一起被使用。
- `rows` ⭐：显示此查询一共扫描了多少行，这个是一个估计值。
- `filtered`：表示此查询条件所过滤的数据的百分比。
- `extra`：额外的信息。

> 更多内容请参考：[MySQL 性能优化神器 Explain 使用分析](https://segmentfault.com/a/1190000008131735)

## 4. optimizer trace

在 MySQL 5.6 及之后的版本中，我们可以使用 optimizer trace 功能查看优化器生成执行计划的整个过程。有了这个功能，我们不仅可以了解优化器的选择过程，更可以了解每一个执行环节的成本，然后依靠这些信息进一步优化查询。

如下代码所示，打开 optimizer_trace 后，再执行 SQL 就可以查询 information_schema.OPTIMIZER_TRACE 表查看执行计划了，最后可以关闭 optimizer_trace 功能：

```sql
SET optimizer_trace="enabled=on";
SELECT * FROM person WHERE NAME >'name84059' AND create_time>'2020-01-24 05:00
SELECT * FROM information_schema.OPTIMIZER_TRACE;
SET optimizer_trace="enabled=off";
```

## 5. 数据模型和业务

- 表字段比较复杂、易变动、结构难以统一的情况下，可以考虑使用 Nosql 来代替关系数据库表存储，如 ElasticSearch、MongoDB。
- 在高并发情况下的查询操作，可以使用缓存（如 Redis）代替数据库操作，提高并发性能。
- 数据量增长较快的表，需要考虑水平分表或分库，避免单表操作的性能瓶颈。
- 除此之外，我们应该通过一些优化，尽量避免比较复杂的 JOIN 查询操作，例如冗余一些字段，减少 JOIN 查询；创建一些中间表，减少 JOIN 查询。

## 6. 参考资料

- [《高性能 MySQL》](https://book.douban.com/subject/23008813/)
- [《Java 性能调优实战》](https://time.geekbang.org/column/intro/100028001)
- [我必须得告诉大家的 MySQL 优化原理](https://www.jianshu.com/p/d7665192aaaf)
- [20+ 条 MySQL 性能优化的最佳经验](https://www.jfox.info/20-tiao-mysql-xing-nen-you-hua-de-zui-jia-jing-yan.html)
- [MySQL 性能优化神器 Explain 使用分析](https://segmentfault.com/a/1190000008131735)
