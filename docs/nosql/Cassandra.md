# Cassandra

> Apache Cassandra 是一个高度可扩展的分区行存储。行被组织成具有所需主键的表。
>
> 最新版本：v4.0

<!-- TOC depthFrom:2 depthTo:2 -->

- [Quick Start](#quick-start)
- [简介](#简介)
- [更多内容](#更多内容)

<!-- /TOC -->

## Quick Start

### 安装

> 先决条件
>
> * JDK8+
> * Python 2.7

## 简介

Apache Cassandra 是一套开源分布式 Key-Value 存储系统。它最初由 Facebook 开发，用于储存特别大的数据。

### 特性

#### 主要特性

- 分布式
- 基于 column 的结构化
- 高伸展性

Cassandra 的主要特点就是它不是一个数据库，而是由一堆数据库节点共同构成的一个分布式网络服务，对 Cassandra 的一个写操作，会被复制到其他节点上去，对 Cassandra 的读操作，也会被路由到某个节点上面去读取。对于一个 Cassandra 群集来说，扩展性能 是比较简单的事情，只管在群集里面添加节点就可以了。

#### 突出特性

- **模式灵活** - 使用 Cassandra，像文档存储，不必提前解决记录中的字段。你可以在系统运行时随意的添加或移除字段。这是一个惊人的效率提升，特别是在大型部署上。
- **真正的可扩展性** - Cassandra 是纯粹意义上的水平扩展。为给集群添加更多容量，可以指向另一台电脑。你不必重启任何进程，改变应用查询，或手动迁移任何数据。
- **多数据中心识别** - 你可以调整你的节点布局来避免某一个数据中心起火，一个备用的数据中心将至少有每条记录的完全复制。
- **范围查询** - 如果你不喜欢全部的键值查询，则可以设置键的范围来查询。
- **列表数据结构** - 在混合模式可以将超级列添加到 5 维。对于每个用户的索引，这是非常方便的。
- **分布式写操作** - 有可以在任何地方任何时间集中读或写任何数据。并且不会有任何单点失败。

## 更多内容

- [Cassandra 官网](http://cassandra.apache.org)
- [Cassandra Github](https://github.com/apache/cassandra)

## :door: 传送门

| [我的 Github 博客](https://github.com/dunwu/blog) | [db-tutorial 首页](https://github.com/dunwu/db-tutorial) |
