# ShardingSphere

## 简介

### ShardingSphere 组件

ShardingSphere 是一套开源的分布式数据库中间件解决方案组成的生态圈，它由 Sharding-JDBC、Sharding-Proxy 和 Sharding-Sidecar（计划中）这 3 款相互独立的产品组成。 他们均提供标准化的数据分片、分布式事务和数据库治理功能，可适用于如 Java 同构、异构语言、云原生等各种多样化的应用场景。

![](https://raw.githubusercontent.com/dunwu/images/dev/snap/20201008151613.png)

#### ShardingSphere-JDBC

定位为轻量级 Java 框架，在 Java 的 JDBC 层提供的额外服务。 它使用客户端直连数据库，以 jar 包形式提供服务，无需额外部署和依赖，可理解为增强版的 JDBC 驱动，完全兼容 JDBC 和各种 ORM 框架。

- 适用于任何基于 JDBC 的 ORM 框架，如：JPA, Hibernate, Mybatis, Spring JDBC Template 或直接使用 JDBC。
- 支持任何第三方的数据库连接池，如：DBCP, C3P0, BoneCP, Druid, HikariCP 等。
- 支持任意实现 JDBC 规范的数据库，目前支持 MySQL，Oracle，SQLServer，PostgreSQL 以及任何遵循 SQL92 标准的数据库。

![](https://raw.githubusercontent.com/dunwu/images/dev/snap/20201008151213.png)

#### Sharding-Proxy

定位为透明化的数据库代理端，提供封装了数据库二进制协议的服务端版本，用于完成对异构语言的支持。 目前提供 MySQL 和 PostgreSQL 版本，它可以使用任何兼容 MySQL/PostgreSQL 协议的访问客户端(如：MySQL Command Client, MySQL Workbench, Navicat 等)操作数据，对 DBA 更加友好。

- 向应用程序完全透明，可直接当做 MySQL/PostgreSQL 使用。
- 适用于任何兼容 MySQL/PostgreSQL 协议的的客户端。

![](https://raw.githubusercontent.com/dunwu/images/dev/snap/20201008151434.png)

#### Sharding-Sidecar（TODO）

定位为 Kubernetes 的云原生数据库代理，以 Sidecar 的形式代理所有对数据库的访问。 通过无中心、零侵入的方案提供与数据库交互的的啮合层，即 `Database Mesh`，又可称数据库网格。

Database Mesh 的关注重点在于如何将分布式的数据访问应用与数据库有机串联起来，它更加关注的是交互，是将杂乱无章的应用与数据库之间的交互进行有效地梳理。 使用 Database Mesh，访问数据库的应用和数据库终将形成一个巨大的网格体系，应用和数据库只需在网格体系中对号入座即可，它们都是被啮合层所治理的对象。

![](https://raw.githubusercontent.com/dunwu/images/dev/snap/20201008151557.png)

| _Sharding-JDBC_ | _Sharding-Proxy_ | _Sharding-Sidecar_ |        |
| :-------------- | :--------------- | :----------------- | ------ |
| 数据库          | 任意             | MySQL              | MySQL  |
| 连接消耗数      | 高               | 低                 | 高     |
| 异构语言        | 仅 Java          | 任意               | 任意   |
| 性能            | 损耗低           | 损耗略高           | 损耗低 |
| 无中心化        | 是               | 否                 | 是     |
| 静态入口        | 无               | 有                 | 无     |

#### 混合架构

ShardingSphere-JDBC 采用无中心化架构，适用于 Java 开发的高性能的轻量级 OLTP 应用；ShardingSphere-Proxy 提供静态入口以及异构语言的支持，适用于 OLAP 应用以及对分片数据库进行管理和运维的场景。

Apache ShardingSphere 是多接入端共同组成的生态圈。 通过混合使用 ShardingSphere-JDBC 和 ShardingSphere-Proxy，并采用同一注册中心统一配置分片策略，能够灵活的搭建适用于各种场景的应用系统，使得架构师更加自由地调整适合与当前业务的最佳系统架构。

![](https://raw.githubusercontent.com/dunwu/images/dev/snap/20201008151658.png)

### 功能列表

#### 数据分片

- 分库 & 分表
- 读写分离
- 分片策略定制化
- 无中心化分布式主键

#### 分布式事务

- 标准化事务接口
- XA 强一致事务
- 柔性事务

#### 数据库治理

- 分布式治理
- 弹性伸缩
- 可视化链路追踪
- 数据加密

## 参考资料

- [shardingsphere Github](https://github.com/apache/incubator-shardingsphere)
- [shardingsphere 官方文档](https://shardingsphere.apache.org/document/current/cn/overview/)
