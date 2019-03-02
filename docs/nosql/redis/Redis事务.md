---
title: Redis 事务
date: 2018/06/11
categories:
- database
tags:
- database
- nosql
- key-value
- transaction
---

# Redis 事务

<!-- TOC depthFrom:2 depthTo:3 -->

- [事务简介](#事务简介)
- [EXEC](#exec)
- [MULTI](#multi)
- [DISCARD](#discard)
- [WATCH](#watch)
    - [取消 WATCH 的场景](#取消-watch-的场景)
    - [使用 WATCH 创建原子操作](#使用-watch-创建原子操作)
- [Redis 不支持回滚](#redis-不支持回滚)
- [Redis 脚本和事务](#redis-脚本和事务)
- [资料](#资料)

<!-- /TOC -->

## 事务简介

事务可以一次执行多个命令，并且有以下两个重要的保证：

- 事务是一个单独的隔离操作：事务中的所有命令都会序列化、按顺序地执行。事务在执行的过程中，不会被其他客户端发送来的命令请求所打断。
- 事务是一个原子操作：事务中的命令要么全部被执行，要么全部都不执行。

## EXEC

**EXEC 命令负责触发并执行事务中的所有命令。**

如果客户端在使用 MULTI 开启了一个事务之后，却因为断线而没有成功执行 EXEC ，那么事务中的所有命令都不会被执行。
另一方面，如果客户端成功在开启事务之后执行 EXEC ，那么事务中的所有命令都会被执行。

## MULTI

**MULTI 命令用于开启一个事务，它总是返回 OK。**

MULTI 执行之后，客户端可以继续向服务器发送任意多条命令，这些命令不会立即被执行，而是被放到一个队列中，当 EXEC 命令被调用时，所有队列中的命令才会被执行。

以下是一个事务例子， 它原子地增加了 foo 和 bar 两个键的值：

```py
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

## DISCARD

**当执行 DISCARD 命令时，事务会被放弃，事务队列会被清空，并且客户端会从事务状态中退出。**

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

## WATCH

WATCH 命令可以为 Redis 事务提供 check-and-set （CAS）行为。

被 WATCH 的键会被监视，并会发觉这些键是否被改动过了。 如果有至少一个被监视的键在 EXEC 执行之前被修改了， 那么整个事务都会被取消， EXEC 返回 null 来表示事务已经失败。

```
WATCH mykey
val = GET mykey
val = val + 1
MULTI
SET mykey $val
EXEC
```

使用上面的代码，如果在 WATCH 执行之后， EXEC 执行之前，有其他客户端修改了 mykey 的值，那么当前客户端的事务就会失败。程序需要做的，就是不断重试这个操作，直到没有发生碰撞为止。

这种形式的锁被称作乐观锁，它是一种非常强大的锁机制。并且因为大多数情况下，不同的客户端会访问不同的键，碰撞的情况一般都很少，所以通常并不需要进行重试。

**WATCH 使得 EXEC 命令需要有条件地执行：事务只能在所有被监视键都没有被修改的前提下执行，如果这个前提不能满足的话，事务就不会被执行。**

WATCH 命令可以被调用多次。对键的监视从 WATCH 执行之后开始生效，直到调用 EXEC 为止。

用户还可以在单个 WATCH 命令中监视任意多个键，例如：

```py
redis> WATCH key1 key2 key3
OK
```

### 取消 WATCH 的场景

当 EXEC 被调用时，不管事务是否成功执行，对所有键的监视都会被取消。

另外，当客户端断开连接时，该客户端对键的监视也会被取消。

使用无参数的 UNWATCH 命令可以手动取消对所有键的监视。对于一些需要改动多个键的事务，有时候程序需要同时对多个键进行加锁，然后检查这些键的当前值是否符合程序的要求。当值达不到要求时，就可以使用 UNWATCH 命令来取消目前对键的监视，中途放弃这个事务，并等待事务的下次尝试。

### 使用 WATCH 创建原子操作

WATCH 可以用于创建 Redis 没有内置的原子操作。

举个例子，以下代码实现了原创的 ZPOP 命令，它可以原子地弹出有序集合中分值（score）最小的元素：

```
WATCH zset
element = ZRANGE zset 0 0
MULTI
ZREM zset element
EXEC
```

## Redis 不支持回滚

Redis 不支持回滚的理由：

- Redis 命令只会因为错误的语法而失败，或是命令用在了错误类型的键上面。
- 因为不需要对回滚进行支持，所以 Redis 的内部可以保持简单且快速。

## Redis 脚本和事务

从定义上来说，Redis 中的脚本本身就是一种事务，所以任何在事务里可以完成的事，在脚本里面也能完成。并且一般来说，使用脚本要来得更简单，并且速度更快。

## 资料

- [Redis 官网](https://redis.io/)
- [事务](http://redis.cn/topics/transactions.html)
- [Redis 实战](https://item.jd.com/11791607.html)
