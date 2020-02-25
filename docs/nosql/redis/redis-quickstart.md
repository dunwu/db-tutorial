# Redis 入门指南

<!-- TOC depthFrom:2 depthTo:2 -->

- [一、Redis 简介](#一redis-简介)
- [二、Redis 数据类型](#二redis-数据类型)
- [三、Redis 内存淘汰](#三redis-内存淘汰)
- [四、Redis 持久化](#四redis-持久化)
- [五、Redis 事件](#五redis-事件)
- [六、Redis 事务](#六redis-事务)
- [七、Redis 管道](#七redis-管道)
- [八、发布与订阅](#八发布与订阅)
- [九、复制](#九复制)
- [十、哨兵](#十哨兵)
- [十一、集群](#十一集群)
- [Redis Client](#redis-client)
- [扩展](#扩展)
- [参考资料](#参考资料)

<!-- /TOC -->

## 一、Redis 简介

> Redis 是速度非常快的非关系型（NoSQL）内存键值数据库，可以存储键和五种不同类型的值之间的映射。
>
> 键的类型只能为字符串，值支持的五种类型数据类型为：字符串、列表、集合、有序集合、散列表。

### Redis 使用场景

- **缓存** - 将热点数据放到内存中，设置内存的最大使用量以及过期淘汰策略来保证缓存的命中率。
- **计数器** - Redis 这种内存数据库能支持计数器频繁的读写操作。
- **应用限流** - 限制一个网站访问流量。
- **消息队列** - 使用 List 数据类型，它是双向链表。
- **查找表** - 使用 HASH 数据类型。
- **交集运算** - 使用 SET 类型，例如求两个用户的共同好友。
- **排行榜** - 使用 ZSET 数据类型。
- **分布式 Session** - 多个应用服务器的 Session 都存储到 Redis 中来保证 Session 的一致性。
- **分布式锁** - 除了可以使用 SETNX 实现分布式锁之外，还可以使用官方提供的 RedLock 分布式锁实现。

### Redis 的优势

- 性能极高 – Redis 能读的速度是 110000 次/s,写的速度是 81000 次/s。
- 丰富的数据类型 - 支持字符串、列表、集合、有序集合、散列表。
- 原子 - Redis 的所有操作都是原子性的。单个操作是原子性的。多个操作也支持事务，即原子性，通过 MULTI 和 EXEC 指令包起来。
- 持久化 - Redis 支持数据的持久化。可以将内存中的数据保存在磁盘中，重启的时候可以再次加载进行使用。
- 备份 - Redis 支持数据的备份，即 master-slave 模式的数据备份。
- 丰富的特性 - Redis 还支持发布订阅, 通知, key 过期等等特性。

### Redis 与 Memcached

Redis 与 Memcached 因为都可以用于缓存，所以常常被拿来做比较，二者主要有以下区别：

**数据类型**

- Memcached 仅支持字符串类型；
- 而 Redis 支持五种不同种类的数据类型，使得它可以更灵活地解决问题。

**数据持久化**

- Memcached 不支持持久化；
- Redis 支持两种持久化策略：RDB 快照和 AOF 日志。

**分布式**

- Memcached 不支持分布式，只能通过在客户端使用像一致性哈希这样的分布式算法来实现分布式存储，这种方式在存储和查询时都需要先在客户端计算一次数据所在的节点。
- Redis Cluster 实现了分布式的支持。

**内存管理机制**

- Memcached 将内存分割成特定长度的块来存储数据，以完全解决内存碎片的问题，但是这种方式会使得内存的利用率不高，例如块的大小为 128 bytes，只存储 100 bytes 的数据，那么剩下的 28 bytes 就浪费掉了。
- 在 Redis 中，并不是所有数据都一直存储在内存中，可以将一些很久没用的 value 交换到磁盘。而 Memcached 的数据则会一直在内存中。

## 二、Redis 数据类型

| 数据类型 | 可以存储的值           | 操作                                                                                                             |
| -------- | ---------------------- | ---------------------------------------------------------------------------------------------------------------- |
| STRING   | 字符串、整数或者浮点数 | 对整个字符串或者字符串的其中一部分执行操作</br> 对整数和浮点数执行自增或者自减操作                               |
| LIST     | 列表                   | 从两端压入或者弹出元素</br> 读取单个或者多个元素</br> 进行修剪，只保留一个范围内的元素                           |
| SET      | 无序集合               | 添加、获取、移除单个元素</br> 检查一个元素是否存在于集合中</br> 计算交集、并集、差集</br> 从集合里面随机获取元素 |
| HASH     | 包含键值对的无序散列表 | 添加、获取、移除单个键值对</br> 获取所有键值对</br> 检查某个键是否存在                                           |
| ZSET     | 有序集合               | 添加、获取、删除元素</br> 根据分值范围或者成员来获取元素</br> 计算一个键的排名                                   |

> [What Redis data structures look like](https://redislabs.com/ebook/part-1-getting-started/chapter-1-getting-to-know-redis/1-2-what-redis-data-structures-look-like/)

### STRING

<div align="center">
<img src="http://dunwu.test.upcdn.net/cs/database/redis/redis-datatype-string.png" width="400"/>
</div>

应用场景：缓存、计数器、共享 Session

命令：

| 命令  | 行为                                                 |
| ----- | ---------------------------------------------------- |
| `GET` | 获取存储在给定键中的值。                             |
| `SET` | 设置存储在给定键中的值。                             |
| `DEL` | 删除存储在给定键中的值（这个命令可以用于所有类型）。 |

> 更多命令请参考：[Redis String 类型命令](https://redis.io/commands#string)

示例：

```shell
127.0.0.1:6379> set hello world
OK
127.0.0.1:6379> get hello
"jack"
127.0.0.1:6379> del hello
(integer) 1
127.0.0.1:6379> get hello
(nil)
```

### HASH

<div align="center">
<img src="http://dunwu.test.upcdn.net/cs/database/redis/redis-datatype-hash.png" width="400"/>
</div>

场景：适合存储结构化数据，如一个对象：用户信息、产品信息等。

命令：

| 命令      | 行为                                       |
| --------- | ------------------------------------------ |
| `HSET`    | 在散列里面关联起给定的键值对。             |
| `HGET`    | 获取指定散列键的值。                       |
| `HGETALL` | 获取散列包含的所有键值对。                 |
| `HDEL`    | 如果给定键存在于散列里面，那么移除这个键。 |

> 更多命令请参考：[Redis Hash 类型命令](https://redis.io/commands#hash)

示例：

```shell
127.0.0.1:6379> hset hash-key sub-key1 value1
(integer) 1
127.0.0.1:6379> hset hash-key sub-key2 value2
(integer) 1
127.0.0.1:6379> hset hash-key sub-key1 value1
(integer) 0
127.0.0.1:6379> hset hash-key sub-key3 value2
(integer) 0
127.0.0.1:6379> hgetall hash-key
1) "sub-key1"
2) "value1"
3) "sub-key2"
4) "value2"
127.0.0.1:6379> hdel hash-key sub-key2
(integer) 1
127.0.0.1:6379> hdel hash-key sub-key2
(integer) 0
127.0.0.1:6379> hget hash-key sub-key1
"value1"
127.0.0.1:6379> hgetall hash-key
1) "sub-key1"
2) "value1"
```

### LIST

<div align="center">
<img src="http://dunwu.test.upcdn.net/cs/database/redis/redis-datatype-list.png" width="400"/>
</div>

适用场景：用于存储列表型数据。如：粉丝列表、商品列表等。

命令：

| 命令     | 行为                                       |
| -------- | ------------------------------------------ |
| `RPUSH`  | 将给定值推入列表的右端。                   |
| `LRANGE` | 获取列表在给定范围上的所有值。             |
| `LINDEX` | 获取列表在给定位置上的单个元素。           |
| `LPOP`   | 从列表的左端弹出一个值，并返回被弹出的值。 |

> 更多命令请参考：[Redis List 类型命令](https://redis.io/commands#list)

示例：

```shell
127.0.0.1:6379> rpush list-key item
(integer) 1
127.0.0.1:6379> rpush list-key item2
(integer) 2
127.0.0.1:6379> rpush list-key item
(integer) 3
127.0.0.1:6379> lrange list-key 0 -1
1) "item"
2) "item2"
3) "item"
127.0.0.1:6379> lindex list-key 1
"item2"
127.0.0.1:6379> lpop list-key
"item"
127.0.0.1:6379> lrange list-key 0 -1
1) "item2"
2) "item"
```

### SET

<div align="center">
<img src="http://dunwu.test.upcdn.net/cs/database/redis/redis-datatype-set.png" width="400"/>
</div>

适用场景：适用于存储不出现重复的列表数据。

命令：

| 命令        | 行为                                           |
| ----------- | ---------------------------------------------- |
| `SADD`      | 将给定元素添加到集合。                         |
| `SMEMBERS`  | 返回集合包含的所有元素。                       |
| `SISMEMBER` | 检查给定元素是否存在于集合中。                 |
| `SREM`      | 如果给定的元素存在于集合中，那么移除这个元素。 |

> 更多命令请参考：[Redis Set 类型命令](https://redis.io/commands#set)

示例：

```shell
127.0.0.1:6379> sadd set-key item
(integer) 1
127.0.0.1:6379> sadd set-key item2
(integer) 1
127.0.0.1:6379> sadd set-key item3
(integer) 1
127.0.0.1:6379> sadd set-key item
(integer) 0
127.0.0.1:6379> smembers set-key
1) "item"
2) "item2"
3) "item3"
127.0.0.1:6379> sismember set-key item4
(integer) 0
127.0.0.1:6379> sismember set-key item
(integer) 1
127.0.0.1:6379> srem set-key item2
(integer) 1
127.0.0.1:6379> srem set-key item2
(integer) 0
127.0.0.1:6379> smembers set-key
1) "item"
2) "item3"
```

### ZSET

<div align="center">
<img src="http://dunwu.test.upcdn.net/cs/database/redis/redis-datatype-zset.png" width="400"/>
</div>

场景：由于可以设置 score，且不重复。适合存储各种排行数据，如：按评分排序的有序商品集合、按时间排序的有序文章集合。

命令：

| 命令            | 行为                                                         |
| --------------- | ------------------------------------------------------------ |
| `ZADD`          | 将一个带有给定分值的成员添加到有序集合里面。                 |
| `ZRANGE`        | 根据元素在有序排列中所处的位置，从有序集合里面获取多个元素。 |
| `ZRANGEBYSCORE` | 获取有序集合在给定分值范围内的所有元素。                     |
| `ZREM`          | 如果给定成员存在于有序集合，那么移除这个成员。               |

> 更多命令请参考：[Redis ZSet 类型命令](https://redis.io/commands#sorted_set)

示例：

```shell
127.0.0.1:6379> zadd zset-key 728 member1
(integer) 1
127.0.0.1:6379> zadd zset-key 982 member0
(integer) 1
127.0.0.1:6379> zadd zset-key 982 member0
(integer) 0

127.0.0.1:6379> zrange zset-key 0 -1 withscores
1) "member1"
2) "728"
3) "member0"
4) "982"

127.0.0.1:6379> zrangebyscore zset-key 0 800 withscores
1) "member1"
2) "728"

127.0.0.1:6379> zrem zset-key member1
(integer) 1
127.0.0.1:6379> zrem zset-key member1
(integer) 0
127.0.0.1:6379> zrange zset-key 0 -1 withscores
1) "member0"
2) "982"
```

## 三、Redis 内存淘汰

### 内存淘汰要点

- **最大缓存** - Redis 允许通过 `maxmemory` 参数来设置内存最大值。

- **主键失效** - 作为一种定期清理无效数据的重要机制，在 Redis 提供的诸多命令中，`EXPIRE`、`EXPIREAT`、`PEXPIRE`、`PEXPIREAT` 以及 `SETEX` 和 `PSETEX` 均可以用来设置一条键值对的失效时间。而一条键值对一旦被关联了失效时间就会在到期后自动删除（或者说变得无法访问更为准确）。

- **淘汰策略** - 随着不断的向 Redis 中保存数据，当内存剩余空间无法满足添加的数据时，Redis 内就会施行数据淘汰策略，清除一部分内容然后保证新的数据可以保存到内存中。内存淘汰机制是为了更好的使用内存，用一定得 miss 来换取内存的利用率，保证 Redis 缓存中保存的都是热点数据。

- **非精准的 LRU** - 实际上 Redis 实现的 LRU 并不是可靠的 LRU，也就是名义上我们使用 LRU 算法淘汰键，但是实际上被淘汰的键并不一定是真正的最久没用的。

### 主键过期时间

Redis 可以为每个键设置过期时间，当键过期时，会自动删除该键。

对于散列表这种容器，只能为整个键设置过期时间（整个散列表），而不能为键里面的单个元素设置过期时间。

可以使用 `EXPIRE` 或 `EXPIREAT` 来为 key 设置过期时间。

> 🔔 注意：当 `EXPIRE` 的时间如果设置的是负数，`EXPIREAT` 设置的时间戳是过期时间，将直接删除 key。

示例：

```py
redis> SET mykey "Hello"
"OK"
redis> EXPIRE mykey 10
(integer) 1
redis> TTL mykey
(integer) 10
redis> SET mykey "Hello World"
"OK"
redis> TTL mykey
(integer) -1
redis>
```

### 淘汰策略

内存淘汰只是 Redis 提供的一个功能，为了更好地实现这个功能，必须为不同的应用场景提供不同的策略，内存淘汰策略讲的是为实现内存淘汰我们具体怎么做，要解决的问题包括淘汰键空间如何选择？在键空间中淘汰键如何选择？

Redis 提供了下面几种淘汰策略供用户选：

- **`noeviction`** - 当内存使用达到阈值的时候，所有引起申请内存的命令会报错。这是 Redis 默认的策略。
- **`allkeys-lru`** - 在主键空间中，优先移除最近未使用的 key。
- **`allkeys-random`** - 在主键空间中，随机移除某个 key。
- **`volatile-lru`** - 在设置了过期时间的键空间中，优先移除最近未使用的 key。
- **`volatile-random`** - 在设置了过期时间的键空间中，随机移除某个 key。
- **`volatile-ttl`** - 在设置了过期时间的键空间中，具有更早过期时间的 key 优先移除。

### 如何选择淘汰策略

- 如果数据呈现幂等分布，也就是一部分数据访问频率高，一部分数据访问频率低，则使用 `allkeys-lru`。
- 如果数据呈现平等分布，也就是所有的数据访问频率都相同，则使用 `allkeys-random`。
- `volatile-lru` 策略和 `volatile-random` 策略适合我们将一个 Redis 实例既应用于缓存和又应用于持久化存储的时候，然而我们也可以通过使用两个 Redis 实例来达到相同的效果。
- 将 key 设置过期时间实际上会消耗更多的内存，因此我们建议使用 `allkeys-lru` 策略从而更有效率的使用内存。

### 内部实现

Redis 删除失效主键的方法主要有两种：

- 消极方法（passive way），在主键被访问时如果发现它已经失效，那么就删除它。
- 主动方法（active way），周期性地从设置了失效时间的主键中选择一部分失效的主键删除。
- 主动删除：当前已用内存超过 `maxmemory` 限定时，触发主动清理策略，该策略由启动参数的配置决定主键具体的失效时间全部都维护在 expires 这个字典表中。

## 四、Redis 持久化

Redis 是内存型数据库，为了保证数据在宕机后不会丢失，需要将内存中的数据持久化到硬盘上。

Redis 支持两种持久化方式：RDB 和 AOF。

### RDB

**RDB 即快照方式，它将某个时间点的所有 Redis 数据保存到一个经过压缩的二进制文件（RDB 文件）中**。

创建 RDB 后，用户可以对 RDB 进行**备份**，可以将 RDB **复制**到其他服务器从而创建具有相同数据的服务器副本，还可以在**重启**服务器时使用。一句话来说：RDB 适合作为 **冷备**。

RDB 既可以手动执行，也可以根据服务器配置选项定期执行。该功能可以将某个时间点的数据库状态保存到一个 RDB 文件中。

有两个 Redis 命令可以用于生成 RDB 文件：`SAVE` 和 `BGSAVE`。

- [SAVE](https://redis.io/commands/save) 命令会阻塞 Redis 服务器进程，直到 RDB 创建完成为止，在阻塞期间，服务器不能响应任何命令请求。
- [BGSAVE](https://redis.io/commands/bgsave) 命令会 fork 一个子进程，然后由子进程负责创建 RDB 文件，服务器进程（父进程）继续处理命令请求。

RDB 的优点：

- RDB 文件非常紧凑，适合作为冷备。
- 恢复大数据集时，RDB 比 AOF 快。

RDB 的缺点：

- 如果系统发生故障，将会丢失最后一次创建快照之后的数据。

- 如果数据量很大，保存快照的时间会很长。

### AOF

`AOF(Append Only File)` 是以文本日志形式将所有写命令追加到 AOF 文件的末尾，以此来记录数据的变化。当服务器重启的时候会重新载入和执行这些命令来恢复原始的数据。AOF 适合作为 **热备**。

AOF 可以通过 `appendonly yes` 配置选项来开启。

命令请求会先保存到 AOF 缓冲区中，之后再定期写入并同步到 AOF 文件。

可以通过 `appendfsync` 配置选项来设置同步频率，它有以下可选项：

- **`always`** - 每个 Redis 写命令都要同步写入硬盘。这样做会严重降低 Redis 的速度。
- **`everysec`** - 每秒执行一次同步，显示地将多个写命令同步到硬盘。为了兼顾数据安全和写入性能，推荐使用 `appendfsync everysec` 选项。Redis 每秒同步一次 AOF 文件时的性能和不使用任何持久化特性时的性能相差无几。
- **`no`** - 让操作系统来决定应该何时进行同步。

AOF 的优点：

- 如果系统发生故障，AOF 丢失数据比 RDB 少。
- AOF 文件可修复。
- AOF 文件可压缩。
- AOF 文件可读。

AOF 的缺点：

- AOF 文件体积一般比 RDB 大。
- 恢复大数据集时，AOF 比 RDB 慢。

> :bulb: 更详细的特性及原理说明请参考：[Redis 持久化](redis-persistence.md)

## 五、Redis 事件

Redis 服务器是一个事件驱动程序，服务器需要处理两类事件：

- **`文件事件（file event）`** - Redis 服务器通过套接字（Socket）与客户端或者其它服务器进行通信，文件事件就是对套接字操作的抽象。服务器与客户端（或其他的服务器）的通信会产生文件事件，而服务器通过监听并处理这些事件来完成一系列网络通信操作。
- **`时间事件（time event）`** - Redis 服务器有一些操作需要在给定的时间点执行，时间事件是对这类定时操作的抽象。

### 文件事件

Redis 基于 Reactor 模式开发了自己的网络时间处理器。

- Redis 文件事件处理器使用 I/O 多路复用程序来同时监听多个套接字，并根据套接字目前执行的任务来为套接字关联不同的事件处理器。
- 当被监听的套接字准备好执行连接应答、读取、写入、关闭操作时，与操作相对应的文件事件就会产生，这时文件事件处理器就会调用套接字之前关联好的事件处理器来处理这些事件。

虽然文件事件处理器以单线程方式运行，但通过使用 I/O 多路复用程序来监听多个套接字，文件事件处理器实现了高性能的网络通信模型。

文件事件处理器有四个组成部分：套接字、I/O 多路复用程序、文件事件分派器、事件处理器。

![img](https://raw.githubusercontent.com/dunwu/images/master/snap/20200130172525.png)

### 时间事件

时间事件又分为：

- **定时事件**：是让一段程序在指定的时间之内执行一次；
- **周期性事件**：是让一段程序每隔指定时间就执行一次。

Redis 将所有时间事件都放在一个无序链表中，每当时间事件执行器运行时，通过遍历整个链表查找出已到达的时间事件，并调用响应的事件处理器。

### 事件的调度与执行

服务器需要不断监听文件事件的套接字才能得到待处理的文件事件，但是不能一直监听，否则时间事件无法在规定的时间内执行，因此监听时间应该根据距离现在最近的时间事件来决定。

事件调度与执行由 aeProcessEvents 函数负责，伪代码如下：

```python
def aeProcessEvents():

    ## 获取到达时间离当前时间最接近的时间事件
    time_event = aeSearchNearestTimer()

    ## 计算最接近的时间事件距离到达还有多少毫秒
    remaind_ms = time_event.when - unix_ts_now()

    ## 如果事件已到达，那么 remaind_ms 的值可能为负数，将它设为 0
    if remaind_ms < 0:
        remaind_ms = 0

    ## 根据 remaind_ms 的值，创建 timeval
    timeval = create_timeval_with_ms(remaind_ms)

    ## 阻塞并等待文件事件产生，最大阻塞时间由传入的 timeval 决定
    aeApiPoll(timeval)

    ## 处理所有已产生的文件事件
    procesFileEvents()

    ## 处理所有已到达的时间事件
    processTimeEvents()
```

将 aeProcessEvents 函数置于一个循环里面，加上初始化和清理函数，就构成了 Redis 服务器的主函数，伪代码如下：

```python
def main():

    ## 初始化服务器
    init_server()

    ## 一直处理事件，直到服务器关闭为止
    while server_is_not_shutdown():
        aeProcessEvents()

    ## 服务器关闭，执行清理操作
    clean_server()
```

从事件处理的角度来看，服务器运行流程如下：

<div align="center">
<img src="http://dunwu.test.upcdn.net/cs/database/redis/redis-event.png" />
</div>

## 六、Redis 事务

> **Redis 提供的不是严格的事务，Redis 只保证串行执行命令，并且能保证全部执行，但是执行命令失败时并不会回滚，而是会继续执行下去**。

`MULTI` 、 `EXEC` 、 `DISCARD` 和 `WATCH` 是 Redis 事务相关的命令。

事务可以一次执行多个命令， 并且有以下两个重要的保证：

- 事务是一个单独的隔离操作：事务中的所有命令都会序列化、按顺序地执行。事务在执行的过程中，不会被其他客户端发送来的命令请求所打断。
- 事务是一个原子操作：事务中的命令要么全部被执行，要么全部都不执行。

### MULTI

**[`MULTI`](https://redis.io/commands/multi) 命令用于开启一个事务，它总是返回 OK 。**

`MULTI` 执行之后， 客户端可以继续向服务器发送任意多条命令， 这些命令不会立即被执行， 而是被放到一个队列中， 当 EXEC 命令被调用时， 所有队列中的命令才会被执行。

以下是一个事务例子， 它原子地增加了 foo 和 bar 两个键的值：

```python
> MULTI
OK
> INCR foo
QUEUED
> INCR bar
QUEUED
> EXEC
1) (integer) 1
2) (integer) 1
```

### EXEC

**[`EXEC`](https://redis.io/commands/exec) 命令负责触发并执行事务中的所有命令。**

- 如果客户端在使用 `MULTI` 开启了一个事务之后，却因为断线而没有成功执行 `EXEC` ，那么事务中的所有命令都不会被执行。
- 另一方面，如果客户端成功在开启事务之后执行 `EXEC` ，那么事务中的所有命令都会被执行。

### DISCARD

**当执行 [`DISCARD`](https://redis.io/commands/discard) 命令时， 事务会被放弃， 事务队列会被清空， 并且客户端会从事务状态中退出。**

示例：

```python
> SET foo 1
OK
> MULTI
OK
> INCR foo
QUEUED
> DISCARD
OK
> GET foo
"1"
```

### WATCH

**[`WATCH`](https://redis.io/commands/watch) 命令可以为 Redis 事务提供 check-and-set （CAS）行为。**

被 WATCH 的键会被监视，并会发觉这些键是否被改动过了。 如果有至少一个被监视的键在 EXEC 执行之前被修改了， 那么整个事务都会被取消， EXEC 返回 nil-reply 来表示事务已经失败。

```python
WATCH mykey
val = GET mykey
val = val + 1
MULTI
SET mykey $val
EXEC
```

使用上面的代码， 如果在 WATCH 执行之后， EXEC 执行之前， 有其他客户端修改了 mykey 的值， 那么当前客户端的事务就会失败。 程序需要做的， 就是不断重试这个操作， 直到没有发生碰撞为止。

这种形式的锁被称作乐观锁， 它是一种非常强大的锁机制。 并且因为大多数情况下， 不同的客户端会访问不同的键， 碰撞的情况一般都很少， 所以通常并不需要进行重试。

WATCH 使得 EXEC 命令需要有条件地执行：事务只能在所有被监视键都没有被修改的前提下执行，如果这个前提不能满足的话，事务就不会被执行。

WATCH 命令可以被调用多次。对键的监视从 WATCH 执行之后开始生效，直到调用 EXEC 为止。

用户还可以在单个 WATCH 命令中监视任意多个键，例如：

```python
redis> WATCH key1 key2 key3
OK
```

#### 取消 WATCH 的场景

当 EXEC 被调用时， 不管事务是否成功执行， 对所有键的监视都会被取消。

另外， 当客户端断开连接时， 该客户端对键的监视也会被取消。

使用无参数的 UNWATCH 命令可以手动取消对所有键的监视。 对于一些需要改动多个键的事务， 有时候程序需要同时对多个键进行加锁， 然后检查这些键的当前值是否符合程序的要求。 当值达不到要求时， 就可以使用 UNWATCH 命令来取消目前对键的监视， 中途放弃这个事务， 并等待事务的下次尝试。

#### 使用 WATCH 创建原子操作

WATCH 可以用于创建 Redis 没有内置的原子操作。

举个例子，以下代码实现了原创的 ZPOP 命令，它可以原子地弹出有序集合中分值（score）最小的元素：

```
WATCH zset
element = ZRANGE zset 0 0
MULTI
ZREM zset element
EXEC
```

### Rollback

**Redis 不支持回滚**。Redis 不支持回滚的理由：

- Redis 命令只会因为错误的语法而失败，或是命令用在了错误类型的键上面。
- 因为不需要对回滚进行支持，所以 Redis 的内部可以保持简单且快速。

## 七、Redis 管道

Redis 是一种基于 C/S 模型以及请求/响应协议的 TCP 服务。Redis 支持管道技术。管道技术允许请求以异步方式发送，即旧请求的应答还未返回的情况下，允许发送新请求。这种方式可以大大提高传输效率。

在需要批量执行 Redis 命令时，如果一条一条执行，显然很低效。为了减少通信次数并降低延迟，可以使用 Redis 管道功能。Redis 的管道（pipeline）功能没有提供命令行支持，但是在各种语言版本的客户端中都有相应的实现。

以 Jedis 为例：

```java
Pipeline pipe = conn.pipelined();
pipe.multi();
pipe.hset("login:", token, user);
pipe.zadd("recent:", timestamp, token);
if (item != null) {
    pipe.zadd("viewed:" + token, timestamp, item);
    pipe.zremrangeByRank("viewed:" + token, 0, -26);
    pipe.zincrby("viewed:", -1, item);
}
pipe.exec();
```

> :bell: 注意：使用管道发送命令时，Redis Server 会将部分请求放到缓存队列中（占用内存），执行完毕后一次性发送结果。如果需要发送大量的命令，会占用大量的内存，因此应该按照合理数量分批次的处理。

## 八、发布与订阅

订阅者订阅了频道之后，发布者向频道发送字符串消息会被所有订阅者接收到。

某个客户端使用 SUBSCRIBE 订阅一个频道，其它客户端可以使用 PUBLISH 向这个频道发送消息。

发布与订阅模式和观察者模式有以下不同：

- 观察者模式中，观察者和主题都知道对方的存在；而在发布与订阅模式中，发布者与订阅者不知道对方的存在，它们之间通过频道进行通信。
- 观察者模式是同步的，当事件触发时，主题会去调用观察者的方法；而发布与订阅模式是异步的；

---

***分割线以下为 Redis 集群功能特性***

## 九、复制

> 关系型数据库通常会使用一个主服务器向多个从服务器发送更新，并使用从服务器来处理所有读请求，Redis 也采用了同样的方式来实现复制特性。

### 旧版复制

Redis 2.8 版本以前的复制功能基于 `SYNC` 命令实现。

Redis 的复制功能分为同步（sync）和命令传播（command propagate）两个操作：

- **`同步（sync）`** - 用于将从服务器的数据库状态更新至主服务器当前的数据库状态。
- **`命令传播（command propagate）`** - 当主服务器的数据库状态被修改，导致主从数据库状态不一致时，让主从服务器的数据库重新回到一致状态。

这种方式存在缺陷：不能高效处理断线重连后的复制情况。

### 新版复制

Redis 2.8 版本以后的复制功能基于 `PSYNC` 命令实现。`PSYNC` 命令具有完整重同步和部分重同步两种模式。

- **`完整重同步（full resychronization）`** - 用于初次复制。执行步骤与 `SYNC` 命令基本一致。
- **`部分重同步（partial resychronization）`** - 用于断线后重复制。**如果条件允许，主服务器可以将主从服务器连接断开期间执行的写命令发送给从服务器**，从服务器只需接收并执行这些写命令，即可将主从服务器的数据库状态保持一致。

### 部分重同步

部分重同步有三个组成部分：

- 主从服务器的**复制偏移量（replication offset）**
- 主服务器的**复制积压缓冲区（replication backlog）**
- **服务器的运行 ID**

### PSYNC 命令

从服务器向要复制的主服务器发送 `PSYNC <runid> <offset>` 命令

- 假如主从服务器的 **master run id 相同**，并且**指定的偏移量（offset）在内存缓冲区中还有效**，复制就会从上次中断的点开始继续。
- 如果其中一个条件不满足，就会进行完全重新同步。

### 心跳检测

主服务器通过向从服务传播命令来更新从服务器状态，保持主从数据一致。

从服务器通过向主服务器发送命令 `REPLCONF ACK <replication_offset>` 来进行心跳检测，以及命令丢失检测。

> :bulb: 更详细的特性及原理说明请参考：[Redis 复制](redis-replication.md)

## 十、哨兵

Sentinel（哨兵）可以监听主服务器，并在主服务器进入下线状态时，自动从从服务器中选举出新的主服务器。

> 💡 更详细的特性及原理说明请参考：[Redis 哨兵](redis-sentinel.md)

## 十一、集群

分片是将数据划分为多个部分的方法，可以将数据存储到多台机器里面，也可以从多台机器里面获取数据，这种方法在解决某些问题时可以获得线性级别的性能提升。

假设有 4 个 Reids 实例 R0，R1，R2，R3，还有很多表示用户的键 user:1，user:2，... 等等，有不同的方式来选择一个指定的键存储在哪个实例中。最简单的方式是范围分片，例如用户 id 从 0\~1000 的存储到实例 R0 中，用户 id 从 1001\~2000 的存储到实例 R1 中，等等。但是这样需要维护一张映射范围表，维护操作代价很高。还有一种方式是哈希分片，使用 CRC32 哈希函数将键转换为一个数字，再对实例数量求模就能知道应该存储的实例。

主要有三种分片方式：

- 客户端分片：客户端使用一致性哈希等算法决定键应当分布到哪个节点。
- 代理分片：将客户端请求发送到代理上，由代理转发请求到正确的节点上。
- 服务器分片：Redis Cluster。

- https://github.com/lettuce-io/lettuce-core)

-

## Redis Client

Redis 社区中有多种编程语言的客户端，可以在这里查找合适的客户端：https://redis.io/clients

redis 官方推荐的 Java Redis Client：

- [jedis](https://github.com/xetorthio/jedis)
- [redisson](https://github.com/redisson/redisson)
- [lettuce](https://github.com/lettuce-io/lettuce-core)

## 扩展

### 缓存

> 💡 Redis 常用于分布式缓存，有关缓存的特性和原理请参考：[缓存基本原理](https://dunwu.github.io/blog/design/theory/cache-theory/)

## 参考资料

- **官网**
  - [Redis 官网](https://redis.io/)
  - [Redis github](https://github.com/antirez/redis)
  - [官方文档翻译版本一](http://ifeve.com/redis-sentinel/) 翻译,排版一般,新
  - [官方文档翻译版本二](http://redisdoc.com/topic/sentinel.html) 翻译有段时间了,但主要部分都包含,排版好
- **书籍**
  - [《Redis 实战》](https://item.jd.com/11791607.html)
  - [《Redis 设计与实现》](https://item.jd.com/11486101.html)
- **资源汇总**
  - [awesome-redis](https://github.com/JamzyWang/awesome-redis)
- **Redis Client**
  - [spring-data-redis 官方文档](https://docs.spring.io/spring-data/redis/docs/1.8.13.RELEASE/reference/html/)
  - [redisson 官方文档(中文,略有滞后)](https://github.com/redisson/redisson/wiki/%E7%9B%AE%E5%BD%95)
  - [redisson 官方文档(英文)](https://github.com/redisson/redisson/wiki/Table-of-Content)
  - [CRUG | Redisson PRO vs. Jedis: Which Is Faster? 翻译](https://www.jianshu.com/p/82f0d5abb002)
  - [redis 分布锁 Redisson 性能测试](https://blog.csdn.net/everlasting_188/article/details/51073505)
