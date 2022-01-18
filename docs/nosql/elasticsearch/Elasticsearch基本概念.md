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

- **`Mapping`** 定义文档字段的类型
- **`Setting`** 定义不同的数据分布

示例：

```json
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

### index template

**`index template`**（索引模板）帮助用户设定 Mapping 和 Setting，并按照一定的规则，自动匹配到新创建的索引之上。

- 模板仅在一个索引被创建时，才会产生作用。修改模板不会影响已创建的索引。
- 你可以设定多个索引模板，这些设置会被 merge 在一起。
- 你可以指定 order 的数值，控制 merge 的过程。

当新建一个索引时

- 应用 ES 默认的 Mapping 和 Setting
- 应用 order 数值低的 index template 中的设定
- 应用 order 数值高的 index template 中的设定，之前的设定会被覆盖
- 应用创建索引是，用户所指定的 Mapping 和 Setting，并覆盖之前模板中的设定。

示例：创建默认索引模板

```bash
PUT _template/template_default
{
  "index_patterns": ["*"],
  "order": 0,
  "version": 1,
  "settings": {
    "number_of_shards": 1,
    "number_of_replicas": 1
  }
}

PUT /_template/template_test
{
  "index_patterns": ["test*"],
  "order": 1,
  "settings": {
    "number_of_shards": 1,
    "number_of_replicas": 2
  },
  "mappings": {
    "date_detection": false,
    "numeric_detection": true
  }
}

# 查看索引模板
GET /_template/template_default
GET /_template/temp*

#写入新的数据，index以test开头
PUT testtemplate/_doc/1
{
  "someNumber": "1",
  "someDate": "2019/01/01"
}
GET testtemplate/_mapping
GET testtemplate/_settings

PUT testmy
{
	"settings":{
		"number_of_replicas":5
	}
}

PUT testmy/_doc/1
{
  "key": "value"
}

GET testmy/_settings
DELETE testmy
DELETE /_template/template_default
DELETE /_template/template_test
```

### dynamic template

- 根据 ES 识别的数据类型，结合字段名称，来动态设定字段类型
  - 所有的字符串类型都设定成 Keyword，或者关闭 keyword 字段。
  - is 开头的字段都设置成 boolean
  - long_ 开头的都设置成 long 类型
- dynamic template 是定义在某个索引的 Mapping 中
- template 有一个名称
- 匹配规则是一个数组
- 为匹配到字段设置 Mapping

示例：

```bash
#Dynaminc Mapping 根据类型和字段名
DELETE my_index

PUT my_index/_doc/1
{
  "firstName": "Ruan",
  "isVIP": "true"
}

GET my_index/_mapping

DELETE my_index
PUT my_index
{
  "mappings": {
    "dynamic_templates": [
      {
        "strings_as_boolean": {
          "match_mapping_type": "string",
          "match": "is*",
          "mapping": {
            "type": "boolean"
          }
        }
      },
      {
        "strings_as_keywords": {
          "match_mapping_type": "string",
          "mapping": {
            "type": "keyword"
          }
        }
      }
    ]
  }
}
GET my_index/_mapping

DELETE my_index
#结合路径
PUT my_index
{
  "mappings": {
    "dynamic_templates": [
      {
        "full_name": {
          "path_match": "name.*",
          "path_unmatch": "*.middle",
          "mapping": {
            "type": "text",
            "copy_to": "full_name"
          }
        }
      }
    ]
  }
}
GET my_index/_mapping


PUT my_index/_doc/1
{
  "name": {
    "first": "John",
    "middle": "Winston",
    "last": "Lennon"
  }
}

GET my_index/_search?q=full_name:John
DELETE my_index
```

## Mapping

在 Elasticsearch 中，**`Mapping`**（映射），用来定义一个文档以及其所包含的字段如何被存储和索引，可以在映射中事先定义字段的数据类型、字段的权重、分词器等属性，就如同在关系型数据库中创建数据表时会设置字段的类型。

Mapping 会把 json 文档映射成 Lucene 所需要的扁平格式

一个 Mapping 属于一个索引的 Type

- 每个文档都属于一个 Type
- 一个 Type 有一个 Mapping 定义
- 7.0 开始，不需要在 Mapping 定义中指定 type 信息

### 映射分类

在 Elasticsearch 中，映射可分为静态映射和动态映射。

#### 静态映射

**静态映射**是在创建索引时手工指定索引映射。静态映射和 SQL 中在建表语句中指定字段属性类似。相比动态映射，通过静态映射可以添加更详细、更精准的配置信息。

如何定义一个 Mapping

```bash
PUT /books
{
    "mappings": {
        "type_one": { ... any mappings ... },
        "type_two": { ... any mappings ... },
        ...
    }
}
```

#### 动态映射

**动态映射**是一种偷懒的方式，可直接创建索引并写入文档，文档中字段的类型是 Elasticsearch **自动识别**的，不需要在创建索引的时候设置字段的类型。在实际项目中，如果遇到的业务在导入数据之前不确定有哪些字段，也不清楚字段的类型是什么，使用动态映射非常合适。当 Elasticsearch 在文档中碰到一个以前没见过的字段时，它会利用动态映射来决定该字段的类型，并自动把该字段添加到映射中，根据字段的取值自动推测字段类型的规则见下表：

| JSON 格式的数据 | 自动推测的字段类型                                                                 |
| :-------------- | :--------------------------------------------------------------------------------- |
| null            | 没有字段被添加                                                                     |
| true or false   | boolean 类型                                                                       |
| 浮点类型数字    | float 类型                                                                         |
| 数字            | long 类型                                                                          |
| JSON 对象       | object 类型                                                                        |
| 数组            | 由数组中第一个非空值决定                                                           |
| string          | 有可能是 date 类型（若开启日期检测）、double 或 long 类型、text 类型、keyword 类型 |

下面举一个例子认识动态 mapping，在 Elasticsearch 中创建一个新的索引并查看它的 mapping，命令如下：

```bash
PUT books
GET books/_mapping
```

此时 books 索引的 mapping 是空的，返回结果如下：

```json
{
  "books": {
    "mappings": {}
  }
}
```

再往 books 索引中写入一条文档，命令如下：

```bash
PUT books/it/1
{
	"id": 1,
	"publish_date": "2019-11-10",
	"name": "master Elasticsearch"
}
```

文档写入完成之后，再次查看 mapping，返回结果如下：

```json
{
  "books": {
    "mappings": {
      "properties": {
        "id": {
          "type": "long"
        },
        "name": {
          "type": "text",
          "fields": {
            "keyword": {
              "type": "keyword",
              "ignore_above": 256
            }
          }
        },
        "publish_date": {
          "type": "date"
        }
      }
    }
  }
}
```

使用动态 mapping 要结合实际业务需求来综合考虑，如果将 Elasticsearch 当作主要的数据存储使用，并且希望出现未知字段时抛出异常来提醒你注意这一问题，那么开启动态 mapping 并不适用。在 mapping 中可以通过 `dynamic` 设置来控制是否自动新增字段，接受以下参数：

- **`true`**：默认值为 true，自动添加字段。
- **`false`**：忽略新的字段。
- **`strict`**：严格模式，发现新的字段抛出异常。

### 基础类型

| 类型       | 关键字                                                              |
| :--------- | :------------------------------------------------------------------ |
| 字符串类型 | string、text、keyword                                               |
| 数字类型   | long、integer、short、byte、double、float、half_float、scaled_float |
| 日期类型   | date                                                                |
| 布尔类型   | boolean                                                             |
| 二进制类型 | binary                                                              |
| 范围类型   | range                                                               |

### 复杂类型

| 类型     | 关键字 |
| :------- | :----- |
| 数组类型 | array  |
| 对象类型 | object |
| 嵌套类型 | nested |

### 特殊类型

| 类型         | 关键字      |
| :----------- | :---------- |
| 地理类型     | geo_point   |
| 地理图形类型 | geo_shape   |
| IP 类型      | ip          |
| 范围类型     | completion  |
| 令牌计数类型 | token_count |
| 附件类型     | attachment  |
| 抽取类型     | percolator  |

### Mapping 属性

Elasticsearch 的 mapping 中的字段属性非常多，具体如下表格：

| 属性名                  | 描述                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     |
| :---------------------- | :------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------ |
| **_`type`_**            | 字段类型，常用的有 text、integer 等等。                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  |
| **_`index`_**           | 当前字段是否被作为索引。可选值为 **_`true`_**，默认为 true。                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             |
| **_`store`_**           | 是否存储指定字段，可选值为 **_`true`_**                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  | **_`false`_**，设置 true 意味着需要开辟单独的存储空间为这个字段做存储，而且这个存储是独立于 **_`_source`_** 的存储的。                           |
| **_`norms`_**           | 是否使用归一化因子，可选值为 **_`true`_**                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                | **_`false`_**，不需要对某字段进行打分排序时，可禁用它，节省空间；_type_ 为 _text_ 时，默认为 _true_；而 _type_ 为 _keyword_ 时，默认为 _false_。 |
| **_`index_options`_**   | 索引选项控制添加到倒排索引（Inverted Index）的信息，这些信息用于搜索（Search）和高亮显示：**_`docs`_**：只索引文档编号(Doc Number)；**_`freqs`_**：索引文档编号和词频率（term frequency）；**_`positions`_**：索引文档编号，词频率和词位置（序号）；**_`offsets`_**：索引文档编号，词频率，词偏移量（开始和结束位置）和词位置（序号）。默认情况下，被分析的字符串（analyzed string）字段使用 _positions_，其他字段默认使用 _docs_。此外，需要注意的是 _index_option_ 是 elasticsearch 特有的设置属性；临近搜索和短语查询时，_index_option_ 必须设置为 _offsets_，同时高亮也可使用 postings highlighter。 |
| **_`term_vector`_**     | 索引选项控制词向量相关信息：**_`no`_**：默认值，表示不存储词向量相关信息；**_`yes`_**：只存储词向量信息；**_`with_positions`_**：存储词项和词项位置；**_`with_offsets`_**：存储词项和字符偏移位置；**_`with_positions_offsets`_**：存储词项、词项位置、字符偏移位置。_term_vector_ 是 lucene 层面的索引设置。                                                                                                                                                                                                                                                                                            |
| **_`similarity`_**      | 指定文档相似度算法（也可以叫评分模型）：**_`BM25`_**：es 5 之后的默认设置。                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              |
| **_`copy_to`_**         | 复制到自定义 \_all 字段，值是数组形式，即表明可以指定多个自定义的字段。                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  |
| **_`analyzer`_**        | 指定索引和搜索时的分析器，如果同时指定 _search_analyzer_ 则搜索时会优先使用 _search_analyzer_。                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| **_`search_analyzer`_** | 指定搜索时的分析器，搜索时的优先级最高。                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 |
| **_`null_value`_**      | 用于需要对 Null 值实现搜索的场景，只有 Keyword 类型支持此配置。                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |

## 分词

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

## 参考资料

- [Elasticsearch 官网](https://www.elastic.co/)
- [Elasticsearch 索引映射类型及 mapping 属性详解](https://www.knowledgedict.com/tutorial/elasticsearch-index-mapping.html)
