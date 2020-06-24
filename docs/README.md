---
home: true
heroImage: /images/dunwu-logo-200.png
heroText: DB-TUTORIAL
tagline: 💾 db-tutorial 是一个数据库教程。
actionLink: /
footer: CC-BY-SA-4.0 Licensed | Copyright © 2018-Now Dunwu
---

# DB-TUTORIAL

![license](https://badgen.net/github/license/dunwu/db-tutorial)
![build](https://api.travis-ci.com/dunwu/db-tutorial.svg?branch=master)

> 💾 **db-tutorial** 是一个数据库教程。
>
> - 🔁 项目同步维护：[Github](https://github.com/dunwu/db-tutorial/) | [Gitee](https://gitee.com/turnon/db-tutorial/)
> - 📖 电子书阅读：[Github Pages](https://dunwu.github.io/db-tutorial/) | [Gitee Pages](https://turnon.gitee.io/db-tutorial/)

## 📖 内容

### 关系型数据库

> [关系型数据库](sql) 整理主流关系型数据库知识点。

- [关系型数据库面试总结](sql/sql-interview.md) 💯
- [SQL Cheat Sheet](sql/sql-cheat-sheet.md)
- [Mysql](sql/mysql) 📚
  - [Mysql 基本原理](sql/mysql/mysql-theory.md)
  - [Mysql 索引](sql/mysql/mysql-index.md)
  - [Mysql 锁](sql/mysql/mysql-lock.md)
  - [Mysql 事务](sql/mysql/mysql-transaction.md)
  - [Mysql 性能优化](sql/mysql/mysql-optimization.md)
  - [Mysql 运维](sql/mysql/mysql-ops.md) 🔨
  - [Mysql 配置](sql/mysql/mysql-config.md)
- [H2 入门指南](sql/h2.md)
- [SqLite 入门指南](sql/sqlite.md)
- [PostgreSQL 入门指南](sql/postgresql.md)

### Nosql 数据库

> [Nosql 数据库](nosql) 整理主流 Nosql 数据库知识点。

- [Nosql 技术选型](nosql/nosql-selection.md)

#### Redis

> [Redis](nosql/redis) 📚

- [Redis 面试总结](nosql/redis/redis-interview.md) 💯
- [Redis 入门指南](nosql/redis/redis-quickstart.md) ⚡
- [Redis 数据类型和应用](nosql/redis/redis-datatype.md)
- [Redis 持久化](nosql/redis/redis-persistence.md)
- [Redis 复制](nosql/redis/redis-replication.md)
- [Redis 哨兵](nosql/redis/redis-sentinel.md)
- [Redis 集群](nosql/redis/redis-cluster.md)
- [Redis 发布与订阅](nosql/redis/redis-pub-sub.md)
- [Redis 运维](nosql/redis/redis-ops.md) 🔨

#### Elasticsearch

> [Elasticsearch](nosql/elasticsearch) 📚

- [Elasticsearch 面试总结](nosql/elasticsearch/elasticsearch-interview.md) 💯
- [ElasticSearch 应用指南](nosql/elasticsearch/elasticsearch-quickstart.md)
- [ElasticSearch API](nosql/elasticsearch/elasticsearch-api.md)
- [ElasticSearch 运维](nosql/elasticsearch/elasticsearch-ops.md)

#### HBase

> [HBase](https://dunwu.github.io/bigdata-tutorial/hbase) 📚 因为常用于大数据项目，所以将其文档和源码整理在 [bigdata-tutorial](https://dunwu.github.io/bigdata-tutorial/) 项目中。

- [HBase 应用指南](https://github.com/dunwu/bigdata-tutorial/blob/master/docs/hbase/hbase-quickstart.md) ⚡
- [HBase 命令](https://github.com/dunwu/bigdata-tutorial/blob/master/docs/hbase/hbase-cli.md)
- [HBase Java API](https://github.com/dunwu/bigdata-tutorial/blob/master/docs/hbase/hbase-api.md)
- [HBase 配置](https://github.com/dunwu/bigdata-tutorial/blob/master/docs/hbase/hbase-ops.md)

### 中间件

- [版本管理中间件 flyway](middleware/flyway.md)
- [分库分表中间件 ShardingSphere](middleware/shardingsphere.md)

## 📚 资料

- **Mysql**
  - **官方**
    - [Mysql 官网](https://www.mysql.com/)
    - [Mysql 官方文档](https://dev.mysql.com/doc/refman/8.0/en/)
    - [Mysql 官方文档之命令行客户端](https://dev.mysql.com/doc/refman/8.0/en/mysql.html)
  - **书籍**
    - [《高性能 MySQL》](https://book.douban.com/subject/23008813/) - 经典，适合 DBA 或作为开发者的参考手册
    - [《MySQL 必知必会》](https://book.douban.com/subject/3354490/) - 适合入门者
  - **教程**
    - [runoob.com MySQL 教程](http://www.runoob.com/mysql/mysql-tutorial.html) - 入门级 SQL 教程
    - [mysql-tutorial](https://github.com/jaywcjlove/mysql-tutorial)
  - **更多资源**
    - [awesome-mysql](https://github.com/jobbole/awesome-mysql-cn)
- **Redis**
  - **官网**
    - [Redis 官网](https://redis.io/)
    - [Redis github](https://github.com/antirez/redis)
    - [Redis 官方文档中文版](http://redis.cn/)
    - [Redis 命令参考](http://redisdoc.com/)
  - **书籍**
    - [《Redis 实战》](https://item.jd.com/11791607.html)
    - [《Redis 设计与实现》](https://item.jd.com/11486101.html)
  - 源码
    - [《Redis 实战》配套 Python 源码](https://github.com/josiahcarlson/redis-in-action)
  - **资源汇总**
    - [awesome-redis](https://github.com/JamzyWang/awesome-redis)
  - **Redis Client**
    - [spring-data-redis 官方文档](https://docs.spring.io/spring-data/redis/docs/1.8.13.RELEASE/reference/html/)
    - [redisson 官方文档(中文,略有滞后)](https://github.com/redisson/redisson/wiki/%E7%9B%AE%E5%BD%95)
    - [redisson 官方文档(英文)](https://github.com/redisson/redisson/wiki/Table-of-Content)
    - [CRUG | Redisson PRO vs. Jedis: Which Is Faster? 翻译](https://www.jianshu.com/p/82f0d5abb002)
    - [redis 分布锁 Redisson 性能测试](https://blog.csdn.net/everlasting_188/article/details/51073505)

## 🚪 传送门

◾ 🏠 [LINUX-TUTORIAL 首页](https://github.com/dunwu/linux-tutorial) ◾ 🎯 [我的博客](https://github.com/dunwu/blog) ◾







