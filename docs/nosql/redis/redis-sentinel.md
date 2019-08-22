# Redis 哨兵

<!-- TOC depthFrom:2 depthTo:3 -->

- [QuickStart](#quickstart)
    - [获取 Sentinel](#获取-sentinel)
    - [运行 Sentinel](#运行-sentinel)
    - [部署之前了解关于 Sentinel 的基本东西](#部署之前了解关于-sentinel-的基本东西)
    - [Sentinel 配置](#sentinel-配置)
    - [其他的 Sentinels 选项](#其他的-sentinels-选项)
    - [Sentinel 部署示例](#sentinel-部署示例)
- [快速教程](#快速教程)
    - [询问 Sentinel 关于主节点的状态](#询问-sentinel-关于主节点的状态)
    - [获取当前主节点的地址](#获取当前主节点的地址)
    - [故障转移测试](#故障转移测试)
- [Sentinel API](#sentinel-api)
    - [Sentinel 命令](#sentinel-命令)
    - [运行时重新配置 Sentinel](#运行时重新配置-sentinel)
    - [添加和移除 sentinels](#添加和移除-sentinels)
    - [移除旧的主节点或不可达的从节点](#移除旧的主节点或不可达的从节点)
    - [发布/订阅消息](#发布订阅消息)
    - [BUSY 状态的处理](#busy-状态的处理)
- [更高级的概念](#更高级的概念)
    - [SDOWN 和 ODOWN 失败状态](#sdown-和-odown-失败状态)
    - [Sentinels 和从节点自动发现](#sentinels-和从节点自动发现)
    - [故障转移之外重新配置](#故障转移之外重新配置)
    - [从节点选举和优先级](#从节点选举和优先级)
- [算法和内部结构](#算法和内部结构)
    - [Quorum](#quorum)
    - [配置 epochs](#配置-epochs)
    - [配置传播](#配置传播)
    - [Sentinel 持久化状态](#sentinel-持久化状态)
    - [TILT 模式](#tilt-模式)

<!-- /TOC -->

Redis Sentinel 为 Redis 提供了高可用解决方案。实际上这意味着使用 Sentinel 可以部署一套 Redis，在没有人为干预的情况下去应付各种各样的失败事件。

Redis Sentinel 同时提供了一些其他的功能，例如：监控、通知、并为 client 提供配置。

下面是 Sentinel 的功能列表：

- 监控（Monitoring）：Sentinel 不断的去检查你的主从实例是否按照预期在工作。
- 通知（Notification）：Sentinel 可以通过一个 api 来通知系统管理员或者另外的应用程序，被监控的 Redis 实例有一些问题。
- 自动故障转移（Automatic failover）：如果一个主节点没有按照预期工作，Sentinel 会开始故障转移过程，把一个从节点提升为主节点，并重新配置其他的从节点使用新的主节点，使用 Redis 服务的应用程序在连接的时候也被通知新的地址。
- 配置提供者（Configuration provider）：Sentinel 给客户端的服务发现提供来源：对于一个给定的服务，客户端连接到 Sentinels 来寻找当前主节点的地址。当故障转移发生的时候，Sentinels 将报告新的地址。

**Sentinel 的分布式特性**

Redis Sentinel 是一个分布式系统，Sentinel 运行在有许多 Sentinel 进程互相合作的环境下，它本身就是这样被设计的。有许多 Sentinel 进程互相合作的优点如下：

- 当多个 Sentinel 同意一个 master 不再可用的时候，就执行故障检测。这明显降低了错误概率。
- 即使并非全部的 Sentinel 都在工作，Sentinel 也可以正常工作，这种特性，让系统非常的健康。

所有的 Sentinels，Redis 实例，连接到 Sentinel 和 Redis 的客户端，本身就是一个有着特殊性质的大型分布式系统。在这篇文章中，我将逐步地介绍这些概念，最开始是一些基本的信息来理解 Sentinel 的基本属性，后面是更复杂的信息来理解 Sentinel 是怎么工作的。

## QuickStart

### 获取 Sentinel

当前版本的 Sentinel 的被称为 Sentinel 2 。它使用更强更简单的预测算法重写了 Sentinel 的初始化实现（文章的后面将会解释）。

Redis Sentinel 的一个稳定版本是随着 Redis2.8 和 3.0 一起的。这两个是 Redis 最新的稳定版。

新的进展在 unstable 分支下进行，一旦新的特性是稳定的，就会被合并到 2.8 和 3.0 分支。

和 Redis 2.6 一起的 Redis Sentinel 版本 1，是过时的。我们不该使用它。

### 运行 Sentinel

如果你使用 redis-sentinel 可执行文件，你可以使用下面的命令来运行 Sentinel：

```
redis-sentinel /path/to/sentinel.conf
```

另外，你可以直接使用 redis-server 并以 Sentinel 模式来启动：

```
redis-server /path/to/sentinel.conf --sentinel
```

两种方式是一样的。

不管咋样，使用一个配置文件来运行 Sentinel 是必须的，这个文件被系统使用来存储当前状态，如果重启，这些状态会被重新载入。如果没有配置文件或者配置文件的路径不对，Sentinel 将会拒绝启动。

默认情况下，Sentinels 监听 TCP 端口 26379，所以为了让 Sentinels 运行，你的机器的 26379 端口必须是打开的，用来接收其他 Sentinel 实例的连接，否则，Sentinels 不能互相交流，也不知道该干什么，也不会执行故障转移。

### 部署之前了解关于 Sentinel 的基本东西

- 一个健康的集群部署，至少需要三个 Sentinel 实例
- 三个 Sentinel 实例应该被放在失败独立的电脑上或虚拟机中，比如说不同的物理机或者在不同的可用区域上执行的虚拟机。
- Sentinel + Redis 分布式系统在失败期间并不确保写入请求被保存，因为 Redis 使用异步拷贝。可是有很多部署 Sentinel 的 方式来让窗口把丢失写入限制在特定的时刻，当然也有另外的不安全的方式来部署。
- 如果你在开发环境中没有经常测试，或者在生产环境中也没有，那就没有高可用的设置是安全的。你或许有一个错误的配置而仅仅只是在很晚的时候才出现（凌晨 3 点你的主节点宕掉了）。
- Sentinel，Docker ，其他的网络地址转换表，端口映射 使用应该很小心的使用：Docker 执行端口重新映射，破坏 Sentinel 自动发现另外的 Sentinel 进程和一个主节点的从节点列表。在文章的稍后部分查看更过关于 Sentinel 和 Docker 的信息。

### Sentinel 配置

Redis 源码中包含一个名为 sentinel.conf 的文件，是一个你可以用来配置 Sentinel 的示例配置文件。一个典型的最小配置文件像下面这样：

```
sentinel monitor mymaster 127.0.0.1 6379 2
sentinel down-after-milliseconds mymaster 60000
sentinel failover-timeout mymaster 180000
sentinel parallel-syncs mymaster 1

sentinel monitor resque 192.168.1.3 6380 4
sentinel down-after-milliseconds resque 10000
sentinel failover-timeout resque 180000
sentinel parallel-syncs resque 5
```

你仅仅只需要指定要监控的主节点，并给每个单独的主节点一个不同的名称。不需要指定从节点，从节点会被自动发现。Sentinel 将会根据从节点额外的信息自动更新配置（为了在重启时保留信息）。在故障转移中每当一个从节点被提升为主节点或者当一个新的 Sentinel 被发现的时候，配置信息也被重新写入。

示例配置在上面，监控两个 Redis 实例集合，每个集合由一个主节点和不明确数量的从节点组成。一个集合叫做 mymaster，另外一个叫做 resque。

sentinel monitor 参数的意思在下面

```
sentinel monitor <master-group-name> <ip> <port> <quorum>
```

为了更加清晰明了，让我们一行一行来检查配置选项的意思：

第一行用来告诉 Redis 监控一个叫做 mymaster 的主节点，地址是 127.0.0.1 端口号是 6379，并且有 2 个仲裁机器。所有的意思都很明显，但是除了这个 quorum 参数：

- quorum 是 需要同意主节点不可用的 Sentinels 的数量
- 然而 quorum 仅仅只是用来检测失败。为了实际的执行故障转移，Sentinels 中的一个需要被选定为 leader 并且被授权进行操作，这仅仅发生在大多数 Sentinels 进行投票的时候。

比如如果你有五个 Sentinel 进程，对于一个主节点 quorum 被设置为 2，下面是发生的事情：

- 同时有两个 Sentinels 同意主节点不可用，其中的一个将会尝试开始故障转移。
- 如果至少有三个 Sentinels 是可用的，故障转移将会被授权并且开始。

实际中，这意味着在失败时，如果大多数的 Sentinel 进程没有同意，Sentinel 永远不会开始故障转移。

### 其他的 Sentinels 选项

其他的选项几乎都是如下形式：

```
sentinel <option_name> <master_name> <option_value>
```

用途如下：

down-after-milliseconds：当一个实例失去联系（要么不回复我们的请求，要么回复一个错误）超过了这个时间（毫秒为单位），Sentinel 就开始认为这个实例挂掉了。

parallel-syncs：设置的从节点的数量，这些从节点在一次故障转移过后可以使用新的主节点进行重新配置。数量越少，完成故障转移过程将花费更多的时间，如果从节点为旧的数据提供服务，你或许不想所有的从节点使用主节点进行重新同步。复制进程对于从节点来说大部分是非阻塞的，还是有一个时刻它会停下来去从主节点加载数据。你或许想确保一次只有一个从节点是不可达的，可以通过设置这个选项的值为 1 来完成。

别的选项在文章的其他部分进行描述。

所有的配置参数都可以在运行时使用 SENTINEL SET 命令进行更改，查看 Reconfiguring Sentinel at runtime 章节获取更多内容。

### Sentinel 部署示例

现在你已经知道了 Sentinel 的基本信息，你或许想知道哪里放置你的 Sentinel 进程，需要多少个 Sentinel 进程等等。这个章节给出了几个部署的例子。

为了以图形（graphical ）格式展示配置示例，我们使用 ASCII 艺术。下面是不同的符号的意思：

```
+--------------------+
| 这是一个独立电脑   |
| 或者VM。我们称它为 |
| “box”            |
+--------------------+
```

我们把我们想要运行的东西写到 boxes 里：

```
+-------------------+
| Redis master M1   |
| Redis Sentinel S1 |
+-------------------+
```

不同的 box 之间通过一条线连接，表示他们之间可以互相交流：

```
+-------------+               +-------------+
| Sentinel S1 |---------------| Sentinel S2 |
+-------------+               +-------------+
```

中断的线条表示不同的网络分区：

```
+-------------+                +-------------+
| Sentinel S1 |------ // ------| Sentinel S2 |
+-------------+                +-------------+
```

同时还要注意：

- 主节点称为 M1，M2，M3，…，Mn。
- 从节点称为 R1，R2，R3，…，Rn。
- Sentinels 称为 S1，S2，S3，…，Sn。
- 客户端称为 C1，C2，C3，…，Cn。
- 当一个实例因为 Sentinels 的行为转换角色，我们把它放在方括号里，所以[M1]表示一个实例现在是主节点。

注意永远不要设置只有两个 Sentinels，因为开始一个故障转移，Sentinels 总是需要和大多数 Sentinels 交流。

#### 示例 1：仅仅只有两个 Sentinels，永远不要这么做

```
+----+         +----+
| M1 |---------| R1 |
| S1 |         | S2 |
+----+         +----+

Configuration: quorum = 1
```

在这个设置中，如果 M1 宕掉了，R1 将会被提升至主节点，因为两个 Sentinels 将会达成一致（显然把 quorum 设置为 1），并且授权开始一个故障转移因为大多数是两个。显然，表面上可以工作，但是请检查下一个点来看看为什么这种设置是不可以的。

如果 M1 的 box 停止工作，M1 也会停止。运行在另外一个 box 中的 S2 将不会被授权进行故障转移，所以系统将不可用。

注意，需要大多数是为了应付不同的故障，最新的配置稍后会传播给所有的 Sentinels。同时注意在上述设置中单独一边的故障转移能力，没有任何协议，将是非常危险的：

```
+----+           +------+
| M1 |----//-----| [M1] |
| S1 |           | S2   |
+----+           +------+
```

在上面的配置中，我们完美对称地创建了两个主节点（假设 S2 在没有授权的情况下可以进行故障转移），客户端或许会不确定写往哪一边，并且没有办法理解当分区治愈时候哪边的配置是正确的。

所以请至少部署三个 Sentinels 在三个不同的 box 当中。

#### 示例 2：三个 box 的基本设置

这是一个非常简单的设置，拥有更加安全的优点。它是基于三个 boxes 的，每个 box 运行一个 Redis 进程和 Sentinel 进程。

```
       +----+
       | M1 |
       | S1 |
       +----+
          |
+----+    |    +----+
| R2 |----+----| R3 |
| S2 |         | S3 |
+----+         +----+

Configuration: quorum = 2
```

如果 M1 挂掉，S2 和 S3 将认同这次失败，并且能授权开始一次故障转移，这样使客户端可以继续使用。

在每一个 Sentinel 设置中，Redis 是异步复制的，总是有丢失一些写入数据的危险，因为当一个从节点被提升为主节点的时候一个写入确认还没有到达。然而在上面的设置中，还有一种更加危险的情况，由于客户端和一个老的主节点在一个网络分区中，就像下面这样：

```
         +----+
         | M1 |
         | S1 | <- C1 (writes will be lost)
         +----+
            |
            /
            /
+------+    |    +----+
| [M2] |----+----| R3 |
| S2   |         | S3 |
+------+         +----+
```

在这种情况下，网络分区把旧的主节点[M1]给孤立了，所以从节点 R2 被提升为主节点。然而，像客户端 C1，和旧的主节点在同一个网络分区中，或许继续像旧的主节点写入数据。当分区治愈，这些数据将永久丢失，这个旧得主节点将会被重新配置，作为新的主节点下的一个从节点，并丢弃它自己的数据。

可以使用下面的 Redis 复制特性减轻这个问题，如果一个主节点发现它不再能够把它的写入请求发送给指定数量的从节点，它就停止接受写入请求。

```
min-slaves-to-write 1
min-slaves-max-lag 10
```

当上面的配置应用于一个 Redis 实例。Redis 发现它不能写入至少一个 1 从节点，作为主节点的 Reids 将会停止接受写入请求。由于复制是异步，不能写入也意味着从节点也是断开的，或者超过了指定的 max-lag 秒数没有发送异步回应。

在上面的示例中，使用这个配置的旧的主节点 M1，在 10 秒过后就不可用了。当分区治愈，Sentinel 配置将会统一为新的，客户端 C1 将获取到一个有效的配置并且继续。

然而天下没有免费的午餐，在这种改进下，如果两个从节点挂掉了，主节点将会停止接收写入请求，这就是一个权衡。

#### 示例 3：Sentinel 在客户端所在的 box 中

有时候，我们只有两个 Redis box 是可用的，一个给主节点，一个给从节点。在那种情况下，示例 2 中的配置是不可行的，我们可以采取下面的方法，Sentinels 被放置在客户端所在的地方：

```
            +----+         +----+
            | M1 |----+----| R1 |
            | S1 |    |    | S2 |
            +----+    |    +----+
                      |
         +------------+------------+
         |            |            |
         |            |            |
      +----+        +----+      +----+
      | C1 |        | C2 |      | C3 |
      | S1 |        | S2 |      | S3 |
      +----+        +----+      +----+

      Configuration: quorum = 2
在这种设置下，Sentinels的视角和客户端是 一样的：如
```

在这种设置下，Sentinels 的视角和客户端是 一样的：如果大部分的客户端认为一个主节点是可用的，它就是可用的。这里的 C1，C2，C3 是一般的客户端， 并不意味着 C1 是连接到 Redis 的单个客户端，它更像一个应用服务器，一个 Redis app，或者类似的东西。

如果 M1 和 S1 所在的 box 挂掉了，故障转移将会进行，但是很明显的看到不同的网络分区将导致不同的行为。比如说，如果客户端和 Redis 服务断开连接，Sentinel 将不会被设置，因为 Redis 的主节点和从节点都是不可用的。

注意如果 C3 和 M1 在一个分区，我们有了一个和示例 2 中描述的类似的问题，不同的是，这里我们没有办法打破对称，因为只有一个主节点和从节点，所以主节点不会停止接收请求。

所以这是一个有效的设置，但是实例 2 中的设置更有优势，比如 Redis 高可用系统，Redis 运行在同一个 box 中，更容易被管理，并且可以限制在小部分的分区中主节点接收写入请求的时间。

#### 示例 4：Sentinel 客户端 这一边少于三个客户端

示例 3 描述的设置中，如果客户端这一边的 box 少于不够三个，这个 设置就不能使用。在这种情况下，我们需要借助混合设置，像下面这样：

```
            +----+         +----+
            | M1 |----+----| R1 |
            | S1 |    |    | S2 |
            +----+    |    +----+
                      |
               +------+-----+
               |            |  
               |            |
            +----+        +----+
            | C1 |        | C2 |
            | S3 |        | S4 |
            +----+        +----+

      Configuration: quorum = 3
```

这和示例 3 中的设置非常相似，但是这里我们在可用的四个 box 中运行了四个 Sentinel。如果主节点 M1 变成不可用节点，其他三个 Sentinel 将执行故障转移。

理论上，当移除 S2 和 S4 正在运行的 box，这个设置可以工作，把 quorum 设置为 2。然而，在应用层没有高可用的系统，想在 Redis 这一边得到高可用是不太可能的。

#### Sentinel，Docker,NAT 和可能的问题

Docker 使用被称为端口映射的技术：与一个程序认为他使用的端口相比，运行在 Docker 容器里面的程序可能被暴露在不同的端口上。为了运行多个容器在相同的服务器上同时使用同一个端口，这是非常有用的。

Docker 不是唯一会发生这件事情的软件系统，也有其他的网络地址转换设置导致端口是被重映射，并且有时候没有端口，只有 IP 地址。

端口和地址重映射在两个方面制造了与 Sentinel 有关的问题：

Sentinel 的自动发现服务将停止工作，因为它使基于每个 Sentinel 往它监听的端口和 IP 地址广播 hello 消息来实现的。但是 Sentinels 没有办法来理解端口和 IP 地址被重映射了，所以他会宣布它和其他的 Sentinels 的连接是不正常的。
在一个主节点的 INFO 输出中，从节点 被列出来也是类似的方式：主节点检查远端对等的 TCP 连接来发现地址，在握手过程中，从节点自己广告他的端口，然而由于相同的原因，端口或许是错误的。
因为 Sentinels 自动发现从节点使用主节点的 INFO 输出信息，发现的从节点是不可达的，并且 Sentinel 将永远不会开始故障转移，因为从系统的观点来看，没有好的从节点，所以目前没有方式监控使用 Docker 部署的主节点和从节点实例，除非你通知 Docker 以 1:1 映射端口。

对于第一个问题，万一你想使用 Docker 运行一堆 Sentinel 实例，你可以使用下面的两个 Sentinel 配置，为了强迫 Sentinel 宣布一个指定的端口和 IP：

```
sentinel announce-ip <ip>
sentinel announce-port <port>
```

注意，Docker 可以运行 host networking 模式。这就不会有问题因为端口不会被重新映射。

## 快速教程

在文章接下来的部分中，所有的说明都是关于 Sentinel API，配置和语义。对于想尽快上手的人，这部分的教程展示了三个 Sentinel 怎么配置和交互。

现在我假设三个实例分别在端口 5000、5001、5002 上。我也假设你在 6379 上有一个主节点 Redis 实例，6380 上有一个从节点实例。在本教程中我们将使用 IPV4 回调地址 127.0.0.1，假设你在你的电脑上运行了 模拟环境。

三个 Sentinel 配置文件应该看起来像下面这样：

```
port 5000
sentinel monitor mymaster 127.0.0.1 6379 2
sentinel down-after-milliseconds mymaster 5000
sentinel failover-timeout mymaster 60000
sentinel parallel-syncs mymaster 1
```

另外的两个配置文件也是相同的，但是使用 5001,5002 作为端口号。

上面的配置中需要注意的一些事情：

主节点集群称为 mymaster，它定义了主节点和它的从节点。因为每个 master set 有一个不同的名称，Sentinel 能同时监控不同的主节点和从节点的集合。
quorum 被设置为 2。
down-after-milliseconds 的值是 5000 毫秒，就是 5 秒钟，所以在这个时间内一旦我们不能收到回复，主节点将发现失败。
一旦你启动了三个 Sentinels，可以看到他们打印的一些信息：

```
+monitor master mymaster 127.0.0.1 637这是一个Sentinel事件，如果你
```

SUBSCRIBE 了指定名称的事件，你可以收到这种事件通过发布/订阅。

Sentinel 在故障检测和故障转移中生成和打印不同的事件。

### 询问 Sentinel 关于主节点的状态

Sentinel 开始启动的时候，要做的事情是检查主节点的监控是否正常：

```py
$ redis-cli -p 5000
127.0.0.1:5000> sentinel master mymaster
 1) "name"
 2) "mymaster"
 3) "ip"
 4) "127.0.0.1"
 5) "port"
 6) "6379"
 7) "runid"
 8) "953ae6a589449c13ddefaee3538d356d287f509b"
 9) "flags"
10) "master"
11) "link-pending-commands"
12) "0"
13) "link-refcount"
14) "1"
15) "last-ping-sent"
16) "0"
17) "last-ok-ping-reply"
18) "735"
19) "last-ping-reply"
20) "735"
21) "down-after-milliseconds"
22) "5000"
23) "info-refresh"
24) "126"
25) "role-reported"
26) "master"
27) "role-reported-time"
28) "532439"
29) "config-epoch"
30) "1"
31) "num-slaves"
32) "1"
33) "num-other-sentinels"
34) "2"
35) "quorum"
36) "2"
37) "failover-timeout"
38) "60000"
39) "parallel-syncs"
40) "1"
```

像你所见的，它打印了主节点的一些信息。有几个是我们特别有兴趣的：

1.  num-other-sentinels 是 2，所以我们知道对于这个主节点 Sentinel 已经发现了两个以上的 Sentinels。如果你检查日志，你可以看到+sentinel 事件发生。
2.  flags 是 master。如果主节点挂掉了，我们可以看到 s_down 或者 o_down 标志。
3.  num-slaves 现在是 1，所以 Sentinel 发现有一个从节点。

为了探测关于这个实例更多的信息，你可以尝试下面的两个命令：

```
SENTINEL slaves mymaster
SENTINEL sentinels mymaster
```

第一个将提供关于从节点类似的信息，第二个是关于另外的 Sentinels。

### 获取当前主节点的地址

Sentinel 也作为一个配置提供者，提供给客户端它们想连接的主节点和从节点的集群。因为可能的故障转移和重配置，客户端不知道一个集群实例内当前的活着的主节点，所以 Sentinel 提供了一个 API：

```py
127.0.0.1:5000> SENTINEL get-master-addr-by-name mymaster
1) "127.0.0.1"
2) "6379"
```

### 故障转移测试

现在我们部署 Sentinel 可以被测试了。我们可以杀死主节点然后查看配置变化。做我们可以做的：

```
redis-cli -p 6379 DEBUG sleep 30
```

这个命令让我们的主节点变为不可达，睡眠 30 秒，它基本上模拟了主节点挂掉的一些原因。

如果你检查 Sentinel 的日志，你应该能看到许多动作：

1.  每个 Sentinel 发现了主节点挂掉了并有一个+sdown 事件
2.  这个事件稍候升级到+odown，意味着大多数 Sentinel 已经同意了主节点是不可达的。
3.  Sentinels 开始投票一个 Sentinel 开始并尝试故障转移
4.  故障转移开始

如果你重新询问 mymaster 的当前主节点的地址，这次我们会得到一个不同的回复：

```
127.0.0.1:5000> SENTINEL get-master-addr-by-name mymaster
1) "127.0.0.1"
2) "6380"
```

目前为止一切都很顺利，现在你可以创建你自己的 Sentinel 部署或者阅读更多来理解 Sentinel 的命令和内部原理。

## Sentinel API

Sentinel 提供了一个 API，可以用来检查它的状态，检查主节点和从节点的健康，订阅具体的通知并在运行时改变 Sentinel 的配置。

默认情况下 Sentinel 使用 TCP 端口号 26379。Sentinels 接收使用 Redis 的协议命令，所以你可以使用 redis-cli 或者其他未修改的 Redis 客户端来和 Sentinel 交流。

直接查询一个 Sentinel 来检查所监控的 Redis 实例的状态，看看另外的 Sentinels 所知道是可能的。有两种方式，使用发布/订阅，每当一些事件发生，比如说一次故障转移，或一个实例发生错误等，都可能接收到一个从 Sentinels 推送过来的通知。

### Sentinel 命令

下面是可以接收的命令列表，没有覆盖到那些用来改变 Sentinel 配置的命令：

- PING 这个命令仅仅返回 PONG。
- SENTINEL masters 展示监控的主节点和它们的状态列表
- SENTINEL master <master name> 展示指定的主节点的信息
- SENTINEL salves <master name> 展示这个主节点的从节点，以及它们的状态
- SENTINEL sentinels <master name> 展示这个主节点的 sentinel 实例，以及它们的状态
- SENTINEL get-master-addr-by-name <master name> 返回主节点的 IP 和端口号。如果这个主节点的一次故障转移正在进行，就返回提升的从节点的 IP 和端口号
- SENTINEL reset <pattern> 这个命令将会根据匹配的名称重置主节点，pattern 参数是通配符（glob-style）类型，重置进程清除主节点中之前的所有状态，并且移除主节点发现和关联的从节点和 sentinel。
- SENTINEL failover <master name> 如果主节点不可达，强制开始故障转移，不需要另外的 Sentinels 同意。
- SENTINEL ckquorum <master name> 检查当前的 Sentinel 配置对于主节点的故障转移是否能达到仲裁人数，并且大多数是需要的来授权故障转移。这个命令应该在监控系统中使用来检查一个 Sentinel 部署是否正常。
- SENTINEL flushconfig 强制 Sentinel 重新写入它的配置到磁盘上，包括当前 Sentinel 状态。通常，每次当它状态里的一些东西改变，Sentinel 就会重写配置信息。然而有时候配置文件会丢失，由于错误的操作、磁盘故障、包升级脚本、或配置管理。在那种情况下，强制 Sentinel 重写它的配置文件是容易的。甚至之前的配置文件完全丢失，这个命令也能很好的工作。

### 运行时重新配置 Sentinel

从 Redis 2.8.4 开始，Sentinel 提供了一个 API 为了增加、移除或者改变一个给定的主节点的配置。注意如果你有多个 sentinels，为了工作正常，你应该改变所有的 Redis Sentinel 实例。这意味着改变单个 Sentinel 的配置不会把变化发送给在网络中另外的 Sentinels.

下面是 SENTINEL 自命令列表，用来更新一个 Sentinel 实例的配置：

- SENTINEL MONITOR <name> <ip> <port> <quorum> 这个命令告诉 Sentinel 开始监控一个指定名称、IP、端口号、quorum 的主节点，它和 sentinel.conf 配置文件中的 sentinel monitor 配置指令是完全相同的，不同的是这里不能使用主机名作为 IP，需要提供一个 IPV4 或 IPV6 地址。
- SENTINEL REMOVE <name> 用来移除指定的主节点：主节点不再被监控，并且将被从 Sentinel 的内部状态中被完全移除，所以不会被 SENTINEL masters 列出。
- SENTINEL SET <name> <option> <value> SET 命令和 Reids 的 CONFIG SET 指令非常相似，被用来改变一个指定主节点的配置参数。多个选项-值可以被指定。所有通过 sentinel.conf 配置的参数可以使用 SET 命令重新配置。

下面是 SENTINEL SET 命令的一个例子，为了修改一个名为 objects-cache 的主节点的 down-after-milliseconds 配置：

```
SENTINEL SET objects-cache-master down-after-milliseconds 1000
```

正如我们提到的，SENTINEL SET 可以被用来设置所有的在启动配置文件中被设置的参数。而且，还可以仅仅改变主节点的 quorum 配置，而不需要使用 SENTINEL REMOVE 和 SENTINEL MONITOR 来删除或者增加主节点，只需要使用：

```
SENTINEL SET objects-cache-master quorum 5
```

注意，没有等价的 GET 命令，因为 SENTINEL MASTER 以一种易于解析的格式提供了所有的配置参数。

### 添加和移除 sentinels

添加一个新的 sentinel 到你的部署中是很容易的一个过程，因为 Sentinel 有自动发现机制。所有的你需要做的事情是开启一个新的 Sentinel 来监控当前的主节点。10 秒过后，Sentinel 将获取到其他的 Sentinels 列表和当前主节点的从节点。

如果你想一次性增加多个 Sentinels，建议你一个接一个的增加，等所有的 Sentinels 已经知道第一个再添加另一个。在添加的新的 Sentinels 过程中错误有可能发生，在这时候保证在一次网络分区内中大部分是可用是很有用的。

在没有网络分区时，通过在 30 秒后增加每个新的节点，这是很容易实现的。

最后，可以使用 SENTINEL MASTER mastername 命令来检查是否全部 Sentinels 都同意了监控主节点的 Sentinels 的总数。

移除一个 Sentinel 稍微复杂一点：Sentinels 永远不会忘记已经看到的 Sentinels，甚至他们在相当长的一段时间内不可达，因为我们不想动态的改变授权一次故障转移和创建新的配置所需要的大多数。在没有网络分区的说话，需要执行下面的步骤来移除一个 Sentinel：

1.  停止你想要移除的 Sentinel 的进程
2.  发送一个 SENTINEL RESET \* 命令到其他的 Sentinel 实例，相继的，两次发送到实例之间至少等待 30 秒
3.  检查所有的 Sentinels 赞同的当前存活的 Sentinels 的数量，通过检查每个 SENTINEL MASTER mastername 的输出。

### 移除旧的主节点或不可达的从节点

Sentinels 永远不会忘记一个主节点的从节点，甚至当他们很长时间都不可达。这是很有用的，因为在一次网络分区或失败事件发生后，Sentinels 应该能正确地重新配置一个返回的从节点。

而且，在故障转移发生之后，被故障转移的主节点实际上被添加为新的主节点的从节点，一旦它可用的时候，这种方式将重新配置来复制新的主节点。

然而有时候你想从 Sentinels 监控的从节点列表中永久的移除一个从节点。

为了做这件事，你需要发送一个 SENTINEL RESET mastername 命令给所有的 Sentinels：它们将在十秒后刷新从节点列表，只添加当前主节点的 INFO 输出中正确的复制列表。

### 发布/订阅消息

一个客户端能使用一个 Sentinel 作为一个 Redis 兼容的发布/订阅服务器，为了 SUBSCRIBE 或者 PSUBSCRIBE 到指定频道，获取指定事件通知。

频道的名称和事件的名称是一样的。比如说名称为+sdown 的频道将收到所有的关于实例进入 SDOWN 条件的通知。

使用 PSUBSCRIBE \* 订阅来获取所有的消息。

下面是一个频道列表，以及使用 API，你可以接收到的消息格式。第一个词是频道/事件名称，剩余部分是数据格式。

注意，指定 instance details 的地方意味着提供了下面的参数用于表示目标实例：

```
<instance-type> <name> <ip> <port> @ <master-name> <master-ip> <master-port>
```

标识主节点的部分（从@开始到结束）是可选的，只有实例本身不是主节点的时指定。

- +reset-master <instance details> — 主节点被重置。
- +slave <instance details> — 一个新的从节点被发现和关联。
- +failover-state-reconf-slaves <instance details> — 故障转移状态被转换为 reconf-slaves 状态。
- +failover-detected <instance details> — 另一个 Sentinel 开始了故障转移或者其他的外部实体被发现（一个关联的从节点变为主节点）。
- +slave-reconf-sent <instance details> — 为了给新的从节点重新配置，sentinel 中的 leader 发送 SLAVEOF 命令到这个实例。
- +slave-reconf-inprog <instance details> –从节点被重新配置展示一个主节点的从节点，但是同步过程尚未完成。
- +slave-reconf-done <instance details> — 从节点现在和主节点是同步的。
- -dup-sentinel <instance details> –指定的主节点，一个或者多个 sentinels 被 移除，因为是重复的。
- +sentinel <instance details> — 这个主节点的一个新的 sentinel 被发现和关联。
- +sdown <instance details> — 指定的实例现在处于主观下线状态。
- -sdown <instance details> — 指定的实例不再处于主观下线状态。
- +odown <instance details> — 指定的实例现在处于客观下线状态。
- -odown <instance details> — 指定的实例现在不处于客观下线状态。
- +new-epoch <instance details> — 当前时间被更新。
- +try-failover <instance details> — 准备新的故障转移，等待大多数的选举。
- +elected-leader <instance details> — 赢得了选举，开始故障转移。
- +failover-state-select-slave <instance details> — 新的故障转移状态是 select-slave：我们 正在寻找合适提升为主节点的从节点。
- no-good-slave <instance details> — 没有合适进行提升的从节点。一般会在稍后重试，但是这或许会改变并且终止故障转移。
- selected-slave <instance details> — 我们找到了指定的从节点来进行提升。
- failover-state-send-slaveof-noone <instance details> — 我们尝试重新配置这个提升后的主节点，等待它切换。
- failover-end-for-timeout <instance details> — 故障转移由于超时而停止，无论如何从节点最后被配置为复制新的主节点。
- failover-end <instance details> — 故障转移由于成功而停止，所有的从节点被配置为复制新的主节点。
- switch-master <master name> <oldip> <oldport> <newip> <newport> — 配置改变后，主节点新的 IP 和地址都是指定的。这是大多数外部用户感兴趣的消息。
- +tilt — 进入 Tilt 模式。
- -tilt — 退出 Tilt 模式。

### BUSY 状态的处理

当一个 Lua 脚本的运行时间超过了配置中指定的 Lua 脚本时间限制，Redis 实例将返回 -BUSY 错误。当这个发生的时候，在触发故障转移之前 Redis Sentinel 将尝试发送 SCRIPT KILL 命令，如果脚本是只读的，就会成功。

如果在这个尝试后，实例仍然处于失败情况，它最后会开始故障转移。

#### 从节点优先

Redis 实例有个配置参数叫 slave-priority。这个信息在 Redis 从节点实例的 INFO 输出中展示出来，并且 Sentinel 使用它来选择一个从节点在一次故障转移中：

1.  如果从节点的优先级被设置为 0，这个从节点永远不会被提升为主节点。
2.  Sentinel 首选一个由更低（ lower）优先级的从节点。

比如在当前主节点的同一个数据中心有一个从节点 S1，并且有另外的从节点 S2 在另外的数据中心，可以将 S1 优先级设置为 10，S2 优先级设置为 100，如果主节点挂掉了并且 S1 和 S2 都是可用的，S1 将是首选的。

查看关于从节点选举的更多信息，请查看本文章的 slave selection and priority 章节。

#### Sentinel 和 Redis 权限

当主节点被配置为从客户端需要密码，作为一个安全措施，从节点也需要知道这个密码为了主节点认证并且创建主-从连接用于异步复制协议。

使用下列的配置选项来实现：

- requirepass 在主节点中，为了设置认证密码，并且确保实例不会处理来自没有认证的客户端的请求。
- masterauth 在从节点中，为了取得主节点的认证，来从主节点正确的复制 数据。

当 Sentinel 使用的时候，没有一个单独的主节点，因为一次故障转移过后，从节点将扮演主节点的角色，并且老的主节点被重新配置作为一个从节点，所以你要做的是在全部的实例中设置上面的选项，包括主节点和从节点。

这通常是一个理智的设置，因为你不想要仅仅在主节点中保护你的数据，在从节点中有同样的数据。

然而，在罕见的情况下，你需要一个从节点是可进入的而不需要认证，你可以设置一个优先级为 0 的从节点来实现，阻止这个从节点被提升为主节点，配置这个从节点的 masterauth 选项，不要使用 requirepass 选项，以便数据可以被读在没有认证的情况下。

#### Sentinel 客户端实现

Sentinel 需要显式的客户端支持，除非系统配置为执行脚本来执行一个透明的重定向对于所有的主节点实例的请求（虚拟 IP 或类似的系统）。可以参考文档 Sentinel clients guidelines。

## 更高级的概念

下面的章节是关于 Sentinel 怎么工作的一些细节，没有付诸于实现的想法和算法在文章的最后章节。

### SDOWN 和 ODOWN 失败状态

Redis Sentine 有两个不同概念的下线，一个被称为主观下线（Subjectively Down ）条件（SDOWN），是一个本地 Sentinel 实例下线条件。另一个被称为客观下线（Objectively Down ）条件（ODOWN），是当足够的 Sentinels 具有 SDOWN 条件就满足 ODOWN，并且从其他的 Sentinels 使用 SENTINEL is-master-down-by-addr 命令得到反馈。

从一个 Sentinel 的角度来看，满足一个 SDOWN 条件就是在指定的时间内对于 PING 请求不能收到有效的回复，这个时间在配置文件中是 is-master-down-after-milliseconds 参数。

一个 PING 请求可接受的回复是下列之一：

回复+PONG。
回复 -LOADING 错误。
回复-MASTERDOWN 错误。
其他的回复（或根本没有回复）被认为是无效的。注意一个合理的主节点在 INFO 输出中通知他自己是一个从节点被认为是下线的。

注意 SDOWN 需要在配置中整个的时间间隔都没有收到有效的回复，因此对于实例如果时间间隔是 30000 毫秒，并且我们每隔 29 秒收到有效的回复，这个实例就被认为在工作。

SDOWN 还不够触发故障转移：它仅仅意味着一个单独的 Sentinel 相信一个 Redis 实例不可达。要触发故障转移，必须达到 ODOWN 状态。

从 SDOWN 转换到 ODOWN，没有使用强一致性算法，而仅仅是 gossip 的形式：如果一个 Sentinel 在一个给定的时间范围内从足够的 Sentinels 得到一个报告说一个主节点没有在工作，SDOWN 被提升为 ODOWN。如果这个确认稍候消失，这个标识也会清除。

一个更加严格的授权是使用大多数需要为了真正的开始故障转移，但是在达到 ODOWN 状态之前不会触发故障转移。

ODOWN 条件只适用于主节点。对于其他类型的实例，Sentinel 不需要采取行动，所以对于从节点和其他的 sentinels 来说 ODOWN 状态永远不可能达到，而仅仅只有 SDOWN 状态。

然而 SDOWN 也有语义的影响，比如一个从节点在 SDOWN 状态不会被选举来提升来执行一个故障转移。

### Sentinels 和从节点自动发现

Sentinels 和其他的 Sentinels 保持连接为了互相之间检查是否可达和交换消息。然而你不需要在每个运行的 Sentinel 实例中配置其他的 Sentinel 地址列表，Sentinel 使用 Redis 实例的发布/订阅能力来发现其他的监控相同的主节点和从节点的 Sentinels。

通过往名称为**sentinel**:hello 的通道发送 hello 消息（hello messages）来实现这个特性。

同样的，你不需要配置一个主节点关联的从节点的列表，Sentinel 也会自动发现这个列表通过问询 Redis：

- 每隔两秒，每个 Sentinel 向每个监控的主节点和从节点的发布/订阅通道**sentinel**:hello 来公布一个消息，宣布它自己的 IP，端口，id。
- 每个 Sentinel 都订阅每个主节点和从节点的发布/订阅通道**sentinel**:hello，寻找未知的 sentinels。当新的 sentinels 被检测到，他们增加这个主节点的 sentinels。
- Hello 消息也包含主节点的全部配置信息，如果接收的 Sentinel 有一个更旧的配置，它会立即更新它的配置。
- 在增加一个主节点的新的 sentinel 之前，Sentinel 总是要检查是否已经有一个有相同的 id、地址的 sentinel。在这种情况下，所有匹配的 sentinels 被移除，新的被增加。

### 故障转移之外重新配置

即使没有故障转移，Sentinels 将尝试设置当前的配置到监控的实例上面。

特别的：

- 从节点声称为主节点，将被作为从节点配置来复制当前的主节点。
- 从节点连接了一个错误的主节点，也会被重新配置来复制正确的主节点。

Sentinels 重新配置从节点，错误的配置在一段时间内应该被观察到，比在广播新的配置的时候要好得多。

这个阻止了有一个过时配置（比如说从一个分区中重新加入）的 Sentinels 在收到更新之前去交换从节点的配置。

同样注意：

- 主节点的故障转移被重新配置作为从节点当他们返回可用的时候
- 在一个网络分区中，从节点一旦可达，被重新配置。

本章最重要的教训就是：Sentinels 是每个进程总是尝试去把最后的配置施加到监控的实例上的一个系统。

### 从节点选举和优先级

当一个 Sentinel 实例准备执行故障转移，因为主节点在 ODOWN 状态下并且 Sentinel 从大多数已知的 Sentinel 实例中收到了授权开始故障转移，一个合适的从节点要被选举出来。

从节点选举过程评估从节点的下列信息：

1.  与主节点断开的时间
2.  从节点优先级
3.  复制偏移处理
4.  运行 ID

一个从节点被发现从主节点断开超过主节点配置超时（down-after-milliseconds 选项）时间十倍以上，加上从正在执行故障转移的 Sentinel 的角度看主节点不可用的时间，将被认为是不合适的并且会被跳过。

在更严格的条件下，一个从节点的 INFO 输出建议了从主节点断开超过：

```
(down-after-milliseconds \* 10) + milliseconds_since_master_is_in_SDOWN_state
```

被认为是不可靠的并且会被无视。

从节点选举只会考虑通过了上述测试的从节点，并根据上面的条件进行排序，以下列顺序：

1.  根据 Redis 实例中的 redis.conf 文件中配置的 slave-priority 进行排序，更低的优先级会被优先。
2.  如果优先级相同，检查复制偏移处理，从主节点收到更加新的数据的从节点会被选择。
3.  如果多个从节点有相同的优先级和数据偏移，执行进一步检查，选择有着更小运行 ID 的从节点。有一个更小的 ID 并不是具有正真的优点，但是对于从节点选举来说更确定，而不是随机选择一个从节点。

如果有机器是首选，Redis 主节点、从节点必须被配置一个 slave-priority。否则，所有的实例都有一个默认的 ID。

一个 Redis 实例可以被配置指定 slave-priority 为 0 为了永远不被 Sentinels 选择为新的主节点。然而一个这样配置的从节点会被 Sentinels 重新配置，为了在一次故障转移后复制新的主节点，唯一不同的是，它永远不会成为主节点。

## 算法和内部结构

下面的章节，我们将会探索 Sentinel 特性的细节。对于使用者来说，并不需要知道全部的细节，但是一个更深入的理解可能会帮助部署和操作 Sentinel 以一个更加有效的方式。

### Quorum

前面的章节展示了每个被 Sentinel 监控的主节点和一个配置的 quorum 相关联。它指定了需要同意主节点是不可达或者错误的 Sentinel 进程的数量为了触发一次故障转移。

可是，在故障转移触发后，为了真正地执行故障转移，至少大多数的 Sentinels 必须授权一个 Sentinel 开始故障转移。当只有小部分的 Sentinels 存在的一个网络分区中，故障转移永远不会执行。

我们尝试让这件事更加清晰：

- Quorum：为了把一个主节点标记成 ODOWN，需要的 Sentinel 进程数量来发现错误条件。
- ODOWN 状态触发故障转移。
- 一旦故障转移被触发，Sentinel 尝试向大多数的 Sentinels 请求授权。

不同之处看起来很微妙，但是实际上很简单地理解和使用。如果你又 5 个 Sentinel 实例，quorum 被设置为 2，一旦 2 个 Sentinel 认为主节点不可达，故障转移就会被触发。然而 2 个 Sentinels 中的一个得到 3 个 Sentinels 的授权才开始故障转移。

把 quorum 设置为 5，必须所有的 Sentinels 同意主节点失败，并为了开始故障转移，需要得到所有 Sentinels 的授权。

这意味着 quorum 在两方面可以被用来调整 Sentinel：

- 如果 quorum 被设置为小于我们部署的 Sentinels 大多数，我们使 Sentinel 对主节点失败更加敏感，并一旦少数的 Sentinels 不再和主节点交流就会触发故障转移。
- 如果 quorum 被设置为大于我们部署的 Sentinels 大多数，仅仅当大多数连接良好的 Sentinels 同意主节点挂掉的时候，Sentinel 才能开始故障转移。

### 配置 epochs

为了开始故障转移，Sentinels 需要从大多数得到授权，有下面几个重要的原因：

当一个 Sentinel 被授权，它为故障转移的主节点获得一个独一无二的配置 epoch。这将用来标识新的配置的版本在故障转移完成之后。因为大多数同意一个版本被分配给指定的 Sentinel，没有其他的 Sentinel 可以使用它。这意味着，每次故障转移的配置都有一个独一无二的版本号。我们将看到这为什么是很重要的。

此外 Sentinels 有一个规则：如果一个 Sentinel 投票给其他的 Sentinel 在一次故障转移中，它将等待一段时间再次尝试故障转移这个主节点，你可以在 sentinel.conf 中配置这个延迟时间 failover-timeout。这意味着 Sentinels 在相同的时间内不会尝试故障转移相同的主节点，第一次请求授权的将会尝试，如果失败了，另一个将会在一段时间后尝试，等等。

Redis Sentinel 保证了活性（liveness）性质，如果大多数 Sentinels 能够交流。如果主节点挂了，最后将有一个会被授权开始故障转移。

Redis Sentinel 同样也保证了安全（safety ）性质，每个 Sentinel 将使用不同的配置 epoch（configuration epoch）来故障转移同一个主节点。

### 配置传播

一旦一个 Sentinel 能成功的故障转移一个主节点，它将开始广播新的配置以便其他的 Sentinels 更新他们关于主节点的信息。

为了认定一次故障转移是成功的，它需要 Sentinel 能发送 SLAVEOF NO ONE 指令给选举的从节点，并且切换为主节点，稍后能在主节点的 INFO 输出中观察到。

这时候，即使从节点的重新配置正在进行，故障转移也被认为是成功的，并且所有的 Sentinels 需要开始报告新的配置。

一个新的配置广播的方式，就是为什么我们需要每次 Sentinel 被授权故障转移时有一个不同的版本号的原因。

每个 Sentinel 使用 Redis 发布/订阅消息来连续不断的广播它的一个主节点的配置的版本号，所有的从节点和主节点。同时，所有的 Sentinels 等待消息来查看其他的 Sentinels 广播的配置。

配置在**sentinel**:hello 发布/订阅频道中被广播。

因为每个配置都有一个不同的版本号，大的版本号总是赢得小的版本号。

例如 一开始所有的 Sentinels 认为主节点 mymaster 的配置为 192.168.1.50:6379。这个配置的版本号为 1。一段时间后，一个被授权开始故障转移有版本号 2，如果故障转移成功，它将广播新的配置，它说是 192.168.1.50:9000，版本号为 2,。所有其他的实例将看到这个配置并更新他们的配置，因为新的配置有更高的版本号。

这意味着 Sentinel 保证第二个活性属性：一个 Sentinels 集合能互相交流并且把配置信息收敛到一个更高的版本号。

基本上，如果网络是分区的，每个分区将收敛到一个更高的本地配置。没有网络分区的特殊性情况下，只有一个分区并且每个 Sentinel 将同意配置。

分区下的一致性

Redis Sentinel 配置是最终一致的，所以每个分区将收敛到更高的可用的配置。然而在使用 Sentinel 的真实世界的系统中，有三个不同的角色：

- Redis 实例
- Sentinel 实例
- 客户端

为了定义系统的行为，我们考虑所有的三种。

下面是一个简单的有三个节点的网络，每个都运行一个 Redis 实例和一个 Sentinel 实例：

```
            +-------------+
            | Sentinel 1  |----- Client A
            | Redis 1 (M) |
            +-------------+
                    |
                    |
+-------------+     |          +------------+
| Sentinel 2  |-----+-- // ----| Sentinel 3 |----- Client B
| Redis 2 (S) |                | Redis 3 (M)|
+-------------+                +------------+
```

在这个系统中，原始状态是 Redis3 是主节点，Redis1 和 Redis2 是从节点。一个网络分区发生隔离了旧的主节点。Sentinels1 和 2 开始一个故障转移过程提升 Sentinel 1 为新的主节点。

Sentinel 的属性保证 Sentinel 1 和 2 现在有了一个主节点的新的配置。可是 Sentinel 3 依然有旧的配置因为它在一个不同的分区中存活。

我们知道 Sentinel 3 将得到他的配置更新当网络分区治愈的时候，但是如果有客户端和旧的主节点在一起，分区时会发生什么呢？

客户端仍然可以向 Redis 3 写入数据。当网络分区治愈，Redis 3 变成 Reids 1 的一个从节点，在分区期间写入的数据都会丢失。

根据你的配置，你可以想或不想让这种情况发生：

- 如果你使用 Redis 作为缓存，客户端 B 仍然可以向旧的主节点写入数据是很方便的，即使数据将会丢失。
- 如果你使用 Redis 作为存储，这是不好的，你需要配置系统为了部分的阻止这个问题。

因为 Redis 是异步复制的，这种情况下，没有办法完全阻止数据丢失，但是你可以使用下面的 Redis 配置选项来限制 Redis 3 和 Redis 1 之间的分歧：

```
min-slaves-to-write 1
min-slaves-max-lag 10
```

当一个 Redis 有上面的配置，当作为主节点的时候，如果他不能向至少一个从节点写入数据，将会停止接受写入请求。因为复制是异步的，不能写（not being able to write ）意味着从节点都是分离的，或者没有发送异步确认超过了指定的 max-lag 的时间。

使用这个配置，上面的例子中的 Redis 3 将会在 10 秒之后变得不可用。当分区治愈，Sentinel 3 的配置将会是新的，Client B 能获取到一个有效的配置并继续工作。

总之， Redis + Sentinel 是一个最终一致性系统（ eventually consistent system），功能是最后一个故障转移获胜（ last failover wins）。旧节点中的数据会被丢弃，从当前主节点复制数据，所以总有一个丢失确认写的窗口。这是由于 Redis 的异步复制和系统的“虚拟”合并功能的丢弃性质。注意，Sentinel 本身没有限制，如果你把故障转移编排起来，相同的属性仍然适用，仅仅有两种方式来避免丢失写入确认：

使用同步复制
使用一个最终一致的系统，相同物体的不同版本能被合并
Redis 现在不能使用上面的任何系统，是目前的发展目标。可是有一个代理实现解决方案 2 在 Redis 存储之上，比如说 SoundCloud Roshi，或者 Netflix Dynomite。

### Sentinel 持久化状态

Sentinel 状态保存在 sentinel 配置文件中。例如，每次一个收到一个新的配置，主节点，配置和配置 epoch 一起被保存在磁盘上。这意味着停止和重启 Sentinel 进程是很安全的。

### TILT 模式

Redis Sentinel 严重依赖电脑时间：例如为了推断一个实例是否可达，它会记住最后一次成功回复 PING 命令的时间，并和当前时间比较来推断哪个是旧的。

可是，如果电脑时间意外改变了，或者电脑非常繁忙，或进程由于某些原因阻塞。Sentinel 或许开始表现意外的行为。

TILT 模式是一个特殊的“保护”模式，当发现奇怪的事情可能降低系统的可靠性，一个 Sentinel 可以进入这个模式。Sentinel 定时中断调用每秒 10 次，所以我们期待定时中断调用之间间隔 100 毫米左右。

Sentinel 所做的就是登记之前的中断调用时间，并和当前的调用时间比较：如果结果是负数或意外的数，将会进入 TILT 模式。

当处于 Sentinel 模式 Sentinel 将会继续监控每件事，但是：

- 停止一切动作
- 开始回复负数给 SENTINEL is-master-down-by-addr 请求让检测失败不再有信
- 如果 30 秒内每件事都表现正常，将退出 TILT 模式。

注意某些情况下，使用许多内核提供的单调时钟 API 代替 TILT 模式。可是它仍然是不清晰的如果这是一个很好的解决方案，因为在进程只是仅仅挂起或调度很长时间没有执行的情况下，当前的系统会避免这个问题。

## 参考资料

- 《Redis 实战》
- 《Redis 设计与实现》
