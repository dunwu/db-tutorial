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

<!-- TOC depthFrom:2 depthTo:3 -->

- [1. 概述](#1-概述)
    - [1.1. 什么是 Redis](#11-什么是-redis)
    - [1.2. 为什么用 Redis](#12-为什么用-redis)
- [2. 安装](#2-安装)
    - [2.1. Window 下安装](#21-window-下安装)
    - [2.2. Linux 下安装](#22-linux-下安装)
    - [2.3. Ubuntu 下安装](#23-ubuntu-下安装)
    - [2.4. 启动 Redis](#24-启动-redis)
    - [2.5. 查看 redis 是否启动？](#25-查看-redis-是否启动)
- [3. Redis 命令](#3-redis-命令)

<!-- /TOC -->

## 1. 概述

### 1.1. 什么是 Redis

Redis 是一个高性能的 key-value 数据库，也可用于缓存和消息代理。

### 1.2. 为什么用 Redis

与其它 key - value 数据库产品相比，具有以下优势：

- 支持数据持久化——可以将内存中的数据保存在磁盘中，重启的时候可以再次加载进行使用。

- 丰富的数据类型——Redis 支持二进制案例的 Strings, Lists, Hashes, Sets 及 Ordered Sets 数据类型操作。
- 支持数据的备份——数据备份采用 master-slave 模式。
- 性能极高——Redis 能读的速度是 110000 次/s，写的速度是 81000 次/s 。
- 原子性——Redis 的所有操作都是原子性的，意思就是要么成功执行要么失败完全不执行。单个操作是原子性的。多个操作也支持事务，即原子性，通过 MULTI 和 EXEC 指令包起来。

## 2. 安装

### 2.1. Window 下安装

**下载地址：**<https://github.com/MSOpenTech/redis/releases>。

Redis 支持 32 位和 64 位。这个需要根据你系统平台的实际情况选择，这里我们下载 **Redis-x64-xxx.zip**压缩包到 C 盘，解压后，将文件夹重新命名为 **redis**。

打开一个 **cmd** 窗口 使用 cd 命令切换目录到 **C:\redis** 运行 **redis-server.exe redis.windows.conf** 。

如果想方便的话，可以把 redis 的路径加到系统的环境变量里，这样就省得再输路径了，后面的那个 redis.windows.conf 可以省略，如果省略，会启用默认的。输入之后，会显示如下界面：

这时候另启一个 cmd 窗口，原来的不要关闭，不然就无法访问服务端了。

切换到 redis 目录下运行 **redis-cli.exe -h 127.0.0.1 -p 6379** 。

设置键值对 **set myKey abc**

取出键值对 **get myKey**

### 2.2. Linux 下安装

**下载地址：**<http://redis.io/download>，下载最新文档版本。

本教程使用的最新文档版本为 2.8.17，下载并安装：

```
$ wget http://download.redis.io/releases/redis-2.8.17.tar.gz
$ tar xzf redis-2.8.17.tar.gz
$ cd redis-2.8.17
$ make
```

make 完后 redis-2.8.17 目录下会出现编译后的 redis 服务程序 redis-server,还有用于测试的客户端程序 redis-cli,两个程序位于安装目录 src 目录下：

下面启动 redis 服务.

```
$ cd src
$ ./redis-server
```

注意这种方式启动 redis 使用的是默认配置。也可以通过启动参数告诉 redis 使用指定配置文件使用下面命令启动。

```
$ cd src
$ ./redis-server redis.conf
```

redis.conf 是一个默认的配置文件。我们可以根据需要使用自己的配置文件。

启动 redis 服务进程后，就可以使用测试客户端程序 redis-cli 和 redis 服务交互了。 比如：

```
$ cd src
$ ./redis-cli
redis> set foo bar
OK
redis> get foo
"bar"
```

### 2.3. Ubuntu 下安装

在 Ubuntu 系统安装 Redi 可以使用以下命令:

```
$sudo apt-get update
$sudo apt-get install redis-server
```

### 2.4. 启动 Redis

```
$ redis-server
```

### 2.5. 查看 redis 是否启动？

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

以上说明我们已经成功安装了 redis。

## 3. Redis 命令

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
