# Redis 哨兵

Redis 哨兵（Sentinel）是 Redis 的**高可用性**（Hight Availability）解决方案：由一个或多个 Sentinel 实例组成的 Sentinel 系统可以监视任意多个主服务器，以及这些主服务器的所有从服务器，并在被监视的主服务器进入下线状态时，自动将下线主服务器的某个从服务器升级为新的主服务器，然后由新的主服务器代替已下线的主服务器继续处理命令请求。

**Sentinel 本质上是一个运行在特殊状模式下的 Redis 服务器**。

![](https://raw.githubusercontent.com/dunwu/images/master/snap/20200131135847.png)

## 一、简介

Sentinel 的主要功能如下：

- **`监控（Monitoring）`** - Sentinel 不断检查主从服务器是否正常在工作。
- **`通知（Notification）`** - Sentinel 可以通过一个 api 来通知系统管理员或者另外的应用程序，被监控的 Redis 实例有一些问题。
- **`自动故障转移（Automatic Failover）`** - 如果一个主服务器下线，Sentinel 会开始自动故障转移：把一个从节点提升为主节点，并重新配置其他的从节点使用新的主节点，使用 Redis 服务的应用程序在连接的时候也被通知新的地址。
- **`配置提供者（Configuration provider）`** - Sentinel 给客户端的服务发现提供来源：对于一个给定的服务，客户端连接到 Sentinels 来寻找当前主节点的地址。当故障转移发生的时候，Sentinel 将报告新的地址。

## 二、启动

启动一个 Sentinel 可以使用下面任意一条命令，两条命令效果完全相同。

```
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

默认情况下，Sentinel 会以每秒一次的频率向所有与它创建了命令连接的实例（包括主服务器、从服务器、其他 Sentinel ）发送 `PING` 命令，并通过实例返回的 `PING` 命令回复来判断实例是否在线。

### 获取服务器信息

> Sentinel 向主服务器发送 `INFO` 命令，获取主服务器及它的从服务器信息。

- **获取主服务器信息** - Sentinel 默认会以每十秒一次的频率，通过命令连接向被监视的主服务器发送 `INFO` 命令，并通过分析 `INFO` 命令的回复来获取主服务器的当前信息。
- **获取从服务器信息** - 当 Sentinel 发现主服务器有新的从服务器出现时，Sentinel 除了会为这个新的从服务器创建相应的实例结构之外，Sentinel 还会创建连接到从服务器的命令连接和订阅连接。

## 四、通知

对于每个与 Sentinel 连接的服务器，Sentinel 既会向服务器的 `__sentinel__:hello` 频道发送消息，也会订阅服务器的 `__sentinel__:hello` 频道的消息。

![](https://raw.githubusercontent.com/dunwu/images/master/snap/20200131153842.png)

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

> Redis Sentinel 系统选举 Leader 的算法是 [Raft 一致性算法论文](https://ramcloud.atlassian.net/wiki/download/attachments/6586375/raft.pdf) 的实现。

当一个主服务器被判断为客观下线时，监视这个下线主服务器的各个 Sentinel 会进行协商，选举出一个领头的 Sentinel，并由领头 Sentinel 对下线主服务器执行故障转移操作。

所有在线 Sentinel 都有资格被选为 Leader。

## 六、故障转移

## 参考资料

- **官网**
  - [Redis 官网](https://redis.io/)
  - [Redis Sentinel](https://redis.io/topics/sentinel)
- **书籍**
  - [《Redis 实战》](https://item.jd.com/11791607.html)
  - [《Redis 设计与实现》](https://item.jd.com/11486101.html)
- **文章**
  - [渐进式解析 Redis 源码 - 哨兵 sentinel](http://www.web-lovers.com/redis-source-sentinel.html)
