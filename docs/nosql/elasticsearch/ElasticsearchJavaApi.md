# ElasticSearch Java API

> **[Elasticsearch](https://github.com/elastic/elasticsearch) 是一个分布式、RESTful 风格的搜索和数据分析引擎**，能够解决不断涌现出的各种用例。 作为 Elastic Stack 的核心，它集中存储您的数据，帮助您发现意料之中以及意料之外的情况。
>
> [Elasticsearch](https://github.com/elastic/elasticsearch) 基于搜索库 [Lucene](https://github.com/apache/lucene-solr) 开发。ElasticSearch 隐藏了 Lucene 的复杂性，提供了简单易用的 REST API / Java API 接口（另外还有其他语言的 API 接口）。
>
> _以下简称 ES_。
>
> REST API 最详尽的文档应该参考：[ES 官方 REST API](https://www.elastic.co/guide/en/elasticsearch/reference/current/rest-apis.html)

<!-- TOC depthFrom:2 depthTo:3 -->

- [1. ElasticSearch Rest API 语法格式](#1-elasticsearch-rest-api-语法格式)
- [2. 索引 API](#2-索引-api)
  - [2.1. 创建索引](#21-创建索引)
  - [2.2. 删除索引](#22-删除索引)
  - [2.3. 查看索引](#23-查看索引)
  - [2.4. 索引别名](#24-索引别名)
  - [2.5. 打开/关闭索引](#25-打开关闭索引)
- [3. 文档](#3-文档)
  - [3.1. 创建文档](#31-创建文档)
  - [3.2. 删除文档](#32-删除文档)
  - [3.3. 更新文档](#33-更新文档)
  - [3.4. 查询文档](#34-查询文档)
  - [3.5. 全文搜索](#35-全文搜索)
  - [3.6. 逻辑运算](#36-逻辑运算)
  - [3.7. 批量执行](#37-批量执行)
  - [3.8. 批量读取](#38-批量读取)
  - [3.9. 批量查询](#39-批量查询)
  - [3.10. URI Search 查询语义](#310-uri-search-查询语义)
  - [3.11. Request Body & DSL](#311-request-body--dsl)
- [4. 集群 API](#4-集群-api)
  - [4.1. 集群健康 API](#41-集群健康-api)
  - [4.2. 集群状态 API](#42-集群状态-api)
- [5. 节点 API](#5-节点-api)
- [6. 分片 API](#6-分片-api)
- [7. 监控 API](#7-监控-api)
- [8. 参考资料](#8-参考资料)

<!-- /TOC -->

## 索引 API

- [Create Index API](https://www.elastic.co/guide/en/elasticsearch/client/java-rest/current/java-rest-high-create-index.html)
- [Delete Index API](https://www.elastic.co/guide/en/elasticsearch/client/java-rest/current/java-rest-high-delete-index.html)
- [Index Exists API](https://www.elastic.co/guide/en/elasticsearch/client/java-rest/current/java-rest-high-indices-exists.html)

## 8. 参考资料

- **官方**
  - [Java High Level REST Client](https://www.elastic.co/guide/en/elasticsearch/client/java-rest/current/java-rest-high.html)
