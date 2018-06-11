---
title: Redis 订阅与发布
date: 2018/06/11
categories:
- database
tags:
- database
- nosql
- key-value
---

# Redis 订阅与发布

Redis 通过 PUBLISH 、SUBSCRIBE 等命令实现了订阅与发布模式，这个功能提供两种信息机制，分别是订阅/发布到频道和订阅/发布到模式。

## 频道的订阅与信息发送

Redis 的 SUBSCRIBE 命令可以让客户端订阅任意数量的频道，每当有新信息发送到被订阅的频道时，信息就会被发送给所有订阅指定频道的客户端。

### 订阅频道

### 发送信息到频道

## 模式的订阅与信息发送

## 资料

- [Redis 官网](https://redis.io/)
- [Redis 实战](https://item.jd.com/11791607.html)
- [Redis 设计与实现](https://item.jd.com/11486101.html)
