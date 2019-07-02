---
title: Redis
date: 2018/06/09
categories:
- database
tags:
- database
- key-value
- cache
---

# Redis

<!-- TOC depthFrom:2 depthTo:3 -->

- [1. 概述](#1-概述)
    - [1.1. Redis 简介](#11-redis-简介)
    - [1.2. Redis 的优势](#12-redis-的优势)
    - [1.3. Redis 与 Memcached](#13-redis-与-memcached)
- [2. 数据类型](#2-数据类型)
    - [2.1. STRING](#21-string)
    - [2.2. LIST](#22-list)
    - [2.3. SET](#23-set)
    - [2.4. HASH](#24-hash)
    - [2.5. ZSET](#25-zset)
- [3. 使用场景](#3-使用场景)
- [4. Redis 管道](#4-redis-管道)
- [5. 键的过期时间](#5-键的过期时间)
- [6. 数据淘汰策略](#6-数据淘汰策略)
- [7. 持久化](#7-持久化)
    - [7.1. 快照持久化](#71-快照持久化)
    - [7.2. AOF 持久化](#72-aof-持久化)
- [8. 发布与订阅](#8-发布与订阅)
- [9. 事务](#9-事务)
    - [9.1. EXEC](#91-exec)
    - [9.2. MULTI](#92-multi)
    - [9.3. DISCARD](#93-discard)
    - [9.4. WATCH](#94-watch)
- [10. 事件](#10-事件)
    - [10.1. 文件事件](#101-文件事件)
    - [10.2. 时间事件](#102-时间事件)
    - [10.3. 事件的调度与执行](#103-事件的调度与执行)
- [11. 集群](#11-集群)
    - [11.1. 复制](#111-复制)
    - [11.2. 哨兵](#112-哨兵)
    - [11.3. 分片](#113-分片)
- [12. Redis Client](#12-redis-client)
- [13. 资料](#13-资料)

<!-- /TOC -->

## 1. 概述

### 1.1. Redis 简介

Redis 是速度非常快的非关系型（NoSQL）内存键值数据库，可以存储键和五种不同类型的值之间的映射。

键的类型只能为字符串，值支持的五种类型数据类型为：字符串、列表、集合、有序集合、散列表。

Redis 支持很多特性，例如将内存中的数据持久化到硬盘中，使用复制来扩展读性能，使用分片来扩展写性能。

### 1.2. Redis 的优势

- 性能极高 – Redis 能读的速度是 110000 次/s,写的速度是 81000 次/s。
- 丰富的数据类型 - 支持字符串、列表、集合、有序集合、散列表。
- 原子 - Redis 的所有操作都是原子性的。单个操作是原子性的。多个操作也支持事务，即原子性，通过 MULTI 和 EXEC 指令包起来。
- 持久化 - Redis 支持数据的持久化。可以将内存中的数据保存在磁盘中，重启的时候可以再次加载进行使用。
- 备份 - Redis 支持数据的备份，即 master-slave 模式的数据备份。
- 丰富的特性 - Redis 还支持发布订阅, 通知, key 过期等等特性。

### 1.3. Redis 与 Memcached

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

## 2. 数据类型

| 数据类型 | 可以存储的值           | 操作                                                                                                             |
| -------- | ---------------------- | ---------------------------------------------------------------------------------------------------------------- |
| STRING   | 字符串、整数或者浮点数 | 对整个字符串或者字符串的其中一部分执行操作</br> 对整数和浮点数执行自增或者自减操作                               |
| LIST     | 列表                   | 从两端压入或者弹出元素</br> 读取单个或者多个元素</br> 进行修剪，只保留一个范围内的元素                           |
| SET      | 无序集合               | 添加、获取、移除单个元素</br> 检查一个元素是否存在于集合中</br> 计算交集、并集、差集</br> 从集合里面随机获取元素 |
| HASH     | 包含键值对的无序散列表 | 添加、获取、移除单个键值对</br> 获取所有键值对</br> 检查某个键是否存在                                           |
| ZSET     | 有序集合               | 添加、获取、删除元素</br> 根据分值范围或者成员来获取元素</br> 计算一个键的排名                                   |

> [What Redis data structures look like](https://redislabs.com/ebook/part-1-getting-started/chapter-1-getting-to-know-redis/1-2-what-redis-data-structures-look-like/)

### 2.1. STRING

<div align="center">
<img src="https://gitee.com/turnon/images/raw/master/images/database/redis/redis-datatype-string.png" width="400"/>
</div>

命令：

| 命令 | 行为                                               |
| ---- | -------------------------------------------------- |
| GET  | 获取存储在给定键中的值                             |
| SET  | 设置存储在给定键中的值                             |
| DEL  | 删除存储在给定键中的值（这个命令可以用于所有类型） |

示例：

```py
127.0.0.1:6379> set name jack
OK
127.0.0.1:6379> get name
"jack"
127.0.0.1:6379> del name
(integer) 1
127.0.0.1:6379> get name
(nil)
```

### 2.2. LIST

<div align="center">
<img src="https://gitee.com/turnon/images/raw/master/images/database/redis/redis-datatype-list.png" width="400"/>
</div>

命令：

| 命令   | 行为                                               |
| ------ | -------------------------------------------------- |
| RPUSH  | 获取存储在给定键中的值                             |
| LRANGE | 设置存储在给定键中的值                             |
| LINDEX | 删除存储在给定键中的值（这个命令可以用于所有类型） |
| LPOP   | 删除存储在给定键中的值（这个命令可以用于所有类型） |

示例：

```py
127.0.0.1:6379> rpush list item1
(integer) 1
127.0.0.1:6379> rpush list item2
(integer) 2
127.0.0.1:6379> rpush list item3
(integer) 3
127.0.0.1:6379> lrange list 0 -1
1) "item1"
2) "item2"
3) "item3"
127.0.0.1:6379> lindex list 1
"item2"
127.0.0.1:6379> lpop list
"item1"
127.0.0.1:6379> lrange list 0 -1
1) "item2"
2) "item3"
```

### 2.3. SET

<div align="center">
<img src="https://gitee.com/turnon/images/raw/master/images/database/redis/redis-datatype-set.png" width="400"/>
</div>

命令：

| 命令      | 行为                             |
| --------- | -------------------------------- |
| SADD      | 添加一个或多个元素到集合里       |
| SMEMBERS  | 获取集合里面的所有元素           |
| SISMEMBER | 确定一个给定的值是一个集合的成员 |
| SREM      | 从集合里删除一个或多个元素       |

示例：

```py
127.0.0.1:6379> sadd set item1
(integer) 1
127.0.0.1:6379> sadd set item2
(integer) 1
127.0.0.1:6379> sadd set item3
(integer) 1
127.0.0.1:6379> sadd set item3
(integer) 0

127.0.0.1:6379> smembers set
1) "item3"
2) "item2"
3) "item1"

127.0.0.1:6379> sismember set item2
(integer) 1
127.0.0.1:6379> sismember set item6
(integer) 0

127.0.0.1:6379> srem set item2
(integer) 1
127.0.0.1:6379> srem set item2
(integer) 0

127.0.0.1:6379> smembers set
1) "item3"
2) "item1"
```

### 2.4. HASH

<div align="center">
<img src="https://gitee.com/turnon/images/raw/master/images/database/redis/redis-datatype-hash.png" width="400"/>
</div>

命令：

| 命令    | 行为                       |
| ------- | -------------------------- |
| HSET    | 设置 hash 里面一个字段的值 |
| HGET    | 获取 hash 中域的值         |
| HGETALL | 从 hash 中读取全部的域和值 |
| HDEL    | 删除一个或多个域           |

示例：

```py
127.0.0.1:6379> hset myhash key1 value1
(integer) 1
127.0.0.1:6379> hset myhash key2 value2
(integer) 1
127.0.0.1:6379> hset myhash key3 value3
(integer) 1
127.0.0.1:6379> hset myhash key3 value2
(integer) 0

127.0.0.1:6379> hgetall myhash
1) "key1"
2) "value1"
3) "key2"
4) "value2"
5) "key3"
6) "value2"

127.0.0.1:6379> hdel myhash key2
(integer) 1
127.0.0.1:6379> hdel myhash key2
(integer) 0

127.0.0.1:6379> hget myhash key2
(nil)

127.0.0.1:6379> hgetall myhash
1) "key1"
2) "value1"
3) "key3"
4) "value2"
127.0.0.1:6379>
```

### 2.5. ZSET

<div align="center">
<img src="https://gitee.com/turnon/images/raw/master/images/database/redis/redis-datatype-zset.png" width="400"/>
</div>

命令：

| 命令          | 行为                                                          |
| ------------- | ------------------------------------------------------------- |
| ZADD          | 添加到有序 set 的一个或多个成员，或更新的分数，如果它已经存在 |
| ZRANGE        | 根据指定的 index 返回，返回 sorted set 的成员列表             |
| ZRANGEBYSCORE | 返回有序集合中指定分数区间内的成员，分数由低到高排序。        |
| ZREM          | 从排序的集合中删除一个或多个成员                              |

示例：

```py
127.0.0.1:6379> zadd zset 1 redis
(integer) 1
127.0.0.1:6379> zadd zset 2 mongodb
(integer) 1
127.0.0.1:6379> zadd zset 3 mysql
(integer) 1
127.0.0.1:6379> zadd zset 3 mysql
(integer) 0
127.0.0.1:6379> zadd zset 4 mysql
(integer) 0

127.0.0.1:6379> zrange zset 0 -1 withscores
1) "redis"
2) "1"
3) "mongodb"
4) "2"
5) "mysql"
6) "4"

127.0.0.1:6379> zrangebyscore zset 0 2 withscores
1) "redis"
2) "1"
3) "mongodb"
4) "2"

127.0.0.1:6379> zrem zset mysql
(integer) 1
127.0.0.1:6379> zrange zset 0 -1 withscores
1) "redis"
2) "1"
3) "mongodb"
4) "2"
```

## 3. 使用场景

- **缓存** - 将热点数据放到内存中，设置内存的最大使用量以及过期淘汰策略来保证缓存的命中率。
- **计数器** - Redis 这种内存数据库能支持计数器频繁的读写操作。
- **应用限流** - 限制一个网站访问流量。
- **消息队列** - 使用 List 数据类型，它是双向链表。
- **查找表** - 使用 HASH 数据类型。
- **交集运算** - 使用 SET 类型，例如求两个用户的共同好友。
- **排行榜** - 使用 ZSET 数据类型。
- **分布式 Session** - 多个应用服务器的 Session 都存储到 Redis 中来保证 Session 的一致性。
- **分布式锁** - 除了可以使用 SETNX 实现分布式锁之外，还可以使用官方提供的 RedLock 分布式锁实现。

## 4. Redis 管道

Redis 是一种基于 C/S 模型以及请求/响应协议的 TCP 服务。

Redis 支持管道技术。管道技术允许请求以异步方式发送，即旧请求的应答还未返回的情况下，允许发送新请求。这种方式可以大大提高传输效率。

使用管道发送命令时，服务器将被迫回复一个队列答复，占用很多内存。所以，如果你需要发送大量的命令，最好是把他们按照合理数量分批次的处理。

## 5. 键的过期时间

Redis 可以为每个键设置过期时间，当键过期时，会自动删除该键。

对于散列表这种容器，只能为整个键设置过期时间（整个散列表），而不能为键里面的单个元素设置过期时间。

可以使用 `EXPIRE` 或 `EXPIREAT` 来为 key 设置过期时间。

> 注意：当 `EXPIRE` 的时间如果设置的是负数，`EXPIREAT` 设置的时间戳是过期时间，将直接删除 key。

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

## 6. 数据淘汰策略

可以设置内存最大使用量，当内存使用量超过时施行淘汰策略，具体有 6 种淘汰策略。

| 策略            | 描述                                                 |
| --------------- | ---------------------------------------------------- |
| volatile-lru    | 从已设置过期时间的数据集中挑选最近最少使用的数据淘汰 |
| volatile-ttl    | 从已设置过期时间的数据集中挑选将要过期的数据淘汰     |
| volatile-random | 从已设置过期时间的数据集中任意选择数据淘汰           |
| allkeys-lru     | 从所有数据集中挑选最近最少使用的数据淘汰             |
| allkeys-random  | 从所有数据集中任意选择数据进行淘汰                   |
| noeviction      | 禁止驱逐数据                                         |

如果使用 Redis 来缓存数据时，要保证所有数据都是热点数据，可以将内存最大使用量设置为热点数据占用的内存量，然后启用 allkeys-lru 淘汰策略，将最近最少使用的数据淘汰。

作为内存数据库，出于对性能和内存消耗的考虑，Redis 的淘汰算法（LRU、TTL）实际实现上并非针对所有 key，而是抽样一小部分 key 从中选出被淘汰 key，抽样数量可通过 maxmemory-samples 配置。

## 7. 持久化

Redis 是内存型数据库，为了保证数据在断电后不会丢失，需要将内存中的数据持久化到硬盘上。

### 7.1. 快照持久化

将某个时间点的所有数据都存放到硬盘上。

可以将快照复制到其它服务器从而创建具有相同数据的服务器副本。

如果系统发生故障，将会丢失最后一次创建快照之后的数据。

如果数据量很大，保存快照的时间会很长。

### 7.2. AOF 持久化

将写命令添加到 AOF 文件（Append Only File）的末尾。

对硬盘的文件进行写入时，写入的内容首先会被存储到缓冲区，然后由操作系统决定什么时候将该内容同步到硬盘，用户可以调用 file.flush() 方法请求操作系统尽快将缓冲区存储的数据同步到硬盘。可以看出写入文件的数据不会立即同步到硬盘上，在将写命令添加到 AOF 文件时，要根据需求来保证何时同步到硬盘上。

有以下同步选项：

|   选项   |         同步频率         |
| :------: | :----------------------: |
|  always  |     每个写命令都同步     |
| everysec |       每秒同步一次       |
|    no    | 让操作系统来决定何时同步 |

- always 选项会严重减低服务器的性能；
- everysec 选项比较合适，可以保证系统奔溃时只会丢失一秒左右的数据，并且 Redis 每秒执行一次同步对服务器性能几乎没有任何影响；
- no 选项并不能给服务器性能带来多大的提升，而且也会增加系统奔溃时数据丢失的数量。

随着服务器写请求的增多，AOF 文件会越来越大。Redis 提供了一种将 AOF 重写的特性，能够去除 AOF 文件中的冗余写命令。

## 8. 发布与订阅

订阅者订阅了频道之后，发布者向频道发送字符串消息会被所有订阅者接收到。

某个客户端使用 SUBSCRIBE 订阅一个频道，其它客户端可以使用 PUBLISH 向这个频道发送消息。

发布与订阅模式和观察者模式有以下不同：

- 观察者模式中，观察者和主题都知道对方的存在；而在发布与订阅模式中，发布者与订阅者不知道对方的存在，它们之间通过频道进行通信。
- 观察者模式是同步的，当事件触发时，主题会去调用观察者的方法；而发布与订阅模式是异步的；

## 9. 事务

MULTI 、 EXEC 、 DISCARD 和 WATCH 是 Redis 事务相关的命令。

事务可以一次执行多个命令， 并且有以下两个重要的保证：

- 事务是一个单独的隔离操作：事务中的所有命令都会序列化、按顺序地执行。事务在执行的过程中，不会被其他客户端发送来的命令请求所打断。
- 事务是一个原子操作：事务中的命令要么全部被执行，要么全部都不执行。

### 9.1. EXEC

EXEC 命令负责触发并执行事务中的所有命令：

- 如果客户端在使用 MULTI 开启了一个事务之后，却因为断线而没有成功执行 EXEC ，那么事务中的所有命令都不会被执行。
- 另一方面，如果客户端成功在开启事务之后执行 EXEC ，那么事务中的所有命令都会被执行。

### 9.2. MULTI

MULTI 命令用于开启一个事务，它总是返回 OK 。 MULTI 执行之后， 客户端可以继续向服务器发送任意多条命令， 这些命令不会立即被执行， 而是被放到一个队列中， 当 EXEC 命令被调用时， 所有队列中的命令才会被执行。

### 9.3. DISCARD

当执行 DISCARD 命令时， 事务会被放弃， 事务队列会被清空， 并且客户端会从事务状态中退出。

示例：

```py
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

### 9.4. WATCH

WATCH 命令可以为 Redis 事务提供 check-and-set （CAS）行为。

被 WATCH 的键会被监视，并会发觉这些键是否被改动过了。 如果有至少一个被监视的键在 EXEC 执行之前被修改了， 那么整个事务都会被取消， EXEC 返回 nil-reply 来表示事务已经失败。

```
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

```py
redis> WATCH key1 key2 key3
OK
```

当 EXEC 被调用时， 不管事务是否成功执行， 对所有键的监视都会被取消。

另外， 当客户端断开连接时， 该客户端对键的监视也会被取消。

使用无参数的 UNWATCH 命令可以手动取消对所有键的监视。 对于一些需要改动多个键的事务， 有时候程序需要同时对多个键进行加锁， 然后检查这些键的当前值是否符合程序的要求。 当值达不到要求时， 就可以使用 UNWATCH 命令来取消目前对键的监视， 中途放弃这个事务， 并等待事务的下次尝试。

## 10. 事件

Redis 服务器是一个事件驱动程序。

### 10.1. 文件事件

服务器通过套接字与客户端或者其它服务器进行通信，文件事件就是对套接字操作的抽象。

Redis 基于 Reactor 模式开发了自己的网络时间处理器，使用 I/O 多路复用程序来同时监听多个套接字，并将到达的时间传送给文件事件分派器，分派器会根据套接字产生的事件类型调用响应的时间处理器。

### 10.2. 时间事件

服务器有一些操作需要在给定的时间点执行，时间事件是对这类定时操作的抽象。

时间事件又分为：

- 定时事件：是让一段程序在指定的时间之内执行一次；
- 周期性事件：是让一段程序每隔指定时间就执行一次。

Redis 将所有时间事件都放在一个无序链表中，通过遍历整个链表查找出已到达的时间事件，并调用响应的事件处理器。

### 10.3. 事件的调度与执行

服务器需要不断监听文件事件的套接字才能得到待处理的文件事件，但是不能监听太久，否则时间事件无法在规定的时间内执行，因此监听时间应该根据距离现在最近的时间事件来决定。

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

## 11. 集群

### 11.1. 复制

通过使用 slaveof host port 命令来让一个服务器成为另一个服务器的从服务器。

一个从服务器只能有一个主服务器，并且不支持主主复制。

#### 12.1. 连接过程

1.  主服务器创建快照文件，发送给从服务器，并在发送期间使用缓冲区记录执行的写命令。快照文件发送完毕之后，开始向从服务器发送存储在缓冲区中的写命令；

2.  从服务器丢弃所有旧数据，载入主服务器发来的快照文件，之后从服务器开始接受主服务器发来的写命令；

3.  主服务器每执行一次写命令，就向从服务器发送相同的写命令。

#### 12.2. 主从链

随着负载不断上升，主服务器可能无法很快地更新所有从服务器，或者重新连接和重新同步从服务器将导致系统超载。为了解决这个问题，可以创建一个中间层来分担主服务器的复制工作。中间层的服务器是最上层服务器的从服务器，又是最下层服务器的主服务器。

### 11.2. 哨兵

Sentinel（哨兵）可以监听主服务器，并在主服务器进入下线状态时，自动从从服务器中选举出新的主服务器。

### 11.3. 分片

分片是将数据划分为多个部分的方法，可以将数据存储到多台机器里面，也可以从多台机器里面获取数据，这种方法在解决某些问题时可以获得线性级别的性能提升。

假设有 4 个 Reids 实例 R0，R1，R2，R3，还有很多表示用户的键 user:1，user:2，... 等等，有不同的方式来选择一个指定的键存储在哪个实例中。最简单的方式是范围分片，例如用户 id 从 0\~1000 的存储到实例 R0 中，用户 id 从 1001\~2000 的存储到实例 R1 中，等等。但是这样需要维护一张映射范围表，维护操作代价很高。还有一种方式是哈希分片，使用 CRC32 哈希函数将键转换为一个数字，再对实例数量求模就能知道应该存储的实例。

主要有三种分片方式：

- 客户端分片：客户端使用一致性哈希等算法决定键应当分布到哪个节点。
- 代理分片：将客户端请求发送到代理上，由代理转发请求到正确的节点上。
- 服务器分片：Redis Cluster。

## 12. Redis Client

Redis 社区中有多种编程语言的客户端，可以在这里查找合适的客户端：https://redis.io/clients

redis 官方推荐的 Java Redis Client：

- [jedis](https://github.com/xetorthio/jedis)
- [redisson](https://github.com/redisson/redisson)
- [lettuce](https://github.com/lettuce-io/lettuce-core)

## 13. 资料

- [Redis 官网](https://redis.io/)
- [awesome-redis](https://github.com/JamzyWang/awesome-redis)
- [Redis 实战](https://item.jd.com/11791607.html)
