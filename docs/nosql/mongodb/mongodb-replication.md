# MongoDB 复制

<!-- TOC depthFrom:2 depthTo:3 -->

- [副本和可用性](#副本和可用性)
- [MongoDB 副本](#mongodb-副本)
- [异步复制](#异步复制)
  - [慢操作](#慢操作)
  - [复制延迟和流控](#复制延迟和流控)
- [故障转移](#故障转移)
- [读操作](#读操作)
  - [读优先](#读优先)
  - [数据可见性](#数据可见性)
  - [镜像读取](#镜像读取)
- [参考资料](#参考资料)

<!-- /TOC -->

## 副本和可用性

副本可以**提供冗余并提高数据可用性**。在不同数据库服务器上使用多个数据副本，可以提供一定程度的容错能力，以防止单个数据库服务器宕机时，数据丢失。

在某些情况下，副本还可以**提供更大的读取吞吐量**。因为客户端可以将读取操作发送到不同的服务器。在不同数据中心中维护数据副本可以提高数据本地性和分布式应用程序的可用性。您还可以维护其他副本以用于专用目的：例如灾难恢复，报告或备份。

## MongoDB 副本

MongoDB 中的副本集是一组维护相同数据集的 mongod 进程。一个副本集包含多个数据承载节点和一个仲裁器节点（可选）。在数据承载节点中，只有一个成员被视为主要节点，而其他节点则被视为次要节点。

**主节点负责接收所有写操作**。副本集只能有一个主副本，能够以 [`{ w: "majority" }`](https://docs.mongodb.com/manual/reference/write-concern/#writeconcern."majority") 来确认集群中节点的写操作成功情况；尽管在某些情况下，另一个 MongoDB 实例可能会暂时认为自己也是主要的。主节点在其操作日志（即 [oplog](https://docs.mongodb.com/manual/core/replica-set-oplog/)）中记录了对其数据集的所有更改。

![img](https://raw.githubusercontent.com/dunwu/images/dev/snap/20200920165054.svg)

**从节点复制主节点的操作日志，并将操作应用于其数据集**，以便同步主节点的数据。如果主节点不可用，则符合条件的从节点将选举新的主节点。

![img](https://raw.githubusercontent.com/dunwu/images/dev/snap/20200920165055.svg)

在某些情况下（例如，有一个主节点和一个从节点，但由于成本限制，禁止添加另一个从节点），您可以选择将 mongod 实例作为仲裁节点添加到副本集。仲裁节点参加选举但不保存数据（即不提供数据冗余）。

![img](https://raw.githubusercontent.com/dunwu/images/dev/snap/20200920165053.svg)

仲裁节点将永远是仲裁节点。在选举期间，主节点可能会降级成为次节点，而次节点可能会升级成为主节点。

## 异步复制

### 慢操作

从节点复制主节点的操作日志，并将操作异步应用于其数据集。通过从节点同步主节点的数据集，即使一个或多个成员失败，副本集（MongoDB 集群）也可以继续运行。

从 4.2 版本开始，副本集的从节点记录慢操作（操作时间比设置的阈值长）的日志条目。这些慢操作在 [`REPL`](https://docs.mongodb.com/manual/reference/log-messages/#REPL) 组件下的 [诊断日志](https://docs.mongodb.com/manual/reference/program/mongod/#cmdoption-mongod-logpath) 中记录了日志消息，并使用了文本 `op: <oplog entry>` 花费了 `<num>ms`。这些慢操作日志条目仅取决于慢操作阈值，而不取决于日志级别（在系统级别或组件级别），配置级别或运行缓慢的采样率。探查器不会捕获缓慢的操作日志条目。

### 复制延迟和流控

复制延迟（[Replication lag](https://docs.mongodb.com/manual/reference/glossary/#term-replication-lag)）是指将主节点上的写操作复制到从节点上所花费的时间。较短的延迟时间是可以接受的，但是随着复制延迟的增加，可能会出现严重的问题：比如在主节点上的缓存压力。

从 MongoDB 4.2 开始，管理员可以限制主节点的写入速率，使得大多数延迟时间保持在可配置的最大值 [`flowControlTargetLagSeconds`](https://docs.mongodb.com/manual/reference/parameters/#param.flowControlTargetLagSeconds) 以下。

默认情况下，流控是开启的。

启用流控后，随着延迟时间越来越接近 [`flowControlTargetLagSeconds`](https://docs.mongodb.com/manual/reference/parameters/#param.flowControlTargetLagSeconds)，主对象上的写操作必须先获得令牌，然后才能进行锁定并执行写操作。通过限制每秒发出的令牌数量，流控机制尝试将延迟保持在目标以下。

## 故障转移

当主节点与集群中的其他成员通信的时间超过配置的 `electionTimeoutMillis`（默认为 10 秒）时，符合选举要求的从节点将要求选举，并提名自己为新的主节点。集群尝试完成选举新主节点并恢复正常工作。

![img](https://raw.githubusercontent.com/dunwu/images/dev/snap/20200920175429.svg)

选举完成前，副本集无法处理写入操作。如果将副本集配置为：在主节点处于脱机状态时，在次节点上运行，则副本集可以继续提供读取查询。

假设[副本配置](https://docs.mongodb.com/manual/reference/replica-configuration/#rsconf.settings)采用默认配置，则集群选择新节点的时间通常不应超过 12 秒，这包括：将主节点标记为不可用并完成选举所需的时间。可以通过修改 [`settings.electionTimeoutMillis`](https://docs.mongodb.com/manual/reference/replica-configuration/#rsconf.settings.electionTimeoutMillis) 配置选项来调整此时间。网络延迟等因素可能会延长完成选举所需的时间，进而影响集群在没有主节点的情况下可以运行的时间。这些因素取决于集群实际的情况。

将默认为 10 秒的 [`electionTimeoutMillis`](https://docs.mongodb.com/manual/reference/replica-configuration/#rsconf.settings.electionTimeoutMillis) 选项数值缩小，可以更快地检测到主要故障。但是，由于网络延迟等因素，集群可能会更频繁地进行选举，即使该主节点实际上处于健康状态。这可能导致 [w : 1](https://docs.mongodb.com/manual/reference/write-concern/#wc-w) 写操作的回滚次数增加。

应用程序的连接逻辑应包括对自动故障转移和后续选举的容错处理。从 MongoDB 3.6 开始，MongoDB 驱动程序可以检测到主节点的失联，并可以自动重试一次某些写入操作。

从 MongoDB4.4 开始，MongoDB 提供镜像读取：将可选举的从节点的最近访问的数据，预热为缓存。预热从节点的缓存可以帮助在选举后更快地恢复。

## 读操作

### 读优先

默认情况下，客户端从主节点读取数据；但是，客户端可以指定读取首选项，以将读取操作发送到从节点。

![img](https://raw.githubusercontent.com/dunwu/images/dev/snap/20200920204024.svg)

异步复制到从节点意味着向从节点读取数据可能会返回与主节点不一致的数据。

包含读取操作的多文档事务必须使用读取主节点优先。给定事务中的所有操作必须路由到同一成员。

### 数据可见性

根据读取的关注点，客户端可以在持久化写入前查看写入结果：

- 不管写的 [write concern](https://docs.mongodb.com/manual/reference/write-concern/) 如何设置，其他使用 [`"local"`](https://docs.mongodb.com/manual/reference/read-concern-local/#readconcern."local") 或 [`"available"`](https://docs.mongodb.com/manual/reference/read-concern-available/#readconcern."available") 的读配置的客户端都可以向发布客户端确认写操作之前看到写操作的结果。
- 使用 [`"local"`](https://docs.mongodb.com/manual/reference/read-concern-local/#readconcern."local") 或 [`"available"`](https://docs.mongodb.com/manual/reference/read-concern-available/#readconcern."available") 读取配置的客户端可以读取数据，这些数据随后可能会在副本集故障转移期间回滚。

对于多文档事务中的操作，当事务提交时，在事务中进行的所有数据更改都将保存，并在事务外部可见。也就是说，事务在回滚其他事务时将不会提交其某些更改。在提交事务前，事务外部看不到在事务中进行的数据更改。

但是，当事务写入多个分片时，并非所有外部读操作都需要等待已提交事务的结果在所有分片上可见。例如，如果提交了一个事务，并且在分片 A 上可以看到写 1，但是在分片 B 上还看不到写 2，则在 [`"local"`](https://docs.mongodb.com/manual/reference/read-concern-local/#readconcern."local") 读配置级别，外部读取可以读取写 1 的结果而看不到写 2。

### 镜像读取

从 MongoDB 4.4 开始，MongoDB 提供镜像读取以预热可选从节点（即优先级大于 0 的成员）的缓存。使用镜像读取（默认情况下已启用），主节点可以镜像它接收到的一部分操作，并将其发送给可选择的从节点的子集。子集的大小是可配置的。

## 参考资料

- **官方**
  - [MongoDB 官网](https://www.mongodb.com/)
  - [MongoDB Github](https://github.com/mongodb/mongo)
  - [MongoDB 官方免费教程](https://university.mongodb.com/)
- **教程**
  - [MongoDB 教程](https://www.runoob.com/mongodb/mongodb-tutorial.html)
  - [MongoDB 高手课](https://time.geekbang.org/course/intro/100040001)
