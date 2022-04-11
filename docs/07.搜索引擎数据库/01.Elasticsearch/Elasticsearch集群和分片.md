---
title: Elasticsearch集群和分片
date: 2022-03-01 20:52:25
permalink: /pages/58cab4/
---
# Elasticsearch 集群和分片

<!-- TOC depthFrom:2 depthTo:3 -->

- [1. 集群](#1-集群)
  - [1.1. 空集群](#11-空集群)
  - [1.2. 集群健康](#12-集群健康)
  - [1.3. 添加索引](#13-添加索引)
  - [1.4. 添加故障转移](#14-添加故障转移)
  - [1.5. 水平扩容](#15-水平扩容)
  - [1.6. 更多的扩容](#16-更多的扩容)
  - [1.7. 应对故障](#17-应对故障)
- [2. 分片](#2-分片)
  - [2.1. 使文本可被搜索](#21-使文本可被搜索)
  - [2.2. 不变性](#22-不变性)
  - [2.3. 动态更新索引](#23-动态更新索引)
  - [2.4. 删除和更新](#24-删除和更新)
  - [2.5. 近实时搜索](#25-近实时搜索)
  - [2.6. refresh API](#26-refresh-api)
  - [2.7. 持久化变更](#27-持久化变更)
  - [2.8. flush API](#28-flush-api)
  - [2.9. 段合并](#29-段合并)
  - [2.10. optimize API](#210-optimize-api)
- [3. 参考资料](#3-参考资料)

<!-- /TOC -->

## 1. 集群

### 1.1. 空集群

如果我们启动了一个单独的节点，里面不包含任何的数据和索引，那我们的集群看起来就是一个包含空内容节点的集群。

**Figure 1. 包含空内容节点的集群**

![包含空内容节点的集群](https://www.elastic.co/guide/cn/elasticsearch/guide/current/images/elas_0201.png)

图 1：只有一个空节点的集群

一个运行中的 Elasticsearch 实例称为一个**节点**，而**集群**是由一个或者多个拥有相同 `cluster.name` 配置的节点组成， 它们共同承担数据和负载的压力。当有节点加入集群中或者从集群中移除节点时，集群将会重新平均分布所有的数据。

当一个节点被选举成为**主节点**时， 它将负责管理集群范围内的**所有变更**，例如增加、删除索引，或者增加、删除节点等。 而主节点并不需要涉及到文档级别的变更和搜索等操作，所以当集群只拥有一个主节点的情况下，即使流量的增加它也不会成为瓶颈。 任何节点都可以成为主节点。我们的示例集群就只有一个节点，所以它同时也成为了主节点。

作为用户，我们可以将请求发送到集群中的任何节点，包括主节点。 每个节点都知道任意文档所处的位置，并且能够将我们的请求直接转发到存储我们所需文档的节点。 无论我们将请求发送到哪个节点，它都能负责从各个包含我们所需文档的节点收集回数据，并将最终结果返回給客户端。 Elasticsearch 对这一切的管理都是透明的。

### 1.2. 集群健康

Elasticsearch 的集群监控信息中包含了许多的统计数据，其中最为重要的一项就是 _集群健康_ ， 它在 `status` 字段中展示为 `green` 、 `yellow` 或者 `red` 。

```bash
GET /_cluster/health
```

在一个不包含任何索引的空集群中，它将会有一个类似于如下所示的返回内容：

```json
{
  "cluster_name": "elasticsearch",
  "status": "green",
  "timed_out": false,
  "number_of_nodes": 1,
  "number_of_data_nodes": 1,
  "active_primary_shards": 0,
  "active_shards": 0,
  "relocating_shards": 0,
  "initializing_shards": 0,
  "unassigned_shards": 0
}
```

`status` 字段指示着当前集群在总体上是否工作正常。它的三种颜色含义如下：

- **`green`**：所有的主分片和副本分片都正常运行。
- **`yellow`**：所有的主分片都正常运行，但不是所有的副本分片都正常运行。
- **`red`**：有主分片没能正常运行。

### 1.3. 添加索引

我们往 Elasticsearch 添加数据时需要用到 _索引_ —— 保存相关数据的地方。索引实际上是指向一个或者多个物理分片的逻辑命名空间 。

一个 _分片_ 是一个底层的 _工作单元_ ，它仅保存了全部数据中的一部分。现在我们只需知道一个分片是一个 Lucene 的实例，以及它本身就是一个完整的搜索引擎。 我们的文档被存储和索引到分片内，但是应用程序是直接与索引而不是与分片进行交互。

Elasticsearch 是利用分片将数据分发到集群内各处的。分片是数据的容器，文档保存在分片内，分片又被分配到集群内的各个节点里。 当你的集群规模扩大或者缩小时， Elasticsearch 会自动的在各节点中迁移分片，使得数据仍然均匀分布在集群里。

一个分片可以是 _主_ 分片或者 _副本_ 分片。 索引内任意一个文档都归属于一个主分片，所以主分片的数目决定着索引能够保存的最大数据量。

> 技术上来说，一个主分片最大能够存储 `Integer.MAX_VALUE - 128` 个文档，但是实际最大值还需要参考你的使用场景：包括你使用的硬件， 文档的大小和复杂程度，索引和查询文档的方式以及你期望的响应时长。

一个副本分片只是一个主分片的拷贝。副本分片作为硬件故障时保护数据不丢失的冗余备份，并为搜索和返回文档等读操作提供服务。

在索引建立的时候就已经确定了主分片数，但是副本分片数可以随时修改。

让我们在包含一个空节点的集群内创建名为 `blogs` 的索引。 索引在默认情况下会被分配 5 个主分片， 但是为了演示目的，我们将分配 3 个主分片和一份副本（每个主分片拥有一个副本分片）：

```java
PUT /blogs
{
   "settings" : {
      "number_of_shards" : 3,
      "number_of_replicas" : 1
   }
}
```

我们的集群现在是 _拥有一个索引的单节点集群_。所有 3 个主分片都被分配在 `Node 1` 。

**Figure 2. 拥有一个索引的单节点集群**

![拥有一个索引的单节点集群](https://www.elastic.co/guide/cn/elasticsearch/guide/current/images/elas_0202.png)

如果我们现在查看集群健康，我们将看到如下内容：

```json
{
  "cluster_name": "elasticsearch",
  "status": "yellow",
  "timed_out": false,
  "number_of_nodes": 1,
  "number_of_data_nodes": 1,
  "active_primary_shards": 3,
  "active_shards": 3,
  "relocating_shards": 0,
  "initializing_shards": 0,
  "unassigned_shards": 3,
  "delayed_unassigned_shards": 0,
  "number_of_pending_tasks": 0,
  "number_of_in_flight_fetch": 0,
  "task_max_waiting_in_queue_millis": 0,
  "active_shards_percent_as_number": 50
}
```

- 集群 status 值为 yellow
- 没有被分配到任何节点的副本数

集群的健康状况为 `yellow` 则表示全部 _主_ 分片都正常运行（集群可以正常服务所有请求），但是 _副本_ 分片没有全部处在正常状态。 实际上，所有 3 个副本分片都是 `unassigned` —— 它们都没有被分配到任何节点。 在同一个节点上既保存原始数据又保存副本是没有意义的，因为一旦失去了那个节点，我们也将丢失该节点上的所有副本数据。

当前我们的集群是正常运行的，但是在硬件故障时有丢失数据的风险。

### 1.4. 添加故障转移

当集群中只有一个节点在运行时，意味着会有一个单点故障问题——没有冗余。 幸运的是，我们只需再启动一个节点即可防止数据丢失。

> 为了测试第二个节点启动后的情况，你可以在同一个目录内，完全依照启动第一个节点的方式来启动一个新节点（参考安装并运行 Elasticsearch）。多个节点可以共享同一个目录。
>
> 当你在同一台机器上启动了第二个节点时，只要它和第一个节点有同样的 cluster.name 配置，它就会自动发现集群并加入到其中。 但是在不同机器上启动节点的时候，为了加入到同一集群，你需要配置一个可连接到的单播主机列表。

如果启动了第二个节点，我们的集群将会拥有两个节点的集群——所有主分片和副本分片都已被分配。

**Figure 3. 拥有两个节点的集群——所有主分片和副本分片都已被分配**

![拥有两个节点的集群](https://www.elastic.co/guide/cn/elasticsearch/guide/current/images/elas_0203.png)

当第二个节点加入到集群后，3 个 _副本分片_ 将会分配到这个节点上——每个主分片对应一个副本分片。 这意味着当集群内任何一个节点出现问题时，我们的数据都完好无损。

所有新近被索引的文档都将会保存在主分片上，然后被并行的复制到对应的副本分片上。这就保证了我们既可以从主分片又可以从副本分片上获得文档。

`cluster-health` 现在展示的状态为 `green` ，这表示所有 6 个分片（包括 3 个主分片和 3 个副本分片）都在正常运行。

```json
{
  "cluster_name": "elasticsearch",
  "status": "green",
  "timed_out": false,
  "number_of_nodes": 2,
  "number_of_data_nodes": 2,
  "active_primary_shards": 3,
  "active_shards": 6,
  "relocating_shards": 0,
  "initializing_shards": 0,
  "unassigned_shards": 0,
  "delayed_unassigned_shards": 0,
  "number_of_pending_tasks": 0,
  "number_of_in_flight_fetch": 0,
  "task_max_waiting_in_queue_millis": 0,
  "active_shards_percent_as_number": 100
}
```

- 集群 `status` 值为 `green`

我们的集群现在不仅仅是正常运行的，并且还处于 _始终可用_ 的状态。

### 1.5. 水平扩容

怎样为我们的正在增长中的应用程序按需扩容呢？ 当启动了第三个节点，我们的集群将拥有三个节点的集群——为了分散负载而对分片进行重新分配。

**Figure 4. 拥有三个节点的集群——为了分散负载而对分片进行重新分配**

![拥有三个节点的集群](https://www.elastic.co/guide/cn/elasticsearch/guide/current/images/elas_0204.png)

`Node 1` 和 `Node 2` 上各有一个分片被迁移到了新的 `Node 3` 节点，现在每个节点上都拥有 2 个分片，而不是之前的 3 个。 这表示每个节点的硬件资源（CPU, RAM, I/O）将被更少的分片所共享，每个分片的性能将会得到提升。

分片是一个功能完整的搜索引擎，它拥有使用一个节点上的所有资源的能力。 我们这个拥有 6 个分片（3 个主分片和 3 个副本分片）的索引可以最大扩容到 6 个节点，每个节点上存在一个分片，并且每个分片拥有所在节点的全部资源。

### 1.6. 更多的扩容

但是如果我们想要扩容超过 6 个节点怎么办呢？

主分片的数目在索引创建时就已经确定了下来。实际上，这个数目定义了这个索引能够 _存储_ 的最大数据量。（实际大小取决于你的数据、硬件和使用场景。） 但是，读操作——搜索和返回数据——可以同时被主分片 _或_ 副本分片所处理，所以当你拥有越多的副本分片时，也将拥有越高的吞吐量。

在运行中的集群上是可以动态调整副本分片数目的，我们可以按需伸缩集群。让我们把副本数从默认的 `1` 增加到 `2` ：

```sense
PUT /blogs/_settings
{
   "number_of_replicas" : 2
}
```

`blogs` 索引现在拥有 9 个分片：3 个主分片和 6 个副本分片。 这意味着我们可以将集群扩容到 9 个节点，每个节点上一个分片。相比原来 3 个节点时，集群搜索性能可以提升 _3_ 倍。

**Figure 5. 将参数 `number_of_replicas` 调大到 2**

![拥有2份副本分片3个节点的集群](https://www.elastic.co/guide/cn/elasticsearch/guide/current/images/elas_0205.png)

> 当然，如果只是在相同节点数目的集群上增加更多的副本分片并不能提高性能，因为每个分片从节点上获得的资源会变少。 你需要增加更多的硬件资源来提升吞吐量。
>
> 但是更多的副本分片数提高了数据冗余量：按照上面的节点配置，我们可以在失去 2 个节点的情况下不丢失任何数据。

### 1.7. 应对故障

我们之前说过 Elasticsearch 可以应对节点故障，接下来让我们尝试下这个功能。 如果我们关闭第一个节点，这时集群的状态为关闭了一个节点后的集群。

**Figure 6. 关闭了一个节点后的集群**

![关闭了一个节点后的集群](https://www.elastic.co/guide/cn/elasticsearch/guide/current/images/elas_0206.png)

我们关闭的节点是一个主节点。而集群必须拥有一个主节点来保证正常工作，所以发生的第一件事情就是选举一个新的主节点： `Node 2` 。

在我们关闭 `Node 1` 的同时也失去了主分片 `1` 和 `2` ，并且在缺失主分片的时候索引也不能正常工作。 如果此时来检查集群的状况，我们看到的状态将会为 `red` ：不是所有主分片都在正常工作。

幸运的是，在其它节点上存在着这两个主分片的完整副本， 所以新的主节点立即将这些分片在 `Node 2` 和 `Node 3` 上对应的副本分片提升为主分片， 此时集群的状态将会为 `yellow` 。 这个提升主分片的过程是瞬间发生的，如同按下一个开关一般。

为什么我们集群状态是 `yellow` 而不是 `green` 呢？ 虽然我们拥有所有的三个主分片，但是同时设置了每个主分片需要对应 2 份副本分片，而此时只存在一份副本分片。 所以集群不能为 `green` 的状态，不过我们不必过于担心：如果我们同样关闭了 `Node 2` ，我们的程序 _依然_ 可以保持在不丢任何数据的情况下运行，因为 `Node 3` 为每一个分片都保留着一份副本。

如果我们重新启动 `Node 1` ，集群可以将缺失的副本分片再次进行分配，那么集群的状态也将如 Figure 5. 将参数 `number_of_replicas` 调大到 2 所示。 如果 `Node 1` 依然拥有着之前的分片，它将尝试去重用它们，同时仅从主分片复制发生了修改的数据文件。

到目前为止，你应该对分片如何使得 Elasticsearch 进行水平扩容以及数据保障等知识有了一定了解。 接下来我们将讲述关于分片生命周期的更多细节。

## 2. 分片

> - 为什么搜索是 _近_ 实时的？
> - 为什么文档的 CRUD (创建-读取-更新-删除) 操作是 _实时_ 的?
> - Elasticsearch 是怎样保证更新被持久化在断电时也不丢失数据?
> - 为什么删除文档不会立刻释放空间？
> - `refresh`, `flush`, 和 `optimize` API 都做了什么, 你什么情况下应该使用他们？

### 2.1. 使文本可被搜索

必须解决的第一个挑战是如何使文本可被搜索。 传统的数据库每个字段存储单个值，但这对全文检索并不够。文本字段中的每个单词需要被搜索，对数据库意味着需要单个字段有索引多值(这里指单词)的能力。

最好的支持 _一个字段多个值_ 需求的数据结构是我们在 [倒排索引](https://www.elastic.co/guide/cn/elasticsearch/guide/current/inverted-index.html) 章节中介绍过的 _倒排索引_ 。 倒排索引包含一个有序列表，列表包含所有文档出现过的不重复个体，或称为 _词项_ ，对于每一个词项，包含了它所有曾出现过文档的列表。

```
Term  | Doc 1 | Doc 2 | Doc 3 | ...
------------------------------------
brown |   X   |       |  X    | ...
fox   |   X   |   X   |  X    | ...
quick |   X   |   X   |       | ...
the   |   X   |       |  X    | ...
```

> 当讨论倒排索引时，我们会谈到 _文档_ 标引，因为历史原因，倒排索引被用来对整个非结构化文本文档进行标引。 Elasticsearch 中的 _文档_ 是有字段和值的结构化 JSON 文档。事实上，在 JSON 文档中， 每个被索引的字段都有自己的倒排索引。

这个倒排索引相比特定词项出现过的文档列表，会包含更多其它信息。它会保存每一个词项出现过的文档总数， 在对应的文档中一个具体词项出现的总次数，词项在文档中的顺序，每个文档的长度，所有文档的平均长度，等等。这些统计信息允许 Elasticsearch 决定哪些词比其它词更重要，哪些文档比其它文档更重要，这些内容在 [什么是相关性?](https://www.elastic.co/guide/cn/elasticsearch/guide/current/relevance-intro.html) 中有描述。

为了能够实现预期功能，倒排索引需要知道集合中的 _所有_ 文档，这是需要认识到的关键问题。

早期的全文检索会为整个文档集合建立一个很大的倒排索引并将其写入到磁盘。 一旦新的索引就绪，旧的就会被其替换，这样最近的变化便可以被检索到。

### 2.2. 不变性

倒排索引被写入磁盘后是 _不可改变_ 的:它永远不会修改。 不变性有重要的价值：

- 不需要锁。如果你从来不更新索引，你就不需要担心多进程同时修改数据的问题。
- 一旦索引被读入内核的文件系统缓存，便会留在哪里，由于其不变性。只要文件系统缓存中还有足够的空间，那么大部分读请求会直接请求内存，而不会命中磁盘。这提供了很大的性能提升。
- 其它缓存(像 filter 缓存)，在索引的生命周期内始终有效。它们不需要在每次数据改变时被重建，因为数据不会变化。
- 写入单个大的倒排索引允许数据被压缩，减少磁盘 I/O 和 需要被缓存到内存的索引的使用量。

当然，一个不变的索引也有不好的地方。主要事实是它是不可变的! 你不能修改它。如果你需要让一个新的文档 可被搜索，你需要重建整个索引。这要么对一个索引所能包含的数据量造成了很大的限制，要么对索引可被更新的频率造成了很大的限制。

### 2.3. 动态更新索引

下一个需要被解决的问题是怎样在保留不变性的前提下实现倒排索引的更新？答案是: 用更多的索引。

通过增加新的补充索引来反映新近的修改，而不是直接重写整个倒排索引。每一个倒排索引都会被轮流查询到—从最早的开始—查询完后再对结果进行合并。

Elasticsearch 基于 Lucene, 这个 java 库引入了 按段搜索 的概念。 每一 段 本身都是一个倒排索引， 但 索引 在 Lucene 中除表示所有 段 的集合外， 还增加了 提交点 的概念 — 一个列出了所有已知段的文件，就像在 Figure 16, “一个 Lucene 索引包含一个提交点和三个段” 中描绘的那样。 如 Figure 17, “一个在内存缓存中包含新文档的 Lucene 索引” 所示，新的文档首先被添加到内存索引缓存中，然后写入到一个基于磁盘的段，如 Figure 18, “在一次提交后，一个新的段被添加到提交点而且缓存被清空。” 所示。

**Figure 16. 一个 Lucene 索引包含一个提交点和三个段**

![A Lucene index with a commit point and three segments](https://www.elastic.co/guide/cn/elasticsearch/guide/current/images/elas_1101.png)

> 被混淆的概念是，一个 _Lucene 索引_ 我们在 Elasticsearch 称作 _分片_ 。 一个 Elasticsearch _索引_ 是分片的集合。 当 Elasticsearch 在索引中搜索的时候， 他发送查询到每一个属于索引的分片(Lucene 索引)，然后像 [_执行分布式检索_](https://www.elastic.co/guide/cn/elasticsearch/guide/current/distributed-search.html) 提到的那样，合并每个分片的结果到一个全局的结果集。

逐段搜索会以如下流程进行工作：

1. 新文档被收集到内存索引缓存， 见 Figure 17, “一个在内存缓存中包含新文档的 Lucene 索引” 。
2. 不时地, 缓存被 _提交_ ：
   - 一个新的段—一个追加的倒排索引—被写入磁盘。
   - 一个新的包含新段名字的 _提交点_ 被写入磁盘。
   - 磁盘进行 _同步_ — 所有在文件系统缓存中等待的写入都刷新到磁盘，以确保它们被写入物理文件。
3. 新的段被开启，让它包含的文档可见以被搜索。
4. 内存缓存被清空，等待接收新的文档。

**Figure 17. 一个在内存缓存中包含新文档的 Lucene 索引**

![A Lucene index with new documents in the in-memory buffer, ready to commit](https://www.elastic.co/guide/cn/elasticsearch/guide/current/images/elas_1102.png)

**Figure 18. 在一次提交后，一个新的段被添加到提交点而且缓存被清空。**

![After a commit, a new segment is added to the index and the buffer is cleared](https://www.elastic.co/guide/cn/elasticsearch/guide/current/images/elas_1103.png)

当一个查询被触发，所有已知的段按顺序被查询。词项统计会对所有段的结果进行聚合，以保证每个词和每个文档的关联都被准确计算。 这种方式可以用相对较低的成本将新文档添加到索引。

### 2.4. 删除和更新

段是不可改变的，所以既不能从把文档从旧的段中移除，也不能修改旧的段来进行反映文档的更新。 取而代之的是，每个提交点会包含一个 `.del` 文件，文件中会列出这些被删除文档的段信息。

当一个文档被 “删除” 时，它实际上只是在 `.del` 文件中被 _标记_ 删除。一个被标记删除的文档仍然可以被查询匹配到， 但它会在最终结果被返回前从结果集中移除。

文档更新也是类似的操作方式：当一个文档被更新时，旧版本文档被标记删除，文档的新版本被索引到一个新的段中。 可能两个版本的文档都会被一个查询匹配到，但被删除的那个旧版本文档在结果集返回前就已经被移除。

在 [段合并](https://www.elastic.co/guide/cn/elasticsearch/guide/current/merge-process.html) , 我们展示了一个被删除的文档是怎样被文件系统移除的。

### 2.5. 近实时搜索

随着按段（per-segment）搜索的发展，一个新的文档从索引到可被搜索的延迟显著降低了。新文档在几分钟之内即可被检索，但这样还是不够快。

磁盘在这里成为了瓶颈。提交（Commiting）一个新的段到磁盘需要一个 [`fsync`](http://en.wikipedia.org/wiki/Fsync) 来确保段被物理性地写入磁盘，这样在断电的时候就不会丢失数据。 但是 `fsync` 操作代价很大; 如果每次索引一个文档都去执行一次的话会造成很大的性能问题。

我们需要的是一个更轻量的方式来使一个文档可被搜索，这意味着 `fsync` 要从整个过程中被移除。

在 Elasticsearch 和磁盘之间是文件系统缓存。 像之前描述的一样， 在内存索引缓冲区（ [Figure 19, “在内存缓冲区中包含了新文档的 Lucene 索引”](https://www.elastic.co/guide/cn/elasticsearch/guide/current/near-real-time.html#img-pre-refresh) ）中的文档会被写入到一个新的段中（ [Figure 20, “缓冲区的内容已经被写入一个可被搜索的段中，但还没有进行提交”](https://www.elastic.co/guide/cn/elasticsearch/guide/current/near-real-time.html#img-post-refresh) ）。 但是这里新段会被先写入到文件系统缓存—这一步代价会比较低，稍后再被刷新到磁盘—这一步代价比较高。不过只要文件已经在缓存中， 就可以像其它文件一样被打开和读取了。

**Figure 19. 在内存缓冲区中包含了新文档的 Lucene 索引**

![A Lucene index with new documents in the in-memory buffer](https://www.elastic.co/guide/cn/elasticsearch/guide/current/images/elas_1104.png)

Lucene 允许新段被写入和打开—使其包含的文档在未进行一次完整提交时便对搜索可见。 这种方式比进行一次提交代价要小得多，并且在不影响性能的前提下可以被频繁地执行。

**Figure 20. 缓冲区的内容已经被写入一个可被搜索的段中，但还没有进行提交**

![The buffer contents have been written to a segment, which is searchable, but is not yet commited](https://www.elastic.co/guide/cn/elasticsearch/guide/current/images/elas_1105.png)

### 2.6. refresh API

在 Elasticsearch 中，写入和打开一个新段的轻量的过程叫做 _refresh_ 。 默认情况下每个分片会每秒自动刷新一次。这就是为什么我们说 Elasticsearch 是 _近_ 实时搜索: 文档的变化并不是立即对搜索可见，但会在一秒之内变为可见。

这些行为可能会对新用户造成困惑: 他们索引了一个文档然后尝试搜索它，但却没有搜到。这个问题的解决办法是用 `refresh` API 执行一次手动刷新:

```bash
POST /_refresh
POST /blogs/_refresh
```

刷新（Refresh）所有的索引

只刷新（Refresh） blogs 索引

> 尽管刷新是比提交轻量很多的操作，它还是会有性能开销。当写测试的时候， 手动刷新很有用，但是不要在生产环境下每次索引一个文档都去手动刷新。 相反，你的应用需要意识到 Elasticsearch 的近实时的性质，并接受它的不足。

并不是所有的情况都需要每秒刷新。可能你正在使用 Elasticsearch 索引大量的日志文件， 你可能想优化索引速度而不是近实时搜索， 可以通过设置 `refresh_interval` ， 降低每个索引的刷新频率：

```json
PUT /my_logs
{
  "settings": {
    "refresh_interval": "30s"
  }
}
```

> 每 30 秒刷新 `my_logs` 索引。

`refresh_interval` 可以在既存索引上进行动态更新。 在生产环境中，当你正在建立一个大的新索引时，可以先关闭自动刷新，待开始使用该索引时，再把它们调回来：

```
PUT /my_logs/_settings
{ "refresh_interval": -1 }

PUT /my_logs/_settings
{ "refresh_interval": "1s" }
```

- 关闭自动刷新。

- 每秒自动刷新。

> `refresh_interval` 需要一个 _持续时间_ 值， 例如 `1s` （1 秒） 或 `2m` （2 分钟）。 一个绝对值 _1_ 表示的是 _1 毫秒_ --无疑会使你的集群陷入瘫痪。

### 2.7. 持久化变更

如果没有用 `fsync` 把数据从文件系统缓存刷（flush）到硬盘，我们不能保证数据在断电甚至是程序正常退出之后依然存在。为了保证 Elasticsearch 的可靠性，需要确保数据变化被持久化到磁盘。

在 [动态更新索引](https://www.elastic.co/guide/cn/elasticsearch/guide/current/dynamic-indices.html)，我们说一次完整的提交会将段刷到磁盘，并写入一个包含所有段列表的提交点。Elasticsearch 在启动或重新打开一个索引的过程中使用这个提交点来判断哪些段隶属于当前分片。

即使通过每秒刷新（refresh）实现了近实时搜索，我们仍然需要经常进行完整提交来确保能从失败中恢复。但在两次提交之间发生变化的文档怎么办？我们也不希望丢失掉这些数据。

Elasticsearch 增加了一个 _translog_ ，或者叫事务日志，在每一次对 Elasticsearch 进行操作时均进行了日志记录。通过 translog ，整个流程看起来是下面这样：

一个文档被索引之后，就会被添加到内存缓冲区，_并且_ 追加到了 translog ，正如 [Figure 21, “新的文档被添加到内存缓冲区并且被追加到了事务日志”](https://www.elastic.co/guide/cn/elasticsearch/guide/current/translog.html#img-xlog-pre-refresh) 描述的一样。

**Figure 21. 新的文档被添加到内存缓冲区并且被追加到了事务日志**

![New documents are added to the in-memory buffer and appended to the transaction log](https://www.elastic.co/guide/cn/elasticsearch/guide/current/images/elas_1106.png)

刷新（refresh）使分片处于 [Figure 22, “刷新（refresh）完成后, 缓存被清空但是事务日志不会”](https://www.elastic.co/guide/cn/elasticsearch/guide/current/translog.html#img-xlog-post-refresh) 描述的状态，分片每秒被刷新（refresh）一次：

- 这些在内存缓冲区的文档被写入到一个新的段中，且没有进行 `fsync` 操作。
- 这个段被打开，使其可被搜索。
- 内存缓冲区被清空。

**Figure 22. 刷新（refresh）完成后, 缓存被清空但是事务日志不会**

![After a refresh, the buffer is cleared but the transaction log is not](https://www.elastic.co/guide/cn/elasticsearch/guide/current/images/elas_1107.png)

这个进程继续工作，更多的文档被添加到内存缓冲区和追加到事务日志（见 [Figure 23, “事务日志不断积累文档”](https://www.elastic.co/guide/cn/elasticsearch/guide/current/translog.html#img-xlog-pre-flush) ）。

**Figure 23. 事务日志不断积累文档**

![The transaction log keeps accumulating documents](https://www.elastic.co/guide/cn/elasticsearch/guide/current/images/elas_1108.png)

1. 每隔一段时间—例如 translog 变得越来越大—索引被刷新（flush）；一个新的 translog 被创建，并且一个全量提交被执行（见 [Figure 24, “在刷新（flush）之后，段被全量提交，并且事务日志被清空”](https://www.elastic.co/guide/cn/elasticsearch/guide/current/translog.html#img-xlog-post-flush) ）：
   - 所有在内存缓冲区的文档都被写入一个新的段。
   - 缓冲区被清空。
   - 一个提交点被写入硬盘。
   - 文件系统缓存通过 `fsync` 被刷新（flush）。
   - 老的 translog 被删除。

translog 提供所有还没有被刷到磁盘的操作的一个持久化纪录。当 Elasticsearch 启动的时候， 它会从磁盘中使用最后一个提交点去恢复已知的段，并且会重放 translog 中所有在最后一次提交后发生的变更操作。

translog 也被用来提供实时 CRUD 。当你试着通过 ID 查询、更新、删除一个文档，它会在尝试从相应的段中检索之前， 首先检查 translog 任何最近的变更。这意味着它总是能够实时地获取到文档的最新版本。

**Figure 24. 在刷新（flush）之后，段被全量提交，并且事务日志被清空**

![After a flush, the segments are fully commited and the transaction log is cleared](https://www.elastic.co/guide/cn/elasticsearch/guide/current/images/elas_1109.png)

### 2.8. flush API

这个执行一个提交并且截断 translog 的行为在 Elasticsearch 被称作一次 _flush_ 。 分片每 30 分钟被自动刷新（flush），或者在 translog 太大的时候也会刷新。请查看 [`translog` 文档](https://www.elastic.co/guide/en/elasticsearch/reference/2.4/index-modules-translog.html#_translog_settings) 来设置，它可以用来 控制这些阈值：

[`flush` API](https://www.elastic.co/guide/en/elasticsearch/reference/5.6/indices-flush.html) 可以被用来执行一个手工的刷新（flush）:

```
POST /blogs/_flush
POST /_flush?wait_for_ongoing
```

- 刷新（flush） blogs 索引。
- 刷新（flush）所有的索引并且并且等待所有刷新在返回前完成。

你很少需要自己手动执行 `flush` 操作；通常情况下，自动刷新就足够了。

这就是说，在重启节点或关闭索引之前执行 [flush](https://www.elastic.co/guide/cn/elasticsearch/guide/current/translog.html#flush-api) 有益于你的索引。当 Elasticsearch 尝试恢复或重新打开一个索引， 它需要重放 translog 中所有的操作，所以如果日志越短，恢复越快。

> translog 的目的是保证操作不会丢失。这引出了这个问题： Translog 有多安全？
>
> 在文件被 `fsync` 到磁盘前，被写入的文件在重启之后就会丢失。默认 translog 是每 5 秒被 `fsync` 刷新到硬盘， 或者在每次写请求完成之后执行(e.g. index, delete, update, bulk)。这个过程在主分片和复制分片都会发生。最终， 基本上，这意味着在整个请求被 `fsync` 到主分片和复制分片的 translog 之前，你的客户端不会得到一个 200 OK 响应。
>
> 在每次请求后都执行一个 fsync 会带来一些性能损失，尽管实践表明这种损失相对较小（特别是 bulk 导入，它在一次请求中平摊了大量文档的开销）。
>
> 但是对于一些大容量的偶尔丢失几秒数据问题也并不严重的集群，使用异步的 fsync 还是比较有益的。比如，写入的数据被缓存到内存中，再每 5 秒执行一次 `fsync` 。
>
> 这个行为可以通过设置 `durability` 参数为 `async` 来启用：
>
> ```js
> PUT /my_index/_settings
> {
>     "index.translog.durability": "async",
>     "index.translog.sync_interval": "5s"
> }
> ```
>
> 这个选项可以针对索引单独设置，并且可以动态进行修改。如果你决定使用异步 translog 的话，你需要 _保证_ 在发生 crash 时，丢失掉 `sync_interval` 时间段的数据也无所谓。请在决定前知晓这个特性。
>
> 如果你不确定这个行为的后果，最好是使用默认的参数（ `"index.translog.durability": "request"` ）来避免数据丢失。

### 2.9. 段合并

由于自动刷新流程每秒会创建一个新的段 ，这样会导致短时间内的段数量暴增。而段数目太多会带来较大的麻烦。 每一个段都会消耗文件句柄、内存和 cpu 运行周期。更重要的是，每个搜索请求都必须轮流检查每个段；所以段越多，搜索也就越慢。

Elasticsearch 通过在后台进行段合并来解决这个问题。小的段被合并到大的段，然后这些大的段再被合并到更大的段。

段合并的时候会将那些旧的已删除文档从文件系统中清除。被删除的文档（或被更新文档的旧版本）不会被拷贝到新的大段中。

启动段合并不需要你做任何事。进行索引和搜索时会自动进行。这个流程像在 [Figure 25, “两个提交了的段和一个未提交的段正在被合并到一个更大的段”](https://www.elastic.co/guide/cn/elasticsearch/guide/current/merge-process.html#img-merge) 中提到的一样工作：

1、 当索引的时候，刷新（refresh）操作会创建新的段并将段打开以供搜索使用。

2、 合并进程选择一小部分大小相似的段，并且在后台将它们合并到更大的段中。这并不会中断索引和搜索。

**Figure 25. 两个提交了的段和一个未提交的段正在被合并到一个更大的段**

![Two commited segments and one uncommited segment in the process of being merged into a bigger segment](https://www.elastic.co/guide/cn/elasticsearch/guide/current/images/elas_1110.png)

[Figure 26, “一旦合并结束，老的段被删除”](https://www.elastic.co/guide/cn/elasticsearch/guide/current/merge-process.html#img-post-merge) 说明合并完成时的活动：

- 新的段被刷新（flush）到了磁盘。 \*\* 写入一个包含新段且排除旧的和较小的段的新提交点。
- 新的段被打开用来搜索。
- 老的段被删除。

**Figure 26. 一旦合并结束，老的段被删除**

![一旦合并结束，老的段被删除](https://www.elastic.co/guide/cn/elasticsearch/guide/current/images/elas_1111.png)

合并大的段需要消耗大量的 I/O 和 CPU 资源，如果任其发展会影响搜索性能。Elasticsearch 在默认情况下会对合并流程进行资源限制，所以搜索仍然 有足够的资源很好地执行。

### 2.10. optimize API

`optimize` API 大可看做是 _强制合并_ API。它会将一个分片强制合并到 `max_num_segments` 参数指定大小的段数目。 这样做的意图是减少段的数量（通常减少到一个），来提升搜索性能。

> `optimize` API _不应该_ 被用在一个活跃的索引————一个正积极更新的索引。后台合并流程已经可以很好地完成工作。 optimizing 会阻碍这个进程。不要干扰它！

在特定情况下，使用 `optimize` API 颇有益处。例如在日志这种用例下，每天、每周、每月的日志被存储在一个索引中。 老的索引实质上是只读的；它们也并不太可能会发生变化。

在这种情况下，使用 optimize 优化老的索引，将每一个分片合并为一个单独的段就很有用了；这样既可以节省资源，也可以使搜索更加快速：

```bash
POST /logstash-2014-10/_optimize?max_num_segments=1
```

合并索引中的每个分片为一个单独的段

> 请注意，使用 `optimize` API 触发段合并的操作不会受到任何资源上的限制。这可能会消耗掉你节点上全部的 I/O 资源, 使其没有余裕来处理搜索请求，从而有可能使集群失去响应。 如果你想要对索引执行 `optimize`，你需要先使用分片分配（查看 [迁移旧索引](https://www.elastic.co/guide/cn/elasticsearch/guide/current/retiring-data.html#migrate-indices)）把索引移到一个安全的节点，再执行。

## 3. 参考资料

- [Elasticsearch 官方文档之 集群内的原理](https://www.elastic.co/guide/cn/elasticsearch/guide/current/distributed-cluster.html)
