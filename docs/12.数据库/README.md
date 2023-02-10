---
title: 数据库
date: 2022-02-22 21:01:01
categories:
  - 数据库
tags:
  - 数据库
permalink: /pages/012488/
hidden: true
---

<p align="center">
    <a href="https://dunwu.github.io/db-tutorial/" target="_blank" rel="noopener noreferrer">
        <img src="https://raw.githubusercontent.com/dunwu/images/dev/common/dunwu-logo.png" alt="logo" width="150px"/>
    </a>
</p>

<p align="center">

  <a href="https://github.com/dunwu/db-tutorial">
      <img alt="star" class="no-zoom" src="https://img.shields.io/github/stars/dunwu/db-tutorial?style=for-the-badge">
  </a>

  <a href="https://github.com/dunwu/db-tutorial">
      <img alt="fork" class="no-zoom" src="https://img.shields.io/github/forks/dunwu/db-tutorial?style=for-the-badge">
  </a>

  <a href="https://github.com/dunwu/db-tutorial/commits/master">
      <img alt="build" class="no-zoom" src="https://img.shields.io/github/actions/workflow/status/dunwu/db-tutorial/deploy.yml?style=for-the-badge">
  </a>

  <a href="https://creativecommons.org/licenses/by-nc-sa/4.0/deed.zh">
      <img alt="code style" class="no-zoom" src="https://img.shields.io/github/license/dunwu/db-tutorial?style=for-the-badge">
  </a>

</p>

<h1 align="center">DB-TUTORIAL</h1>

> 💾 **db-tutorial** 是一个数据库教程。
>
> - 🔁 项目同步维护：[Github](https://github.com/dunwu/db-tutorial/) | [Gitee](https://gitee.com/turnon/db-tutorial/)
> - 📖 电子书阅读：[Github Pages](https://dunwu.github.io/db-tutorial/) | [Gitee Pages](https://turnon.gitee.io/db-tutorial/)

## 数据库综合

### 分布式存储原理

#### 分布式理论

- [分布式理论](https://dunwu.github.io/design/pages/367308/)
- [深入剖析共识性算法 Paxos](https://dunwu.github.io/design/pages/874539/)
- [深入剖析共识性算法 Raft](https://dunwu.github.io/design/pages/e40812/)
- [分布式算法 Gossip](https://dunwu.github.io/design/pages/d15993/)

#### 分布式关键技术

##### 流量调度

- [流量控制](https://dunwu.github.io/design/pages/282676/)
- [负载均衡](https://dunwu.github.io/blog/pages/98a1c1/)
- [服务路由](https://dunwu.github.io/design/pages/d04ece/)
- [分布式会话基本原理](https://dunwu.github.io/design/pages/3e66c2/)

##### 数据调度

- [缓存基本原理](https://dunwu.github.io/design/pages/471208/)
- [读写分离基本原理](https://dunwu.github.io/design/pages/7da6ca/)
- [分库分表基本原理](https://dunwu.github.io/design/pages/103382/)
- [分布式 ID 基本原理](https://dunwu.github.io/design/pages/0b2e59/)
- [分布式事务基本原理](https://dunwu.github.io/design/pages/910bad/)
- [分布式锁基本原理](https://dunwu.github.io/design/pages/69360c/)

### 其他

- [Nosql 技术选型](01.数据库综合/01.Nosql技术选型.md)
- [数据结构与数据库索引](01.数据库综合/02.数据结构与数据库索引.md)

## 数据库中间件

- [ShardingSphere 简介](02.数据库中间件/01.Shardingsphere/01.ShardingSphere简介.md)
- [ShardingSphere Jdbc](02.数据库中间件/01.Shardingsphere/02.ShardingSphereJdbc.md)
- [版本管理中间件 Flyway](02.数据库中间件/02.Flyway.md)

## 关系型数据库

> [关系型数据库](03.关系型数据库) 整理主流关系型数据库知识点。

### 公共知识

- [关系型数据库面试总结](03.关系型数据库/01.综合/01.关系型数据库面试.md) 💯
- [SQL Cheat Sheet](03.关系型数据库/01.综合/02.SqlCheatSheet.md) 是一个 SQL 入门教程。
- [扩展 SQL](03.关系型数据库/01.综合/03.扩展SQL.md) 是一个 SQL 入门教程。

### Mysql

![img](https://raw.githubusercontent.com/dunwu/images/dev/snap/20200716103611.png)

- [Mysql 应用指南](03.关系型数据库/02.Mysql/01.Mysql应用指南.md) ⚡
- [Mysql 工作流](03.关系型数据库/02.Mysql/02.MySQL工作流.md) - 关键词：`连接`、`缓存`、`语法分析`、`优化`、`执行引擎`、`redo log`、`bin log`、`两阶段提交`
- [Mysql 事务](03.关系型数据库/02.Mysql/03.Mysql事务.md) - 关键词：`ACID`、`AUTOCOMMIT`、`事务隔离级别`、`死锁`、`分布式事务`
- [Mysql 锁](03.关系型数据库/02.Mysql/04.Mysql锁.md) - 关键词：`乐观锁`、`表级锁`、`行级锁`、`意向锁`、`MVCC`、`Next-key 锁`
- [Mysql 索引](03.关系型数据库/02.Mysql/05.Mysql索引.md) - 关键词：`Hash`、`B 树`、`聚簇索引`、`回表`
- [Mysql 性能优化](03.关系型数据库/02.Mysql/06.Mysql性能优化.md)
- [Mysql 运维](03.关系型数据库/02.Mysql/20.Mysql运维.md) 🔨
- [Mysql 配置](03.关系型数据库/02.Mysql/21.Mysql配置.md) 🔨
- [Mysql 问题](03.关系型数据库/02.Mysql/99.Mysql常见问题.md)

### 其他

- [PostgreSQL 应用指南](03.关系型数据库/99.其他/01.PostgreSQL.md)
- [H2 应用指南](03.关系型数据库/99.其他/02.H2.md)
- [SqLite 应用指南](03.关系型数据库/99.其他/03.Sqlite.md)

## 文档数据库

### MongoDB

> MongoDB 是一个基于文档的分布式数据库，由 C++ 语言编写。旨在为 WEB 应用提供可扩展的高性能数据存储解决方案。
>
> MongoDB 是一个介于关系型数据库和非关系型数据库之间的产品。它是非关系数据库当中功能最丰富，最像关系数据库的。它支持的数据结构非常松散，是类似 json 的 bson 格式，因此可以存储比较复杂的数据类型。
>
> MongoDB 最大的特点是它支持的查询语言非常强大，其语法有点类似于面向对象的查询语言，几乎可以实现类似关系数据库单表查询的绝大部分功能，而且还支持对数据建立索引。

- [MongoDB 应用指南](04.文档数据库/01.MongoDB/01.MongoDB应用指南.md)
- [MongoDB 的 CRUD 操作](04.文档数据库/01.MongoDB/02.MongoDB的CRUD操作.md)
- [MongoDB 聚合操作](04.文档数据库/01.MongoDB/03.MongoDB的聚合操作.md)
- [MongoDB 事务](04.文档数据库/01.MongoDB/04.MongoDB事务.md)
- [MongoDB 建模](04.文档数据库/01.MongoDB/05.MongoDB建模.md)
- [MongoDB 建模示例](04.文档数据库/01.MongoDB/06.MongoDB建模示例.md)
- [MongoDB 索引](04.文档数据库/01.MongoDB/07.MongoDB索引.md)
- [MongoDB 复制](04.文档数据库/01.MongoDB/08.MongoDB复制.md)
- [MongoDB 分片](04.文档数据库/01.MongoDB/09.MongoDB分片.md)
- [MongoDB 运维](04.文档数据库/01.MongoDB/20.MongoDB运维.md)

## KV 数据库

### Redis

![img](https://raw.githubusercontent.com/dunwu/images/dev/snap/20200713105627.png)

- [Redis 面试总结](05.KV数据库/01.Redis/01.Redis面试总结.md) 💯
- [Redis 应用指南](05.KV数据库/01.Redis/02.Redis应用指南.md) ⚡ - 关键词：`内存淘汰`、`事件`、`事务`、`管道`、`发布与订阅`
- [Redis 数据类型和应用](05.KV数据库/01.Redis/03.Redis数据类型和应用.md) - 关键词：`STRING`、`HASH`、`LIST`、`SET`、`ZSET`、`BitMap`、`HyperLogLog`、`Geo`
- [Redis 持久化](05.KV数据库/01.Redis/04.Redis持久化.md) - 关键词：`RDB`、`AOF`、`SAVE`、`BGSAVE`、`appendfsync`
- [Redis 复制](05.KV数据库/01.Redis/05.Redis复制.md) - 关键词：`SLAVEOF`、`SYNC`、`PSYNC`、`REPLCONF ACK`
- [Redis 哨兵](05.KV数据库/01.Redis/06.Redis哨兵.md) - 关键词：`Sentinel`、`PING`、`INFO`、`Raft`
- [Redis 集群](05.KV数据库/01.Redis/07.Redis集群.md) - 关键词：`CLUSTER MEET`、`Hash slot`、`MOVED`、`ASK`、`SLAVEOF no one`、`redis-trib`
- [Redis 实战](05.KV数据库/01.Redis/08.Redis实战.md) - 关键词：`缓存`、`分布式锁`、`布隆过滤器`
- [Redis 运维](05.KV数据库/01.Redis/20.Redis运维.md) 🔨 - 关键词：`安装`、`命令`、`集群`、`客户端`

## 列式数据库

### HBase

> [HBase](https://dunwu.github.io/bigdata-tutorial/hbase) 📚 因为常用于大数据项目，所以将其文档和源码整理在 [bigdata-tutorial](https://dunwu.github.io/bigdata-tutorial/) 项目中。

- [HBase 原理](https://github.com/dunwu/bigdata-tutorial/blob/master/docs/hbase/HBase原理.md) ⚡
- [HBase 命令](https://github.com/dunwu/bigdata-tutorial/blob/master/docs/hbase/HBase命令.md)
- [HBase 应用](https://github.com/dunwu/bigdata-tutorial/blob/master/docs/hbase/HBase应用.md)
- [HBase 运维](https://github.com/dunwu/bigdata-tutorial/blob/master/docs/hbase/HBase运维.md)

## 搜索引擎数据库

### Elasticsearch

> Elasticsearch 是一个基于 Lucene 的搜索和数据分析工具，它提供了一个分布式服务。Elasticsearch 是遵从 Apache 开源条款的一款开源产品，是当前主流的企业级搜索引擎。

- [Elasticsearch 面试总结](07.搜索引擎数据库/01.Elasticsearch/01.Elasticsearch面试总结.md) 💯
- [Elasticsearch 快速入门](07.搜索引擎数据库/01.Elasticsearch/02.Elasticsearch快速入门.md)
- [Elasticsearch 简介](07.搜索引擎数据库/01.Elasticsearch/03.Elasticsearch简介.md)
- [Elasticsearch 索引](07.搜索引擎数据库/01.Elasticsearch/04.Elasticsearch索引.md)
- [Elasticsearch 查询](07.搜索引擎数据库/01.Elasticsearch/05.Elasticsearch查询.md)
- [Elasticsearch 高亮](07.搜索引擎数据库/01.Elasticsearch/06.Elasticsearch高亮.md)
- [Elasticsearch 排序](07.搜索引擎数据库/01.Elasticsearch/07.Elasticsearch排序.md)
- [Elasticsearch 聚合](07.搜索引擎数据库/01.Elasticsearch/08.Elasticsearch聚合.md)
- [Elasticsearch 分析器](07.搜索引擎数据库/01.Elasticsearch/09.Elasticsearch分析器.md)
- [Elasticsearch 性能优化](07.搜索引擎数据库/01.Elasticsearch/10.Elasticsearch性能优化.md)
- [Elasticsearch Rest API](07.搜索引擎数据库/01.Elasticsearch/11.ElasticsearchRestApi.md)
- [ElasticSearch Java API 之 High Level REST Client](07.搜索引擎数据库/01.Elasticsearch/12.ElasticsearchHighLevelRestJavaApi.md)
- [Elasticsearch 集群和分片](07.搜索引擎数据库/01.Elasticsearch/13.Elasticsearch集群和分片.md)
- [Elasticsearch 运维](07.搜索引擎数据库/01.Elasticsearch/20.Elasticsearch运维.md)

### Elastic

- [Elastic 快速入门](07.搜索引擎数据库/02.Elastic/01.Elastic快速入门.md)
- [Elastic 技术栈之 Filebeat](07.搜索引擎数据库/02.Elastic/02.Elastic技术栈之Filebeat.md)
- [Filebeat 运维](07.搜索引擎数据库/02.Elastic/03.Filebeat运维.md)
- [Elastic 技术栈之 Kibana](07.搜索引擎数据库/02.Elastic/04.Elastic技术栈之Kibana.md)
- [Kibana 运维](07.搜索引擎数据库/02.Elastic/05.Kibana运维.md)
- [Elastic 技术栈之 Logstash](07.搜索引擎数据库/02.Elastic/06.Elastic技术栈之Logstash.md)
- [Logstash 运维](07.搜索引擎数据库/02.Elastic/07.Logstash运维.md)

## 资料 📚

### 数据库综合资料

- [DB-Engines](https://db-engines.com/en/ranking) - 数据库流行度排名
- **书籍**
  - [《数据密集型应用系统设计》](https://book.douban.com/subject/30329536/) - 这可能是目前最好的分布式存储书籍，强力推荐【进阶】
- **教程**
  - [CMU 15445 数据库基础课程](https://15445.courses.cs.cmu.edu/fall2019/schedule.html)
  - [CMU 15721 数据库高级课程](https://15721.courses.cs.cmu.edu/spring2020/schedule.html)
  - [检索技术核心 20 讲](https://time.geekbang.org/column/intro/100048401) - 极客教程【进阶】
  - [后端存储实战课](https://time.geekbang.org/column/intro/100046801) - 极客教程【入门】：讲解存储在电商领域的种种应用和一些基本特性
- **论文**
  - [Efficiency in the Columbia Database Query Optimizer](https://15721.courses.cs.cmu.edu/spring2018/papers/15-optimizer1/xu-columbia-thesis1998.pdf)
  - [How Good Are Query Optimizers, Really?](http://www.vldb.org/pvldb/vol9/p204-leis.pdf)
  - [Architecture of a Database System](https://dsf.berkeley.edu/papers/fntdb07-architecture.pdf)
  - [Data Structures for Databases](https://www.cise.ufl.edu/~mschneid/Research/papers/HS05BoCh.pdf)
- **文章**
  - [Data Structures and Algorithms for Big Databases](https://people.csail.mit.edu/bradley/BenderKuszmaul-tutorial-xldb12.pdf)

### 关系型数据库资料

- **综合资料**
  - [《数据库的索引设计与优化》](https://book.douban.com/subject/26419771/)
  - [《SQL 必知必会》](https://book.douban.com/subject/35167240/) - SQL 的基本概念和语法【入门】
- **Oracle 资料**
  - [《Oracle Database 9i/10g/11g 编程艺术》](https://book.douban.com/subject/5402711/)

#### Mysql 资料

- **官方**
  - [Mysql 官网](https://www.mysql.com/)
  - [Mysql 官方文档](https://dev.mysql.com/doc/)
  - **官方 PPT**
    - [How to Analyze and Tune MySQL Queries for Better Performance](https://www.mysql.com/cn/why-mysql/presentations/tune-mysql-queries-performance/)
    - [MySQL Performance Tuning 101](https://www.mysql.com/cn/why-mysql/presentations/mysql-performance-tuning101/)
    - [MySQL Performance Schema & Sys Schema](https://www.mysql.com/cn/why-mysql/presentations/mysql-performance-sys-schema/)
    - [MySQL Performance: Demystified Tuning & Best Practices](https://www.mysql.com/cn/why-mysql/presentations/mysql-performance-tuning-best-practices/)
    - [MySQL Security Best Practices](https://www.mysql.com/cn/why-mysql/presentations/mysql-security-best-practices/)
    - [MySQL Cluster Deployment Best Practices](https://www.mysql.com/cn/why-mysql/presentations/mysql-cluster-deployment-best-practices/)
    - [MySQL High Availability with InnoDB Cluster](https://www.mysql.com/cn/why-mysql/presentations/mysql-high-availability-innodb-cluster/)
- **书籍**
  - [《高性能 MySQL》](https://book.douban.com/subject/23008813/) - 经典，适合 DBA 或作为开发者的参考手册【进阶】
  - [《MySQL 技术内幕：InnoDB 存储引擎》](https://book.douban.com/subject/24708143/)
  - [《MySQL 必知必会》](https://book.douban.com/subject/3354490/) - Mysql 的基本概念和语法【入门】
- **教程**
  - [runoob.com MySQL 教程](http://www.runoob.com/mysql/mysql-tutorial.html) - 入门级 SQL 教程
  - [mysql-tutorial](https://github.com/jaywcjlove/mysql-tutorial)
- **文章**
  - [MySQL 索引背后的数据结构及算法原理](http://blog.codinglabs.org/articles/theory-of-mysql-index.html)
  - [Some study on database storage internals](https://medium.com/@kousiknath/data-structures-database-storage-internals-1f5ed3619d43)
  - [Sharding Pinterest: How we scaled our MySQL fleet](https://medium.com/@Pinterest_Engineering/sharding-pinterest-how-we-scaled-our-mysql-fleet-3f341e96ca6f)
  - [Guide to MySQL High Availability](https://www.mysql.com/cn/why-mysql/white-papers/mysql-guide-to-high-availability-solutions/)
  - [Choosing MySQL High Availability Solutions](https://dzone.com/articles/choosing-mysql-high-availability-solutions)
  - [High availability with MariaDB TX: The definitive guide](https://mariadb.com/sites/default/files/content/Whitepaper_High_availability_with_MariaDB-TX.pdf)
  - Mysql 相关经验
    - [Booking.com: Evolution of MySQL System Design](https://www.percona.com/live/mysql-conference-2015/sessions/bookingcom-evolution-mysql-system-design) ，Booking.com 的 MySQL 数据库使用的演化，其中有很多不错的经验分享，我相信也是很多公司会遇到的的问题。
    - [Tracking the Money - Scaling Financial Reporting at Airbnb](https://medium.com/airbnb-engineering/tracking-the-money-scaling-financial-reporting-at-airbnb-6d742b80f040) ，Airbnb 的数据库扩展的经验分享。
    - [Why Uber Engineering Switched from Postgres to MySQL](https://eng.uber.com/mysql-migration/) ，无意比较两个数据库谁好谁不好，推荐这篇 Uber 的长文，主要是想让你从中学习到一些经验和技术细节，这是一篇很不错的文章。
  - Mysql 集群复制
    - [Monitoring Delayed Replication, With A Focus On MySQL](https://engineering.imvu.com/2013/01/09/monitoring-delayed-replication-with-a-focus-on-mysql/)
    - [Mitigating replication lag and reducing read load with freno](https://githubengineering.com/mitigating-replication-lag-and-reducing-read-load-with-freno/)
    - [Better Parallel Replication for MySQL](https://medium.com/booking-com-infrastructure/better-parallel-replication-for-mysql-14e2d7857813)
    - [Evaluating MySQL Parallel Replication Part 2: Slave Group Commit](https://medium.com/booking-com-infrastructure/evaluating-mysql-parallel-replication-part-2-slave-group-commit-459026a141d2)
    - [Evaluating MySQL Parallel Replication Part 3: Benchmarks in Production](https://medium.com/booking-com-infrastructure/evaluating-mysql-parallel-replication-part-3-benchmarks-in-production-db5811058d74)
    - [Evaluating MySQL Parallel Replication Part 4: More Benchmarks in Production](https://medium.com/booking-com-infrastructure/evaluating-mysql-parallel-replication-part-4-more-benchmarks-in-production-49ee255043ab)
    - [Evaluating MySQL Parallel Replication Part 4, Annex: Under the Hood](https://medium.com/booking-com-infrastructure/evaluating-mysql-parallel-replication-part-4-annex-under-the-hood-eb456cf8b2fb)
  - Mysql 数据分区
    - [StackOverflow: MySQL sharding approaches?](https://stackoverflow.com/questions/5541421/mysql-sharding-approaches)
    - [Why you don’t want to shard](https://www.percona.com/blog/2009/08/06/why-you-dont-want-to-shard/)
    - [How to Scale Big Data Applications](https://www.percona.com/sites/default/files/presentations/How to Scale Big Data Applications.pdf)
    - [MySQL Sharding with ProxySQL](https://www.percona.com/blog/2016/08/30/mysql-sharding-with-proxysql/)
  - 各公司的 Mysql 数据分区经验分享
    - [MailChimp: Using Shards to Accommodate Millions of Users](https://devs.mailchimp.com/blog/using-shards-to-accommodate-millions-of-users/)
    - [Uber: Code Migration in Production: Rewriting the Sharding Layer of Uber’s Schemaless Datastore](https://eng.uber.com/schemaless-rewrite/)
    - [Sharding & IDs at Instagram](https://instagram-engineering.com/sharding-ids-at-instagram-1cf5a71e5a5c)
    - [Airbnb: How We Partitioned Airbnb’s Main Database in Two Weeks](https://medium.com/airbnb-engineering/how-we-partitioned-airbnb-s-main-database-in-two-weeks-55f7e006ff21)
- **更多资源**
  - [awesome-mysql](https://github.com/jobbole/awesome-mysql-cn) - MySQL 的资源列表

### Nosql 数据库综合

- Martin Fowler 在 YouTube 上分享的 NoSQL 介绍 [Introduction To NoSQL](https://youtu.be/qI_g07C_Q5I)， 以及他参与编写的 [NoSQL Distilled - NoSQL 精粹](https://book.douban.com/subject/25662138/)，这本书才 100 多页，是本难得的关于 NoSQL 的书，很不错，非常易读。
- [NoSQL Databases: a Survey and Decision Guidance](https://medium.com/baqend-blog/nosql-databases-a-survey-and-decision-guidance-ea7823a822d#.nhzop4d23)，这篇文章可以带你自上而下地从 CAP 原理到开始了解 NoSQL 的种种技术，是一篇非常不错的文章。
- [Distribution, Data, Deployment: Software Architecture Convergence in Big Data Systems](https://resources.sei.cmu.edu/asset_files/WhitePaper/2014_019_001_90915.pdf)，这是卡内基·梅隆大学的一篇讲分布式大数据系统的论文。其中主要讨论了在大数据时代下的软件工程中的一些关键点，也说到了 NoSQL 数据库。
- [No Relation: The Mixed Blessings of Non-Relational Databases](http://ianvarley.com/UT/MR/Varley_MastersReport_Full_2009-08-07.pdf)，这篇论文虽然有点年代久远。但这篇论文是 HBase 的基础，你花上一点时间来读读，就可以了解到，对各种非关系型数据存储优缺点的一个很好的比较。
- [NoSQL Data Modeling Techniques](https://highlyscalable.wordpress.com/2012/03/01/nosql-data-modeling-techniques/) ，NoSQL 建模技术。这篇文章我曾经翻译在了 CoolShell 上，标题为 [NoSQL 数据建模技术](https://coolshell.cn/articles/7270.htm)，供你参考。
  - [MongoDB - Data Modeling Introduction](https://docs.mongodb.com/manual/core/data-modeling-introduction/) ，虽然这是 MongoDB 的数据建模介绍，但是其很多观点可以用于其它的 NoSQL 数据库。
  - [Firebase - Structure Your Database](https://firebase.google.com/docs/database/android/structure-data) ，Google 的 Firebase 数据库使用 JSON 建模的一些最佳实践。
- 因为 CAP 原理，所以当你需要选择一个 NoSQL 数据库的时候，你应该看看这篇文档 [Visual Guide to NoSQL Systems](http://blog.nahurst.com/visual-guide-to-nosql-systems)。

选 SQL 还是 NoSQL，这里有两篇文章，值得你看看。

- [SQL vs. NoSQL Databases: What’s the Difference?](https://www.upwork.com/hiring/data/sql-vs-nosql-databases-whats-the-difference/)
- [Salesforce: SQL or NoSQL](https://engineering.salesforce.com/sql-or-nosql-9eaf1d92545b)

### 列式数据库资料

#### Cassandra 资料

- 沃尔玛实验室有两篇文章值得一读。
  - [Avoid Pitfalls in Scaling Cassandra Cluster at Walmart](https://medium.com/walmartlabs/avoid-pitfalls-in-scaling-your-cassandra-cluster-lessons-and-remedies-a71ca01f8c04)
  - [Storing Images in Cassandra at Walmart](https://medium.com/walmartlabs/building-object-store-storing-images-in-cassandra-walmart-scale-a6b9c02af593)
- [Yelp: How We Scaled Our Ad Analytics with Apache Cassandra](https://engineeringblog.yelp.com/2016/08/how-we-scaled-our-ad-analytics-with-cassandra.html) ，Yelp 的这篇博客也有一些相关的经验和教训。
- [Discord: How Discord Stores Billions of Messages](https://blog.discordapp.com/how-discord-stores-billions-of-messages-7fa6ec7ee4c7) ，Discord 公司分享的一个如何存储十亿级消息的技术文章。
- [Cassandra at Instagram](https://www.slideshare.net/DataStax/cassandra-at-instagram-2016) ，Instagram 的一个 PPT，其中介绍了 Instagram 中是怎么使用 Cassandra 的。
- [Netflix: Benchmarking Cassandra Scalability on AWS - Over a million writes per second](https://medium.com/netflix-techblog/benchmarking-cassandra-scalability-on-aws-over-a-million-writes-per-second-39f45f066c9e) ，Netflix 公司在 AWS 上给 Cassandra 做的一个 Benchmark。

#### HBase 资料

- [Imgur Notification: From MySQL to HBASE](https://medium.com/imgur-engineering/imgur-notifications-from-mysql-to-hbase-9dba6fc44183)
- [Pinterest: Improving HBase Backup Efficiency](https://medium.com/@Pinterest_Engineering/improving-hbase-backup-efficiency-at-pinterest-86159da4b954)
- [IBM : Tuning HBase performance](https://www.ibm.com/support/knowledgecenter/en/SSPT3X_2.1.2/com.ibm.swg.im.infosphere.biginsights.analyze.doc/doc/bigsql_TuneHbase.html)
- [HBase File Locality in HDFS](http://www.larsgeorge.com/2010/05/hbase-file-locality-in-hdfs.html)
- [Apache Hadoop Goes Realtime at Facebook](http://borthakur.com/ftp/RealtimeHadoopSigmod2011.pdf)
- [Storage Infrastructure Behind Facebook Messages: Using HBase at Scale](http://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.294.8459&rep=rep1&type=pdf)
- [GitHub: Awesome HBase](https://github.com/rayokota/awesome-hbase)

针对于 HBase 有两本书你可以考虑一下。

- 首先，先推荐两本书，一本是偏实践的《[HBase 实战](https://book.douban.com/subject/25706541/)》，另一本是偏大而全的手册型的《[HBase 权威指南](https://book.douban.com/subject/10748460/)》。
- 当然，你也可以看看官方的 [The Apache HBase™ Reference Guide](http://hbase.apache.org/0.94/book/book.html)
- 另外两个列数据库：
  - [ClickHouse - Open Source Distributed Column Database at Yandex](https://clickhouse.yandex/)
  - [Scaling Redshift without Scaling Costs at GIPHY](https://engineering.giphy.com/scaling-redshift-without-scaling-costs/)

### KV 数据库资料

#### Redis 资料

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
- **文章**
  - [Learn Redis the hard way (in production) at Trivago](http://tech.trivago.com/2017/01/25/learn-redis-the-hard-way-in-production/)
  - [Twitter: How Twitter Uses Redis To Scale - 105TB RAM, 39MM QPS, 10,000+ Instances](http://highscalability.com/blog/2014/9/8/how-twitter-uses-redis-to-scale-105tb-ram-39mm-qps-10000-ins.html)
  - [Slack: Scaling Slack’s Job Queue - Robustly Handling Billions of Tasks in Milliseconds Using Kafka and Redis](https://slack.engineering/scaling-slacks-job-queue-687222e9d100)
  - [GitHub: Moving persistent data out of Redis at GitHub](https://githubengineering.com/moving-persistent-data-out-of-redis/)
  - [Instagram: Storing Hundreds of Millions of Simple Key-Value Pairs in Redis](https://engineering.instagram.com/storing-hundreds-of-millions-of-simple-key-value-pairs-in-redis-1091ae80f74c)
  - [Redis in Chat Architecture of Twitch (from 27:22)](https://www.infoq.com/presentations/twitch-pokemon)
  - [Deliveroo: Optimizing Session Key Storage in Redis](https://deliveroo.engineering/2016/10/07/optimising-session-key-storage.html)
  - [Deliveroo: Optimizing Redis Storage](https://deliveroo.engineering/2017/01/19/optimising-membership-queries.html)
  - [GitHub: Awesome Redis](https://github.com/JamzyWang/awesome-redis)

### 文档数据库资料

- [Couchbase Ecosystem at LinkedIn](https://engineering.linkedin.com/blog/2017/12/couchbase-ecosystem-at-linkedin)
- [SimpleDB at Zendesk](https://medium.com/zendesk-engineering/resurrecting-amazon-simpledb-9404034ec506)
- [Data Points - What the Heck Are Document Databases?](https://msdn.microsoft.com/en-us/magazine/hh547103.aspx)

#### MongoDB 资料

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
  - [eBay: Building Mission-Critical Multi-Data Center Applications with MongoDB](https://www.mongodb.com/blog/post/ebay-building-mission-critical-multi-data-center-applications-with-mongodb)
  - [The AWS and MongoDB Infrastructure of Parse: Lessons Learned](https://medium.baqend.com/parse-is-gone-a-few-secrets-about-their-infrastructure-91b3ab2fcf71)
  - [Migrating Mountains of Mongo Data](https://medium.com/build-addepar/migrating-mountains-of-mongo-data-63e530539952)
- **更多资源**
  - [Github: Awesome MongoDB](https://github.com/ramnes/awesome-mongodb)

### 搜索引擎数据库资料

#### ElasticSearch

- **官方**
  - [Elasticsearch 官网](https://www.elastic.co/cn/products/elasticsearch)
  - [Elasticsearch Github](https://github.com/elastic/elasticsearch)
  - [Elasticsearch 官方文档](https://www.elastic.co/guide/en/elasticsearch/reference/current/index.html)
  - [Elasticsearch: The Definitive Guide](https://www.elastic.co/guide/en/elasticsearch/guide/master/index.html) - ElasticSearch 官方学习资料
- **书籍**
  - [《Elasticsearch 实战》](https://book.douban.com/subject/30380439/)
- **教程**
  - [ELK Stack 权威指南](https://github.com/chenryn/logstash-best-practice-cn)
  - [Elasticsearch 教程](https://www.knowledgedict.com/tutorial/elasticsearch-intro.html)
- **文章**
  - [Elasticsearch+Logstash+Kibana 教程](https://www.cnblogs.com/xing901022/p/4704319.html)
  - [ELK（Elasticsearch、Logstash、Kibana）安装和配置](https://github.com/judasn/Linux-Tutorial/blob/master/ELK-Install-And-Settings.md)
  - **性能调优相关**的工程实践
    - [Elasticsearch Performance Tuning Practice at eBay](https://www.ebayinc.com/stories/blogs/tech/elasticsearch-performance-tuning-practice-at-ebay/)
    - [Elasticsearch at Kickstarter](https://kickstarter.engineering/elasticsearch-at-kickstarter-db3c487887fc)
    - [9 tips on ElasticSearch configuration for high performance](https://www.loggly.com/blog/nine-tips-configuring-elasticsearch-for-high-performance/)
    - [Elasticsearch In Production - Deployment Best Practices](https://medium.com/@abhidrona/elasticsearch-deployment-best-practices-d6c1323b25d7)
- **更多资源**
  - [GitHub: Awesome ElasticSearch](https://github.com/dzharii/awesome-elasticsearch)

### 图数据库

- 首先是 IBM Devloperworks 上的两个简介性的 PPT。
  - [Intro to graph databases, Part 1, Graph databases and the CRUD operations](https://www.ibm.com/developerworks/library/cl-graph-database-1/cl-graph-database-1-pdf.pdf)
  - [Intro to graph databases, Part 2, Building a recommendation engine with a graph database](https://www.ibm.com/developerworks/library/cl-graph-database-2/cl-graph-database-2-pdf.pdf)
- 然后是一本免费的电子书《[Graph Database](http://graphdatabases.com)》。
- 接下来是一些图数据库的介绍文章。
  - [Handling Billions of Edges in a Graph Database](https://www.infoq.com/presentations/graph-database-scalability)
  - [Neo4j case studies with Walmart, eBay, AirBnB, NASA, etc](https://neo4j.com/customers/)
  - [FlockDB: Distributed Graph Database for Storing Adjacency Lists at Twitter](https://blog.twitter.com/engineering/en_us/a/2010/introducing-flockdb.html)
  - [JanusGraph: Scalable Graph Database backed by Google, IBM and Hortonworks](https://architecht.io/google-ibm-back-new-open-source-graph-database-project-janusgraph-1d74fb78db6b)
  - [Amazon Neptune](https://aws.amazon.com/neptune/)

### 时序数据库

- [What is Time-Series Data & Why We Need a Time-Series Database](https://blog.timescale.com/what-the-heck-is-time-series-data-and-why-do-i-need-a-time-series-database-dcf3b1b18563)
- [Time Series Data: Why and How to Use a Relational Database instead of NoSQL](https://blog.timescale.com/time-series-data-why-and-how-to-use-a-relational-database-instead-of-nosql-d0cd6975e87c)
- [Beringei: High-performance Time Series Storage Engine @Facebook](https://code.facebook.com/posts/952820474848503/beringei-a-high-performance-time-series-storage-engine/)
- [Introducing Atlas: Netflix’s Primary Telemetry Platform @Netflix](https://medium.com/netflix-techblog/introducing-atlas-netflixs-primary-telemetry-platform-bd31f4d8ed9a)
- [Building a Scalable Time Series Database on PostgreSQL](https://blog.timescale.com/when-boring-is-awesome-building-a-scalable-time-series-database-on-postgresql-2900ea453ee2)
- [Scaling Time Series Data Storage - Part I @Netflix](https://medium.com/netflix-techblog/scaling-time-series-data-storage-part-i-ec2b6d44ba39)
- [Design of a Cost Efficient Time Series Store for Big Data](https://medium.com/@leventov/design-of-a-cost-efficient-time-series-store-for-big-data-88c5dc41af8e)
- [GitHub: Awesome Time-Series Database](https://github.com/xephonhq/awesome-time-series-database)

## 传送 🚪

◾ 💧 [钝悟的 IT 知识图谱](https://dunwu.github.io/waterdrop/) ◾ 🎯 [钝悟的博客](https://dunwu.github.io/blog/) ◾
