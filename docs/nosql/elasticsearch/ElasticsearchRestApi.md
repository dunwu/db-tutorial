# ElasticSearch Rest API

> **[Elasticsearch](https://github.com/elastic/elasticsearch) 是一个分布式、RESTful 风格的搜索和数据分析引擎**，能够解决不断涌现出的各种用例。 作为 Elastic Stack 的核心，它集中存储您的数据，帮助您发现意料之中以及意料之外的情况。
>
> [Elasticsearch](https://github.com/elastic/elasticsearch) 基于搜索库 [Lucene](https://github.com/apache/lucene-solr) 开发。ElasticSearch 隐藏了 Lucene 的复杂性，提供了简单易用的 REST API / Java API 接口（另外还有其他语言的 API 接口）。
>
> _以下简称 ES_。

> REST API 最详尽的文档应该参考：[ES 官方 REST API](https://www.elastic.co/guide/en/elasticsearch/reference/current/rest-apis.html)

## 索引 API

> 参考资料：[Elasticsearch 官方之 cat 索引 API](https://www.elastic.co/guide/en/elasticsearch/reference/current/cat-indices.html)

### 创建索引

新建 Index，可以直接向 ES 服务器发出 `PUT` 请求。

（1）直接创建索引

```bash
curl -X POST 'localhost:9200/user'
```

服务器返回一个 JSON 对象，里面的 `acknowledged` 字段表示操作成功。

```javascript
{"acknowledged":true,"shards_acknowledged":true,"index":"user"}
```

（2）创建索引时指定配置

语法格式：

```bash
$ curl -X PUT /my_index
{
    "settings": { ... any settings ... },
    "mappings": {
        "type_one": { ... any mappings ... },
        "type_two": { ... any mappings ... },
        ...
    }
}
```

示例：

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

如果你想禁止自动创建索引，可以通过在 `config/elasticsearch.yml` 的每个节点下添加下面的配置：

```js
action.auto_create_index: false
```

### 删除索引

然后，我们可以通过发送 `DELETE` 请求，删除这个 Index。

```bash
curl -X DELETE 'localhost:9200/user'
```

删除多个索引

```js
DELETE /index_one,index_two
DELETE /index_*
```

### 查看索引

可以通过 GET 请求查看索引信息

```bash
# 查看索引相关信息
GET kibana_sample_data_ecommerce

# 查看索引的文档总数
GET kibana_sample_data_ecommerce/_count

# 查看前10条文档，了解文档格式
GET kibana_sample_data_ecommerce/_search

# _cat indices API
# 查看indices
GET /_cat/indices/kibana*?v&s=index

# 查看状态为绿的索引
GET /_cat/indices?v&health=green

# 按照文档个数排序
GET /_cat/indices?v&s=docs.count:desc

# 查看具体的字段
GET /_cat/indices/kibana*?pri&v&h=health,index,pri,rep,docs.count,mt

# 查看索引占用的内存
GET /_cat/indices?v&h=i,tm&s=tm:desc
```

### 打开/关闭索引

通过在 `POST` 中添加 `_close` 或 `_open` 可以打开、关闭索引。

打开索引

```bash
# 打开索引
POST kibana_sample_data_ecommerce/_open
# 关闭索引
POST kibana_sample_data_ecommerce/_close
```

## 文档

#### 新增记录

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

#### 删除记录

删除记录就是发出 `DELETE` 请求。

```bash
curl -X DELETE 'localhost:9200/user/admin/2'
```

#### 更新记录

更新记录就是使用 `PUT` 请求，重新发送一次数据。

```bash
$ curl -X PUT -H 'Content-Type: application/json' 'localhost:9200/user/admin/1' -d '
{
"user": "张三",
"title": "工程师",
"desc": "超级管理员"
}'
```

#### 查询记录

向`/Index/Type/Id`发出 GET 请求，就可以查看这条记录。

```bash
curl 'localhost:9200/user/admin/1?pretty'
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

#### 查询所有记录

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

#### 全文搜索

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

#### 逻辑运算

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

## 集群 API

> [Elasticsearch 官方之 Cluster API](https://www.elastic.co/guide/en/elasticsearch/reference/current/cluster.html)

一些集群级别的 API 可能会在节点的子集上运行，这些节点可以用节点过滤器指定。例如，任务管理、节点统计和节点信息 API 都可以报告来自一组过滤节点而不是所有节点的结果。

节点过滤器以逗号分隔的单个过滤器列表的形式编写，每个过滤器从所选子集中添加或删除节点。每个过滤器可以是以下之一：

- `_all`：将所有节点添加到子集
- `_local`：将本地节点添加到子集
- `_master`：将当前主节点添加到子集
- 根据节点ID或节点名将匹配节点添加到子集
- 根据IP地址或主机名将匹配节点添加到子集
- 使用通配符，将节点名、地址名或主机名匹配的节点添加到子集
- `master:true`, `data:true`, `ingest:true`, `voting_only:true`, `ml:true` 或 `coordinating_only:true`, 分别意味着将所有主节点、所有数据节点、所有摄取节点、所有仅投票节点、所有机器学习节点和所有协调节点添加到子集中。
- `master:false`, `data:false`, `ingest:false`, `voting_only:true`, `ml:false` 或 `coordinating_only:false`, 分别意味着将所有主节点、所有数据节点、所有摄取节点、所有仅投票节点、所有机器学习节点和所有协调节点排除在子集外。
- 配对模式，使用 `*` 通配符，格式为 `attrname:attrvalue`，将所有具有自定义节点属性的节点添加到子集中，其名称和值与相应的模式匹配。自定义节点属性是通过 `node.attr.attrname: attrvalue` 形式在配置文件中设置的。

```bash
# 如果没有给出过滤器，默认是查询所有节点
GET /_nodes
# 查询所有节点
GET /_nodes/_all
# 查询本地节点
GET /_nodes/_local
# 查询主节点
GET /_nodes/_master
# 根据名称查询节点（支持通配符）
GET /_nodes/node_name_goes_here
GET /_nodes/node_name_goes_*
# 根据地址查询节点（支持通配符）
GET /_nodes/10.0.0.3,10.0.0.4
GET /_nodes/10.0.0.*
# 根据规则查询节点
GET /_nodes/_all,master:false
GET /_nodes/data:true,ingest:true
GET /_nodes/coordinating_only:true
GET /_nodes/master:true,voting_only:false
# 根据自定义属性查询节点（如：查询配置文件中含 node.attr.rack:2 属性的节点）
GET /_nodes/rack:2
GET /_nodes/ra*:2
GET /_nodes/ra*:2*
```

### 集群健康 API

```bash
GET /_cluster/health
GET /_cluster/health?level=shards
GET /_cluster/health/kibana_sample_data_ecommerce,kibana_sample_data_flights
GET /_cluster/health/kibana_sample_data_flights?level=shards
```

### 集群状态 API

集群状态 API 返回表示整个集群状态的元数据。

```bash
GET /_cluster/state
```



## 节点 API

> [Elasticsearch 官方之 cat Nodes API](https://www.elastic.co/guide/en/elasticsearch/reference/current/cat-nodes.html)——返回有关集群节点的信息。

```bash
# 查看默认的字段
GET /_cat/nodes?v=true
# 查看指定的字段
GET /_cat/nodes?v=true&h=id,ip,port,v,m
```

## 分片 API

> [Elasticsearch 官方之 cat Shards API](https://www.elastic.co/guide/en/elasticsearch/reference/current/cat-shards.html)——shards 命令是哪些节点包含哪些分片的详细视图。它会告诉你它是主还是副本、文档数量、它在磁盘上占用的字节数以及它所在的节点。

```bash
# 查看默认的字段
GET /_cat/shards
# 根据名称查询分片（支持通配符）
GET /_cat/shards/my-index-*
# 查看指定的字段
GET /_cat/shards?h=index,shard,prirep,state,unassigned.reason
```

## 参考资料

- **官方**
  - [Elasticsearch 官网](https://www.elastic.co/cn/products/elasticsearch)
  - [Elasticsearch Github](https://github.com/elastic/elasticsearch)
  - [Elasticsearch 官方文档](https://www.elastic.co/guide/en/elasticsearch/reference/current/index.html)
