# Mysql 事务

## 一、事务简介

> 事务简单来说：**一个 Session 中所进行所有的操作，要么同时成功，要么同时失败**。进一步说，事务指的是满足 ACID 特性的一组操作，可以通过 `Commit` 提交一个事务，也可以使用 `Rollback` 进行回滚。

![img](http://dunwu.test.upcdn.net/cs/database/RDB/数据库事务.png)

**事务就是一组原子性的 SQL 语句**。具体来说，事务指的是满足 ACID 特性的一组操作。

**事务内的 SQL 语句，要么全执行成功，要么全执行失败**。

想象一下，如果没有事务，在并发环境下，就可能出现丢失修改的问题。

T<sub>1</sub> 和 T<sub>2</sub> 两个线程都对一个数据进行修改，T<sub>1</sub> 先修改，T<sub>2</sub> 随后修改，T<sub>2</sub> 的修改覆盖了 T<sub>1</sub> 的修改。

![img](http://dunwu.test.upcdn.net/cs/database/RDB/数据库并发一致性-丢失修改.png)

不是所有的 Mysql 存储引擎都实现了事务处理。支持事务的存储引擎有：`InnoDB` 和 `NDB Cluster`。

用户可以根据业务是否需要事务处理（事务处理可以保证数据安全，但会增加系统开销），选择合适的存储引擎。

## 二、事务用法

### 事务处理指令

Mysql 中，使用 `START TRANSACTION` 语句开始一个事务；使用 `COMMIT` 语句提交所有的修改；使用 `ROLLBACK` 语句撤销所有的修改。不能回退 `SELECT` 语句，回退 `SELECT` 语句也没意义；也不能回退 `CREATE` 和 `DROP` 语句。

- `START TRANSACTION` - 指令用于标记事务的起始点。
- `SAVEPOINT` - 指令用于创建保留点。
- `ROLLBACK TO` - 指令用于回滚到指定的保留点；如果没有设置保留点，则回退到 `START TRANSACTION` 语句处。
- `COMMIT` - 提交事务。

事务处理示例：

（1）创建一张示例表

```sql
-- 撤销表 user
DROP TABLE IF EXISTS user;

-- 创建表 user
CREATE TABLE user (
  id int(10) unsigned NOT NULL COMMENT 'Id',
  username varchar(64) NOT NULL DEFAULT 'default' COMMENT '用户名',
  password varchar(64) NOT NULL DEFAULT 'default' COMMENT '密码',
  email varchar(64) NOT NULL DEFAULT 'default' COMMENT '邮箱'
) COMMENT='用户表';
```

（2）执行事务操作

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

（3）执行结果

```sql
SELECT * FROM user;
```

结果：

```
1	root1	root1	xxxx@163.com
```

### AUTOCOMMIT

**MySQL 默认采用隐式提交策略（`autocommit`）**。每执行一条语句就把这条语句当成一个事务然后进行提交。当出现 `START TRANSACTION` 语句时，会关闭隐式提交；当 `COMMIT` 或 `ROLLBACK` 语句执行后，事务会自动关闭，重新恢复隐式提交。

通过 `set autocommit=0` 可以取消自动提交，直到 `set autocommit=1` 才会提交；`autocommit` 标记是针对每个连接而不是针对服务器的。

```sql
-- 查看 AUTOCOMMIT
SHOW VARIABLES LIKE 'AUTOCOMMIT';

-- 关闭 AUTOCOMMIT
SET autocommit = 0;

-- 开启 AUTOCOMMIT
SET autocommit = 1;
```

## 三、ACID

ACID 是数据库事务正确执行的四个基本要素。

- **原子性（Atomicity）**
  - 事务被视为不可分割的最小单元，事务中的所有操作要么全部提交成功，要么全部失败回滚。
  - 回滚可以用日志来实现，日志记录着事务所执行的修改操作，在回滚时反向执行这些修改操作即可。
- **一致性（Consistency）**
  - 数据库在事务执行前后都保持一致性状态。
  - 在一致性状态下，所有事务对一个数据的读取结果都是相同的。
- **隔离性（Isolation）**
  - 一个事务所做的修改在最终提交以前，对其它事务是不可见的。
- **持久性（Durability）**
  - 一旦事务提交，则其所做的修改将会永远保存到数据库中。即使系统发生崩溃，事务执行的结果也不能丢失。
  - 可以通过数据库备份和恢复来实现，在系统发生奔溃时，使用备份的数据库进行数据恢复。

**一个支持事务（Transaction）中的数据库系统，必需要具有这四种特性，否则在事务过程（Transaction processing）当中无法保证数据的正确性，交易过程极可能达不到交易。**

- 只有满足一致性，事务的执行结果才是正确的。
- 在无并发的情况下，事务串行执行，隔离性一定能够满足。此时只要能满足原子性，就一定能满足一致性。
- 在并发的情况下，多个事务并行执行，事务不仅要满足原子性，还需要满足隔离性，才能满足一致性。
- 事务满足持久化是为了能应对系统崩溃的情况。

![img](http://dunwu.test.upcdn.net/cs/database/RDB/数据库ACID.png)

> MySQL 默认采用自动提交模式（`AUTO COMMIT`）。也就是说，如果不显式使用 `START TRANSACTION` 语句来开始一个事务，那么每个查询操作都会被当做一个事务并自动提交。

## 四、事务隔离级别

在并发环境下，事务的隔离性很难保证，因此会出现很多并发一致性问题。

在 SQL 标准中，定义了四种事务隔离级别（级别由低到高）：

- 未提交读
- 提交读
- 可重复读
- 串行化

Mysql 中查看和设置事务隔离级别：

```sql
-- 查看事务隔离级别
SHOW VARIABLES LIKE 'transaction_isolation';

-- 设置事务隔离级别为 READ UNCOMMITTED
SET SESSION TRANSACTION ISOLATION LEVEL READ UNCOMMITTED;

-- 设置事务隔离级别为 READ COMMITTED
SET SESSION TRANSACTION ISOLATION LEVEL READ COMMITTED;

-- 设置事务隔离级别为 REPEATABLE READ
SET SESSION TRANSACTION ISOLATION LEVEL REPEATABLE READ;

-- 设置事务隔离级别为 SERIALIZABLE
SET SESSION TRANSACTION ISOLATION LEVEL SERIALIZABLE;
```

### 未提交读

**`未提交读（READ UNCOMMITTED）` 是指：事务中的修改，即使没有提交，对其它事务也是可见的**。

未提交读的问题：事务可以读取未提交的数据，也被称为 **脏读（Dirty Read）**。

T<sub>1</sub> 修改一个数据，T<sub>2</sub> 随后读取这个数据。如果 T<sub>1</sub> 撤销了这次修改，那么 T<sub>2</sub> 读取的数据是脏数据。

![img](http://dunwu.test.upcdn.net/cs/database/RDB/数据库并发一致性-脏数据.png)

### 提交读

**`提交读（READ COMMITTED）` 是指：一个事务只能读取已经提交的事务所做的修改**。换句话说，一个事务所做的修改在提交之前对其它事务是不可见的。提交读解决了脏读的问题。

提交读是大多数数据库的默认事务隔离级别。

提交读有时也叫不可重复读，它的问题是：执行两次相同的查询，得到的结果可能不一致。

T<sub>2</sub> 读取一个数据，T<sub>1</sub> 对该数据做了修改。如果 T<sub>2</sub> 再次读取这个数据，此时读取的结果和第一次读取的结果不同。

![img](http://dunwu.test.upcdn.net/cs/database/RDB/数据库并发一致性-不可重复读.png)

### 可重复读

**`可重复读（REPEATABLE READ）` 是指：保证在同一个事务中多次读取同样数据的结果是一样的**。可重复读解决了不可重复读问题。

可重复读是 Mysql 的默认事务隔离级别。

可重复读的问题：当某个事务读取某个范围内的记录时，另外一个事务又在该范围内插入了新的记录，当之前的事务又再次读取该范围的记录时，会产生 **幻读（Phantom Read）**。

T<sub>1</sub> 读取某个范围的数据，T<sub>2</sub> 在这个范围内插入新的数据，T<sub>1</sub> 再次读取这个范围的数据，此时读取的结果和和第一次读取的结果不同。

![img](http://dunwu.test.upcdn.net/cs/database/RDB/数据库并发一致性-幻读.png)

### 串行化

**`串行化（SERIALIXABLE）` 是指：强制事务串行执行**。

强制事务串行执行，则避免了所有的并发问题。串行化策略会在读取的每一行数据上都加锁，这可能导致大量的超时和锁竞争。这对于高并发应用基本上是不可接受的，所以一般不会采用这个级别。

### 隔离级别小结

- **`未提交读（READ UNCOMMITTED）`** - 事务中的修改，即使没有提交，对其它事务也是可见的。
- **`提交读（READ COMMITTED）`** - 一个事务只能读取已经提交的事务所做的修改。换句话说，一个事务所做的修改在提交之前对其它事务是不可见的。
- **`重复读（REPEATABLE READ）`** - 保证在同一个事务中多次读取同样数据的结果是一样的。
- **`串行化（SERIALIXABLE）`** - 强制事务串行执行。

数据库隔离级别解决的问题：

| 隔离级别 | 脏读 | 不可重复读 | 幻读 |
| :------: | :--: | :--------: | :--: |
| 未提交读 |  ❌  |     ❌     |  ❌  |
|  提交读  |  ✔️  |     ❌     |  ❌  |
| 可重复读 |  ✔️  |     ✔️     |  ❌  |
| 可串行化 |  ✔️  |     ✔️     |  ✔️  |

## 五、死锁

死锁是指两个或多个事务竞争同一资源，并请求锁定对方占用的资源，从而导致恶性循环的现象。

产生死锁的场景：

当多个事务试图以不同的顺序锁定资源时，就可能会产生死锁。

多个事务同时锁定同一个资源时，也会产生死锁。

为了解决死锁问题，不同数据库实现了各自的死锁检测和超时机制。InnoDB 的处理策略是：**将持有最少行级排它锁的事务进行回滚**。

## 六、分布式事务

在单一数据节点中，事务仅限于对单一数据库资源的访问控制，称之为 **本地事务**。几乎所有的成熟的关系型数据库都提供了对本地事务的原生支持。

**分布式事务** 是指事务的参与者、支持事务的服务器、资源服务器以及事务管理器分别位于不同的分布式系统的不同节点之上。

### 两阶段提交

两阶段提交（XA）对业务侵入很小。 它最大的优势就是对使用方透明，用户可以像使用本地事务一样使用基于 XA 协议的分布式事务。 XA 协议能够严格保障事务 `ACID` 特性。

严格保障事务 `ACID` 特性是一把双刃剑。 事务执行在过程中需要将所需资源全部锁定，它更加适用于执行时间确定的短事务。 对于长事务来说，整个事务进行期间对数据的独占，将导致对热点数据依赖的业务系统并发性能衰退明显。 因此，在高并发的性能至上场景中，基于 XA 协议的分布式事务并不是最佳选择。

### 柔性事务

如果将实现了`ACID` 的事务要素的事务称为刚性事务的话，那么基于`BASE`事务要素的事务则称为柔性事务。 `BASE`是基本可用、柔性状态和最终一致性这三个要素的缩写。

- 基本可用（Basically Available）保证分布式事务参与方不一定同时在线。
- 柔性状态（Soft state）则允许系统状态更新有一定的延时，这个延时对客户来说不一定能够察觉。
- 而最终一致性（Eventually consistent）通常是通过消息传递的方式保证系统的最终一致性。

在`ACID`事务中对隔离性的要求很高，在事务执行过程中，必须将所有的资源锁定。柔性事务的理念则是通过业务逻辑将互斥锁操作从资源层面上移至业务层面。通过放宽对强一致性要求，来换取系统吞吐量的提升。

基于`ACID`的强一致性事务和基于`BASE`的最终一致性事务都不是银弹，只有在最适合的场景中才能发挥它们的最大长处。 可通过下表详细对比它们之间的区别，以帮助开发者进行技术选型。

### 事务方案对比

|          | 本地事务         | 两（三）阶段事务 | 柔性事务        |
| :------- | :--------------- | :--------------- | --------------- |
| 业务改造 | 无               | 无               | 实现相关接口    |
| 一致性   | 不支持           | 支持             | 最终一致        |
| 隔离性   | 不支持           | 支持             | 业务方保证      |
| 并发性能 | 无影响           | 严重衰退         | 略微衰退        |
| 适合场景 | 业务方处理不一致 | 短事务 & 低并发  | 长事务 & 高并发 |

## 参考资料

- [《高性能 MySQL》](https://book.douban.com/subject/23008813/)
- [ShardingSphere 分布式事务](https://shardingsphere.apache.org/document/current/cn/features/transaction/)
