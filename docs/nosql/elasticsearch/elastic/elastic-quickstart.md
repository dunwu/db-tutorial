# Elastic 快速入门

> 开源协议：[Apache 2.0](https://github.com/elastic/elasticsearch/tree/7.4/licenses/APACHE-LICENSE-2.0.txt)

## 1. 简介

### 1.1. Elastic Stack 是什么

**Elastic Stack** 即 **ELK Stack**。

ELK 是指 Elastic 公司旗下三款产品 [ElasticSearch](https://www.elastic.co/cn/products/elasticsearch) 、[Logstash](https://www.elastic.co/cn/products/logstash) 、[Kibana](https://www.elastic.co/cn/products/kibana) 的首字母组合。

- Elasticsearch 是一个搜索和分析引擎。
- Logstash 是服务器端数据处理管道，能够同时从多个来源采集数据，转换数据，然后将数据发送到诸如 Elasticsearch 等“存储库”中。
- Kibana 则可以让用户在 Elasticsearch 中使用图形和图表对数据进行可视化。

而 Elastic Stack 是 ELK Stack 的更新换代产品，最新产品引入了轻量型的单一功能数据采集器，并把它们叫做 [Beats](https://www.elastic.co/cn/products/beats)。

### 1.2. 为什么使用 Elastic Stack

对于有一定规模的公司来说，通常会很多个应用，并部署在大量的服务器上。运维和开发人员常常需要通过查看日志来定位问题。如果应用是集群化部署，试想如果登录一台台服务器去查看日志，是多么费时费力。

而通过 ELK 这套解决方案，可以同时实现日志收集、日志搜索和日志分析的功能。

### 1.3. Elastic Stack 架构

![img](https://www.elastic.co/guide/en/logstash/current/static/images/deploy3.png)

> **说明**
>
> 以上是 Elastic Stack 的一个架构图。从图中可以清楚的看到数据流向。
>
> - [Beats](https://www.elastic.co/products/beats) 是单一用途的数据传输平台，它可以将多台机器的数据发送到 Logstash 或 ElasticSearch。但 Beats 并不是不可或缺的一环，所以本文中暂不介绍。
> - [Logstash](https://www.elastic.co/products/logstash) 是一个动态数据收集管道。支持以 TCP/UDP/HTTP 多种方式收集数据（也可以接受 Beats 传输来的数据），并对数据做进一步丰富或提取字段处理。
> - [ElasticSearch](https://www.elastic.co/products/elasticsearch) 是一个基于 JSON 的分布式的搜索和分析引擎。作为 ELK 的核心，它集中存储数据。
>
> - [Kibana](https://www.elastic.co/products/kibana) 是 ELK 的用户界面。它将收集的数据进行可视化展示（各种报表、图形化数据），并提供配置、管理 ELK 的界面。

## 2. ElasticSearch

> [Elasticsearch](https://github.com/elastic/elasticsearch) 是一个分布式、RESTful 风格的搜索和数据分析引擎，能够解决不断涌现出的各种用例。 作为 Elastic Stack 的核心，它集中存储您的数据，帮助您发现意料之中以及意料之外的情况。

### 2.1. ElasticSearch 简介

[Elasticsearch](https://github.com/elastic/elasticsearch) 基于搜索库 [Lucene](https://github.com/apache/lucene-solr) 开发。ElasticSearch 隐藏了 Lucene 的复杂性，提供了简单易用的 REST API / Java API 接口（另外还有其他语言的 API 接口）。

ElasticSearch 可以视为一个文档存储，它**将复杂数据结构序列化为 JSON 存储**。

**ElasticSearch 是近乎于实时的全文搜素**，这是指：

- 从写入数据到数据可以被搜索，存在较小的延迟（大概是 1s）
- 基于 ES 执行搜索和分析可以达到秒级

#### 2.1.1. 核心概念

- **`索引（Index）`** 可以认为是文档（document）的优化集合。
- 每个 **`文档（document）`** 都是字段（field）的集合。
- **`字段（field）`** 是包含数据的键值对。
- 默认情况下，Elasticsearch 对每个字段中的所有数据建立索引，并且每个索引字段都具有专用的优化数据结构。
- 每个索引里可以有一个或者多个类型（type）。`类型（type）` 是 index 的一个逻辑分类，
- 当单台机器不足以存储大量数据时，Elasticsearch 可以将一个索引中的数据切分为多个 **`分片（shard）`** 。 **`分片（shard）`** 分布在多台服务器上存储。有了 shard 就可以横向扩展，存储更多数据，让搜索和分析等操作分布到多台服务器上去执行，提升吞吐量和性能。每个 shard 都是一个 lucene index。
- 任何一个服务器随时可能故障或宕机，此时 shard 可能就会丢失，因此可以为每个 shard 创建多个 **`副本（replica）`**。replica 可以在 shard 故障时提供备用服务，保证数据不丢失，多个 replica 还可以提升搜索操作的吞吐量和性能。primary shard（建立索引时一次设置，不能修改，默认 5 个），replica shard（随时修改数量，默认 1 个），默认每个索引 10 个 shard，5 个 primary shard，5 个 replica shard，最小的高可用配置，是 2 台服务器。

### 2.2. ElasticSearch 原理

#### 2.2.1. ES 写数据过程

- 客户端选择一个 node 发送请求过去，这个 node 就是 `coordinating node`（协调节点）。
- `coordinating node` 对 document 进行**路由**，将请求转发给对应的 node（有 primary shard）。
- 实际的 node 上的 `primary shard` 处理请求，然后将数据同步到 `replica node`。
- `coordinating node` 如果发现 `primary node` 和所有 `replica node` 都搞定之后，就返回响应结果给客户端。

![es-write](https://github.com/doocs/advanced-java/raw/master/images/es-write.png)

#### 2.2.2. es 读数据过程

可以通过 `doc id` 来查询，会根据 `doc id` 进行 hash，判断出来当时把 `doc id` 分配到了哪个 shard 上面去，从那个 shard 去查询。

- 客户端发送请求到**任意**一个 node，成为 `coordinate node`。
- `coordinate node` 对 `doc id` 进行哈希路由，将请求转发到对应的 node，此时会使用 `round-robin` **随机轮询算法**，在 `primary shard` 以及其所有 replica 中随机选择一个，让读请求负载均衡。
- 接收请求的 node 返回 document 给 `coordinate node`。
- `coordinate node` 返回 document 给客户端。

#### 2.2.3. 写数据底层原理

![es-write-detail](https://github.com/doocs/advanced-java/raw/master/images/es-write-detail.png)

先写入内存 buffer，在 buffer 里的时候数据是搜索不到的；同时将数据写入 translog 日志文件。

如果 buffer 快满了，或者到一定时间，就会将内存 buffer 数据 `refresh` 到一个新的 `segment file` 中，但是此时数据不是直接进入 `segment file` 磁盘文件，而是先进入 `os cache` 。这个过程就是 `refresh`。

每隔 1 秒钟，es 将 buffer 中的数据写入一个**新的** `segment file`，每秒钟会产生一个**新的磁盘文件** `segment file`，这个 `segment file` 中就存储最近 1 秒内 buffer 中写入的数据。

但是如果 buffer 里面此时没有数据，那当然不会执行 refresh 操作，如果 buffer 里面有数据，默认 1 秒钟执行一次 refresh 操作，刷入一个新的 segment file 中。

操作系统里面，磁盘文件其实都有一个东西，叫做 `os cache`，即操作系统缓存，就是说数据写入磁盘文件之前，会先进入 `os cache`，先进入操作系统级别的一个内存缓存中去。只要 `buffer` 中的数据被 refresh 操作刷入 `os cache`中，这个数据就可以被搜索到了。

为什么叫 es 是**准实时**的？ `NRT`，全称 `near real-time`。默认是每隔 1 秒 refresh 一次的，所以 es 是准实时的，因为写入的数据 1 秒之后才能被看到。可以通过 es 的 `restful api` 或者 `java api`，**手动**执行一次 refresh 操作，就是手动将 buffer 中的数据刷入 `os cache`中，让数据立马就可以被搜索到。只要数据被输入 `os cache` 中，buffer 就会被清空了，因为不需要保留 buffer 了，数据在 translog 里面已经持久化到磁盘去一份了。

重复上面的步骤，新的数据不断进入 buffer 和 translog，不断将 `buffer` 数据写入一个又一个新的 `segment file` 中去，每次 `refresh` 完 buffer 清空，translog 保留。随着这个过程推进，translog 会变得越来越大。当 translog 达到一定长度的时候，就会触发 `commit` 操作。

commit 操作发生第一步，就是将 buffer 中现有数据 `refresh` 到 `os cache` 中去，清空 buffer。然后，将一个 `commit point` 写入磁盘文件，里面标识着这个 `commit point` 对应的所有 `segment file`，同时强行将 `os cache` 中目前所有的数据都 `fsync` 到磁盘文件中去。最后**清空** 现有 translog 日志文件，重启一个 translog，此时 commit 操作完成。

这个 commit 操作叫做 `flush`。默认 30 分钟自动执行一次 `flush`，但如果 translog 过大，也会触发 `flush`。flush 操作就对应着 commit 的全过程，我们可以通过 es api，手动执行 flush 操作，手动将 os cache 中的数据 fsync 强刷到磁盘上去。

translog 日志文件的作用是什么？你执行 commit 操作之前，数据要么是停留在 buffer 中，要么是停留在 os cache 中，无论是 buffer 还是 os cache 都是内存，一旦这台机器死了，内存中的数据就全丢了。所以需要将数据对应的操作写入一个专门的日志文件 `translog` 中，一旦此时机器宕机，再次重启的时候，es 会自动读取 translog 日志文件中的数据，恢复到内存 buffer 和 os cache 中去。

translog 其实也是先写入 os cache 的，默认每隔 5 秒刷一次到磁盘中去，所以默认情况下，可能有 5 秒的数据会仅仅停留在 buffer 或者 translog 文件的 os cache 中，如果此时机器挂了，会**丢失** 5 秒钟的数据。但是这样性能比较好，最多丢 5 秒的数据。也可以将 translog 设置成每次写操作必须是直接 `fsync` 到磁盘，但是性能会差很多。

实际上你在这里，如果面试官没有问你 es 丢数据的问题，你可以在这里给面试官炫一把，你说，其实 es 第一是准实时的，数据写入 1 秒后可以搜索到；可能会丢失数据的。有 5 秒的数据，停留在 buffer、translog os cache、segment file os cache 中，而不在磁盘上，此时如果宕机，会导致 5 秒的**数据丢失**。

**总结一下**，数据先写入内存 buffer，然后每隔 1s，将数据 refresh 到 os cache，到了 os cache 数据就能被搜索到（所以我们才说 es 从写入到能被搜索到，中间有 1s 的延迟）。每隔 5s，将数据写入 translog 文件（这样如果机器宕机，内存数据全没，最多会有 5s 的数据丢失），translog 大到一定程度，或者默认每隔 30mins，会触发 commit 操作，将缓冲区的数据都 flush 到 segment file 磁盘文件中。

> 数据写入 segment file 之后，同时就建立好了倒排索引。

#### 2.2.4. 删除/更新数据底层原理

如果是删除操作，commit 的时候会生成一个 `.del` 文件，里面将某个 doc 标识为 `deleted` 状态，那么搜索的时候根据 `.del` 文件就知道这个 doc 是否被删除了。

如果是更新操作，就是将原来的 doc 标识为 `deleted` 状态，然后新写入一条数据。

buffer 每 refresh 一次，就会产生一个 `segment file`，所以默认情况下是 1 秒钟一个 `segment file`，这样下来 `segment file` 会越来越多，此时会定期执行 merge。每次 merge 的时候，会将多个 `segment file` 合并成一个，同时这里会将标识为 `deleted` 的 doc 给**物理删除掉**，然后将新的 `segment file` 写入磁盘，这里会写一个 `commit point`，标识所有新的 `segment file`，然后打开 `segment file` 供搜索使用，同时删除旧的 `segment file`。

#### 2.2.5. 底层 lucene

简单来说，lucene 就是一个 jar 包，里面包含了封装好的各种建立倒排索引的算法代码。我们用 Java 开发的时候，引入 lucene jar，然后基于 lucene 的 api 去开发就可以了。

通过 lucene，我们可以将已有的数据建立索引，lucene 会在本地磁盘上面，给我们组织索引的数据结构。

#### 2.2.6. 倒排索引

在搜索引擎中，每个文档都有一个对应的文档 ID，文档内容被表示为一系列关键词的集合。例如，文档 1 经过分词，提取了 20 个关键词，每个关键词都会记录它在文档中出现的次数和出现位置。

那么，倒排索引就是**关键词到文档** ID 的映射，每个关键词都对应着一系列的文件，这些文件中都出现了关键词。

举个栗子。

有以下文档：

| DocId | Doc                                            |
| ----- | ---------------------------------------------- |
| 1     | 谷歌地图之父跳槽 Facebook                      |
| 2     | 谷歌地图之父加盟 Facebook                      |
| 3     | 谷歌地图创始人拉斯离开谷歌加盟 Facebook        |
| 4     | 谷歌地图之父跳槽 Facebook 与 Wave 项目取消有关 |
| 5     | 谷歌地图之父拉斯加盟社交网站 Facebook          |

对文档进行分词之后，得到以下**倒排索引**。

| WordId | Word     | DocIds    |
| ------ | -------- | --------- |
| 1      | 谷歌     | 1,2,3,4,5 |
| 2      | 地图     | 1,2,3,4,5 |
| 3      | 之父     | 1,2,4,5   |
| 4      | 跳槽     | 1,4       |
| 5      | Facebook | 1,2,3,4,5 |
| 6      | 加盟     | 2,3,5     |
| 7      | 创始人   | 3         |
| 8      | 拉斯     | 3,5       |
| 9      | 离开     | 3         |
| 10     | 与       | 4         |
| ..    | ..      | ..       |

另外，实用的倒排索引还可以记录更多的信息，比如文档频率信息，表示在文档集合中有多少个文档包含某个单词。

那么，有了倒排索引，搜索引擎可以很方便地响应用户的查询。比如用户输入查询 `Facebook`，搜索系统查找倒排索引，从中读出包含这个单词的文档，这些文档就是提供给用户的搜索结果。

要注意倒排索引的两个重要细节：

- 倒排索引中的所有词项对应一个或多个文档；
- 倒排索引中的词项**根据字典顺序升序排列**

> 上面只是一个简单的栗子，并没有严格按照字典顺序升序排列。

## 3. Logstash

> [Logstash](https://github.com/elastic/logstash) 是开源的服务器端数据处理管道，能够同时从多个来源采集数据，转换数据，然后将数据发送到您最喜欢的“存储库”中。

### 3.1. Logstash 简介

Logstash 可以传输和处理你的日志、事务或其他数据。

Logstash 是 Elasticsearch 的最佳数据管道。

Logstash 是插件式管理模式，在输入、过滤、输出以及编码过程中都可以使用插件进行定制。Logstash 社区有超过 200 种可用插件。

### 3.2. Logstash 原理

Logstash 有两个必要元素：`input` 和 `output` ，一个可选元素：`filter`。

这三个元素，分别代表 Logstash 事件处理的三个阶段：输入 > 过滤器 > 输出。

![img](https://www.elastic.co/guide/en/logstash/current/static/images/basic_logstash_pipeline.png)

- **input** - 负责从数据源采集数据。
- **`filter`** - 将数据修改为你指定的格式或内容。
- **`output`** - 将数据传输到目的地。

在实际应用场景中，通常输入、输出、过滤器不止一个。Logstash 的这三个元素都使用插件式管理方式，用户可以根据应用需要，灵活的选用各阶段需要的插件，并组合使用。

## 4. Beats

> **[Beats](https://github.com/elastic/beats) 是安装在服务器上的数据中转代理**。
>
> Beats 可以将数据直接传输到 Elasticsearch 或传输到 Logstash 。

![img](https://www.elastic.co/guide/en/beats/libbeat/current/images/beats-platform.png)

Beats 有多种类型，可以根据实际应用需要选择合适的类型。

常用的类型有：

- **Packetbeat：**网络数据包分析器，提供有关您的应用程序服务器之间交换的事务的信息。
- **Filebeat：**从您的服务器发送日志文件。
- **Metricbeat：**是一个服务器监视代理程序，它定期从服务器上运行的操作系统和服务收集指标。
- **Winlogbeat：**提供 Windows 事件日志。

### 4.1. Filebeat 简介

> *由于本人仅接触过 Filebeat，所以本文只介绍 Beats 组件中的 Filebeat*。

相比 Logstash，FileBeat 更加轻量化。

在任何环境下，应用程序都有停机的可能性。 Filebeat 读取并转发日志行，如果中断，则会记住所有事件恢复联机状态时所在位置。

Filebeat 带有内部模块（auditd，Apache，Nginx，System 和 MySQL），可通过一个指定命令来简化通用日志格式的收集，解析和可视化。

FileBeat 不会让你的管道超负荷。FileBeat 如果是向 Logstash 传输数据，当 Logstash 忙于处理数据，会通知 FileBeat 放慢读取速度。一旦拥塞得到解决，FileBeat 将恢复到原来的速度并继续传播。

![img](https://www.elastic.co/guide/en/beats/filebeat/current/images/filebeat.png)

### 4.2. Filebeat 原理

Filebeat 有两个主要组件：

- `harvester`：负责读取一个文件的内容。它会逐行读取文件内容，并将内容发送到输出目的地。
- `prospector`：负责管理 harvester 并找到所有需要读取的文件源。比如类型是日志，prospector 就会遍历制定路径下的所有匹配要求的文件。

```yaml
filebeat.prospectors:
  - type: log
    paths:
      - /var/log/*.log
      - /var/path2/*.log
```

Filebeat 保持每个文件的状态，并经常刷新注册表文件中的磁盘状态。状态用于记住 harvester 正在读取的最后偏移量，并确保发送所有日志行。

Filebeat 将每个事件的传递状态存储在注册表文件中。所以它能保证事件至少传递一次到配置的输出，没有数据丢失。

## 5. 运维

- [ElasticSearch 运维](nosql/elasticsearch/elasticsearch-ops.md)
- [Logstash 运维](nosql/elasticsearch/elastic/elastic-logstash-ops.mdstic/elastic-logstash-ops.md)
- [Kibana 运维](nosql/elasticsearch/elastic/elastic-kibana-ops.mdlastic/elastic-kibana-ops.md)
- [Beats 运维](nosql/elasticsearch/elastic/elastic-beats-ops.mdelastic/elastic-beats-ops.md)

## 6. 参考资料

- **官方资源**
  - [Elasticsearch 官网](https://www.elastic.co/cn/products/elasticsearch)
  - [Elasticsearch Github](https://github.com/elastic/elasticsearch)
  - [Elasticsearch 官方文档](https://www.elastic.co/guide/en/elasticsearch/reference/current/index.html)
  - [Logstash 官网](https://www.elastic.co/cn/products/logstash)
  - [Logstash Github](https://github.com/elastic/logstash)
  - [Logstash 官方文档](https://www.elastic.co/guide/en/logstash/current/index.html)
  - [Kibana 官网](https://www.elastic.co/cn/products/kibana)
  - [Kibana Github](https://github.com/elastic/kibana)
  - [Kibana 官方文档](https://www.elastic.co/guide/en/kibana/current/index.html)
  - [Beats 官网](https://www.elastic.co/cn/products/beats)
  - [Beats Github](https://github.com/elastic/beats)
  - [Beats 官方文档](https://www.elastic.co/guide/en/beats/libbeat/current/index.html)
- **文章**
  - [什么是 ELK Stack？](https://www.elastic.co/cn/what-is/elk-stack)
  - [https://github.com/doocs/advanced-java/blob/master/docs/high-concurrency/es-introduction.md](https://github.com/doocs/advanced-java/blob/master/docs/high-concurrency/es-introduction.md)
  - [es-write-query-search](https://github.com/doocs/advanced-java/blob/master/docs/high-concurrency/es-write-query-search.md)
