# Elasticsearch 基本概念

## 文档

Elasticsearch 是面向文档的，文档是所有可搜索数据的最小单位。

Elasticsearch 使用 [_JSON_](http://en.wikipedia.org/wiki/Json) 作为文档的序列化格式。

每个文档都有一个 Unique ID

- 用户可以自己指定
- 或通过 Elasticsearch 自动生成

### 文档的元数据

一个文档不仅仅包含它的数据 ，也包含**元数据** —— 有关文档的信息。

- `_index`：文档在哪存放
- `_type`：文档表示的对象类别
- `_id`：文档唯一标识
- `_source`：文档的原始 Json 数据
- `_all`：整合所有字段内容到该字段，已被废除
- `_version`：文档的版本信息
- `_score`：相关性打分

示例：

```json
{
  "_index": "megacorp",
  "_type": "employee",
  "_id": "1",
  "_version": 1,
  "found": true,
  "_source": {
    "first_name": "John",
    "last_name": "Smith",
    "age": 25,
    "about": "I love to go rock climbing",
    "interests": ["sports", "music"]
  }
}
```

## 索引

索引在不同语境，有着不同的含义

- 索引（名词）：一个 **索引** 类似于传统关系数据库中的一个 **数据库** ，是一个存储关系型文档的容器。 索引 (_index_) 的复数词为 indices 或 indexes 。索引实际上是指向一个或者多个**物理分片**的**逻辑命名空间** 。
- 索引（动词）：索引一个文档 就是存储一个文档到一个 _索引_ （名词）中以便被检索和查询。这非常类似于 SQL 语句中的 `INSERT` 关键词，除了文档已存在时，新文档会替换旧文档情况之外。
- 倒排索引：关系型数据库通过增加一个索引比如一个 B 树索引到指定的列上，以便提升数据检索速度。Elasticsearch 和 Lucene 使用了一个叫做 **倒排索引** 的结构来达到相同的目的。

索引的 Mapping 和 Setting

Mapping 定义文档字段的类型

Setting 定义不同的数据分布

示例：

```
{
    "settings": { ... any settings ... },
    "mappings": {
        "type_one": { ... any mappings ... },
        "type_two": { ... any mappings ... },
        ...
    }
}
```

### 倒排索引

![](https://raw.githubusercontent.com/dunwu/images/dev/snap/20220108215559.PNG)

## 节点

### 节点简介

一个运行中的 Elasticsearch 实例称为一个**节点**。

Elasticsearch 实例本质上是一个 Java 进程。一台机器上可以运行多个 Elasticsearch 进程，但是生产环境建议一台机器上只运行一个 Elasticsearch 进程

每个节点都有名字，通过配置文件配置，或启动时通过 `-E node.name=node1` 指定。

每个节点在启动后，会分配一个 UID，保存在 data 目录下。

### 节点类型

- **主节点（master node）**：每个节点都保存了集群的状态，只有 master 节点才能修改集群的状态信息（保证数据一致性）。**集群状态**，维护了以下信息：
  - 所有的节点信息
  - 所有的索引和其相关的 mapping 和 setting 信息
  - 分片的路由信息
- **候选节点（master eligible node）**：master eligible 节点可以参加选主流程。第一个启动的节点，会将自己选举为 mater 节点。
  - 每个节点启动后，默认为 master eligible 节点，可以通过配置 `node.master: false` 禁止
- **数据节点（data node）**：负责保存分片数据。
- **协调节点（coordinating node）**：负责接收客户端的请求，将请求分发到合适的接地那，最终把结果汇集到一起。每个 Elasticsearch 节点默认都是协调节点（coordinating node）。
- **冷/热节点（warm/hot node）**：针对不同硬件配置的数据节点（data node），用来实现 Hot & Warm 架构，降低集群部署的成本。
- **机器学习节点（machine learning node）**：负责执行机器学习的 Job，用来做异常检测。

### 节点配置

| 配置参数    | 默认值 | 说明                                  |
| ----------- | ------ | ------------------------------------- |
| node.master | true   | 是否为主节点                          |
| node.data   | true   | 是否为数据节点                        |
| node.ingest | true   |                                       |
| node.ml     | true   | 是否为机器学习节点（需要开启 x-pack） |

> **建议**
>
> 开发环境中一个节点可以承担多种角色。但是，在生产环境中，节点应该设置为单一角色。

## 集群

### 集群简介

拥有相同 `cluster.name` 配置的 Elasticsearch 节点组成一个**集群**。 `cluster.name` 默认名为 `elasticsearch`，可以通过配置文件修改，或启动时通过 `-E cluster.name=xxx` 指定。

当有节点加入集群中或者从集群中移除节点时，集群将会重新平均分布所有的数据。

当一个节点被选举成为主节点时，它将负责管理集群范围内的所有变更，例如增加、删除索引，或者增加、删除节点等。 而主节点并不需要涉及到文档级别的变更和搜索等操作，所以当集群只拥有一个主节点的情况下，即使流量增加，它也不会成为瓶颈。 任何节点都可以成为主节点。

作为用户，我们可以将请求发送到集群中的任何节点 ，包括主节点。 每个节点都知道任意文档所处的位置，并且能够将我们的请求直接转发到存储我们所需文档的节点。 无论我们将请求发送到哪个节点，它都能负责从各个包含我们所需文档的节点收集回数据，并将最终结果返回給客户端。 Elasticsearch 对这一切的管理都是透明的。

### 集群健康

Elasticsearch 的集群监控信息中包含了许多的统计数据，其中最为重要的一项就是 _集群健康_ ， 它在 `status` 字段中展示为 `green` 、 `yellow` 或者 `red` 。

在一个不包含任何索引的空集群中，它将会有一个类似于如下所示的返回内容：

```js
{
  "cluster_name" : "elasticsearch",
  "status" : "green",
  "timed_out" : false,
  "number_of_nodes" : 1,
  "number_of_data_nodes" : 1,
  "active_primary_shards" : 5,
  "active_shards" : 5,
  "relocating_shards" : 0,
  "initializing_shards" : 0,
  "unassigned_shards" : 0,
  "delayed_unassigned_shards" : 0,
  "number_of_pending_tasks" : 0,
  "number_of_in_flight_fetch" : 0,
  "task_max_waiting_in_queue_millis" : 0,
  "active_shards_percent_as_number" : 100.0
}
```

`status` 字段指示着当前集群在总体上是否工作正常。它的三种颜色含义如下：

- **`green`**：所有的主分片和副本分片都正常运行。
- **`yellow`**：所有的主分片都正常运行，但不是所有的副本分片都正常运行。
- **`red`**：有主分片没能正常运行。

## 分片

### 分片简介

索引实际上是指向一个或者多个**物理分片**的**逻辑命名空间** 。

一个分片是一个底层的工作单元 ，它仅保存了全部数据中的一部分。一个分片可以视为一个 Lucene 的实例，并且它本身就是一个完整的搜索引擎。 我们的文档被存储和索引到分片内，但是应用程序是直接与索引而不是与分片进行交互。

Elasticsearch 是利用分片将数据分发到集群内各处的。分片是数据的容器，文档保存在分片内，分片又被分配到集群内的各个节点里。 当你的集群规模扩大或者缩小时， Elasticsearch 会自动的在各节点中迁移分片，使得数据仍然均匀分布在集群里。

### 主分片和副分片 

分片分为主分片（Primary Shard）和副分片（Replica Shard）。

主分片：用于解决数据水平扩展的问题。通过主分片，可以将数据分布到集群内不同节点上。

- 索引内任意一个文档都归属于一个主分片。
- 主分片数在索引创建时指定，后序不允许修改，除非 Reindex

副分片（Replica Shard）：用于解决数据高可用的问题。副分片是主分片的拷贝。副本分片作为硬件故障时保护数据不丢失的冗余备份，并为搜索和返回文档等读操作提供服务。

- 副分片数可以动态调整
- 增加副本数，还可以在一定程度上提高服务的可用性（读取的吞吐）

对于生产环境中分片的设定，需要提前做好容量规划

分片数过小

- 无法水平扩展
- 单个分片的数量太大，导致数据重新分配耗时

分片数过大

- 影响搜索结果的相关性打分，影响统计结果的准确性
- 单节点上过多的分片，会导致资源浪费，同时也会影响性能

### 故障转移

当集群中只有一个节点运行时，意味着存在单点故障问题——没有冗余。

---

文档的基本 CRUD 与批量操作

倒排索引入门

通过分析器进行分词

Search API 概览

URI Search 详解

Request Body 与 Query DSL 简介

Query String & Simple Query String 查询

Dynamic Mapping 和常见字段类型

显式 Mapping 设置与常见参数介绍

多字段特性及 Mapping 中配置自定义 Analyzer

Index Template 和 Dynamic Template

Elasticsearch 聚合分析简介

## 参考资料

- [Elasticsearch 官网](https://www.elastic.co/)
