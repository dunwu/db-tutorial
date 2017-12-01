---
title: Redis 快速入门
date: 2017-12-01
categories:
- db
tags:
- database
- key-value
- cache
---

# Redis 快速入门

## 概述

### 什么是 Redis

Redis 是一个高性能的 key-value 数据库，也可用于缓存和消息代理。

### 为什么用 Redis

与其它 key - value 数据库产品相比，具有以下优势：

- 支持数据持久化——可以将内存中的数据保存在磁盘中，重启的时候可以再次加载进行使用。

- 丰富的数据类型——Redis支持二进制案例的 Strings, Lists, Hashes, Sets 及 Ordered Sets 数据类型操作。
- 支持数据的备份——数据备份采用 master-slave 模式。
- 性能极高——Redis 能读的速度是110000次/s，写的速度是81000次/s 。
- 原子性——Redis的所有操作都是原子性的，意思就是要么成功执行要么失败完全不执行。单个操作是原子性的。多个操作也支持事务，即原子性，通过MULTI和EXEC指令包起来。

## 安装

### Window 下安装

**下载地址：**<https://github.com/MSOpenTech/redis/releases>。

Redis 支持 32 位和 64 位。这个需要根据你系统平台的实际情况选择，这里我们下载 **Redis-x64-xxx.zip**压缩包到 C 盘，解压后，将文件夹重新命名为 **redis**。

打开一个 **cmd** 窗口 使用cd命令切换目录到 **C:\redis** 运行 **redis-server.exe redis.windows.conf** 。

如果想方便的话，可以把 redis 的路径加到系统的环境变量里，这样就省得再输路径了，后面的那个 redis.windows.conf 可以省略，如果省略，会启用默认的。输入之后，会显示如下界面：

这时候另启一个cmd窗口，原来的不要关闭，不然就无法访问服务端了。

切换到redis目录下运行 **redis-cli.exe -h 127.0.0.1 -p 6379** 。

设置键值对 **set myKey abc**

取出键值对 **get myKey**

### Linux 下安装

**下载地址：**<http://redis.io/download>，下载最新文档版本。

本教程使用的最新文档版本为 2.8.17，下载并安装：

```
$ wget http://download.redis.io/releases/redis-2.8.17.tar.gz
$ tar xzf redis-2.8.17.tar.gz
$ cd redis-2.8.17
$ make
```

make完后 redis-2.8.17目录下会出现编译后的redis服务程序redis-server,还有用于测试的客户端程序redis-cli,两个程序位于安装目录 src 目录下：

下面启动redis服务.

```
$ cd src
$ ./redis-server
```

注意这种方式启动redis 使用的是默认配置。也可以通过启动参数告诉redis使用指定配置文件使用下面命令启动。

```
$ cd src
$ ./redis-server redis.conf
```

redis.conf是一个默认的配置文件。我们可以根据需要使用自己的配置文件。

启动redis服务进程后，就可以使用测试客户端程序redis-cli和redis服务交互了。 比如：

```
$ cd src
$ ./redis-cli
redis> set foo bar
OK
redis> get foo
"bar"
```

### Ubuntu 下安装

在 Ubuntu 系统安装 Redi 可以使用以下命令:

```
$sudo apt-get update
$sudo apt-get install redis-server
```

### 启动 Redis

```
$ redis-server
```

### 查看 redis 是否启动？

```
$ redis-cli
```

以上命令将打开以下终端：

```
redis 127.0.0.1:6379>
```

127.0.0.1 是本机 IP ，6379 是 redis 服务端口。现在我们输入 PING 命令。

```
redis 127.0.0.1:6379> ping
PONG
```

以上说明我们已经成功安装了redis。

## Redis 命令

Redis 命令用于在 redis 服务上执行操作。

要在 redis 服务上执行命令，需要先进入 redis 客户端。

进入 redis 客户端的方法：

```
$ redis-cli
```

远程进入 redis 客户端的方法：

```
$ redis-cli -h host -p port -a password
```

**实例**

以下实例演示了如何连接到主机为 127.0.0.1，端口为 6379 ，密码为 pass 的 redis 服务上：

```
$redis-cli -h 127.0.0.1 -p 6379 -a "pass"
redis 127.0.0.1:6379>
redis 127.0.0.1:6379> PING
PONG
```

更多命令行可以参考：[Redis 官方命令行字典](https://redis.io/commands)

## 数据类型

### String

> string 是 redis 最基本的类型，你可以理解成与 Memcached 一模一样的类型，一个 key 对应一个 value。
>
> string 类型是二进制安全的。意思是 redis 的 string 可以包含任何数据。比如图片或者序列化的对象 。
>
> string 类型是 Redis 最基本的数据类型，一个键最大能存储 512MB。

**实例**

```sh
redis 127.0.0.1:6379> set name "abc"
OK
redis 127.0.0.1:6379> get name
"abc"
```

### Hash

> hash 是一个键值对集合。
>
> hash 是一个 string 类型的 field 和 value 的映射表，hash 特别适合用于存储对象。

**实例**

```sh
127.0.0.1:6379> HMSET user:1 username root password root points 200
OK
127.0.0.1:6379> HGETALL user:1
1) "username"
2) "root"
3) "password"
4) "root"
5) "points"
6) "200"
```

以上实例中 hash 数据类型存储了包含用户脚本信息的用户对象。 实例中我们使用了 Redis **HMSET, HGETALL** 命令，**user:1**为键值。

### List

> List 是简单的字符串列表，按照插入顺序排序。你可以添加一个元素到列表的头部（左边）或者尾部（右边）。

**实例**

```sh
127.0.0.1:6379> lpush mylist redis
(integer) 1
127.0.0.1:6379> lpush mylist mongodb
(integer) 2
127.0.0.1:6379> lpush mylist hbase
(integer) 3
127.0.0.1:6379> lrange mylist 0 10
1) "hbase"
2) "mongodb"
3) "redis"
127.0.0.1:6379>
```

### Set

> Redis 的 Set 是 string 类型的无序集合。
>
> 集合是通过哈希表实现的，所以添加，删除，查找的复杂度都是O(1)。

**实例**

```sh
127.0.0.1:6379> sadd colorset red
(integer) 1
127.0.0.1:6379> sadd colorset red
(integer) 0
127.0.0.1:6379> sadd colorset yellow
(integer) 1
127.0.0.1:6379> sadd colorset blue
(integer) 1
127.0.0.1:6379> smembers colorset
1) "yellow"
2) "red"
3) "blue"
127.0.0.1:6379> srem colorset red
(integer) 1
127.0.0.1:6379> smembers colorset
1) "yellow"
2) "blue"
```

### Sorted Set

> Redis zset 和 set 一样也是 string 类型元素的集合,且不允许重复的成员。
>
> 不同的是每个元素都会关联一个 double 类型的分数。redis 正是通过分数来为集合中的成员进行从小到大的排序。
>
> zset 的成员是唯一的,但分数(score)却可以重复。

**实例**

```sh
127.0.0.1:6379> zadd week 5 Friday
(integer) 1
127.0.0.1:6379> zadd week 6 Saturday
(integer) 1
127.0.0.1:6379> zadd week 1 Monday
(integer) 1
127.0.0.1:6379> zadd week 2 Tuesday
(integer) 1
127.0.0.1:6379> zadd week 3 Wednesday
(integer) 1
127.0.0.1:6379> zadd week 4 Thursday
(integer) 1
127.0.0.1:6379> zadd week 7 Sunday
(integer) 1
127.0.0.1:6379> zrangebyscore week 0 1000
1) "Monday"
2) "Tuesday"
3) "Wednesday"
4) "Thursday"
5) "Friday"
6) "Saturday"
7) "Sunday"
```

