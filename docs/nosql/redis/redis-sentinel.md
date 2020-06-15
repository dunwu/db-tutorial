# Redis 哨兵

Redis 哨兵（Sentinel）是 Redis 的**高可用性**（Hight Availability）解决方案：由一个或多个 Sentinel 实例组成的 Sentinel 系统可以监视任意多个主服务器，以及这些主服务器的所有从服务器，并在被监视的主服务器进入下线状态时，自动将下线主服务器的某个从服务器升级为新的主服务器，然后由新的主服务器代替已下线的主服务器继续处理命令请求。

**Sentinel 本质上是一个运行在特殊状模式下的 Redis 服务器**。

![img](http://dunwu.test.upcdn.net/snap/20200131135847.png)

## 一、哨兵简介

Sentinel 的主要功能如下：

- **`监控（Monitoring）`** - Sentinel 不断检查主从服务器是否正常在工作。
- **`通知（Notification）`** - Sentinel 可以通过一个 api 来通知系统管理员或者另外的应用程序，被监控的 Redis 实例有一些问题。
- **`自动故障转移（Automatic Failover）`** - 如果一个主服务器下线，Sentinel 会开始自动故障转移：把一个从节点提升为主节点，并重新配置其他的从节点使用新的主节点，使用 Redis 服务的应用程序在连接的时候也被通知新的地址。
- **`配置提供者（Configuration provider）`** - Sentinel 给客户端的服务发现提供来源：对于一个给定的服务，客户端连接到 Sentinels 来寻找当前主节点的地址。当故障转移发生的时候，Sentinel 将报告新的地址。

## 二、启动哨兵

启动一个 Sentinel 可以使用下面任意一条命令，两条命令效果完全相同。

```shell
redis-sentinel /path/to/sentinel.conf
redis-server /path/to/sentinel.conf --sentinel
```

当一个 Sentinel 启动时，它需要执行以下步骤：

1. 初始化服务器。
2. 将普通 Redis 服务器使用的代码替换成 Sentinel 专用代码。
3. 初始化 Sentinel 状态。
4. 根据给定的配置文件， 初始化 Sentinel 的监视主服务器列表。
5. 创建连向主服务器的网络连接。

**Sentinel 本质上是一个运行在特殊状模式下的 Redis 服务器**。

Sentinel 模式下 Redis 服务器主要功能的使用情况：

| 功能                                                     | 使用情况                                                                                                                                                        |
| :------------------------------------------------------- | :-------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 数据库和键值对方面的命令， 比如 SET 、 DEL 、 FLUSHDB 。 | 不使用。                                                                                                                                                        |
| 事务命令， 比如 MULTI 和 WATCH 。                        | 不使用。                                                                                                                                                        |
| 脚本命令，比如 EVAL 。                                   | 不使用。                                                                                                                                                        |
| RDB 持久化命令， 比如 SAVE 和 BGSAVE 。                  | 不使用。                                                                                                                                                        |
| AOF 持久化命令， 比如 BGREWRITEAOF 。                    | 不使用。                                                                                                                                                        |
| 复制命令，比如 SLAVEOF 。                                | Sentinel 内部可以使用，但客户端不可以使用。                                                                                                                     |
| 发布与订阅命令， 比如 PUBLISH 和 SUBSCRIBE 。            | SUBSCRIBE 、 PSUBSCRIBE 、 UNSUBSCRIBE PUNSUBSCRIBE 四个命令在 Sentinel 内部和客户端都可以使用， 但 PUBLISH 命令只能在 Sentinel 内部使用。                      |
| 文件事件处理器（负责发送命令请求、处理命令回复）。       | Sentinel 内部使用， 但关联的文件事件处理器和普通 Redis 服务器不同。                                                                                             |
| 时间事件处理器（负责执行 `serverCron` 函数）。           | Sentinel 内部使用， 时间事件的处理器仍然是 `serverCron` 函数， `serverCron` 函数会调用 `sentinel.c/sentinelTimer` 函数， 后者包含了 Sentinel 要执行的所有操作。 |

## 三、监控

### 检测服务器状态

> **Sentinel 向 Redis 服务器发送 `PING` 命令，检查其状态**。

默认情况下，**每个** `Sentinel` 节点会以 **每秒一次** 的频率对 `Redis` 节点和 **其它** 的 `Sentinel` 节点发送 `PING` 命令，并通过节点的 **回复** 来判断节点是否在线。

- **主观下线**

**主观下线** 适用于所有 **主节点** 和 **从节点**。如果在 `down-after-milliseconds` 毫秒内，`Sentinel` 没有收到 **目标节点** 的有效回复，则会判定 **该节点** 为 **主观下线**。

- **客观下线**

**客观下线** 只适用于 **主节点**。如果 **主节点** 出现故障，`Sentinel` 节点会通过 `sentinel is-master-down-by-addr` 命令，向其它 `Sentinel` 节点询问对该节点的 **状态判断**。如果超过 `` 个数的节点判定 **主节点** 不可达，则该 `Sentinel` 节点会判断 **主节点** 为 **客观下线**。

### 获取服务器信息

> **Sentinel 向主服务器发送 `INFO` 命令，获取主服务器及它的从服务器信息**。

- **获取主服务器信息** - Sentinel 默认会以每十秒一次的频率，通过命令连接向被监视的主服务器发送 `INFO` 命令，并通过分析 `INFO` 命令的回复来获取主服务器的当前信息。
- **获取从服务器信息** - 当 Sentinel 发现主服务器有新的从服务器出现时，Sentinel 除了会为这个新的从服务器创建相应的实例结构之外，Sentinel 还会创建连接到从服务器的命令连接和订阅连接。

## 四、通知

对于每个与 Sentinel 连接的服务器，Sentinel 既会向服务器的 `__sentinel__:hello` 频道发送消息，也会订阅服务器的 `__sentinel__:hello` 频道的消息。

![img](http://dunwu.test.upcdn.net/snap/20200131153842.png)

### 向服务器发送消息

在默认情况下，Sentinel 会以每两秒一次的频率，通过命令向所有被监视的主服务器和从服务器发送以下格式的命令。

```
PUBLISH __sentinel__:hello "<s_ip>,<s_port>,<s_runid>,<s_epoch>,<m_name>,<m_ip>,<m_port>,<m_epoch>"
```

这条命令向服务器的 `__sentinel__:hello` 频道发送一条消息。

### 接收服务器的消息

当 Sentinel 与一个主服务器或从服务器建立起订阅连接后，Sentinel 就会通过订阅连接，向服务器发送以下命令：`SUBSCRIBE __sentinel__:hello`。

Sentinel 对 `__sentinel__:hello` 频道的订阅会一直持续到 Sentinel 与服务器断开连接为止。

## 五、选举 Leader

> Redis Sentinel 系统选举 Leader 的算法是 [Raft](https://ramcloud.atlassian.net/wiki/download/attachments/6586375/raft.pdf) 的实现。
>
> Raft 是一种共识性算法，想了解其原理，可以参考 [深入剖析共识性算法 Raft](https://github.com/dunwu/blog/blob/master/source/_posts/theory/raft.md)。

当一个主服务器被判断为客观下线时，监视这个下线主服务器的各个 Sentinel 会进行协商，选举出一个领头的 Sentinel，并由领头 Sentinel 对下线主服务器执行故障转移操作。

所有在线 Sentinel 都有资格被选为 Leader。

每个 `Sentinel` 节点都需要 **定期执行** 以下任务：

（1）每个 `Sentinel` 以 **每秒钟** 一次的频率，向它所知的 **主服务器**、**从服务器** 以及其他 `Sentinel` **实例** 发送一个 `PING` 命令。



![img](https://user-gold-cdn.xitu.io/2018/8/22/16560ce61df44c4d?imageView2/0/w/1280/h/960/format/webp/ignore-error/1)



（2）如果一个 **实例**（`instance`）距离 **最后一次** 有效回复 `PING` 命令的时间超过 `down-after-milliseconds` 所指定的值，那么这个实例会被 `Sentinel` 标记为 **主观下线**。



![img](https://user-gold-cdn.xitu.io/2018/8/22/16560ce61dc739de?imageView2/0/w/1280/h/960/format/webp/ignore-error/1)



（3）如果一个 **主服务器** 被标记为 **主观下线**，那么正在 **监视** 这个 **主服务器** 的所有 `Sentinel` 节点，要以 **每秒一次** 的频率确认 **主服务器** 的确进入了 **主观下线** 状态。



![img](https://user-gold-cdn.xitu.io/2018/8/22/16560ce647a39535?imageView2/0/w/1280/h/960/format/webp/ignore-error/1)



（4）如果一个 **主服务器** 被标记为 **主观下线**，并且有 **足够数量** 的 `Sentinel`（至少要达到 **配置文件** 指定的数量）在指定的 **时间范围** 内同意这一判断，那么这个 **主服务器** 被标记为 **客观下线**。



![img](https://user-gold-cdn.xitu.io/2018/8/22/16560ce647c2583e?imageView2/0/w/1280/h/960/format/webp/ignore-error/1)



（5）在一般情况下， 每个 `Sentinel` 会以每 `10` 秒一次的频率，向它已知的所有 **主服务器** 和 **从服务器** 发送 `INFO` 命令。当一个 **主服务器** 被 `Sentinel` 标记为 **客观下线** 时，`Sentinel` 向 **下线主服务器** 的所有 **从服务器** 发送 `INFO` 命令的频率，会从 `10` 秒一次改为 **每秒一次**。



![img](https://user-gold-cdn.xitu.io/2018/8/22/16560ce6738a30db?imageView2/0/w/1280/h/960/format/webp/ignore-error/1)



（6）`Sentinel` 和其他 `Sentinel` 协商 **主节点** 的状态，如果 **主节点** 处于 `SDOWN` 状态，则投票自动选出新的 **主节点**。将剩余的 **从节点** 指向 **新的主节点** 进行 **数据复制**。



![img](https://user-gold-cdn.xitu.io/2018/8/22/16560ce676a95a54?imageView2/0/w/1280/h/960/format/webp/ignore-error/1)



（7）当没有足够数量的 `Sentinel` 同意 **主服务器** 下线时， **主服务器** 的 **客观下线状态** 就会被移除。当 **主服务器** 重新向 `Sentinel` 的 `PING` 命令返回 **有效回复** 时，**主服务器** 的 **主观下线状态** 就会被移除。



![img](https://user-gold-cdn.xitu.io/2018/8/22/16560ce6759c1cb3?imageView2/0/w/1280/h/960/format/webp/ignore-error/1)



> 注意：一个有效的 `PING` 回复可以是：`+PONG`、`-LOADING` 或者 `-MASTERDOWN`。如果 **服务器** 返回除以上三种回复之外的其他回复，又或者在 **指定时间** 内没有回复 `PING` 命令， 那么 `Sentinel` 认为服务器返回的回复 **无效**（`non-valid`）。

## 六、故障转移

在选举产生出 Sentinel Leader 后，Sentinel Leader 将对已下线的主服务器执行故障转移操作。操作含以下三个步骤：

1. 选出新的主服务器
2. 修改从服务器的复制目标
3. 将旧的主服务器变为从服务器

## 七、要点总结

![img](http://dunwu.test.upcdn.net/snap/20200224221812.png)

## 参考资料

- **官网**
  - [Redis 官网](https://redis.io/)
  - [Redis Sentinel](https://redis.io/topics/sentinel)
- **书籍**
  - [《Redis 实战》](https://item.jd.com/11791607.html)
  - [《Redis 设计与实现》](https://item.jd.com/11486101.html)
- **文章**
  - [渐进式解析 Redis 源码 - 哨兵 sentinel](http://www.web-lovers.com/redis-source-sentinel.html)
  - [深入剖析Redis系列(二) - Redis哨兵模式与高可用集群](https://juejin.im/post/5b7d226a6fb9a01a1e01ff64)
