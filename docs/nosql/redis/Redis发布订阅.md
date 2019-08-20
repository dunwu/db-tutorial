---
title: Redis 发布订阅
date: 2018-06-11
categories:
- database
tags:
- database
- nosql
- key-value
---

# Redis 发布订阅

Redis 通过 PUBLISH 、SUBSCRIBE 等命令实现了订阅与发布模式，这个功能提供两种信息机制，分别是订阅/发布到频道和订阅/发布到模式。

| 命令         | 描述                                                                     |
| ------------ | ------------------------------------------------------------------------ |
| SUBSCRIBE    | 订阅给定的一个或多个频道。                                               |
| UNSUBSCRIBE  | 退订给定的一个或多个频道，如果执行时灭有给定任何频道，那么退订所有频道。 |
| PUBLISH      | 向给定频道发送消息。                                                     |
| PSUBSCRIBE   | 订阅与给定模式相匹配的所有频道。                                         |
| PUNSUBSCRIBE | 退订给定的模式，如果执行时没有给定任何模式，那么退订所有模式。           |

## 频道的订阅与信息发送

Redis 的 SUBSCRIBE 命令可以让客户端订阅任意数量的频道，每当有新信息发送到被订阅的频道时，信息就会被发送给所有订阅指定频道的客户端。

### 订阅频道

### 发送信息到频道

## 模式的订阅与信息发送

## 资料

- [Redis 官网](https://redis.io/)
- [Redis 实战](https://item.jd.com/11791607.html)
- [Redis 设计与实现](https://item.jd.com/11486101.html)
