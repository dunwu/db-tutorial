# Elasticsearch

> **[Elasticsearch](https://github.com/elastic/elasticsearch) 是一个分布式、RESTful 风格的搜索和数据分析引擎**，能够解决不断涌现出的各种用例。 作为 Elastic Stack 的核心，它集中存储您的数据，帮助您发现意料之中以及意料之外的情况。
>
> [Elasticsearch](https://github.com/elastic/elasticsearch) 基于搜索库 [Lucene](https://github.com/apache/lucene-solr) 开发。ElasticSearch 隐藏了 Lucene 的复杂性，提供了简单易用的 REST API / Java API 接口（另外还有其他语言的 API 接口）。
>
> *以下简称 ES*。

## 一、Elasticsearch 简介

### 1.1. 什么是 Elasticsearch

**[Elasticsearch](https://github.com/elastic/elasticsearch) 是一个分布式、RESTful 风格的搜索和数据分析引擎**，能够解决不断涌现出的各种用例。 作为 Elastic Stack 的核心，它集中存储您的数据，帮助您发现意料之中以及意料之外的情况。

[Elasticsearch](https://github.com/elastic/elasticsearch) **基于搜索库 [Lucene](https://github.com/apache/lucene-solr) 开发**。ElasticSearch 隐藏了 Lucene 的复杂性，提供了简单易用的 REST API / Java API 接口（另外还有其他语言的 API 接口）。

ElasticSearch 可以视为一个文档存储，它**将复杂数据结构序列化为 JSON 存储**。

**ElasticSearch 是近乎于实时的全文搜素**，这是指：

- 从写入数据到数据可以被搜索，存在较小的延迟（大概是 1s）
- 基于 ES 执行搜索和分析可以达到秒级

### 1.2. 核心概念

#### 1.2.1. Index

**可以认为是文档（document）的优化集合。**

ES 会为所有字段建立索引，经过处理后写入一个反向索引（Inverted Index）。查找数据的时候，直接查找该索引。

所以，ES 数据管理的顶层单位就叫做 Index（索引）。它是单个数据库的同义词。每个 Index （即数据库）的名字必须是小写。

#### 1.2.2. Document

Index 里面单条的记录称为 Document（文档）。许多条 Document 构成了一个 Index。

每个 **`文档（document）`** 都是字段（field）的集合。

Document 使用 JSON 格式表示，下面是一个例子。

```javascript
{
"user": "张三",
"title": "工程师",
"desc": "数据库管理"
}
```

同一个 Index 里面的 Document，不要求有相同的结构（scheme），但是最好保持相同，这样有利于提高搜索效率。

#### 1.2.3. Field

**`字段（field）`** 是包含数据的键值对。

默认情况下，Elasticsearch 对每个字段中的所有数据建立索引，并且每个索引字段都具有专用的优化数据结构。

#### 1.2.4. Type

每个索引里可以有一个或者多个类型（type）。`类型（type）` 是 index 的一个逻辑分类。

不同的 Type 应该有相似的结构（schema），举例来说，`id`字段不能在这个组是字符串，在另一个组是数值。这是与关系型数据库的表的[一个区别](https://www.elastic.co/guide/en/elasticsearch/guide/current/mapping.html)。性质完全不同的数据（比如`products`和`logs`）应该存成两个 Index，而不是一个 Index 里面的两个 Type（虽然可以做到）。

> 注意：根据[规划](https://www.elastic.co/blog/index-type-parent-child-join-now-future-in-elasticsearch)，Elastic 6.x 版只允许每个 Index 包含一个 Type，7.x 版将会彻底移除 Type。

#### 1.2.5. Shard

当单台机器不足以存储大量数据时，Elasticsearch 可以将一个索引中的数据切分为多个 **`分片（shard）`** 。 **`分片（shard）`** 分布在多台服务器上存储。有了 shard 就可以横向扩展，存储更多数据，让搜索和分析等操作分布到多台服务器上去执行，提升吞吐量和性能。每个 shard 都是一个 lucene index。

#### 1.2.6. Replica

任何一个服务器随时可能故障或宕机，此时 shard 可能就会丢失，因此可以为每个 shard 创建多个 **`副本（replica）`**。replica 可以在 shard 故障时提供备用服务，保证数据不丢失，多个 replica 还可以提升搜索操作的吞吐量和性能。primary shard（建立索引时一次设置，不能修改，默认 5 个），replica shard（随时修改数量，默认 1 个），默认每个索引 10 个 shard，5 个 primary shard，5 个 replica shard，最小的高可用配置，是 2 台服务器。

## 二、REST API

> REST API 最详尽的文档应该参考：[ES 官方 REST API](https://www.elastic.co/guide/en/elasticsearch/reference/current/rest-apis.html)

### 3.1. 索引

新建 Index，可以直接向 ES 服务器发出 `PUT` 请求。

#### 3.1.1. 创建索引

示例：直接创建索引

```bash
$ curl -X POST 'localhost:9200/user'
```

服务器返回一个 JSON 对象，里面的 `acknowledged` 字段表示操作成功。

```javascript
{"acknowledged":true,"shards_acknowledged":true,"index":"user"}
```

示例：创建索引时指定配置

```bash
$ curl -X PUT -H 'Content-Type: application/json' 'localhost:9200/user'  -d '
{
    "settings" : {
        "index" : {
            "number_of_shards" : 3,
            "number_of_replicas" : 2
        }
    }
}'
```

示例：创建索引时指定 `mappings`

```bash
$ curl -X PUT -H 'Content-Type: application/json' 'localhost:9200/user'  -d '
{
    "settings" : {
        "index" : {
            "number_of_shards" : 3,
            "number_of_replicas" : 2
        }
    }
}'
```

#### 3.1.2. 删除索引

然后，我们可以通过发送 `DELETE` 请求，删除这个 Index。

```bash
$ curl -X DELETE 'localhost:9200/user'
```

#### 3.1.3. 查看索引

可以通过 GET 请求查看索引信息

```bash
$ curl -X GET 'localhost:9200/user'
```

#### 3.1.4. 打开/关闭索引

通过在 `POST` 中添加 `_close` 或 `_open` 可以打开、关闭索引。
关闭索引

```bash
$ curl -X POST 'localhost:9200/user/_close'
```

打开索引

```bash
$ curl -X POST 'localhost:9200/user/_open'
```

### 3.2. 文档

#### 3.2.1. 新增记录

向指定的 `/Index/type` 发送 PUT 请求，就可以在 Index 里面新增一条记录。比如，向 `/user/admin` 发送请求，就可以新增一条人员记录。

```bash
$ curl -X PUT -H 'Content-Type: application/json' 'localhost:9200/user/admin/1' -d '
{
"user": "张三",
"title": "工程师",
"desc": "数据库管理"
}'
```

服务器返回的 JSON 对象，会给出 Index、Type、Id、Version 等信息。

```json
{
  "_index": "user",
  "_type": "admin",
  "_id": "1",
  "_version": 1,
  "result": "created",
  "_shards": { "total": 3, "successful": 1, "failed": 0 },
  "_seq_no": 0,
  "_primary_term": 2
}
```

如果你仔细看，会发现请求路径是`/user/admin/1`，最后的`1`是该条记录的 Id。它不一定是数字，任意字符串（比如`abc`）都可以。

新增记录的时候，也可以不指定 Id，这时要改成 POST 请求。

```bash
$ curl -X POST -H 'Content-Type: application/json' 'localhost:9200/user/admin' -d '
{
"user": "李四",
"title": "工程师",
"desc": "系统管理"
}'
```

上面代码中，向`/user/admin`发出一个 POST 请求，添加一个记录。这时，服务器返回的 JSON 对象里面，`_id`字段就是一个随机字符串。

```json
{
  "_index": "user",
  "_type": "admin",
  "_id": "WWuoDG8BHwECs7SiYn93",
  "_version": 1,
  "result": "created",
  "_shards": { "total": 3, "successful": 1, "failed": 0 },
  "_seq_no": 1,
  "_primary_term": 2
}
```

注意，如果没有先创建 Index（这个例子是`accounts`），直接执行上面的命令，Elastic 也不会报错，而是直接生成指定的 Index。所以，打字的时候要小心，不要写错 Index 的名称。

#### 3.2.2. 删除记录

删除记录就是发出 `DELETE` 请求。

```bash
$ curl -X DELETE 'localhost:9200/user/admin/2'
```

#### 3.2.3. 更新记录

更新记录就是使用 `PUT` 请求，重新发送一次数据。

```bash
$ curl -X PUT -H 'Content-Type: application/json' 'localhost:9200/user/admin/1' -d '
{
"user": "张三",
"title": "工程师",
"desc": "超级管理员"
}'
```

#### 3.2.4. 查询记录

向`/Index/Type/Id`发出 GET 请求，就可以查看这条记录。

```bash
$ curl 'localhost:9200/user/admin/1?pretty'
```

上面代码请求查看 `/user/admin/1` 这条记录，URL 的参数 `pretty=true` 表示以易读的格式返回。

返回的数据中，`found` 字段表示查询成功，`_source`字段返回原始记录。

```json
{
  "_index": "user",
  "_type": "admin",
  "_id": "1",
  "_version": 2,
  "found": true,
  "_source": {
    "user": "张三",
    "title": "工程师",
    "desc": "超级管理员"
  }
}
```

如果 Id 不正确，就查不到数据，`found` 字段就是 `false`

#### 3.2.5. 查询所有记录

使用 `GET` 方法，直接请求 `/index/type/_search`，就会返回所有记录。

```bash
$ curl 'localhost:9200/user/admin/_search?pretty'
{
  "took" : 1,
  "timed_out" : false,
  "_shards" : {
    "total" : 3,
    "successful" : 3,
    "skipped" : 0,
    "failed" : 0
  },
  "hits" : {
    "total" : 2,
    "max_score" : 1.0,
    "hits" : [
      {
        "_index" : "user",
        "_type" : "admin",
        "_id" : "WWuoDG8BHwECs7SiYn93",
        "_score" : 1.0,
        "_source" : {
          "user" : "李四",
          "title" : "工程师",
          "desc" : "系统管理"
        }
      },
      {
        "_index" : "user",
        "_type" : "admin",
        "_id" : "1",
        "_score" : 1.0,
        "_source" : {
          "user" : "张三",
          "title" : "工程师",
          "desc" : "超级管理员"
        }
      }
    ]
  }
}
```

上面代码中，返回结果的 `took`字段表示该操作的耗时（单位为毫秒），`timed_out`字段表示是否超时，`hits`字段表示命中的记录，里面子字段的含义如下。

- `total`：返回记录数，本例是 2 条。
- `max_score`：最高的匹配程度，本例是`1.0`。
- `hits`：返回的记录组成的数组。

返回的记录中，每条记录都有一个`_score`字段，表示匹配的程序，默认是按照这个字段降序排列。

#### 3.2.6. 全文搜索

ES 的查询非常特别，使用自己的[查询语法](https://www.elastic.co/guide/en/elasticsearch/reference/5.5/query-dsl.html)，要求 GET 请求带有数据体。

```bash
$ curl -H 'Content-Type: application/json' 'localhost:9200/user/admin/_search?pretty'  -d '
{
"query" : { "match" : { "desc" : "管理" }}
}'
```

上面代码使用 [Match 查询](https://www.elastic.co/guide/en/elasticsearch/reference/5.5/query-dsl-match-query.html)，指定的匹配条件是`desc`字段里面包含"软件"这个词。返回结果如下。

```javascript
{
  "took" : 2,
  "timed_out" : false,
  "_shards" : {
    "total" : 3,
    "successful" : 3,
    "skipped" : 0,
    "failed" : 0
  },
  "hits" : {
    "total" : 2,
    "max_score" : 0.38200712,
    "hits" : [
      {
        "_index" : "user",
        "_type" : "admin",
        "_id" : "WWuoDG8BHwECs7SiYn93",
        "_score" : 0.38200712,
        "_source" : {
          "user" : "李四",
          "title" : "工程师",
          "desc" : "系统管理"
        }
      },
      {
        "_index" : "user",
        "_type" : "admin",
        "_id" : "1",
        "_score" : 0.3487891,
        "_source" : {
          "user" : "张三",
          "title" : "工程师",
          "desc" : "超级管理员"
        }
      }
    ]
  }
}
```

Elastic 默认一次返回 10 条结果，可以通过`size`字段改变这个设置，还可以通过`from`字段，指定位移。

```bash
$ curl 'localhost:9200/user/admin/_search'  -d '
{
  "query" : { "match" : { "desc" : "管理" }},
  "from": 1,
  "size": 1
}'
```

上面代码指定，从位置 1 开始（默认是从位置 0 开始），只返回一条结果。

#### 3.2.7. 逻辑运算

如果有多个搜索关键字， Elastic 认为它们是`or`关系。

```bash
$ curl 'localhost:9200/user/admin/_search'  -d '
{
"query" : { "match" : { "desc" : "软件 系统" }}
}'
```

上面代码搜索的是`软件 or 系统`。

如果要执行多个关键词的`and`搜索，必须使用[布尔查询](https://www.elastic.co/guide/en/elasticsearch/reference/5.5/query-dsl-bool-query.html)。

```bash
$ curl -H 'Content-Type: application/json' 'localhost:9200/user/admin/_search?pretty'  -d '
{
	"query": {
		"bool": {
			"must": [
				{ "match": { "desc": "管理" } },
				{ "match": { "desc": "超级" } }
			]
		}
	}
}'
```

## 三、ElasticSearch 基本原理

### 2.1. ES 写数据过程

- 客户端选择一个 node 发送请求过去，这个 node 就是 `coordinating node`（协调节点）。
- `coordinating node` 对 document 进行**路由**，将请求转发给对应的 node（有 primary shard）。
- 实际的 node 上的 `primary shard` 处理请求，然后将数据同步到 `replica node`。
- `coordinating node` 如果发现 `primary node` 和所有 `replica node` 都搞定之后，就返回响应结果给客户端。

### 2.2. ES 读数据过程

可以通过 `doc id` 来查询，会根据 `doc id` 进行 hash，判断出来当时把 `doc id` 分配到了哪个 shard 上面去，从那个 shard 去查询。

- 客户端发送请求到**任意**一个 node，成为 `coordinate node`。
- `coordinate node` 对 `doc id` 进行哈希路由，将请求转发到对应的 node，此时会使用 `round-robin` **随机轮询算法**，在 `primary shard` 以及其所有 replica 中随机选择一个，让读请求负载均衡。
- 接收请求的 node 返回 document 给 `coordinate node`。
- `coordinate node` 返回 document 给客户端。

### 2.3. 写数据底层原理

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

### 2.4. 删除/更新数据底层原理

如果是删除操作，commit 的时候会生成一个 `.del` 文件，里面将某个 doc 标识为 `deleted` 状态，那么搜索的时候根据 `.del` 文件就知道这个 doc 是否被删除了。

如果是更新操作，就是将原来的 doc 标识为 `deleted` 状态，然后新写入一条数据。

buffer 每 refresh 一次，就会产生一个 `segment file`，所以默认情况下是 1 秒钟一个 `segment file`，这样下来 `segment file` 会越来越多，此时会定期执行 merge。每次 merge 的时候，会将多个 `segment file` 合并成一个，同时这里会将标识为 `deleted` 的 doc 给**物理删除掉**，然后将新的 `segment file` 写入磁盘，这里会写一个 `commit point`，标识所有新的 `segment file`，然后打开 `segment file` 供搜索使用，同时删除旧的 `segment file`。

### 2.5. 底层 lucene

简单来说，lucene 就是一个 jar 包，里面包含了封装好的各种建立倒排索引的算法代码。我们用 Java 开发的时候，引入 lucene jar，然后基于 lucene 的 api 去开发就可以了。

通过 lucene，我们可以将已有的数据建立索引，lucene 会在本地磁盘上面，给我们组织索引的数据结构。

### 2.6. 倒排索引

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
| ..     | ..       | ..        |

另外，实用的倒排索引还可以记录更多的信息，比如文档频率信息，表示在文档集合中有多少个文档包含某个单词。

那么，有了倒排索引，搜索引擎可以很方便地响应用户的查询。比如用户输入查询 `Facebook`，搜索系统查找倒排索引，从中读出包含这个单词的文档，这些文档就是提供给用户的搜索结果。

要注意倒排索引的两个重要细节：

- 倒排索引中的所有词项对应一个或多个文档；
- 倒排索引中的词项**根据字典顺序升序排列**

> 上面只是一个简单的栗子，并没有严格按照字典顺序升序排列。

## Elastic 技术栈系列

- [ElasticSearch 运维](elasticsearch-ops.md) - ElasticSearch 安装、配置、命令详解。

## 参考资料

- **官方**
  - [Elasticsearch 官网](https://www.elastic.co/cn/products/elasticsearch)
  - [Elasticsearch Github](https://github.com/elastic/elasticsearch)
  - [Elasticsearch 官方文档](https://www.elastic.co/guide/en/elasticsearch/reference/current/index.html)
- **文章**
  - [Install Elasticsearch with RPM](https://www.elastic.co/guide/en/elasticsearch/reference/current/rpm.html#rpm)
  - https://www.ruanyifeng.com/blog/2017/08/elasticsearch.html
  - [es-introduction](https://github.com/doocs/advanced-java/blob/master/docs/high-concurrency/es-introduction.md)
  - [es-write-query-search](https://github.com/doocs/advanced-java/blob/master/docs/high-concurrency/es-write-query-search.md)
