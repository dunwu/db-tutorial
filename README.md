<p align="center">
    <a href="https://dunwu.github.io/db-tutorial/" target="_blank" rel="noopener noreferrer">
        <img src="https://raw.githubusercontent.com/dunwu/images/dev/common/dunwu-logo-200.png" alt="logo" width="150px"/>
    </a>
</p>

<p align="center">
    <img src="https://badgen.net/github/license/dunwu/db-tutorial" alt="license">
    <img src="https://travis-ci.com/dunwu/db-tutorial.svg?branch=master" alt="build">
</p>

<h1 align="center">DB-TUTORIAL</h1>

> 💾 **db-tutorial** 是一个数据库教程。
>
> - 🔁 项目同步维护：[Github](https://github.com/dunwu/db-tutorial/) | [Gitee](https://gitee.com/turnon/db-tutorial/)
> - 📖 电子书阅读：[Github Pages](https://dunwu.github.io/db-tutorial/) | [Gitee Pages](https://turnon.gitee.io/db-tutorial/)

## 数据库原理

### 数据结构

TODO...

### 分布式

- [分布式简介](https://dunwu.github.io/design/distributed/分布式简介.html)
- [分布式基础理论](https://dunwu.github.io/design/distributed/分布式理论.html) - 关键词：`拜占庭将军`、`CAP`、`BASE`
- [分布式算法 Paxos](https://dunwu.github.io/design/distributed/分布式算法Paxos.html) - 关键词：`共识性算法`
- [分布式算法 Raft](https://dunwu.github.io/design/distributed/分布式算法Raft.html) - 关键词：`共识性算法`
- [负载均衡](https://dunwu.github.io/design/distributed/负载均衡.html) - 关键词：`轮询`、`随机`、`最少连接`、`源地址哈希`、`一致性哈希`、`虚拟 hash 槽`
- [消息队列](https://dunwu.github.io/design/distributed/消息队列.html) - 关键词：`重复消费`、`消息丢失`、`消息顺序性`、`消息积压`
- [分布式存储](https://dunwu.github.io/design/distributed/分布式存储.html) - 关键词：`读写分离`、`分库分表`、`迁移`、`扩容`
- [分布式缓存](https://dunwu.github.io/design/distributed/分布式缓存.html) - 关键词：`进程内缓存`、`分布式缓存`、`缓存雪崩`、`缓存穿透`、`缓存击穿`、`缓存更新`、`缓存预热`、`缓存降级`
- [分布式锁](https://dunwu.github.io/design/distributed/分布式锁.html) - 关键词：`数据库`、`Redis`、`ZooKeeper`、`互斥`、`可重入`、`死锁`、`容错`、`自旋尝试`
- [分布式 ID](https://dunwu.github.io/design/distributed/分布式ID.html) - 关键词：`UUID`、`自增序列`、`雪花算法`、`Leaf`
- [分布式事务](https://dunwu.github.io/design/distributed/分布式事务.html) - 关键词：`2PC`、`3PC`、`TCC`、`本地消息表`、`MQ 消息`、`SAGA`
- [分布式会话](https://dunwu.github.io/design/distributed/分布式会话.html) - 关键词：`粘性 Session`、`Session 复制共享`、`基于缓存的 session 共享`
- [流量控制](https://dunwu.github.io/design/distributed/流量控制.html) - 关键词：`计数器法`、`时间窗口法`、`令牌桶法`、`漏桶法`

## 关系型数据库

> [关系型数据库](docs/sql) 整理主流关系型数据库知识点。

- [关系型数据库面试总结](docs/sql/common/sql-interview.md) 💯
- [SQL Cheat Sheet](docs/sql/common/sql-cheat-sheet.md) 是一个 SQL 入门教程。

### [Mysql](docs/sql/mysql)

![img](https://raw.githubusercontent.com/dunwu/images/dev/snap/20200716103611.png)

- [Mysql 应用指南](docs/sql/mysql/mysql-quickstart.md) ⚡
- [Mysql 工作流](docs/sql/mysql/mysql-workflow.md) - 关键词：`连接`、`缓存`、`语法分析`、`优化`、`执行引擎`、`redo log`、`bin log`、`两阶段提交`
- [Mysql 索引](docs/sql/mysql/mysql-index.md) - 关键词：`Hash`、`B 树`、`聚簇索引`、`回表`
- [Mysql 锁](docs/sql/mysql/mysql-lock.md) - 关键词：`乐观锁`、`表级锁`、`行级锁`、`意向锁`、`MVCC`、`Next-key 锁`
- [Mysql 事务](docs/sql/mysql/mysql-transaction.md) - 关键词：`ACID`、`AUTOCOMMIT`、`事务隔离级别`、`死锁`、`分布式事务`
- [Mysql 性能优化](docs/sql/mysql/mysql-optimization.md)
- [Mysql 运维](docs/sql/mysql/mysql-ops.md) 🔨
- [Mysql 配置](docs/sql/mysql/mysql-config.md)
- [Mysql 问题](docs/sql/mysql/mysql-faq.md)

### 其他

- [H2 应用指南](docs/sql/h2.md)
- [SqLite 应用指南](docs/sql/sqlite.md)
- [PostgreSQL 应用指南](docs/sql/postgresql.md)

## [Redis](docs/nosql/redis)

![img](https://raw.githubusercontent.com/dunwu/images/dev/snap/20200713105627.png)

- [Redis 面试总结](docs/nosql/redis/redis-interview.md) 💯
- [Redis 应用指南](docs/nosql/redis/redis-quickstart.md) ⚡ - 关键词：`内存淘汰`、`事件`、`事务`、`管道`、`发布与订阅`
- [Redis 数据类型和应用](docs/nosql/redis/redis-datatype.md) - 关键词：`STRING`、`HASH`、`LIST`、`SET`、`ZSET`、`BitMap`、`HyperLogLog`、`Geo`
- [Redis 持久化](docs/nosql/redis/redis-persistence.md) - 关键词：`RDB`、`AOF`、`SAVE`、`BGSAVE`、`appendfsync`
- [Redis 复制](docs/nosql/redis/redis-replication.md) - 关键词：`SLAVEOF`、`SYNC`、`PSYNC`、`REPLCONF ACK`
- [Redis 哨兵](docs/nosql/redis/redis-sentinel.md) - 关键词：`Sentinel`、`PING`、`INFO`、`Raft`
- [Redis 集群](docs/nosql/redis/redis-cluster.md) - 关键词：`CLUSTER MEET`、`Hash slot`、`MOVED`、`ASK`、`SLAVEOF no one`、`redis-trib`
- [Redis 实战](docs/nosql/redis/redis-action.md) - 关键词：`缓存`、`分布式锁`、`布隆过滤器`
- [Redis 运维](docs/nosql/redis/redis-ops.md) 🔨 - 关键词：`安装`、`命令`、`集群`、`客户端`

## [Elasticsearch](docs/nosql/elasticsearch)

> Elasticsearch 是一个基于 Lucene 的搜索和数据分析工具，它提供了一个分布式服务。Elasticsearch 是遵从 Apache 开源条款的一款开源产品，是当前主流的企业级搜索引擎。

- [Elasticsearch 面试总结](docs/nosql/elasticsearch/elasticsearch-interview.md) 💯
- [Elasticsearch 快速入门](docs/nosql/elasticsearch/Elasticsearch快速入门.md)
- [Elasticsearch 简介](docs/nosql/elasticsearch/Elasticsearch简介.md)
- [Elasticsearch Rest API](docs/nosql/elasticsearch/ElasticsearchRestApi.md)
- [ElasticSearch Java API 之 High Level REST Client](docs/nosql/elasticsearch/ElasticsearchHighLevelRestJavaApi.md)
- [Elasticsearch 索引管理](docs/nosql/elasticsearch/Elasticsearch索引管理.md)
- [Elasticsearch 查询](docs/nosql/elasticsearch/Elasticsearch查询.md)
- [Elasticsearch 高亮](docs/nosql/elasticsearch/Elasticsearch高亮.md)
- [Elasticsearch 排序](docs/nosql/elasticsearch/Elasticsearch排序.md)
- [Elasticsearch 聚合](docs/nosql/elasticsearch/Elasticsearch聚合.md)
- [Elasticsearch 分析器](docs/nosql/elasticsearch/Elasticsearch分析器.md)
- [Elasticsearch 运维](docs/nosql/elasticsearch/Elasticsearch运维.md)
- [Elasticsearch 性能优化](docs/nosql/elasticsearch/Elasticsearch性能优化.md)

## [MongoDB](docs/nosql/mongodb)

> MongoDB 是一个基于文档的分布式数据库，由 C++ 语言编写。旨在为 WEB 应用提供可扩展的高性能数据存储解决方案。
>
> MongoDB 是一个介于关系型数据库和非关系型数据库之间的产品。它是非关系数据库当中功能最丰富，最像关系数据库的。它支持的数据结构非常松散，是类似 json 的 bson 格式，因此可以存储比较复杂的数据类型。
>
> MongoDB 最大的特点是它支持的查询语言非常强大，其语法有点类似于面向对象的查询语言，几乎可以实现类似关系数据库单表查询的绝大部分功能，而且还支持对数据建立索引。

- [MongoDB 应用指南](docs/nosql/mongodb/mongodb-quickstart.md)
- [MongoDB 聚合操作](docs/nosql/mongodb/mongodb-aggregation.md)
- [MongoDB 建模](docs/nosql/mongodb/mongodb-model.md)
- [MongoDB 建模示例](docs/nosql/mongodb/mongodb-model-example.md)
- [MongoDB 索引](docs/nosql/mongodb/mongodb-index.md)
- [MongoDB 复制](docs/nosql/mongodb/mongodb-replication.md)
- [MongoDB 分片](docs/nosql/mongodb/mongodb-sharding.md)
- [MongoDB 运维](docs/nosql/mongodb/mongodb-ops.md)

## HBase

> [HBase](https://dunwu.github.io/bigdata-tutorial/hbase) 📚 因为常用于大数据项目，所以将其文档和源码整理在 [bigdata-tutorial](https://dunwu.github.io/bigdata-tutorial/) 项目中。

- [HBase 原理](https://github.com/dunwu/bigdata-tutorial/blob/master/docs/hbase/HBase原理.md) ⚡
- [HBase 命令](https://github.com/dunwu/bigdata-tutorial/blob/master/docs/hbase/HBase命令.md)
- [HBase 应用](https://github.com/dunwu/bigdata-tutorial/blob/master/docs/hbase/HBase应用.md)
- [HBase 运维](https://github.com/dunwu/bigdata-tutorial/blob/master/docs/hbase/HBase运维.md)

## 中间件

- [版本管理中间件 flyway](docs/middleware/flyway.md)
- [分库分表中间件 ShardingSphere](docs/middleware/shardingsphere.md)

## 资料 📚

### 综合

- [DB-Engines](https://db-engines.com/en/ranking) - 数据库流行度排名
- **书籍**
  - [《数据密集型应用系统设计》](https://book.douban.com/subject/30329536/) - 强力推荐【进阶】
- **课程**
  - [CMU 15445 数据库基础课程](https://15445.courses.cs.cmu.edu/fall2019/schedule.html)
  - [CMU 15721 数据库高级课程](https://15721.courses.cs.cmu.edu/spring2020/schedule.html)
- **论文**
  - [Efficiency in the Columbia Database Query Optimizer](https://15721.courses.cs.cmu.edu/spring2018/papers/15-optimizer1/xu-columbia-thesis1998.pdf)
  - [How Good Are Query Optimizers, Really?](http://www.vldb.org/pvldb/vol9/p204-leis.pdf)
  - [Architecture of a Database System](https://dsf.berkeley.edu/papers/fntdb07-architecture.pdf)

### Mysql 资料

- **官方**
  - [Mysql 官网](https://www.mysql.com/)
  - [Mysql 官方文档](https://dev.mysql.com/doc/refman/8.0/en/)
  - [Mysql 官方文档之命令行客户端](https://dev.mysql.com/doc/refman/8.0/en/mysql.html)
- **书籍**
  - [《高性能 MySQL》](https://book.douban.com/subject/23008813/) - 经典，适合 DBA 或作为开发者的参考手册【进阶】
  - [《MySQL 必知必会》](https://book.douban.com/subject/3354490/) - Mysql的基本概念和语法【入门】
  - [《SQL必知必会》](https://book.douban.com/subject/35167240/) - SQL的基本概念和语法【入门】
  - 
- **教程**
  - [runoob.com MySQL 教程](http://www.runoob.com/mysql/mysql-tutorial.html) - 入门级 SQL 教程
  - [mysql-tutorial](https://github.com/jaywcjlove/mysql-tutorial)
- **更多资源**
  - [awesome-mysql](https://github.com/jobbole/awesome-mysql-cn)

### Redis 资料

- **官网**
  - [Redis 官网](https://redis.io/)
  - [Redis github](https://github.com/antirez/redis)
  - [Redis 官方文档中文版](http://redis.cn/)
  - [Redis 命令参考](http://redisdoc.com/)
- **书籍**
  - [《Redis 实战》](https://item.jd.com/11791607.html)
  - [《Redis 设计与实现》](https://item.jd.com/11486101.html)
- **源码**
  - [《Redis 实战》配套 Python 源码](https://github.com/josiahcarlson/redis-in-action)
- **资源汇总**
  - [awesome-redis](https://github.com/JamzyWang/awesome-redis)
- **Redis Client**
  - [spring-data-redis 官方文档](https://docs.spring.io/spring-data/redis/docs/1.8.13.RELEASE/reference/html/)
  - [redisson 官方文档(中文,略有滞后)](https://github.com/redisson/redisson/wiki/%E7%9B%AE%E5%BD%95)
  - [redisson 官方文档(英文)](https://github.com/redisson/redisson/wiki/Table-of-Content)
  - [CRUG | Redisson PRO vs. Jedis: Which Is Faster? 翻译](https://www.jianshu.com/p/82f0d5abb002)
  - [redis 分布锁 Redisson 性能测试](https://blog.csdn.net/everlasting_188/article/details/51073505)

### MongoDB 资料

- **官方**
  - [MongoDB 官网](https://www.mongodb.com/)
  - [MongoDB Github](https://github.com/mongodb/mongo)
  - [MongoDB 官方免费教程](https://university.mongodb.com/)
- **教程**
  - [MongoDB 教程](https://www.runoob.com/mongodb/mongodb-tutorial.html)
  - [MongoDB 高手课](https://time.geekbang.org/course/intro/100040001)
- **数据**
  - [mongodb-json-files](https://github.com/ozlerhakan/mongodb-json-files)
- **文章**
  - [Introduction to MongoDB](https://www.slideshare.net/mdirolf/introduction-to-mongodb)

## 传送 🚪

◾ 🏠 [DB-TUTORIAL 首页](https://github.com/dunwu/db-tutorial) ◾ 🎯 [我的博客](https://github.com/dunwu/blog) ◾
